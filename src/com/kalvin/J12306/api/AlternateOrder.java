package com.kalvin.J12306.api;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.cache.TicketCache;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.UserInfoDTO;
import com.kalvin.J12306.http.Session;
import com.kalvin.J12306.utils.EmailUtil;
import com.kalvin.J12306.utils.J12306Util;

import java.util.HashMap;

/**
 * 候补订单
 * Create by Kalvin on 2019/9/25.
 */
public class AlternateOrder {

    private final static Log log = LogFactory.get();

    private Session session;
    private String secretStr;
    private String seatType;
    private String trainNo;
    private String jzdhDateE;
    private String jzdhHourE;

    public AlternateOrder(Session session, String secretStr, String seatType, String trainNo) {
        this.session = session;
        this.secretStr = secretStr;
        this.seatType = seatType;
        this.trainNo = trainNo;
    }

    public boolean checkFace() {
        TicketCache ticketCache = TicketCache.getInstance();
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("secretList", this.getSecretList());
        formData.put("_json_att", "");
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.CHECK_FACE, formData);
        String body = httpResponse.body();
//        log.info("checkFace body ={}", body);
        JSON parse = JSONUtil.parse(body);
        if ((boolean) parse.getByPath("status") && parse.getByPath("data") != null) {
            if ((boolean) parse.getByPath("data.face_flag")) {
                log.info("已通过人脸核验，可以进行候补车票！");
                return true;
            } else {
                log.info("你未通过人脸核验，通过人证一致性核验的用户及激活的“铁路畅行”会员可以提交候补需求，请您按照操作说明在铁路12306app.上完成人证核验");
                return false;
            }
        } else if (parse.getByPath("messages") != null) {
            ticketCache.put(this.trainNo, this.trainNo, Constants.BLACK_ROOM_CACHE_EXP_TIME * 60);
            log.info(((JSONArray) parse.getByPath("messages")).get(0).toString());
            return false;
        }
        ticketCache.put(this.trainNo, this.trainNo, Constants.BLACK_ROOM_CACHE_EXP_TIME * 60);
        return false;
    }

    public void getSuccessRate() {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("successSecret", this.getSuccessSecret());
        formData.put("_json_att", "");
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.GET_SUCCESS_RATE, formData);
        String body = httpResponse.body();
//        log.info("getSuccessRate body ={}", body);
        JSON parse = JSONUtil.parse(body);
        if ((boolean) parse.getByPath("status") && parse.getByPath("data") != null) {
//            this.trainNo = (String) parse.getByPath("data.flag[0].train_no");
            String info = (String) parse.getByPath("data.flag[0].info");
            log.info("开始提交候补订单，{}", info);
            this.submitOrderRequestAN();
        } else {
            log.info("getSuccessRate message:{}{}", ((JSONArray) parse.getByPath("messages")).get(0).toString(), parse.getByPath("validateMessages").toString());
        }
    }

    private void submitOrderRequestAN() {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("secretList", this.getSecretList());
        formData.put("_json_att", "");
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.SUBMIT_ORDER_REQUEST_AN, formData);
        String body = httpResponse.body();
//        log.info("submitOrderRequestAN body ={}", body);
        JSON parse = JSONUtil.parse(body);
        if ((boolean) parse.getByPath("status") && parse.getByPath("data.flag") != null) {
            this.lineUpToPayInit();
            this.passengerInitApi();
            this.confirmHB();
        } else {
            log.info("提交候补订单失败：{}{}", ((JSONArray) parse.getByPath("messages")).get(0).toString(), parse.getByPath("validateMessages").toString());
        }
    }

    private void lineUpToPayInit() {
        this.session.httpClient.send(UrlsEnum.LINE_UP_TO_PAY);
    }

    private void passengerInitApi() {
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.PASSENGER_INIT_API);
        String body = httpResponse.body();
        JSON parse = JSONUtil.parse(body);
        if ((boolean) parse.getByPath("status") && parse.getByPath("data") != null) {
            this.jzdhDateE = (String) parse.getByPath("data.jzdhDateE");
            this.jzdhHourE = (String) parse.getByPath("data.jzdhHourE");
        } else {
            log.info("passengerInitApi message:{}{}", ((JSONArray) parse.getByPath("messages")).get(0).toString(), parse.getByPath("validateMessages").toString());
        }
    }

    private void confirmHB() {
        HashMap<String, Object> formData = this.getConfirmHBParams();
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.CONFIRM_HB, formData);
        String body = httpResponse.body();
//        log.info("confirmHB body ={}", body);
        JSON parse = JSONUtil.parse(body);
        if ((boolean) parse.getByPath("status")) {
            if (parse.getByPath("data.flag") != null) {
                log.info("候补订单排队");
                this.queryQueue();
            } else {
                log.info("confirmHB error:{}", parse.getByPath("data.msg"));
            }
        } else {
            log.info("confirmHB message:{}{}", ((JSONArray) parse.getByPath("messages")).get(0).toString(), parse.getByPath("validateMessages").toString());
        }
    }

    private void queryQueue() {
        int maxNum = 10;
        int i = 0;
        while (i < maxNum) {
            HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.QUERY_QUEUE);
            String body = httpResponse.body();
            JSON parse = JSONUtil.parse(body);
            if ((boolean) parse.getByPath("status")) {
                EmailUtil.send("12306候补成功", "恭喜您候补成功，请立即打开浏览器登录12306，访问‘候补订单’，在30分钟内完成支付!");
                break;
            }
            i++;
            J12306Util.sleep(1);
        }
    }

    private String getSecretList() {
        String secretList = "{secretStr}#{seatType}|";
        return secretList
                .replace("{secretStr}", this.secretStr)
                .replace("{seatType}", this.seatType);
    }

    private String getSuccessSecret() {
        String successSecret = "{secretStr}#{seatType}";
        return successSecret
                .replace("{secretStr}", this.secretStr)
                .replace("{seatType}", this.seatType);
    }

    private HashMap<String, Object> getConfirmHBParams() {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("passengerInfo", this.getPassengerInfo());
        formData.put("jzParam", "{0}#{1}"
                .replace("{0}", this.jzdhDateE)
                .replace("{1}", this.jzdhHourE)
                .replace(":", "#"));
        formData.put("hbTrain", "{0},{1}#"
                .replace("{0}", this.trainNo)
                .replace("{1}", this.seatType));
        formData.put("lkParam", "");
        return formData;
    }

    private String getPassengerInfo() {
        TicketCache ticketCache = TicketCache.getInstance();
        UserInfoDTO userInfoDTO = (UserInfoDTO) ticketCache.get(Constants.USER_INFO_KEY);
        String passengerInfo = "1#{name}#1#{idNo}#{encStr};";
        return passengerInfo
                .replace("{name}", userInfoDTO.getName())
                .replace("{idNo}", userInfoDTO.getIdNo())
                .replace("{encStr}", userInfoDTO.getUserEncStr());
    }
}
