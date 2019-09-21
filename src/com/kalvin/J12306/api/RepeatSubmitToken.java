package com.kalvin.J12306.api;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpResponse;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.SubmitTicketInfoDTO;
import com.kalvin.J12306.http.Session;

import java.util.List;

/**
 * RepeatSubmitToken
 * Create by Kalvin on 2019/9/20.
 */
public class RepeatSubmitToken {

    private Session session;

    public RepeatSubmitToken(Session session) {
        this.session = session;
    }

    /**
     * 获取提交订单相关参数
     * @return SubmitTicketInfoDTO
     */
    public SubmitTicketInfoDTO getSubmitTicketInfo() {
        HttpResponse httpResponse = this.session.httpClient.send(UrlsEnum.INIT_DC);
        String body = httpResponse.body();
        List<String> list0 = ReUtil.findAll("globalRepeatSubmitToken = '(.*?)'", body, 1);
        List<String> list1 = ReUtil.findAll("'key_check_isChange':'(.*?)'", body, 1);
        List<String> list2 = ReUtil.findAll("'purpose_codes':'(.*?)'", body, 1);
        List<String> list3 = ReUtil.findAll("'leftTicketStr':'(.*?)'", body, 1);
        SubmitTicketInfoDTO submitTicketInfoDTO = new SubmitTicketInfoDTO();
        submitTicketInfoDTO.setRepeatSubmitToken(list0.get(0));
        submitTicketInfoDTO.setKeyCheckIsChange(list1.get(0));
        submitTicketInfoDTO.setPurposeCodes(list2.get(0));
        submitTicketInfoDTO.setLeftTicketStr(list3.get(0));
        return submitTicketInfoDTO;
    }
}
