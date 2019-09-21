package com.kalvin.J12306.api;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.SubmitTicketInfoDTO;
import com.kalvin.J12306.http.Session;

import java.util.HashMap;

/**
 * 检查订单
 * Create by Kalvin on 2019/9/20.
 */
public class CheckOrderInfo {

    private final static Log log = LogFactory.get();

    private Session session;
    private String repeatSubmitToken;
    private String seatType;
    private String passengerTicketStr;
    private String oldPassengerStr;
    private String trainDate;
    private String trainNo;
    private String trainNum;
    private String fromStationCode;
    private String toStationCode;
    private String trainLocation;
    private SubmitTicketInfoDTO submitTicketInfoDTO;

    public CheckOrderInfo(Session session, String repeatSubmitToken, String seatType,
                          String passengerTicketStr, String oldPassengerStr, String trainDate,
                          String trainNo, String trainNum, String fromStationCode, String toStationCode,
                          String trainLocation, SubmitTicketInfoDTO submitTicketInfoDTO) {
        this.session = session;
        this.repeatSubmitToken = repeatSubmitToken;
        this.seatType = seatType;
        this.passengerTicketStr = passengerTicketStr;
        this.oldPassengerStr = oldPassengerStr;
        this.trainDate = trainDate;
        this.trainNo = trainNo;
        this.trainNum = trainNum;
        this.fromStationCode = fromStationCode;
        this.toStationCode = toStationCode;
        this.trainLocation = trainLocation;
        this.submitTicketInfoDTO = submitTicketInfoDTO;
    }

    public void send() {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("cancel_flag", 2);
        formData.put("bed_level_order_num", "000000000000000000000000000000");
        formData.put("passengerTicketStr", this.passengerTicketStr);
        formData.put("oldPassengerStr", this.oldPassengerStr);
        formData.put("tour_flag", "dc"); // 单程
        formData.put("randCode", "");
        formData.put("whatsSelect", 1);   // 1-成人票 2-学生票
        formData.put("_json_att", "");
        formData.put("REPEAT_SUBMIT_TOKEN", this.repeatSubmitToken);

        try {
            HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.CHECK_ORDER_INFO, formData);
            String body = httpResponse.body();
            JSON parse = JSONUtil.parse(body);
//            log.info("checkOrderInfo body = {}", body);

            if (parse.getByPath("data.submitStatus") != null && (boolean) parse.getByPath("data.submitStatus")) {
                log.info("车票提交通过，正在尝试排队...");
                int ifShowPassCodeTime = Integer.parseInt((String) parse.getByPath("data.ifShowPassCodeTime"));
                String ifShowPassCode = (String) parse.getByPath("data.ifShowPassCode");
                boolean isNeedCode = "Y".equals(ifShowPassCode);
                // 获取排队队列位置
                new GetQueueCount(
                        this.session,
                        this.repeatSubmitToken,
                        this.trainDate,
                        this.trainNo,
                        this.trainNum,
                        this.passengerTicketStr,
                        this.oldPassengerStr,
                        this.seatType,
                        this.fromStationCode,
                        this.toStationCode,
                        this.trainLocation,
                        ifShowPassCodeTime,
                        isNeedCode,
                        this.submitTicketInfoDTO
                ).send();
            } else {
                log.info("车票提交失败，正在重试...");
            }
        } catch (Exception e) {
            log.info("车票提交异常，正在重试...错误信息：{}", e.getMessage());
        }

    }
}
