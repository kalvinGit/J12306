package com.kalvin.J12306.api;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.config.UrlConfig;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.UserInfoDTO;
import com.kalvin.J12306.exception.J12306Exception;
import com.kalvin.J12306.http.Session;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        Captcha captcha = new Captcha(this.session);
        // 获取登录验证码
        captcha.getLoginCaptchaImg();
        // 获取deviceId
        this.initLogDevice();
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
                throw new J12306Exception(Constants.UPDATE_LOG_DEVICE_ERROR_MSG);
            }

            JSONObject jsonObject = JSONUtil.parseObj(body);
            Integer resultCode = (Integer) jsonObject.get("result_code");
            if (resultCode == 0) {   // 登录成功，获取tk
                log.info("登录成功");
                this.userLogin();
                this.passport();
                new GetJS(this.session).send();
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
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";
        Session ldSession = new Session();
        UrlConfig urlConfig = UrlsEnum.LOG_DEVICE.getUrlConfig();
        /*urlConfig.setUrl(urlConfig.getUrl()
                .replace("{0}", userAgent)
                .replace("{1}", String.valueOf(System.currentTimeMillis())));*/
        urlConfig.setUrl(this.fillLogDeviceUrlParams(urlConfig.getUrl()));
        UrlsEnum.LOG_DEVICE.setUrlConfig(urlConfig);
        HttpResponse httpResponse = ldSession.httpClient.send(UrlsEnum.LOG_DEVICE);
        String body = httpResponse.body();
//        log.info("deviceInfo body = {}", body);
        String startIdxStr = "{";
        String endIdxStr = "}";
        body = body.substring(body.indexOf(startIdxStr), body.indexOf(endIdxStr) + 1);
        JSONObject jsonObject = JSONUtil.parseObj(body);
        // 设置到session的cookie中
        String railExpiration = jsonObject.get("exp").toString();
        String railDeviceId = jsonObject.get("dfp").toString();
//        log.info("railDeviceId={}", railDeviceId);
        this.session.setCookie("RAIL_EXPIRATION=" + railExpiration);
        this.session.setCookie("RAIL_DEVICEID=" + railDeviceId);
//        this.session.httpClient.setHeader(new HashMap<String, String>() {{put("User-Agent", userAgent);}});
    }

    private String fillLogDeviceUrlParams(String url) {
        final StringBuilder sb = new StringBuilder();
        /**
         * 如果RAIL_DEVICEID失效了，以下参数需要更新（顺序一定要对，不然找不到logdevice）
         * 更新步骤：
         * 1.浏览器访问：https://kyfw.12306.cn/otn/login/init
         * 2.按f12进入调试模式并点击Network选项
         * 3.清除浏览器缓存的有关12306.cn和kyfw.12306.cn的Cookie（谷歌浏览器点击浏览器地址栏的小锁）
         * 4.按f5重新刷新(只有第1次刷新才有出现，所以不要刷新2次)
         * 5.在Network选项下找到logdevice请求，点击它，在Headers选项下拉到最下面就可以找到如下几个参数，复制替换它即可
         */
        final String algID = "PyvGQGRrn7";
        final String hashCode = "zgwkwwmfXov0h0OiTVGEm5O3x8wCUon2_s6JFyCUmFE";
        final String EOQP = "8f58b1186770646318a429cb33977d8c";
        final String jp76 = "52d67b2a5aa5e031084733d5006cc664";
        final String q5aJ = "-8";
        final String Oaew = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";
        final String E3gR = "662690cce73ebaa8bae40c90cd3a15d5";

        new LinkedHashMap<String, Object>() {{
            put("algID", algID);
            put("hashCode", hashCode);
            put("FMQw", "0");
            put("q4f3", "zh-CN");
            put("VPIf", "1");
            put("custID", "133");
            put("VEek", "unknown");
            put("dzuS", "0");
            put("yD16", "0");
            put("EOQP", EOQP);
            put("jp76", jp76);
            put("hAqN", "Win32");
            put("platform", "WEB");
            put("ks0Q", "d22ca0b81584fbea62237b14bd04c866");
            put("TeRS", "1040x1920");
            put("tOHY", "24xx1080x1920");
            put("Fvje", "i1l1o1s1");
            put("q5aJ", q5aJ);
            put("wNLf", "99115dfb07133750ba677d055874de87");
            put("0aew", Oaew);
            put("E3gR", E3gR);
            put("timestamp", String.valueOf(System.currentTimeMillis()));
        }}.forEach((k, v) -> {
            if (sb.length() == 0) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            sb.append(k).append("=").append(v);
        });
        return url + sb.toString();
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
        /*if (StrUtil.isNotBlank(uamTK)) {
            HashMap<String, String> headers = new HashMap<String, String>() {{
//                put("uamtk", uamTK);
            }};
            this.session.httpClient.setHeader(headers);
        }*/

        HashMap<String, Object> formData = new HashMap<String, Object>() {{
            put("appid", "otn");
        }};
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.UAM_TK, formData);
        log.info("postUamTK http status = {}", httpResponse.getStatus());
        String body = httpResponse.body();
//        log.info("postUamTK body = {}", body);
        try {
            JSONObject jsonObject = JSONUtil.parseObj(body);
            Integer resultCode = (Integer) jsonObject.get("result_code");
            if (resultCode == 0) {
                this.session.token = (String) jsonObject.get("newapptk");
                return this.postUamAuthClient();
            }
        } catch (JSONException je) {
            return false;
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
        try {
            JSONObject jsonObject = JSONUtil.parseObj(body);
            Integer resultCode = (Integer) jsonObject.get("result_code");
            return resultCode == 0;
        } catch (JSONException e) {
            return false;
        }
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
