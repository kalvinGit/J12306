package com.kalvin.J12306.http;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.config.UrlConfig;
import com.kalvin.J12306.config.UrlsEnum;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;

/**
 * 会话类
 * Create by Kalvin on 2019/9/18.
 */
public class Session {

    public HttpClient httpClient;
    public String token;
    public String cookie;

    public Session() {
        this.httpClient = new HttpClient();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        if (StrUtil.isNotEmpty(this.cookie)) {
            this.cookie += ";";
        } else {
            this.cookie = "";
        }
        this.cookie += cookie;
    }

    public void setCookie(List<HttpCookie> cookies) {
        cookies.forEach(hc -> {
            if (StrUtil.isNotEmpty(this.cookie)) {
                this.cookie += ";";
            } else {
                this.cookie = "";
            }
            this.cookie += hc.toString();
        });
    }

    public class HttpClient {

        private HttpRequest httpRequest;

        private HttpClient() {
        }

        public HttpResponse send(UrlsEnum urlsEnum) {
            return this.send(urlsEnum, null, false);
        }

        public HttpResponse send(UrlsEnum urlsEnum, HashMap<String, Object> formData) {
            return this.send(urlsEnum, formData, false);
        }

        public HttpResponse sendAsync(UrlsEnum urlsEnum) {
            return this.send(urlsEnum, null, true);
        }

        public HttpResponse sendAsync(UrlsEnum urlsEnum, HashMap<String, Object> formData) {
            return this.send(urlsEnum, formData, true);
        }

        public HttpResponse send(UrlsEnum urlsEnum, HashMap<String, Object> formData, boolean async) {
            UrlConfig urlConfig = urlsEnum.getUrlConfig();
            if (this.httpRequest == null) {
                this.httpRequest = HttpUtil.createGet(urlConfig.getUrl());
                this.httpRequest.header("Host", Constants.HOST);
                this.httpRequest.header("Origin", Constants.ORIGIN);
                this.httpRequest.header("Connection", "keep-alive");
                this.httpRequest.header("Accept", Constants.ACCEPT);
            } else {
                this.httpRequest.setUrl(urlConfig.getUrl());
                this.httpRequest.setMethod(this.getMethod(urlConfig.getMethod()));
            }
            if (!"".equals(urlConfig.getReferer())) {
                this.httpRequest.header("Referer", urlConfig.getReferer());
            }
            // 设置cookie
            if (StrUtil.isNotBlank(cookie)) {
                this.httpRequest.header("Cookie", cookie);
            }
//            this.httpRequest.headers().forEach((k, v) -> System.out.println(k + "=" + v));
            // 设置表单参数
            if (formData != null) {
                this.httpRequest.form(formData);
            }
            if (async) {
                return this.httpRequest.executeAsync();
            } else {
                return this.httpRequest.execute();
            }
        }

        private Method getMethod(String methodStr) {
            return methodStr.equalsIgnoreCase("get") ? Method.GET : Method.POST;
        }

        public void setHeader(HashMap<String, String> headers) {
            headers.forEach((k, v) -> this.httpRequest.header(k, v));
        }

        public String getHeader(String name) {
            return this.httpRequest.header(name);
        }
    }

}
