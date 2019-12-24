package com.kalvin.J12306.api;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.AI.Easy12306AI;
import com.kalvin.J12306.AI.ImageAI;
import com.kalvin.J12306.cache.TicketCache;
import com.kalvin.J12306.config.Constants;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.SubmitTicketInfoDTO;
import com.kalvin.J12306.exception.J12306Exception;
import com.kalvin.J12306.http.Session;
import com.kalvin.J12306.utils.J12306Util;

import java.util.HashMap;

/**
 * 真正下单
 * Create by Kalvin on 2019/9/20.
 */
public class ConfirmSingleForQueue {

    private final static Log log = LogFactory.get();

    private Session session;
    private String repeatSubmitToken;
    private String passengerTicketStr;
    private String oldPassengerStr;
    private String trainNum;
    private String trainLocation;
    private int ifShowPassCodeTime;
    private boolean isNeedCode;
    private SubmitTicketInfoDTO submitTicketInfoDTO;

    public ConfirmSingleForQueue(Session session, String repeatSubmitToken, String passengerTicketStr,
                                 String oldPassengerStr, String trainNum, String trainLocation, int ifShowPassCodeTime,
                                 boolean isNeedCode, SubmitTicketInfoDTO submitTicketInfoDTO) {
        this.session = session;
        this.repeatSubmitToken = repeatSubmitToken;
        this.passengerTicketStr = passengerTicketStr;
        this.oldPassengerStr = oldPassengerStr;
        this.trainNum = trainNum;
        this.trainLocation = trainLocation;
        this.ifShowPassCodeTime = ifShowPassCodeTime;
        this.isNeedCode = isNeedCode;
        this.submitTicketInfoDTO = submitTicketInfoDTO;
    }

    public void send() {
        TicketCache ticketCache = TicketCache.getInstance();
        String randCode = "";   // 订单验证码，默认空
        try {
            if (this.isNeedCode) {
                log.info("需要订单验证码，正在打印验证码...");
                Captcha captcha = new Captcha(this.session);
                // 获取订单验证码
                String orderCaptchaImg = captcha.getOrderCaptchaImg();
                for (int i = 0; i < 3; i++) {
                    // 若需要使用其它打码平台AI，在AI包下新增一个类实现ImageAI接口并更换下面图片AI实例即可
                    ImageAI imageAI = new Easy12306AI(Constants.IMAGE_AI_URL, Constants.CAPTCHA_IMG_PRE_PATH + orderCaptchaImg);
                    String code = imageAI.printCode();
                    // 转化为图片坐标点
                    randCode = J12306Util.getCaptchaPos(code);
                    // 检查验证码
                    boolean checkCode = new CheckRandCodeAsync(this.session, randCode, this.repeatSubmitToken).send();
                    if (checkCode) {
                        log.info("验证码通过，正在提交订单...");
                        break;
                    } else {
                        log.info("验证码不通过，{}次重试中...", i + 1);
                    }
                }
            } else {
                log.info("不需要订单验证码，直接提交");
            }

            log.info("开始正式下单...");
            J12306Util.sleep(ifShowPassCodeTime / 1000);

            HashMap<String, Object> formData = new HashMap<>();
            formData.put("passengerTicketStr", this.passengerTicketStr);
            formData.put("oldPassengerStr", this.oldPassengerStr);
            formData.put("randCode", randCode);
            formData.put("purpose_codes", this.submitTicketInfoDTO.getPurposeCodes());
            formData.put("key_check_isChange", this.submitTicketInfoDTO.getKeyCheckIsChange());
            formData.put("leftTicketStr", this.submitTicketInfoDTO.getLeftTicketStr());   // 这个参数不需要解码的。
            formData.put("train_location", this.trainLocation);
            formData.put("choose_seats", "");
            formData.put("seatDetailType", "000");   // 选择座位，不选默认000
            formData.put("whatsSelect", 1);    // 1-成人票 2-学生票
            formData.put("roomType", "00");  // 好像是根据一个id来判断选中的，两种 第一种是00，第二种是10，但是我在12306的页面没找到该id，目前写死是00
            formData.put("dwAll", "N");
            formData.put("_json_att", "");
            formData.put("REPEAT_SUBMIT_TOKEN", this.repeatSubmitToken);

            HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.CONFIRM_SINGLE_FOR_QUEUE, formData);
            String body = httpResponse.body();
//            log.info("confirmSingleForQueue body = {}", body);

            JSON parse = JSONUtil.parse(body);
            if (parse.getByPath("status") != null && (boolean) parse.getByPath("status")) {
                if (parse.getByPath("status") != null && (boolean) parse.getByPath("data.submitStatus")) {
                    new QueryOrderWaitTime(
                            this.session,
                            this.repeatSubmitToken,
                            this.trainNum
                    ).send();
                } else {
                    ticketCache.put(this.trainNum, this.trainNum, Constants.BLACK_ROOM_CACHE_EXP_TIME * 60);
                    log.error("正式下单失败，错误信息：{}。此列车{}加入小黑屋闭关3分钟", parse.getByPath("data.errMsg"), this.trainNum);
                }
            } else {
                ticketCache.put(this.trainNum, this.trainNum, Constants.BLACK_ROOM_CACHE_EXP_TIME * 60);
                log.error("正式下单失败，错误信息：{}。此列车{}加入小黑屋闭关3分钟", parse.getByPath("messages"), this.trainNum);
            }
        } catch (Exception e) {
            if (e instanceof J12306Exception) {
                throw new J12306Exception(e.getMessage());
            } else {
                log.error("正式下单异常：{}", e.getMessage());
            }
        }

    }
}
