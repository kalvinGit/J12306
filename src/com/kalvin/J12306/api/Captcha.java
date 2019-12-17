package com.kalvin.J12306.api;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.kalvin.J12306.AI.Easy12306AI;
import com.kalvin.J12306.AI.ImageAI;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.config.UrlConfig;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.exception.J12306Exception;
import com.kalvin.J12306.http.Session;
import com.kalvin.J12306.utils.J12306Util;

import java.io.File;
import java.util.HashMap;

/**
 * 验证码
 * Create by Kalvin on 2019/9/18.
 */
public class Captcha {

    private Session session;
    private String loginCaptchaImageName;
    private String orderCaptchaImageName;

    public Captcha(Session session) {
        this.session = session;
    }

    /**
     * 登录验证码
     */
    public void getLoginCaptchaImg() {
        UrlConfig urlConfig = UrlsEnum.CAPTCHA.getUrlConfig();
        urlConfig.setUrl(urlConfig.getUrl().replace("{0}", J12306Util.genRandNumber()));
        UrlsEnum.CAPTCHA.setUrlConfig(urlConfig);
        HttpResponse httpResponse = session.httpClient.sendAsync(UrlsEnum.CAPTCHA);
        this.loginCaptchaImageName = this.getNewLoginCaptchaImgFileName();
        httpResponse.writeBody(new File(Constants.CAPTCHA_IMG_PRE_PATH + this.loginCaptchaImageName));
    }

    /**
     * 订单页面验证码
     */
    public String getOrderCaptchaImg() {
        UrlConfig urlConfig = UrlsEnum.GET_PASSCODE_NEW.getUrlConfig();
        urlConfig.setUrl(urlConfig.getUrl().replace("{0}", J12306Util.genRandNumber()));
        UrlsEnum.GET_PASSCODE_NEW.setUrlConfig(urlConfig);
        HttpResponse httpResponse = session.httpClient.sendAsync(UrlsEnum.GET_PASSCODE_NEW);
        this.orderCaptchaImageName = this.getNewOrderCaptchaImgFileName();
        httpResponse.writeBody(new File(Constants.CAPTCHA_IMG_PRE_PATH + this.orderCaptchaImageName));
        return this.orderCaptchaImageName;
    }

    public boolean checkLoginCaptchaImg() {
        J12306Util.sleep(2);
        // 若需要使用其它打码平台AI，在AI包下新增一个类实现ImageAI接口并更换下面图片AI实例即可
        ImageAI imageAI = new Easy12306AI(Constants.IMAGE_AI_URL, Constants.CAPTCHA_IMG_PRE_PATH + this.loginCaptchaImageName);
        String code = imageAI.printCode();
        // 转化为图片坐标点
        String answerCode = J12306Util.getCaptchaPos(code);
//        System.out.println("answerCode = " + answerCode);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("answer", answerCode);
        hashMap.put("login_site", "E");
        hashMap.put("rand", "sjrand");
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.CHECK_CAPTCHA, hashMap);
        String body = httpResponse.body();
        if (StrUtil.isBlank(body)) {
            throw new J12306Exception(Constants.UPDATE_LOG_DEVICE_ERROR_MSG);
        }
//        System.out.println("checkLoginCaptchaImg body = " + body);
        JSONObject jsonObject = JSONUtil.parseObj(body);
        String resultCode = jsonObject.get("result_code").toString();
        return "4".equals(resultCode);
    }

    private String getNewLoginCaptchaImgFileName() {
        return "login" + RandomUtil.randomString(5) + ".png";
    }

    private String getNewOrderCaptchaImgFileName() {
        return "order" + RandomUtil.randomString(5) + ".png";
    }
}
