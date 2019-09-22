package com.kalvin.J12306.api;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.UserInfoDTO;
import com.kalvin.J12306.exception.J12306Exception;
import com.kalvin.J12306.http.Session;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;

/**
 * 登录
 * Create by Kalvin on 2019/9/18.
 */
public class Login {

    private static final Log log = LogFactory.get();
    private Session session;
    private String username;
    private String password;

    public Login(Session session, String username, String password) {
        this.session = session;
        this.username = username;
        this.password = password;
    }

    public UserInfoDTO send() {
        HttpResponse initRes = this.session.httpClient.send(UrlsEnum.LOGIN_INIT);
        this.session.setCookie(initRes.getCookies());
        log.info("进入12306登录页，状态码：{}", initRes.getStatus());
        this.initLogDevice();
        Captcha captcha = new Captcha(this.session);
        // 获取登录验证码
        captcha.getLoginCaptchaImg();
        // 校验登录验证码
        if (captcha.checkLoginCaptchaImg()) {
            log.info("验证码通过，开始密码登录");
            // 开始密码登录
            HashMap<String, Object> formData = new HashMap<>();
            formData.put("username", this.username);
            formData.put("password", this.password);
            formData.put("appid", "otn");
            HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.LOGIN, formData);
            String body = httpResponse.body();
//            log.info("login body={}", body);

            if (StrUtil.isBlank(body)) {
                throw new J12306Exception("登录失败，可能需要设置RAIL_EXPIRATION cookie值！");
            }

            JSONObject jsonObject = JSONUtil.parseObj(body);
            Integer resultCode = (Integer) jsonObject.get("result_code");
            if (resultCode == 0) {   // 登录成功，获取tk
                log.info("登录成功");
                this.userLogin();
                this.passport();
                boolean uamtk = this.postUamTK((String) jsonObject.get("uamtk"));
                if (uamtk) {
                    return this.getUserInfo();
                }
            } else {
                log.info("登录失败，请检查12306账号或密码是否正确。");
                throw new J12306Exception("登录失败，请检查12306账号或密码是否正确。");
            }
        } else {
            log.info("登录失败，验证码不通过，请重试...");
        }
        return null;
    }

    private void initLogDevice() {
        /*String userAgent = this.session.httpClient.getHeadder("User-Agent");
        UrlConfig urlConfig = UrlsEnum.LOG_DEVICE.getUrlConfig();
        urlConfig.setUrl(urlConfig.getUrl()
                .replace("{0}", userAgent)
                .replace("{1}", String.valueOf(System.currentTimeMillis())));
        UrlsEnum.LOG_DEVICE.setUrlConfig(urlConfig);*/
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.LOG_DEVICE);
        String body = httpResponse.body();
//        log.info("deviceInfo body = {}", body);
        String startIdxStr = "{";
        String endIdxStr = "}";
        body = body.substring(body.indexOf(startIdxStr), body.indexOf(endIdxStr) + 1);
        JSONObject jsonObject = JSONUtil.parseObj(body);
        // 设置到session的cookie中
        String railExpiration = jsonObject.get("exp").toString();
        String railDeviceId = jsonObject.get("dfp").toString();
        // todo 目前可用的railDeviceId暂时还没获取到，可手动配置，如下即可完成登录
//        railDeviceId = "oL0EiDIpSoF3hTcOmgROih1TZS_Pd2YfKFLZJPftrOKphLUdXKEBrBh59ay4T6J1xNA7q6mif-qQrlXnYQYrbCBxbmQ6TU5ZV6Q9EOLohmKxowKP7niwJ-POTI7JKKXHm-GQvZLqoGQ08GMdBKDbz5nxjrmI7jNy";
        this.session.setCookie("RAIL_EXPIRATION=" + railExpiration);
        this.session.setCookie("RAIL_DEVICEID=" + railDeviceId);
    }

    private void userLogin() {
        this.session.httpClient.send(UrlsEnum.USER_LOGIN);
    }

    private void passport() {
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.PASSPORT);
        List<HttpCookie> cookies = httpResponse.getCookies();
        // 设置cookies
        this.session.setCookie(cookies);
    }

    /**
     * 获取登录token
     * @param uamTK uamTK令牌
     */
    private boolean postUamTK(String uamTK) {
        if (StrUtil.isNotBlank(uamTK)) {
            HashMap<String, String> headers = new HashMap<String, String>() {{
                put("uamtk", uamTK);
            }};
            this.session.httpClient.setHeader(headers);
        }

        HashMap<String, Object> formData = new HashMap<String, Object>() {{
            put("appid", "otn");
        }};
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.UAM_TK, formData);
        String body = httpResponse.body();

        JSONObject jsonObject = JSONUtil.parseObj(body);
        Integer resultCode = (Integer) jsonObject.get("result_code");
        if (resultCode == 0) {
            this.session.token = (String) jsonObject.get("newapptk");
            return this.postUamAuthClient();
        }
        return false;
    }

    /**
     * 获取权限
     */
    private boolean postUamAuthClient() {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("tk", this.session.token);
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.UAM_AUTH_CLIENT, formData);
        List<HttpCookie> cookies = httpResponse.getCookies();
        this.session.setCookie(cookies);

        String body = httpResponse.body();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        Integer resultCode = (Integer) jsonObject.get("result_code");
        return resultCode == 0;
    }

    /**
     * 获取用户信息
     */
    private UserInfoDTO getUserInfo() {
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.GET_USER_INFO);
        String body = httpResponse.body();
//        log.info("queryPassengerInfo body={}", body);
        JSON parse = JSONUtil.parse(body);
        JSONObject object = (JSONObject) parse.getByPath("data.userDTO.loginUserDTO");
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setIdNo(object.get("id_no").toString());
        userInfo.setName(object.get("name").toString());
        userInfo.setUsername(object.get("user_name").toString());
        userInfo.setIdTypeCode(object.get("id_type_code").toString());
        userInfo.setIdTypeName(object.get("id_type_name").toString());
        userInfo.setEmail(parse.getByPath("data.userDTO.email").toString());
        return userInfo;
    }

}
