package com.twentyfive.twentyfivedb.emailDB;


import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


@Slf4j
@Service
public class EmailSenderService {
    private JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    //@Value("${spring.mail.username}")
    private String email = "spot97fy@gmail.com";
   // @Value("${spring.mail.password}")
    private String password = "rfrtyzxvzdbtonwq";
   // @Value("${spring.mail.host}")
    private String host = "smtp.gmail.com";
    //@Value("${spring.mail.port}")
    private Integer port = 587;

    //@Value("${custom.frontend.url}")
    private String frontendUrl;

    public void sendEmail(String to, String subject, String token) throws MessagingException {
        String resetLink = frontendUrl + "/resetPassword/" + token;
        String text = "Accedi al link: <a> " + resetLink + " </a> per resettare la password";

        MimeMessage msg = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, false);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText("<html> <head><div class='row' style='background-color:#390099; width:100%;'> </head></div><h1 style = 'font-weight: bold;'>RECUPERO PASSWORD TWENTYFIVE:</h1><div>"+text+"</div></html>", true);


        log.info("Preparing email..." + to);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(email);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);

        log.info("Sending email..." + simpleMailMessage);

        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
        javaMailSender.setUsername(email);
        javaMailSender.setPassword(password);
        javaMailSender.getJavaMailProperties().setProperty("mail.smtp.starttls.enable", "true");

        javaMailSender.send(helper.getMimeMessage());

        log.info("Email sent successfully");
    }


}
