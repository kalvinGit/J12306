package com.kalvin.J12306.config;

/**
 * url枚举类
 * Create by Kalvin on 2019/9/18.
 */
public enum UrlsEnum {

    LOG_DEVICE(new UrlConfig(   // 设备信息
            "https://kyfw.12306.cn/otn/HttpZF/logdevice",   // url地址
            "get",  // 请求方法
            "https://kyfw.12306.cn/otn/passport?redirect=/otn/" // referer
    )),
    LOGIN_INIT(new UrlConfig(   // 登录页
            "https://kyfw.12306.cn/otn/login/init",
            "get",
            "https://kyfw.12306.cn/otn/index/init"
    )),
    CAPTCHA(new UrlConfig(  // 登录验证码
            "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&{0}",
            "get",
            "https://kyfw.12306.cn/otn/resources/login.html"
    )),
    CAPTCHA_BASE64(new UrlConfig(  // 登录验证码2
            "https://kyfw.12306.cn/passport/captcha/captcha-image64?login_site=E&module=login&rand=sjrand&{0}&callback=jQuery19108016482864806321_1554298927290&_=1554298927293",
            "get",
            "https://kyfw.12306.cn/otn/resources/login.html"
    )),
    CHECK_CAPTCHA(new UrlConfig(    // 校验登录验证码
            "https://kyfw.12306.cn/passport/captcha/captcha-check",
            "post",
            "https://kyfw.12306.cn/otn/login/init"
    )),
    LOGIN(new UrlConfig(    // 登录接口
            "https://kyfw.12306.cn/passport/web/login",
            "post",
            "https://kyfw.12306.cn/otn/resources/login.html"
    )),
    USER_LOGIN(new UrlConfig(   // 用户登录
            "https://kyfw.12306.cn/otn/login/userLogin",
            "get",
            "https://kyfw.12306.cn/otn/login/init"
    )),
    PASSPORT(new UrlConfig(   // PASSPORT
            "https://kyfw.12306.cn/otn/passport?redirect=/otn/login/userLogin",
            "get",
            "https://kyfw.12306.cn/otn/login/init"
    )),
    UAM_TK(new UrlConfig(   // 获取token
            "https://kyfw.12306.cn/passport/web/auth/uamtk",
            "post",
            "https://kyfw.12306.cn/otn/resources/login.html"
    )),
    UAM_AUTH_CLIENT(new UrlConfig(
            "https://kyfw.12306.cn/otn/uamauthclient",
            "post",
            "https://kyfw.12306.cn/otn/passport?redirect=/otn/login/userLogin"
    )),
    GET_USER_INFO(new UrlConfig(    // 获取用户信息
            "https://kyfw.12306.cn/otn/modifyUser/initQueryUserInfoApi",
            "post",
            ""
    )),
    INIT_TICKET(new UrlConfig(  // 查票页
            "https://kyfw.12306.cn/otn/leftTicket/init",
            "get",
            "https://www.12306.cn/index/index.html"
    )),
    QUERY_TICKET(new UrlConfig( // 查票接口
            "https://kyfw.12306.cn/otn/leftTicket/queryA?leftTicketDTO.train_date={0}&leftTicketDTO.from_station={1}&leftTicketDTO.to_station={2}&purpose_codes=ADULT",
            "post",
            "https://kyfw.12306.cn/otn/leftTicket/init"
    )),
    CHECK_USER(new UrlConfig(   // 检查用户状态
            "https://kyfw.12306.cn/otn/login/checkUser",
            "post",
            "https://kyfw.12306.cn/otn/leftTicket/init"
    )),
    SUBMIT_ORDER_REQUEST(new UrlConfig( // 提交订单
            "https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest",
            "post",
            "https://kyfw.12306.cn/otn/leftTicket/init"
    )),
    INIT_DC(new UrlConfig(  // initDc
            "https://kyfw.12306.cn/otn/confirmPassenger/initDc",
            "get",
            "https://kyfw.12306.cn/otn/leftTicket/init"
    )),
    GET_JS(new UrlConfig(   // GetJS
            "https://kyfw.12306.cn/otn/HttpZF/GetJS",
            "get",
            "https://kyfw.12306.cn/otn/confirmPassenger/initDc"
    )),
    GET_PASSENGERDTOS(new UrlConfig(    // 获取乘客信息列表
            "https://kyfw.12306.cn/otn/confirmPassenger/getPassengerDTOs",
            "post",
            "https://kyfw.12306.cn/otn/confirmPassenger/initDc"
    )),
    GET_PASSCODE_NEW(new UrlConfig( // 订单页面验证码
            "https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew?module=passenger&rand=randp&{0}",
            "get",
            "https://kyfw.12306.cn/otn/confirmPassenger/initDc"
    )),
    CHECK_ORDER_INFO(new UrlConfig( // 检查订单
            "https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo",
            "post",
            "https://kyfw.12306.cn/otn/confirmPassenger/initDc"
    )),
    GET_QUEUE_COUNT(new UrlConfig(  // 队列计数（准备进入下单步骤）
            "https://kyfw.12306.cn/otn/confirmPassenger/getQueueCount",
            "post",
            "https://kyfw.12306.cn/otn/confirmPassenger/initDc"
    )),
    CHECK_RAND_CODE_ASYNC(new UrlConfig(  // 验证订单验证码
            "https://kyfw.12306.cn/otn/passcodeNew/checkRandCodeAnsyn",
            "post",
            "https://kyfw.12306.cn/otn/confirmPassenger/initDc"
    )),
    CONFIRM_SINGLE_FOR_QUEUE(new UrlConfig( // 正式下单
            "https://kyfw.12306.cn/otn/confirmPassenger/confirmSingleForQueue",
            "post",
            "https://kyfw.12306.cn/otn/confirmPassenger/initDc"
    )),
    QUERY_ORDER_WAIT_TIME(new UrlConfig(    // 下单确认中（调用两次）
            "https://kyfw.12306.cn/otn/confirmPassenger/queryOrderWaitTime?random={0}&tourFlag=dc&_json_att=&REPEAT_SUBMIT_TOKEN={1}",
            "post",
            "https://kyfw.12306.cn/otn/confirmPassenger/initDc"
    )),
    RESULT_ORDER_FOR_DC_QUEUE(new UrlConfig(    // 结果回执
            "https://kyfw.12306.cn/otn/confirmPassenger/resultOrderForDcQueue",
            "post",
            ""
    ));

    private UrlConfig urlConfig;

    UrlsEnum(UrlConfig urlConfig) {
        this.urlConfig = urlConfig;
    }

    public UrlConfig getUrlConfig() {
        return urlConfig;
    }

    public void setUrlConfig(UrlConfig urlConfig) {
        this.urlConfig = urlConfig;
    }
}
