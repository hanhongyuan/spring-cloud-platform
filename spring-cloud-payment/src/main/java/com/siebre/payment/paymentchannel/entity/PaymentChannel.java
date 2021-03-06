package com.siebre.payment.paymentchannel.entity;

import com.siebre.basic.model.BaseObject;
import com.siebre.payment.entity.enums.PaymentChannelStatus;

import java.util.Date;

/**
 * 支付渠道
 * @author lizhiqiang
 *
 */
public class PaymentChannel extends BaseObject {

    private static final long serialVersionUID = -5745390794438045221L;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 渠道代码
     */
    private String channelCode;

    /**
     * 商户id
     */
    private String merchantCode;

    /**
     * 商户用户名
     */
    private String merchantName;

    /**
     * 商户用户密码
     */
    private String merchantPwd;

    /**
     * 终端号
     */
    private String  terminalId;

    /**
     * 图标地址
     */
    private String iconUrl;

    /**
     * 渠道状态
     */
    private PaymentChannelStatus status;

    /**
     * 是否支持退款
     */
    private Boolean supportRefunded;

    /**
     * 合同号
     */
    private String contractNumber;

    /**
     * 合同开始日期
     */
    private Date contractStartDate;

    /**
     * 合同结束日期
     */
    private Date contractEndDate;

    /**
     * 渠道生效日期
     */
    private Date startDate;

    /**
     * 渠道失效日期
     */
    private Date endDate;

    /**
     * 收款人银行账号
     */
    private String payeeAccount;

    /**
     * 备注
     */
    private String remark;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName == null ? null : channelName.trim();
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode == null ? null : channelCode.trim();
    }

    public Boolean getSupportRefunded() {
        return supportRefunded;
    }

    public void setSupportRefunded(Boolean supportRefunded) {
        this.supportRefunded = supportRefunded;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber == null ? null : contractNumber.trim();
    }

    public Date getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(Date contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public Date getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(Date contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(String payeeAccount) {
        this.payeeAccount = payeeAccount == null ? null : payeeAccount.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public PaymentChannelStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentChannelStatus status) {
        this.status = status;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantPwd() {
        return merchantPwd;
    }

    public void setMerchantPwd(String getMerchantPwd) {
        this.merchantPwd = getMerchantPwd;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
}