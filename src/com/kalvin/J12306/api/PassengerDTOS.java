package com.kalvin.J12306.api;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.kalvin.J12306.cache.TicketCache;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.UserInfoDTO;
import com.kalvin.J12306.http.Session;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 乘客信息
 * Create by Kalvin on 2019/9/20.
 */
public class PassengerDTOS {

    private Session session;
    private String repeatSubmitToken;
    private String seatType;

    public PassengerDTOS(Session session, String repeatSubmitToken, String seatType) {
        this.session = session;
        this.repeatSubmitToken = repeatSubmitToken;
        this.seatType = seatType;
    }

    /**
     * 获取乘客购票信息
     * @return passengerTicketStr
     */
    public String getPassengerTicketStr() {
        String passengerTicketStr = "{seatType},0,1,{name},1,{passengerIdCard},,N,{allEncStr}";
        // 从缓存中获取用户信息
        TicketCache ticketCache = TicketCache.getInstance();
        UserInfoDTO userInfo = (UserInfoDTO) ticketCache.get("userInfo");
        String idNo = userInfo.getIdNo();
        String name = userInfo.getName();

        HashMap<String, Object> formData = new HashMap<>();
        formData.put("REPEAT_SUBMIT_TOKEN", repeatSubmitToken);
        formData.put("_json_att", "");
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.GET_PASSENGERDTOS, formData);
        String body = httpResponse.body();
        JSON parse = JSONUtil.parse(body);
        JSONArray jsonArray = JSONUtil.parseArray(parse.getByPath("data.normal_passengers"));
        List<Object> list = jsonArray.stream().filter(object ->
                ((JSONObject) object).get("passenger_id_no").equals(idNo))
                .collect(Collectors.toList());
        String allEncStr = ((JSONObject) list.get(0)).get("allEncStr").toString();

        return passengerTicketStr
                .replace("{seatType}", this.seatType)
                .replace("{name}", name)
                .replace("{passengerIdCard}", idNo)
                .replace("{allEncStr}", allEncStr);
    }

    /**
     * 获取乘客信息
     * @return oldPassengerStr
     */
    public String getOldPassengerStr() {
        String oldPassengerStr = "{name},1,{passengerIdCard},1_";
        // 从缓存中获取用户信息
        TicketCache ticketCache = TicketCache.getInstance();
        UserInfoDTO userInfo = (UserInfoDTO) ticketCache.get("userInfo");
        String idNo = userInfo.getIdNo();
        String name = userInfo.getName();
        return oldPassengerStr.replace("{name}", name).replace("{passengerIdCard}", idNo);
    }
}
