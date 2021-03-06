package com.siebre.payment.paymentgateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;

/**
 * @author Huang Tianci
 * 微信js api支付参数
 */
public class WechatJsApiParams implements Serializable {

    private String appId;

    private String nonceStr;

    private String paySign;

    @JsonProperty("package")
    private String packageSrt;

    private String signType;

    private String timeStamp;

    public WechatJsApiParams() {
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getPaySign() {
        return paySign;
    }

    public void setPaySign(String paySign) {
        this.paySign = paySign;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPackageSrt() {
        return packageSrt;
    }

    public void setPackageSrt(String packageSrt) {
        this.packageSrt = packageSrt;
    }
}
