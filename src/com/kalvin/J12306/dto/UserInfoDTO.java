package com.kalvin.J12306.dto;

/**
 * 保存用户基础信息
 * Create by Kalvin on 2019/9/19.
 */
public class UserInfoDTO {

    private String name;    // 用户姓名
    private String username;    // 用户账号
    private String idTypeCode;  // 证件类型代码
    private String idTypeName;  // 证件类型名称
    private String idNo;    // 证件号码
    private String email;   // 用户绑定的邮箱账号
    private String userEncStr;  // 用户密钥串

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdTypeCode() {
        return idTypeCode;
    }

    public void setIdTypeCode(String idTypeCode) {
        this.idTypeCode = idTypeCode;
    }

    public String getIdTypeName() {
        return idTypeName;
    }

    public void setIdTypeName(String idTypeName) {
        this.idTypeName = idTypeName;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserEncStr() {
        return userEncStr;
    }

    public void setUserEncStr(String userEncStr) {
        this.userEncStr = userEncStr;
    }

    @Override
    public String toString() {
        return "UserInfoDTO{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", idTypeCode='" + idTypeCode + '\'' +
                ", idTypeName='" + idTypeName + '\'' +
                ", idNo='" + idNo + '\'' +
                ", email='" + email + '\'' +
                ", userEncStr='" + userEncStr + '\'' +
                '}';
    }
}
