package com.ruoyi.system.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.base.BaseException;
import com.ruoyi.system.domain.StepConfig;
import com.ruoyi.system.domain.XmStepRunLog;
import com.ruoyi.system.domain.dto.BarkPushContent;
import com.ruoyi.system.mapper.StepConfigMapper;
import com.ruoyi.system.mapper.XmStepRunLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;


@Slf4j
@Component("miMotionRunner")
public class MiMotionRunner {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private StepConfigMapper StepConfigMapper;

    @Resource
    private XmStepRunLogMapper xmStepRunLogMapper;

    private static final String KEY = "Yx9#mK2$pL7@qN4^";
    private static final String IV = "Bw5&hT8!vR3%jM6*";

    private final AES aes = new AES(
            Mode.CBC,
            Padding.PKCS5Padding,
            KEY.getBytes(CharsetUtil.CHARSET_UTF_8),
            IV.getBytes(CharsetUtil.CHARSET_UTF_8)
    );

    public void runStep(String key) {
        StepConfig stepConfig;
        int stepCount = 0;
        boolean flag = true;
        String configInfo = (String) this.stringRedisTemplate.opsForHash().get("xm-step-config", key);
        if (StringUtils.isBlank(configInfo)) {
            stepConfig = this.StepConfigMapper.selectStepConfigById(key);
        } else {
            stepConfig = JSON.parseObject(configInfo, StepConfig.class);
        }

        log.info("执行小米步数任务,用户名:{},详情:{}", stepConfig.getUserName(), JSON.toJSONString(stepConfig));
        XmStepRunLog.XmStepRunLogBuilder builder = XmStepRunLog.builder()
                .configId(stepConfig.getId())
                .userName(stepConfig.getUserName());
        try {

            if (StringUtils.isBlank(stepConfig.getStepCount())) {
                throw new BaseException("步数未配置:" + stepConfig.getUserName());
            }
            String[] range = stepConfig.getStepCount().split("-");
            if (stepConfig.getModel().equals("1")) {
                List<XmStepRunLog> logs = this.xmStepRunLogMapper.selectByConfigId(stepConfig.getId(), 5);
                if (CollectionUtils.isEmpty(logs)) {
                    stepCount = range.length > 1 ? RandomUtil.randomInt(Integer.parseInt(range[0]), Integer.parseInt(range[1])) : Integer.parseInt(stepConfig.getStepCount());
                } else {
                    stepCount = logs.get(0).getStepCount() + (range.length > 1 ? RandomUtil.randomInt(Integer.parseInt(range[0]), Integer.parseInt(range[1])) : Integer.parseInt(stepConfig.getStepCount()));
                }
            } else {
                stepCount = range.length > 1 ? RandomUtil.randomInt(Integer.parseInt(range[0]), Integer.parseInt(range[1])) : Integer.parseInt(stepConfig.getStepCount());
            }
            builder.stepCount(stepCount);


            JSONObject payload = new JSONObject();
            payload.put("ups1", stepConfig.getUserName());
            payload.put("ups2", stepConfig.getPassword());
            payload.put("ups3", stepCount);
            payload.put("timestamp", System.currentTimeMillis());



            String encryptedData = aes.encryptBase64(payload.toJSONString());


            try (HttpResponse execute = HttpUtil.createPost("https://bs.yanwan.store/run4/mi20251001.php")
                    .header("Cookie", "_d_id=1b2002ffba9fcc78bd09d9340c0d15")
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36")
                    .header("Referer", "https://bs.yanwan.store/run4/")
                    .header("Host", "bs.yanwan.store")
                    .header("Connection", "keep-alive")
                    .header("sec-ch-ua-platform", "\"macOS\"")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("sec-ch-ua", "\"Google Chrome\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Origin", "https://bs.yanwan.store")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-Mode", "cors")
                    .form("encrypted", encryptedData)
                    .execute()) {

                log.info("请求结果:{}", execute.body());

                JSONObject resp = JSON.parseObject(execute.body());

                if(!resp.getInteger("code").equals(200)){
                    throw new BaseException(execute.body());
                }
                builder.stepResult(execute.body());

            }catch (Exception e){
                log.error("请求api错误",e);
                builder.stepResult(e.getMessage());
                throw new BaseException("请求api错误");

            }

        } catch (BaseException e) {
            flag = false;
            builder.stepCount(0);
            log.error("小米步数业务业务异常", e);
        } catch (Exception e) {
            flag = false;
            builder.stepCount(0);
            log.error("小米步数未知异常", e);
        }

        if (stepConfig.getNotice() != null && stepConfig.getNotice() == 0 && StringUtils.isNotBlank(stepConfig.getNoticeId())) {

            for (String id : stepConfig.getNoticeId().split(",")) {
                try {
                    HttpUtil.createPost("https://bark.aiyatou.cn/" + id)
                            .body(JSON.toJSONString(BarkPushContent
                                    .builder()
                                    .title("小米步数任务通知")
                                    .body("步数刷入" + (flag ? "成功✌\uD83C\uDFFB" : "失败\uD83D\uDE1E") + "\n" +
                                            "用户:"+ stepConfig.getUserName() + "\n" +
                                            "当前步数:" + stepCount)
                                    .build())).execute();
                } catch (Exception e) {
                    log.error("推送通知失败", e);
                }
            }
        }
        XmStepRunLog build = builder.build();
        build.setCreateTime(new Date());
        this.xmStepRunLogMapper.insertXmStepRunLog(build);

    }


}
