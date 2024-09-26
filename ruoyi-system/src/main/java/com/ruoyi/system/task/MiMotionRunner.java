package com.ruoyi.system.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.system.domain.XmStepConfig;
import com.ruoyi.system.domain.XmStepRunLog;
import com.ruoyi.system.mapper.XmStepConfigMapper;
import com.ruoyi.system.mapper.XmStepRunLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Slf4j
@Component
public class MiMotionRunner {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private XmStepConfigMapper xmStepConfigMapper;

    @Resource
    private XmStepRunLogMapper xmStepRunLogMapper;


    public String generateRandomIp() {
        Random random = new Random();
        return random.nextInt(256) + "." + random.nextInt(256) + "." +
                random.nextInt(256) + "." + random.nextInt(256);
    }

    public void runStep(String key) {

        try {
            XmStepConfig stepConfig;
            String configInfo = this.stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isBlank(configInfo)) {
                stepConfig = this.xmStepConfigMapper.selectXmStepConfigById(key);
            } else {
                stepConfig = JSON.parseObject(configInfo, XmStepConfig.class);
            }

            XmStepRunLog.XmStepRunLogBuilder builder = XmStepRunLog.builder()
                    .configId(stepConfig.getId())
                    .userName(stepConfig.getUserName());

            Map<String, String> header = new HashMap<>();
            header.put(HttpHeaders.CONTENT_TYPE, ContentType.FORM_URLENCODED.getValue());
            header.put(HttpHeaders.USER_AGENT, "ZeppLife/6.10.1 (iPhone; iOS 18.0; Scale/3.00)");
            header.put("X-Forwarded-For", generateRandomIp());


            String access;
            try (HttpResponse response = HttpUtil.createPost("https://api-user.huami.com/registrations/" + stepConfig.getUserName() + "/tokens")
                    .addHeaders(header)
                    .form("client_id", "HuaMi")
                    .form("password", stepConfig.getPassword())
                    .form("redirect_uri", "https://s3-us-west-2.amazonaws.com/hm-registration/successsignin.html")
                    .form("token", "access").execute()) {
                if (response.getStatus() != 303) {
                    return;
                }
                access = UrlBuilder.of(response.header("Location")).getQuery().get("access").toString();
                builder.userTokenResult(JSON.toJSONString(response));
            } catch (Exception e) {
                log.error("获取用户access失败", e);
                return;
            }


            String url = "https://account.huami.com/v2/client/login";
            String data = "{\"allow_registration=\":\"false\",\"app_name\":\"com.xiaomi.hm.health\",\"app_version\":\"6.3.5\",\"code\":\"123\",\"country_code\":\"CN\",\"device_id\":\"2C8B4939-0CCD-4E94-8CBA-CB8EA6E613A1\",\"device_model\":\"phone\",\"dn\":\"api-user.huami.com%2Capi-mifit.huami.com%2Capp-analytics.huami.com\",\"grant_type\":\"access_token\",\"lang\":\"zh_CN\",\"os_version\":\"1.5.0\",\"source\":\"com.xiaomi.hm.health\",\"third_name\":\"email\"}";
            JSONObject jsonObject = JSON.parseObject(data);
            jsonObject.put("code", access);

            String token, userId;
            try (HttpResponse response1 = HttpUtil.createPost(url)
                    .addHeaders(header)
                    .form(jsonObject).execute()) {
                JSONObject resp = JSON.parseObject(response1.body());
                token = resp.getJSONObject("token_info").getString("login_token");
                userId = resp.getJSONObject("token_info").getString("user_id");
                builder.loginResult(resp.toJSONString());
            } catch (Exception e) {
                log.error("登录失败", e);
                return;
            }


            String appToken;
            try (HttpResponse appTokenResp = HttpUtil.createGet("https://account-cn.huami.com/v1/client/app_tokens?app_name=com.xiaomi.hm.health&dn=api-user.huami.com%2Capi-mifit.huami.com%2Capp-analytics.huami.com&login_token=" + token)
                    .addHeaders(header).execute()) {
                JSONObject appTokenRespJson = JSON.parseObject(appTokenResp.body());
                appToken = appTokenRespJson.getJSONObject("token_info").getString("app_token");
                builder.appTokenResult(appTokenRespJson.toJSONString());
            } catch (Exception e) {
                log.error("获取appToken失败", e);
                return;
            }

            int stepCount;
            if (stepConfig.getModel().equals("1")) {
                if (StringUtils.isBlank(stepConfig.getStep())) {
                    return;
                }
                String[] range = stepConfig.getStep().split("-");

                List<XmStepRunLog> logs = this.xmStepRunLogMapper.selectByConfigId(stepConfig.getId(), 5);
                if (CollectionUtils.isEmpty(logs)) {
                    stepCount = range.length > 1 ? RandomUtil.randomInt(Integer.parseInt(range[0]), Integer.parseInt(range[1])) : Integer.parseInt(stepConfig.getStep());
                } else {
                    stepCount = logs.get(0).getStepCount() + range.length > 1 ? RandomUtil.randomInt(Integer.parseInt(range[0]), Integer.parseInt(range[1])) : Integer.parseInt(stepConfig.getStep());
                }
            } else {
                String[] range = stepConfig.getStepCount().split("-");
                stepCount = range.length > 1 ? RandomUtil.randomInt(Integer.parseInt(range[0]), Integer.parseInt(range[1])) : Integer.parseInt(stepConfig.getStepCount());
            }
            builder.stepCount(stepCount);


            File bushu = new File(new ClassPathResource("xmStep").getPath() + File.separator + "summary.json");
            JSONObject summaryJson = JSON.parseObject(FileUtil.readString(bushu, StandardCharsets.UTF_8)
                    .replace("startTime", DateUtil.offsetHour(new Date(), -1).getTime() / 1000 + "")
                    .replace("endTime", System.currentTimeMillis() / 1000 + "")
                    .replace("totalCount", stepCount + ""));
            File bandFile = new File(new ClassPathResource("xmStep").getPath() + File.separator + "band.json");
            JSONObject bandJson = JSON.parseObject(FileUtil.readString(bandFile, StandardCharsets.UTF_8));
            bandJson.put("date", DateUtil.formatDate(new Date()));
            bandJson.put("summary", summaryJson.toJSONString());

            JSONArray requestArray = new JSONArray();
            requestArray.add(bandJson);

            try (HttpResponse finalResp = HttpUtil.createPost("https://api-mifit-cn.huami.com/v1/data/band_data.json?&t=" + System.currentTimeMillis())
                    .addHeaders(header)
                    .header("apptoken", appToken)
                    .form("userid", userId)
                    .form("last_sync_data_time", 1727147104)
                    .form("device_type", 0)
                    .form("last_deviceid", "DA932FFFFE8816E1")
                    .form("data_json", requestArray.toJSONString()).execute()) {
                builder.stepResult(finalResp.body());
            } catch (Exception e) {
                log.error("刷入步数失败", e);
                return;
            }
            this.xmStepRunLogMapper.insertXmStepRunLog(builder.build());
        } catch (Exception e) {
            log.error("小米步数未知异常", e);
        }
    }


}
