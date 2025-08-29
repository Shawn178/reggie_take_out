package com.itheima.reggie.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author mrs
 * @create 2022-06-06 0:26
 *
 * 邮件发送工具类，替换掉短信发送工具类SMSUtils
 */
public class EmailUtils {
    public static void sendAuthCodeEmail(String email, String authCode) {
        try {
            // 创建邮件服务器配置
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.qq.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            
            // 创建会话
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("1783250716@qq.com", "tcjaltvagvrzchai");
                }
            });
            
            // 创建邮件消息
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("1783250716@qq.com", "瑞吉外卖"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("验证码");
            message.setText("尊敬的用户: 你好!\n  你的验证码为:" + authCode + "\n" + "(有效期为一分钟)");
            
            // 发送邮件
            Transport.send(message);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
