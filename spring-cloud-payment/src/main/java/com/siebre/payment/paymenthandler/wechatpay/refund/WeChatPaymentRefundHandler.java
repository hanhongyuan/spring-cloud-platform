package com.siebre.payment.paymenthandler.wechatpay.refund;

import com.siebre.payment.entity.enums.EncryptionMode;
import com.siebre.payment.entity.enums.PaymentTransactionStatus;
import com.siebre.payment.entity.enums.RefundApplicationStatus;
import com.siebre.payment.entity.enums.ReturnCode;
import com.siebre.payment.paymentchannel.entity.PaymentChannel;
import com.siebre.payment.paymentchannel.mapper.PaymentChannelMapper;
import com.siebre.payment.paymenthandler.basic.paymentrefund.AbstractPaymentRefundComponent;
import com.siebre.payment.paymenthandler.wechatpay.sdk.WeChatParamConvert;
import com.siebre.payment.paymentinterface.entity.PaymentInterface;
import com.siebre.payment.paymenttransaction.entity.PaymentTransaction;
import com.siebre.payment.paymentway.entity.PaymentWay;
import com.siebre.payment.refundapplication.dto.PaymentRefundRequest;
import com.siebre.payment.refundapplication.dto.PaymentRefundResponse;
import com.siebre.payment.refundapplication.entity.RefundApplication;
import com.siebre.payment.utils.messageconvert.ConvertToXML;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.math.BigDecimal;
import java.security.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Huang Tianci
 * 微信退款接口
 */
@Service("weChatPaymentRefundHandler")
public class WeChatPaymentRefundHandler extends AbstractPaymentRefundComponent {

    @Autowired
    private PaymentChannelMapper paymentChannelMapper;

    @Override
    protected void handleInternal(PaymentRefundRequest paymentRefundRequest, PaymentRefundResponse refundResponse) {

        PaymentWay paymentWay = paymentRefundRequest.getPaymentWay();
        PaymentInterface paymentInterface = paymentRefundRequest.getPaymentInterface();

        PaymentChannel channel = paymentChannelMapper.selectByChannelCode(paymentRefundRequest.getPaymentOrder().getChannelCode());
        //1.读取证书
        CloseableHttpClient httpclient = this.getHttpClient(channel, paymentWay);
        //2.拼装返回参数
        Map<String,String> params = this.generateRefundParams(paymentRefundRequest ,channel);
        //3.签名
        this.processSign(params, paymentWay.getEncryptionMode(), paymentWay.getSecretKey());

        //4.调用api
        Map<String, String> res = refund(httpclient,paymentInterface.getRequestUrl(),params);

        PaymentTransaction refundTransaction = paymentRefundRequest.getRefundTransaction();
        RefundApplication refundApplication = paymentRefundRequest.getRefundApplication();
        //处理返回结果
        refundResponse.setReturnMessage(res.get("return_msg"));

        if("SUCCESS".equals(res.get("return_code"))){
            if("SUCCESS".equals(res.get("result_code"))){
                logger.error("退款成功");

                refundTransaction.setExternalTransactionNumber(res.get("refund_id"));
                refundTransaction.setPaymentStatus(PaymentTransactionStatus.REFUND_SUCCESS);//退款交易调用成功

                refundApplication.setStatus(RefundApplicationStatus.SUCCESS);
                refundApplication.setResponse(RefundApplicationStatus.SUCCESS.getDescription());

                refundResponse.setExternalTransactionNumber(res.get("refund_id"));
                refundResponse.setReturnCode(ReturnCode.SUCCESS.getDescription());
                refundResponse.setReturnMessage("退款成功");
                refundResponse.setRefundApplicationStatus(RefundApplicationStatus.SUCCESS);
            }else{
                String failReason = "退款失败，原因:" + res.get("err_code_des");
                logger.error(failReason);

                refundTransaction.setPaymentStatus(PaymentTransactionStatus.REFUND_FAILED);

                refundApplication.setStatus(RefundApplicationStatus.FAILED);
                refundApplication.setResponse(failReason);

                refundResponse.setReturnCode(ReturnCode.FAIL.getDescription());
                refundResponse.setReturnMessage(failReason);
                refundResponse.setRefundApplicationStatus(RefundApplicationStatus.FAILED);
            }
        }else{
            String failReason = "退款失败，原因:" + res.get("return_msg");
            logger.error(failReason);

            refundTransaction.setPaymentStatus(PaymentTransactionStatus.REFUND_FAILED);

            refundApplication.setStatus(RefundApplicationStatus.FAILED);
            refundApplication.setResponse(failReason);

            refundResponse.setReturnCode(ReturnCode.FAIL.getDescription());
            refundResponse.setReturnMessage(failReason);
            refundResponse.setRefundApplicationStatus(RefundApplicationStatus.FAILED);
        }
        refundResponse.setRefundApplication(refundApplication);
        refundResponse.setRefundTransaction(refundTransaction);
    }

