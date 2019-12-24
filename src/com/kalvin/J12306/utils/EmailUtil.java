package com.kalvin.J12306.utils;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

/**
 * 邮件工具类
 * Create by Kalvin on 2019/9/23.
 */
public class EmailUtil {

    private final static Log log = LogFactory.get();

    private static MailAccount account;

    private static void initAccount() {
        account = new MailAccount();
        account.setHost((String) YmlUtil.get("j12306.notice.email.sender.host"));
        account.setPort((Integer) YmlUtil.get("j12306.notice.email.sender.port"));
        account.setAuth(true);
        account.setFrom((String) YmlUtil.get("j12306.notice.email.sender.from"));
        account.setUser((String) YmlUtil.get("j12306.notice.email.sender.user"));
        account.setPass((String) YmlUtil.get("j12306.notice.email.sender.pass"));
    }

    public static void send(String subject, String content) {
        if (account == null) {
            initAccount();
        }
        try {
            MailUtil.send(account, (String) YmlUtil.get("j12306.notice.email.receiver"), subject, content, false);
        } catch (Exception e) {
            log.error("无法通过邮件通知您，请检查config.yml配置文件，确保邮件相关配置正确！error：" + e.getMessage());
        }
    }

    public static void sendTest() {
        EmailUtil.send("J12306邮件配置测试", "测试邮件是否能发送成功！收到邮件证明邮件信息配置正确！");
    }
}
