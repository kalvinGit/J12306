package com.kalvin.J12306.api;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.http.Session;

import java.util.HashMap;

/**
 * 我的订单
 * Create by Kalvin on 2019/9/24.
 */
public class MyOrder {

    private final static Log log = LogFactory.get();

    private Session session;

    public MyOrder(Session session) {
        this.session = session;
    }

    public void init() {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("_json_att", "");
        this.session.httpClient.send(UrlsEnum.INIT_NO_COMPLETE, formData);
    }

    /**
     * 查询未完成的订单
     * @return httpResponse
     */
    public HttpResponse queryMyNoComplete() {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("_json_att", "");
        try {
            return this.session.httpClient.send(UrlsEnum.QUERY_MY_ORDER_NO_COMPLETE, formData);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查订单状态
     * @return State
     */
    public State check() {
        HttpResponse httpResponse = this.queryMyNoComplete();
        if (httpResponse == null) {
            return State.fail();
        }
        String body = httpResponse.body();
        JSON parse = JSONUtil.parse(body);
        if (parse.getByPath("data") != null && parse.getByPath("data.orderDBList") != null) {
            JSONArray objects = JSONUtil.parseArray(parse.getByPath("data.orderDBList"));
            JSON parseODB = JSONUtil.parse(objects.get(0));
            String sequenceNo = (String) parseODB.getByPath("sequence_no");
            if (parseODB.getByPath("tickets.ticket_status_code").equals("i")) { // 待支付状态
                return State.noPay(sequenceNo);
            } else {
                return State.queue(sequenceNo);
            }
        }
        return State.fail();
    }

    /**
     * 取消订单
     * @param sequenceNo 订单ID
     */
    public void cancelNoComplete(String sequenceNo) {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("sequence_no", sequenceNo);
        formData.put("cancel_flag", "cancel_order");
        formData.put("_json_att", "");
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.CANCEL_NO_COMPLETE_MY_ORDER, formData);
        String body = httpResponse.body();
        JSON parse = JSONUtil.parse(body);
        if (parse.getByPath("data") != null && parse.getByPath("data.existError").equals("N")) {
            log.info("订单【{}】取消成功", sequenceNo);
        } else {
            log.info("订单【{}】取消失败", sequenceNo);
        }
    }

    /**
     * 订单状态
     */
    public static class State {
        /**
         * 待支付状态
         */
        public final static String NO_PAY_CODE = "NO_PAY";
        /**
         * 排队状态
         */
        public final static String QUEUE_CODE = "QUEUE";
        /**
         * 失败状态
         */
        public final static String FAIL_CODE = "FAIL";

        private String code;
        private String sequenceNo;

        private State(String code, String sequenceNo) {
            this.code = code;
            this.sequenceNo = sequenceNo;
        }

        private static State noPay(String sequenceNo) {
            return new State(State.NO_PAY_CODE, sequenceNo);
        }

        private static State queue(String sequenceNo) {
            return new State(State.QUEUE_CODE, sequenceNo);
        }

        private static State fail() {
            return new State(State.FAIL_CODE, "");
        }

        public String getCode() {
            return code;
        }

        public String getSequenceNo() {
            return sequenceNo;
        }
    }

}