    private CloseableHttpClient getHttpClient( PaymentChannel channel, PaymentWay paymentWay){
        String merchId = channel.getMerchantCode();
        KeyStore keyStore = null;
        InputStream instream = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            byte[] bytes = paymentWay.getApiClientCertPkcs();
            //instream = new FileInputStream(new ClassPathResource("apiclient_cert.p12").getFile());
            instream = new ByteArrayInputStream(bytes);
            keyStore.load(instream, merchId.toCharArray());
        }catch (Exception e) {
            logger.error("获取证书失败，请检查原因={}", e);
        } finally {
            try {
                instream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, merchId.toCharArray())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        return httpclient;
    }

    private Map<String,String> generateRefundParams(PaymentRefundRequest paymentRefundRequest, PaymentChannel channel){
        PaymentWay paymentWay = paymentRefundRequest.getPaymentWay();
        RefundApplication refundApplication = paymentRefundRequest.getRefundApplication();
        Map<String,String> params = new HashMap<>();
        params.put("appid", paymentWay.getAppId());
        params.put("mch_id", channel.getMerchantCode());
        params.put("nonce_str",String.valueOf(UUID.randomUUID()).substring(0, 31));
        params.put("sign_type", paymentWay.getEncryptionMode().getDescription());
        if(StringUtils.isNotBlank(paymentRefundRequest.getOriginExternalNumber())){
            params.put("transaction_id",paymentRefundRequest.getOriginExternalNumber());
        }else{
            params.put("out_trade_no",paymentRefundRequest.getOriginInternalNumber());
        }
        params.put("out_refund_no",refundApplication.getRefundApplicationNumber());
        //微信要求金额单位为分，只能为整数
        params.put("total_fee", paymentRefundRequest.getPaymentOrder().getAmount().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toString());
        params.put("refund_fee", refundApplication.getRefundAmount().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toString());
        params.put("op_user_id", channel.getMerchantCode());   //操作员帐号, 默认为商户号
        return params;
    }

    private void processSign(Map<String, String> params, EncryptionMode encryptionMode, String secretKey) {
        if (EncryptionMode.MD5.equals(encryptionMode)) {
            String sign = WeChatParamConvert.signMd5(params, secretKey);
            logger.info("Wechat sign key generated, original paramerers={},encryptionMode={}, secretKey={},sign={}", params.toString(), encryptionMode.getDescription(), secretKey, sign);
            params.put("sign", sign);
            return;
        }
    }

    private Map<String, String> refund(CloseableHttpClient httpclient, String url, Map<String, String> params) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            String xmlParams = ConvertToXML.toXml(params);
            //HttpGet httpget = new HttpGet("https://api.mch.weixin.qq.com/secapi/pay/refund");
            HttpPost httpPost = new HttpPost(url);
            StringEntity sendParams = new StringEntity(xmlParams);
            sendParams.setContentEncoding("UTF-8");
            //sendParams.setContentType("text/xml");
            sendParams.setContentType("application/xml");
            httpPost.setEntity(sendParams);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    System.out.println("Response content length: " + entity.getContentLength());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
                    String temp = "";
                    String text;
                    while ((text = bufferedReader.readLine()) != null) {
                        temp += text;
                    }
                    resultMap = ConvertToXML.toMap(temp);
                }
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }


}
