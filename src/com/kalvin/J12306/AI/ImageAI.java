package com.kalvin.J12306.AI;

/**
 * 图片验证码识别AI接口
 * Create by Kalvin on 2019/9/19.
 */
public interface ImageAI {

    /**
     * 打码
     * @return 图片识别码（如12306图片识别码：1,3,5。分别代码横排数起图片位置）
     */
    String printCode();

}
