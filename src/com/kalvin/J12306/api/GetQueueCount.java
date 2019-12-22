package com.kalvin.J12306.api;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.cache.TicketCache;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.SubmitTicketInfoDTO;
import com.kalvin.J12306.http.Session;
import com.kalvin.J12306.utils.J12306Util;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 获取队列位置
 * Create by Kalvin on 2019/9/20.
 */
public class GetQueueCount {

    private final static Log log = LogFactory.get();

    private Session session;
    private String repeatSubmitToken;
    private String trainDate;
    private String trainNo;
    private String trainNum;
    private String passengerTicketStr;
    private String oldPassengerStr;
    private String seatType;
    private String fromStationCode;
    private String toStationCode;
    private String trainLocation;
    private int ifShowPassCodeTime;
    private boolean isNeedCode;
    private SubmitTicketInfoDTO submitTicketInfoDTO;

    public GetQueueCount(Session session, String repeatSubmitToken, String trainDate, String trainNo,
                         String trainNum, String passengerTicketStr, String oldPassengerStr, String seatType,
                         String fromStationCode, String toStationCode, String trainLocation, int ifShowPassCodeTime,
                         boolean isNeedCode, SubmitTicketInfoDTO submitTicketInfoDTO) {
        this.session = session;
        this.repeatSubmitToken = repeatSubmitToken;
        this.trainDate = trainDate;
        this.trainNo = trainNo;
        this.trainNum = trainNum;
        this.passengerTicketStr = passengerTicketStr;
        this.oldPassengerStr = oldPassengerStr;
        this.seatType = seatType;
        this.fromStationCode = fromStationCode;
        this.toStationCode = toStationCode;
        this.trainLocation = trainLocation;
        this.ifShowPassCodeTime = ifShowPassCodeTime;
        this.isNeedCode = isNeedCode;
        this.submitTicketInfoDTO = submitTicketInfoDTO;
    }

    public void send() {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("train_date", J12306Util.formatDateGMT(this.trainDate));
        formData.put("train_no", this.trainNo);
        formData.put("stationTrainCode", this.trainNum);
        formData.put("seatType", this.seatType);
        formData.put("fromStationTelecode", this.fromStationCode);
        formData.put("toStationTelecode", this.toStationCode);
        formData.put("leftTicket", submitTicketInfoDTO.getLeftTicketStr()); // todo 是否解码
        formData.put("purpose_codes", submitTicketInfoDTO.getPurposeCodes());
        formData.put("train_location", this.trainLocation);
        formData.put("_json_att", "");
        formData.put("REPEAT_SUBMIT_TOKEN", this.repeatSubmitToken);
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.GET_QUEUE_COUNT, formData);
        String body = httpResponse.body();
//        log.info("getQueueCount body = {}", body);

        TicketCache ticketCache = TicketCache.getInstance();
        // 余票数
        int ticketCount;
        JSONObject object = JSONUtil.parseObj(body);
        JSONObject dataObj = (JSONObject) object.get("data");
        if (object.get("status") != null && (boolean) object.get("status")) {
            String ticket = dataObj.get("ticket").toString();
            if (dataObj.get("count") != null) {
                Integer count = Integer.valueOf(dataObj.get("count").toString());
                if (!ticket.contains(",")) {
                    ticketCount = Integer.parseInt(ticket);
                } else {
                    String[] ticketSplit = ticket.split(",");
                    ticketCount = Arrays.stream(ticketSplit).map(Integer::valueOf).reduce(0, Integer::sum);
                }
                log.info("排队成功，你当前排在{}位，当前余票还有{}张", count, ticketCount);
                // 正式下单
                new ConfirmSingleForQueue(
                        this.session,
                        this.repeatSubmitToken,
                        this.passengerTicketStr,
                        this.oldPassengerStr,
                        this.trainNum,
                        this.trainLocation,
                        this.ifShowPassCodeTime,
                        this.isNeedCode,
                        this.submitTicketInfoDTO
                ).send();
            } else {
                // 将此列车加入小黑屋3分钟
                ticketCache.put(this.trainNum, this.trainNum, Constants.BLACK_ROOM_CACHE_EXP_TIME * 60);
                log.error("排队失败，错误信息：{}，将此列车{}加入小黑屋3分钟", object.get("messages"), this.trainNum);
            }
        } else {
            ticketCache.put(this.trainNum, this.trainNum, Constants.BLACK_ROOM_CACHE_EXP_TIME * 60);
            log.error("排队失败，错误信息：{}，将此列车{}加入小黑屋3分钟", object.get("messages"), this.trainNum);
        }
    }
            
}
