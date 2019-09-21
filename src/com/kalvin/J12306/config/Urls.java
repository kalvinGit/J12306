package com.kalvin.J12306.config;

/**
 * 接口url
 * Create by Kalvin on 2019/9/18.
 */
@Deprecated
public class Urls {

    // 设备信息
    private static String LOG_DEVICE = "https://kyfw.12306.cn/otn/HttpZF/logdevice?algID=1oMrl2Kf4t&hashCode=NrIjgGA7fXtB_4v4eTDmOPBVxUD08R6x4M7uSypZ14w&FMQw=0&q4f3=zh-CN&VySQ=FGFm2gYQYLbALw8nbWLg6D8wQj4uOWaI&VPIf=1&custID=133&VEek=unknown&dzuS=0&yD16=0&EOQP=8f58b1186770646318a429cb33977d8c&jp76=52d67b2a5aa5e031084733d5006cc664&hAqN=Win32&platform=WEB&ks0Q=d22ca0b81584fbea62237b14bd04c866&TeRS=1040x1920&tOHY=24xx1080x1920&Fvje=i1l1o1s1&q5aJ=-8&wNLf=99115dfb07133750ba677d055874de87&0aew=Mozilla/5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/76.0.3809.132%20Safari/537.36&E3gR=05d47ed076f9affcd4517ac2b4fa4223&timestamp={timestamp}";
    // 登录页
    private static String LOGIN_INIT = "https://kyfw.12306.cn/otn/login/init";
    // 生成验证码
    private static String CAPTCHA = "https://kyfw.12306.cn/passport/captcha/captcha-image";
    private static String CAPTCHA_IMAGE64 = "https://kyfw.12306.cn/passport/captcha/captcha-image64";
    // 校验验证码
    private static String CHECK_CAPTCHA = "https://kyfw.12306.cn/passport/captcha/captcha-check";
    // 登录接口
    private static String LOGIN = "https://kyfw.12306.cn/passport/web/login";
    // 获取token
    private static String UAM_TK = "https://kyfw.12306.cn/passport/web/auth/uamtk";
    private static String UAM_AUTH_CLIENT = "https://kyfw.12306.cn/otn/uamauthclient";
    // 查票页
    private static String INIT_TICKET = "https://kyfw.12306.cn/otn/leftTicket/init";
    // 查票接口
    private static String QUERY_TICKET = "https://kyfw.12306.cn/otn/leftTicket/queryA";
    // 检查用户状态
    private static String CHECK_USER = "https://kyfw.12306.cn/otn/login/checkUser";
    // 提交订单
    private static String SUBMIT_ORDER_REQUEST = "https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest";
    // initDc
    private static String INIT_DC = "https://kyfw.12306.cn/otn/confirmPassenger/initDc";
    // GetJS
    private static String GET_JS = "https://kyfw.12306.cn/otn/HttpZF/GetJS";
    // 获取乘客信息列表
    private static String GET_PASSENGERDTOS = "https://kyfw.12306.cn/otn/confirmPassenger/getPassengerDTOs";

    private static String GET_PASSCODE_NEW = "https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew";
    // 检查订单
    private static String CHECK_ORDER_INFO = "https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo";
    // 队列计数（准备进入下单步骤）
    private static String GET_QUEUE_COUNT = "https://kyfw.12306.cn/otn/confirmPassenger/getQueueCount";
    // 正式下单
    private static String CONFIRM_SINGLE_FOR_QUEUE = "https://kyfw.12306.cn/otn/confirmPassenger/confirmSingleForQueue";
    // 下单确认中（调用两次）
    private static String QUERY_ORDER_WAIT_TIME = "https://kyfw.12306.cn/otn/confirmPassenger/queryOrderWaitTime";
    // 结果回执
    private static String RESULT_ORDER_FOR_DC_QUEUE = "https://kyfw.12306.cn/otn/confirmPassenger/resultOrderForDcQueue";

}
