package com.kalvin.J12306.api;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.SubmitTicketInfoDTO;
import com.kalvin.J12306.http.Session;
import com.kalvin.J12306.utils.StationUtil;

import java.util.Date;
import java.util.HashMap;

/**
 * 提交订单
 * Create by Kalvin on 2019/9/19.
 */
public class SubmitOrderRequest {

    private static final Log log = LogFactory.get();

    private Session session;
    private String secretStr;
    private String seatType;
    private String trainDate;
    private String formStationCode;
    private String toStationCode;
    private String trainNo;
    private String trainNum;
    private String trainLocation;

    public SubmitOrderRequest(Session session, String secretStr, String seatType, String trainDate, String formStationCode, String toStationCode, String trainNo, String trainNum, String trainLocation) {
        this.session = session;
        this.secretStr = secretStr;
        this.seatType = seatType;
        this.trainDate = trainDate;
        this.formStationCode = formStationCode;
        this.toStationCode = toStationCode;
        this.trainNo = trainNo;
        this.trainNum = trainNum;
        this.trainLocation = trainLocation;
    }

    public void send() {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("secretStr", this.secretStr);
        formData.put("train_date", this.trainDate);
        formData.put("back_train_date", DateUtil.format(new Date(), "yyyy-MM-dd")); // 返程日
        formData.put("tour_flag", "dc"); // 单程
        formData.put("purpose_codes", "ADULT");  // 成人票
        formData.put("query_from_station_name", StationUtil.getStationName(this.formStationCode));
        formData.put("query_to_station_name", StationUtil.getStationName(this.toStationCode));
        formData.put("undefined", "");
        try {
            HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.SUBMIT_ORDER_REQUEST, formData);
            if (httpResponse.getStatus() == Constants.REQ_SUCCESS_STATUS) {
                String body = httpResponse.body();
//                log.info("submitOrderRequest body ={}", body);
                JSONObject jsonObject = JSONUtil.parseObj(body);
                boolean status = (boolean) jsonObject.get("status");
                // 提交订单
                if (status && "N".equals(jsonObject.get("data").toString())) {
                    RepeatSubmitToken repeatSubmitToken = new RepeatSubmitToken(this.session);
                    SubmitTicketInfoDTO submitTicketInfo = repeatSubmitToken.getSubmitTicketInfo();
                    // GetJS
                    new GetJS(this.session).send();
                    // 获取乘客信息
                    PassengerDTOS passengerDTOS = new PassengerDTOS(this.session, submitTicketInfo.getRepeatSubmitToken(), this.seatType);
                    final String passengerTicketStr = passengerDTOS.getPassengerTicketStr();
                    final String oldPassengerStr = passengerDTOS.getOldPassengerStr();
                    // 获取订单页面验证码
                    new Captcha(this.session).getOrderCaptchaImg();
                    // 检查订单
                    new CheckOrderInfo(
                            this.session,
                            submitTicketInfo.getRepeatSubmitToken(),
                            this.seatType,
                            passengerTicketStr,
                            oldPassengerStr,
                            this.trainDate,
                            this.trainNo,
                            this.trainNum,
                            this.formStationCode,
                            this.toStationCode,
                            this.trainLocation,
                            submitTicketInfo
                    ).send();
                } else {
                    log.info("订单提交失败，正在重试...错误信息：{}", jsonObject.get("messages"));
                }
            }
        } catch (Exception e) {
            log.info("订单提交异常，正在重试...错误信息：{}", e.getMessage());
        }

    }



}
