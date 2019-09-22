package com.kalvin.J12306;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.api.Login;
import com.kalvin.J12306.api.SubmitOrderRequest;
import com.kalvin.J12306.api.Ticket;
import com.kalvin.J12306.cache.TicketCache;
import com.kalvin.J12306.config.ConfigConst;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.dto.TicketInfoDTO;
import com.kalvin.J12306.dto.UserInfoDTO;
import com.kalvin.J12306.exception.J12306Exception;
import com.kalvin.J12306.http.Session;
import com.kalvin.J12306.utils.J12306Util;
import com.kalvin.J12306.utils.StationUtil;

import java.util.List;

/**
 * 12306抢票程序
 * Create by Kalvin on 2019/9/18.
 */
public class Go12306 {

    private static final Log log = LogFactory.get();

    private Session session;    // 会话保持

    private String username;    // 12306用户账号
    private String password;    // 密码

    private TicketCache ticketCache = TicketCache.getInstance();

    /*乘客订票相关参数*/
    private String trainDate;  // 乘车日期（2019-10-01）
    private String fromStation; // 出发站（IZQ）
    private String toStation;   // 到达站（FAQ）
    private String trainNums;   // 列车编号（D2834）。多个使用英文半角逗号分隔。目前暂时只能人工看列车编号啦
    private String seats;   // 列车座席。M,O,N分别代表：一等座、二等座、无座。目前只支持这三种选择

    private int queryCount = 0; // 刷票次数

    public static Go12306 newInstance() {
        return new Go12306();
    }

