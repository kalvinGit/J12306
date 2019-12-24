package com.kalvin.J12306.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.api.Ticket;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.config.TicketSeatType;
import com.kalvin.J12306.dto.TicketInfoDTO;
import com.kalvin.J12306.exception.J12306Exception;
import com.kalvin.J12306.http.Session;

import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
            log.error("Thread.sleep error = {}", e.getMessage());
        }
    }

    public static void sleepM(int millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            log.error("Thread.sleep error = {}", e.getMessage());
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
                ticketInfoDTO.setOnSale("预订".equals(split[1]));
                ticketInfoDTO.setTrainNo(split[2]);
                ticketInfoDTO.setTrainNum(split[3]);
                ticketInfoDTO.setFormStationTelecode(split[6]);
                ticketInfoDTO.setToStationTelecode(split[7]);
                ticketInfoDTO.setGoOffTime(split[8]);
                ticketInfoDTO.setArrivalTime(split[9]);
                ticketInfoDTO.setLastTime(split[10]);
                ticketInfoDTO.setLeftTicket(split[12]);
                ticketInfoDTO.setTrainLocation(split[15]);
                ticketInfoDTO.setBusinessSeat(split[32]);   // or 5
                ticketInfoDTO.setL1Seat(split[31]);
                ticketInfoDTO.setL2Seat(split[30]);
                ticketInfoDTO.setL1SoftBerth(split[23]);
                ticketInfoDTO.setL2HardBerth(split[28]);
                ticketInfoDTO.setSoftSeat(split[24]);
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

    public static boolean hasTicket(String seatInfo) {
        return (NumberUtil.isNumber(seatInfo) && Integer.parseInt(seatInfo) > 0) || "有".equals(seatInfo);
    }

    public static LinkedHashMap<String, Boolean> getSeatsTicketInfo(String seats, TicketInfoDTO ticketInfo) {
        final LinkedHashMap<String, Boolean> linkedHashMap = new LinkedHashMap<>();
        for (String seat : seats.split(",")) {
            if (TicketSeatType.L2_SEAT.getCode().equals(seat)) {
                linkedHashMap.put(TicketSeatType.L2_SEAT.getCode(), J12306Util.hasTicket(ticketInfo.getL2Seat()));
            }
            if (TicketSeatType.L1_SEAT.getCode().equals(seat)) {
                linkedHashMap.put(TicketSeatType.L1_SEAT.getCode(), J12306Util.hasTicket(ticketInfo.getL1Seat()));
            }
            if (TicketSeatType.SOFT_SEAT.getCode().equals(seat)) {
                linkedHashMap.put(TicketSeatType.SOFT_SEAT.getCode(), J12306Util.hasTicket(ticketInfo.getSoftSeat()));
            }
            if (TicketSeatType.HARD_SEAT.getCode().equals(seat)) {
                linkedHashMap.put(TicketSeatType.HARD_SEAT.getCode(), J12306Util.hasTicket(ticketInfo.getHardSeat()));
            }
            if (TicketSeatType.SORT_SLEEPER.getCode().equals(seat)) {
                linkedHashMap.put(TicketSeatType.SORT_SLEEPER.getCode(), J12306Util.hasTicket(ticketInfo.getL1SoftBerth()));
            }
            if (TicketSeatType.HARD_SLEEPER.getCode().equals(seat)) {
                linkedHashMap.put(TicketSeatType.HARD_SLEEPER.getCode(), J12306Util.hasTicket(ticketInfo.getL2HardBerth()));
            }
            if (TicketSeatType.SP_SEAT.getCode().equals(seat)) {
                linkedHashMap.put(TicketSeatType.SP_SEAT.getCode(), J12306Util.hasTicket(ticketInfo.getBusinessSeat()));
            }
            if (Constants.NO_SEAT_CODE.equals(seat)) {
                linkedHashMap.put(TicketSeatType.NO_SEAT.getCode(), J12306Util.hasTicket(ticketInfo.getNoSeat()));
            }
        }
        return linkedHashMap;
    }

    public static boolean noNeedTicket(LinkedHashMap<String, Boolean> seatsTicketInfo) {
        for (String seatCode : seatsTicketInfo.keySet()) {
            if (seatsTicketInfo.get(seatCode)) {
                return false;
            }
        }
        return true;
    }

    public static String formatDateStr(String dateStr) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = simpleDateFormat.parse(dateStr);
            return simpleDateFormat.format(date);
        } catch (ParseException e) {
            throw new J12306Exception("日期格式不正确");
        }
    }

    public static void printlnLeftTicket(String trainDate, String fromStation, String toStation) {
        HttpResponse httpResponse = new Ticket(new Session(), J12306Util.formatDateStr(trainDate), StationUtil.getStationCode(fromStation), StationUtil.getStationCode(toStation))
                .queryZ();
        if (httpResponse.getStatus() == Constants.REQ_SUCCESS_STATUS) {
            List<TicketInfoDTO> ticketInfoDTOS = J12306Util.parseTicketInfo(httpResponse.body());
            ticketInfoDTOS.forEach(t -> log.info("出发日期【{}】车次【{}】出发时间【{}】到达时间【{}】", trainDate, t.getTrainNum(), t.getGoOffTime(), t.getArrivalTime()));
        } else {
            log.error("无法查询车票信息，状态【{}】", httpResponse.getStatus());
        }
    }

    public static String getCurrAftOneMinuteTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Calendar afterTime = Calendar.getInstance();
        afterTime.add(Calendar.MINUTE, 1);// 1分钟之后的时间
        Date beforeD = afterTime.getTime();
        return sdf.format(beforeD);
    }

    public static String getAfter5MinuteTime(String time) {
        DateTime dateTime = DateUtil.parse(DateUtil.formatDate(DateUtil.date()) + " " + time);
        Calendar calendar = DateUtil.calendar(dateTime);
        calendar.add(Calendar.MINUTE, 5);
        return DateUtil.format(calendar.getTime(), "HH:mm");
    }

    public static String getCurrTime() {
        return DateUtil.format(DateUtil.date(), "HH:mm");
    }

}
