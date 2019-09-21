package com.kalvin.J12306.config;

/**
 * Create by Kalvin on 2019/9/18.
 */
public class UrlConfig {
    private String url;
    private String method;
    private String host;
    private String referer;

    public UrlConfig(String url, String method, String referer) {
        this.url = url;
        this.method = method;
        this.referer = referer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
