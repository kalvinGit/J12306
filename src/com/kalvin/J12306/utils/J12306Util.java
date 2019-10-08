package com.kalvin.J12306.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.dto.TicketInfoDTO;

import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 工具类
 * Create by Kalvin on 2019/9/19.
 */
public class J12306Util {

    private final static Log log = LogFactory.get();

    public static String genRandNumber() {
        return String.valueOf(RandomUtil.randomDouble(0, 0.9, 17, RoundingMode.HALF_UP));
    }

    public static void sleep(int second) {
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
            System.out.println("Thread.sleep error = " + e.getMessage());
        }
    }

    public static String getCaptchaPos(String codeIdx) {
        final List<String> DICT_CODE = Arrays.asList("36,46", "116,46", "188,46", "267,43", "40,118", "113,119", "190,122", "264,115");
        StringBuilder sb = new StringBuilder();
        for (String i : codeIdx.split(",")) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(DICT_CODE.get(Integer.parseInt(i) - 1));
        }
        return sb.toString();
    }

    /**
     * 解析车票信息实体
     * @param ticketRes 车票查询结果字符串
     * @return List<TicketInfoDTO>
     */
    public static List<TicketInfoDTO> parseTicketInfo(String ticketRes) {
        JSON parse = JSONUtil.parse(ticketRes);
        JSONObject map = (JSONObject) parse.getByPath("data.map");
        JSONArray result = (JSONArray) parse.getByPath("data.result");
        List<TicketInfoDTO> ticketInfoDTOS = new ArrayList<>();
        TicketInfoDTO ticketInfoDTO;

        for (Object object : result) {
            ticketInfoDTO = new TicketInfoDTO();
            String trainInfo = object.toString();
            String[] split = trainInfo.split("\\|");

            if (StrUtil.isNotEmpty(split[0])) {
                ticketInfoDTO.setSecretStr(split[0]);
                ticketInfoDTO.setTrainNo(split[2]);
                ticketInfoDTO.setTrainNum(split[3]);
                ticketInfoDTO.setFormStationTelecode(split[6]);
                ticketInfoDTO.setToStationTelecode(split[7]);
                ticketInfoDTO.setGoOffTime(split[8]);
                ticketInfoDTO.setArrivalTime(split[9]);
                ticketInfoDTO.setLastTime(split[10]);
                ticketInfoDTO.setLeftTicket(split[12]);
                ticketInfoDTO.setTrainLocation(split[15]);
//            ticketInfoDTO.setBusinessSeat(split[32]);   // or 5
                ticketInfoDTO.setL1Seat(split[31]);
                ticketInfoDTO.setL2Seat(split[30]);
                ticketInfoDTO.setL1SoftBerth(split[23]);
                ticketInfoDTO.setL2HardBerth(split[28]);
                ticketInfoDTO.setHardSeat(split[29]);
                ticketInfoDTO.setNoSeat(split[26]);
                ticketInfoDTO.setCanAlternate(split[37].equals("1"));
                if (split.length == 38) {
                    ticketInfoDTO.setCanNotAlternateSeatType("");
                }
                if (split.length == 39) {
                    ticketInfoDTO.setCanNotAlternateSeatType(split[38]);
                }

                ticketInfoDTO.setFormStationName(map.get(split[6]).toString());
                ticketInfoDTO.setToStationName(map.get(split[7]).toString());

                ticketInfoDTOS.add(ticketInfoDTO);
            }
        }
        return ticketInfoDTOS;
    }

    public static String urlDecode(String str) {
        try {
            // 先进行一次解码。避免提交后再编码一次导致参数secretStr失效
            return URLDecoder.decode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        return str;
    }

    public static String formatDateGMT(String dateStr) {
        final DateTime date = DateUtil.parse(dateStr);
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy '00:00:00' 'GMT'Z '(中国标准时间)'", Locale.ENGLISH);
        return sdf.format(date);
    }

}
