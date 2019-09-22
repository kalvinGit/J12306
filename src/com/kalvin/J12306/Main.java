package com.kalvin.J12306;

public class Main {

    public static void main(String[] args) {
        // 开始抢票，可开多个
        selectTicket1();
//        selectTicket2();
    }


    public static void selectTicket1() {
        Go12306.newInstance()
                .initUser("182xxxxx", "123456")    // 用户名/密码
                .initBookTicketInfo("2019-09-26", // 乘车日期
                        "广州",   // 始发站
                        "怀集",   // 到达站
                        "D2980",    // 列车编号（D2834）。多个使用英文半角逗号分隔。目前暂时只能人工看列车编号啦
                        "M,O,N")    // 列车座席。M,O,N分别代表：一等座、二等座、无座。目前只支持这三种选择
                .run();
    }

    public static void selectTicket2() {
        Go12306.newInstance()
                .initUser("182xxxxxx", "123456")    // 用户名/密码
                .initBookTicketInfo("2019-09-26", // 乘车日期
                        "广州",   // 始发站
                        "怀集",   // 到达站
                        "D2834",    // 列车编号（D2834）。多个使用英文半角逗号分隔。目前暂时只能人工看列车编号啦
                        "M,O,N")    // 列车座席。M,O,N分别代表：一等座、二等座、无座。目前只支持这三种选择
                .run();
    }


}
