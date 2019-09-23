package com.kalvin.J12306.utils;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;

/**
 * 邮件工具类
 * Create by Kalvin on 2019/9/23.
 */
public class EmailUtil {

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
        MailUtil.send(account, (String) YmlUtil.get("j12306.notice.email.receiver"), subject, content, false);
    }
}
