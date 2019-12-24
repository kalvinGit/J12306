package com.kalvin.J12306;

import com.kalvin.J12306.utils.EmailUtil;
import com.kalvin.J12306.utils.J12306Util;

public class Main {

    public static void main(String[] args) {
        // 测试配置邮件是否能成功发送（收件人能成功接收到测试邮件证明配置正确）
//        EmailUtil.sendTest();
        // 可以在控制台打印指定日期、出发站点、达到站点的所有车次，主要为了方便填写车次信息
        // 你可以启动抢票前执行此方法（不是必要的），获取指定日期车次后，填写抢票信息，再启动抢票
//        J12306Util.printlnLeftTicket("2020-01-21", "广州", "怀集");
        // 开始抢票
        selectTicket1();
    }


    public static void selectTicket1() {
        Go12306.newInstance()
                .initUser("182xxxx", "123456")    // 用户名/密码
                .initBookTicketInfo("2020-01-21", // 乘车日期
                        "广州",   // 始发站
                        "怀集",   // 到达站
                        "D1818,D1822,D2948,D1870,G2904,D2972,D1872,D2936",    // 列车编号（D2834）。多个使用英文半角逗号分隔。目前暂时只能人工看列车编号啦
                        "P,M,O,N,4,3,2,1")    // 列车座席。P,M,O,N,4,3,2,1分别代表：商务特等座(P)、一等座(M)、二等座(O)、无座(N)、软卧(4)、硬卧(3)、软座(2)、硬座(1)。
                .run();
    }

    public static void selectTicket2() {
        Go12306.newInstance()
                .initUser("182xxxx", "123456")    // 用户名/密码
                .initBookTicketInfo("2020-01-22", // 乘车日期
                        "广州",   // 始发站
                        "怀集",   // 到达站
                        "D1882,D7551,D2962,D1818,D1754,D1822",    // 列车编号（D2834）。多个使用英文半角逗号分隔。目前暂时只能人工看列车编号啦
                        "P,M,O,N,4,3,2,1")    // 列车座席。P,M,O,N,4,3,2,1分别代表：商务特等座(P)、一等座(M)、二等座(O)、无座(N)、软卧(4)、硬卧(3)、软座(2)、硬座(1)。
                .run();
    }

}
