package com.kalvin.J12306.api;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.config.UrlConfig;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.exception.J12306Exception;
import com.kalvin.J12306.http.Session;
import com.kalvin.J12306.utils.J12306Util;

/**
 * 排队获取订单等待信息,每隔3秒请求一次，最高请求次数为20次！
 * Create by Kalvin on 2019/9/20.
 */
public class QueryOrderWaitTime {

    private static final Log log = LogFactory.get();

    private Session session;
    private String repeatSubmitToken;

    public QueryOrderWaitTime(Session session, String repeatSubmitToken) {
        this.session = session;
        this.repeatSubmitToken = repeatSubmitToken;
    }

    public void send() {
        int tryTimes = 0;
        boolean success = false;
        String orderId = "";

        while (true) {
            if (tryTimes >= Constants.MAX_TRY_TIMES) {
                // todo 自动取消订单未实现
                log.info("排队失败，取消订单");
                break;
            }
            try {
                tryTimes++;
                log.info("下单ing...正在第{}次排队ing...", tryTimes);

                UrlConfig urlConfig = UrlsEnum.QUERY_ORDER_WAIT_TIME.getUrlConfig();
                urlConfig.setUrl(urlConfig.getUrl()
                        .replace("{0}", String.valueOf(System.currentTimeMillis()))
                        .replace("{1}", this.repeatSubmitToken));
                UrlsEnum.QUERY_ORDER_WAIT_TIME.setUrlConfig(urlConfig);
                HttpResponse httpResponse = session.httpClient.sendAsync(UrlsEnum.QUERY_ORDER_WAIT_TIME);
                String body = httpResponse.body();
//                log.info("queryOrderWaitTime body = {}", body);

                JSON parse = JSONUtil.parse(body);
                if (parse.getByPath("status") != null && (boolean) parse.getByPath("status")) {
                    JSONObject dObj = (JSONObject) parse.getByPath("data");
                    orderId = dObj.get("orderId").toString();
                    String waitCount = dObj.get("waitCount").toString();
//                    LOGGER.info("orderId={}", orderId);
                    if (StrUtil.isNotBlank(orderId) && !"null".equals(orderId) && orderId != null) {
                        log.info("订票成功！");
                        success = true;
                        break;
                    } else if (dObj.get("msg") == null) {
                        log.info("等待提交订单，等待队列：{}，时间剩余：{}毫秒", waitCount, dObj.get("waitTime"));
                    } else {
                        log.info("等待提交订单失败：{}", dObj.get("msg"));
                        break;
                    }
                } else {
                    log.info("等待提交订单中，信息：{}", parse.getByPath("messages"));
                }
            } catch (Exception e) {
                log.error("等待提交订单异常，错误信息：{}", e.getMessage());
                break;
            }
            J12306Util.sleep(2);   // 睡眠2秒
        }

        if (success) {
            // 订票成功，使用邮件通知抢票人
            log.info("恭喜您订票成功，订单号为：{}, 请立即打开浏览器登录12306，访问‘未完成订单’，在30分钟内完成支付!", orderId);
            log.info("以邮件方式通知抢票人");
            J12306Util.sendSuccessEmail(orderId);
            throw new J12306Exception("订票成功，结束抢票程序！");
        } else {
            // todo 有些情况排队失败也会订票成功，所有此处查询一下用户订单信息
            log.info("有些情况排队失败也会订票成功，所以建议您登录12306检查下未完成的订单。。。");
        }

    }
}