    public Go12306 initUser(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * 初始化订票参数信息
     * @param trainDate 乘车日期
     * @param fromStation 始发站
     * @param toStation 终点站
     * @param trainNums 列表车次
     * @param seats 座席类型：M、O、N
     */
    public Go12306 initBookTicketInfo(String trainDate, String fromStation, String toStation, String trainNums, String seats) {
        this.trainDate = trainDate;
        final String fromStationCode = StationUtil.getStationCode(fromStation);
        if (fromStationCode == null) {
            throw new J12306Exception("无法找到始发站站点【" + fromStation + "】，请确保始发站点名正确。");
        }
        final String toStationCode = StationUtil.getStationCode(toStation);
        if (toStationCode == null) {
            throw new J12306Exception("无法找到到达站站点【" + fromStation + "】，请确保到达站点名正确。");
        }
        this.fromStation = fromStationCode;
        this.toStation = toStationCode;
        this.trainNums = trainNums;
        this.seats = seats;
        return this;
    }

    public void run() {
        // 构建会话
        this.session = new Session();
        // 开始登录
        Login login = new Login(this.session, this.username, this.password);
        UserInfoDTO userInfo = login.send();
        if (userInfo == null) {
            log.info("重次登录一次");
            login.send();
        }
        // 用户信息保存到缓存中
        this.ticketCache.put("userInfo", userInfo);

        // 开始查询余票
        Ticket ticket = new Ticket(this.session, this.trainDate, this.fromStation, this.toStation);

//        String[] split = this.trainDates.split(",");
//        int len = split.length;
//        ExecutorService executorService = Executors.newFixedThreadPool(len);
        stopLop: while (true) {
            HttpResponse httpResponse = ticket.query();
            String body = httpResponse.body();
//            log.info("query tickets status = {}，body={}", httpResponse.getStatus(), body);

            if (httpResponse.getStatus() == Constants.REQ_SUCCESS_STATUS) {
                List<TicketInfoDTO> ticketInfoDTOS = J12306Util.parseTicketInfo(body);
                for (TicketInfoDTO ticketInfoDTO : ticketInfoDTOS) {
                    String trainNum = ticketInfoDTO.getTrainNum();
                    String l1Seat = ticketInfoDTO.getL1Seat();
                    String l2Seat = ticketInfoDTO.getL2Seat();
                    String noSeat = ticketInfoDTO.getNoSeat();

                    // 判断当前车次是否在小黑屋中，若在，跳过此车次
                    if (ticketCache.get(trainNum) != null) {
                        break;
                    }

                    boolean hasL1Seat = (NumberUtil.isNumber(l1Seat) && Integer.parseInt(l1Seat) > 0) || "有".equals(l1Seat);
                    boolean hasL2Seat = (NumberUtil.isNumber(l2Seat) && Integer.parseInt(l2Seat) > 0) || "有".equals(l2Seat);
                    boolean hasNoSeat = (NumberUtil.isNumber(noSeat) && Integer.parseInt(noSeat) > 0) || "有".equals(noSeat);

                    if (hasL1Seat || hasL2Seat || hasNoSeat) {
                        log.info("可预订车票信息：发车日期：{}，车次：{}，出发时间：{}，到达时间：{}，座席：一等座{}、二等座{}、无座{}",
                                trainDate, trainNum, ticketInfoDTO.getGoOffTime(), ticketInfoDTO.getArrivalTime(), l1Seat, l2Seat, noSeat);
                    }

                    if (this.trainNums.contains(trainNum)) {
                        // 先进行一次解码。避免提交后再编码一次导致参数失效
                        String secretStr = J12306Util.urlDecode(ticketInfoDTO.getSecretStr());
                        String leftTicket = J12306Util.urlDecode(ticketInfoDTO.getLeftTicket());
                        String trainNo = ticketInfoDTO.getTrainNo();
                        String fromStationCode = ticketInfoDTO.getFormStationTelecode();
                        String toStationCode = ticketInfoDTO.getToStationTelecode();
                        String trainLocation = ticketInfoDTO.getTrainLocation();

                        if (!hasL1Seat && !hasL2Seat && !hasNoSeat) {
                            break;
                        }
                        try {
                            /* 优先考虑二等座,其次一等座,最后选择无座 */
                            if (this.seats.contains(Constants.L2_SEAT_CODE)) {
                                // 提交订单
                                if (hasL2Seat) {
                                    new SubmitOrderRequest(
                                            this.session,
                                            secretStr,
                                            Constants.L2_SEAT_CODE,
                                            trainDate,
                                            fromStationCode,
                                            toStationCode,
                                            trainNo,
                                            trainNum,
                                            trainLocation
                                    ).send();
                                    log.info("提交订单：车次：{}，二等座，发车日期：{}，座席类型：{}", trainNum, trainDate, Constants.L2_SEAT_CODE);
                                }
                            }
                            if (this.seats.contains(Constants.L1_SEAT_CODE)) {
                                if (hasL1Seat) {
                                    new SubmitOrderRequest(
                                            this.session,
                                            secretStr,
                                            Constants.L1_SEAT_CODE,
                                            trainDate,
                                            fromStationCode,
                                            toStationCode,
                                            trainNo,
                                            trainNum,
                                            trainLocation
                                    ).send();
                                    log.info("提交订单：车次：{}，二等座，发车日期：{}，座席类型：{}", trainNum, trainDate, Constants.L1_SEAT_CODE);
                                }
                            }
                            if (this.seats.contains(Constants.NO_SEAT_CODE)) {
                                if (hasNoSeat) {
                                    new SubmitOrderRequest(
                                            this.session,
                                            secretStr,
                                            Constants.NO_SEAT_CODE,
                                            trainDate,
                                            fromStationCode,
                                            toStationCode,
                                            trainNo,
                                            trainNum,
                                            trainLocation
                                    ).send();
                                    log.info("提交订单：车次：{}，二等座，发车日期：{}，座席类型：{}", trainNum, trainDate, Constants.NO_SEAT_CODE);
                                }
                            }
                        } catch (J12306Exception e) {
                            log.info("抢票程序结束：{}", e.getMsg());
                            break stopLop;
                        }

                    }
                }
                this.queryCount++;
                log.info("-------线程【{}】已为账号{}刷票{}次，启程日期：{}--------", Thread.currentThread().getName(), this.username, this.queryCount, trainDate);
            } else {
                log.info("-------线程【{}】无法获取车票信息，状态码：{}", Thread.currentThread().getName(), httpResponse.getStatus());
            }
            // 睡眠2秒
            J12306Util.sleep(ConfigConst.QUERY_TICKET_SPEED_SECOND);
        }

    }


}
