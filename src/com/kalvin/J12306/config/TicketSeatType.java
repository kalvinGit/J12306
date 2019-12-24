package com.kalvin.J12306.config;

/**
 * 车票座席枚举
 * Create by Kalvin on 2019/9/25.
 */
public enum  TicketSeatType {

    SP_SEAT("P", "特等座"),
    L1_SEAT("M", "一等座"),
    L2_SEAT("O", "二等座"),
    NO_SEAT("O", "无座"),
    BUS_SEAT("9", "商务座"),
    HARD_SEAT("1", "硬座"),
    SOFT_SEAT("2", "软座"),
    HARD_SLEEPER("3", "硬卧"),
    SORT_SLEEPER("4", "软卧");

    private String code;
    private String name;

    TicketSeatType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TicketSeatType get(String code) {
        for (TicketSeatType ticketSeatType : TicketSeatType.values()) {
            if (ticketSeatType.getCode().equals(code)) {
                return ticketSeatType;
            }
        }
        throw new RuntimeException("无效的code：" + code);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
