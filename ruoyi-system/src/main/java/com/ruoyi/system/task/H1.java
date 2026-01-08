package com.ruoyi.system.task;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static sun.security.x509.CertificateAlgorithmId.ALGORITHM;

public class H1 {
    private static final String KEY = "Yx9#mK2$pL7@qN4^";
    private static final String IV = "Bw5&hT8!vR3%jM6*";

//
//    public static void main(String[] args) {
//
//        AES aes = new AES(
//                Mode.CBC,
//                Padding.PKCS5Padding,
//                KEY.getBytes(CharsetUtil.CHARSET_UTF_8),
//                IV.getBytes(CharsetUtil.CHARSET_UTF_8)
//        );
//
//        // 1. 这是你抓包得到的原始字符串（带%的）
//        String encryptedFromPacket = "oFy535NsPbUPXV9tmVj1%2BcHovIkZU4qZMq%2FRMNewaj8e2wE40dK3Rf2LSu8MykEVy4yQcyXP82TnJUZf1RjIBsdX0VQvr0yyDiXit7B8uJKFhv%2Fz%2Bl%2Fe9dQ%2FfUrIB0Ig";
//
//
//        String decode = URLUtil.decode(encryptedFromPacket);
//
//
//
//        System.out.println(aes.decryptStr(decode));
//
//
//    }
//    @SneakyThrows
//    public static void main(String[] args) {
//
//        AES aes = new AES(
//                Mode.CBC,
//                Padding.PKCS5Padding,
//                KEY.getBytes(CharsetUtil.CHARSET_UTF_8),
//                IV.getBytes(CharsetUtil.CHARSET_UTF_8)
//        );
//        // 1. 手动构造 JSON 字符串，确保没有多余空格（非常重要，空格会导致密文完全不同）
//        String user = "banxiachuqing@gmail.com";
//        String pass = "Bxcq5276";
//        String step = "28889";
//
//        JSONObject payload = new JSONObject();
//        payload.put("ups1", user);
//        payload.put("ups2", pass);
//        payload.put("ups3", step);
//        payload.put("timestamp", System.currentTimeMillis());
//
//
//        String encryptedData = aes.encryptBase64(payload.toJSONString());
//
//
//
//        HttpResponse execute = HttpUtil.createPost("https://bs.yanwan.store/run4/mi20251001.php")
//                .header("Cookie", "_d_id=1b2002ffba9fcc78bd09d9340c0d15")
//                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36")
//                .header("Referer","https://bs.yanwan.store/run4/")
//                .header("Host","bs.yanwan.store")
//                .header("Connection","keep-alive")
//                .header("sec-ch-ua-platform","\"macOS\"")
//                .header("X-Requested-With","XMLHttpRequest")
//                .header("Accept","application/json, text/javascript, */*; q=0.01")
//                .header("sec-ch-ua","\"Google Chrome\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"")
//                .header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
//                .header("Origin","https://bs.yanwan.store")
//                .header("Sec-Fetch-Site","same-origin")
//                .header("Sec-Fetch-Mode","cors")
//
//                .form("encrypted", encryptedData)
//                .execute();
//
//
//        System.out.println(execute.body());
//
//    }
}
