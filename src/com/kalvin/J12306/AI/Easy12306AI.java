package com.kalvin.J12306.AI;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.io.File;

/**
 * 此图片识别AI由https://github.com/zhaipro/easy12306提供
 * Create by Kalvin on 2019/9/19.
 */
public class Easy12306AI implements ImageAI {

    private static final Log log = LogFactory.get();

    private String aiUrl;
    private String imgPath;

    public Easy12306AI(String aiUrl, String imgPath) {
        this.aiUrl = aiUrl;
        this.imgPath = imgPath;
    }

    @Override
    public String printCode() {
        // 此AI不支持验证多个标签的图片验证码
        HttpRequest request = HttpUtil.createPost(this.aiUrl);
        request.form("file", new File(this.imgPath));
        String body = request.execute().body(); // text: 辣椒酱 , images: 调色板 , 雨靴 , 绿豆 , 辣椒酱 , 雨靴 , 雨靴 , 档案袋 , 辣椒酱
        body = body.replaceAll(" ", "");
//        log.info("printCode body = {}", body);
        String tagTxt = body.substring(body.indexOf("text:") + 5, body.indexOf(",images"));
        String imagesTxt = body.substring(body.indexOf("images:") + 7);
        String[] imageTxtArr = imagesTxt.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < imageTxtArr.length; i++) {
            if (tagTxt.equals(imageTxtArr[i])) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(i + 1);
            }
        }
        log.info("验证码：{}", sb.toString());
        return sb.toString();
    }
}
