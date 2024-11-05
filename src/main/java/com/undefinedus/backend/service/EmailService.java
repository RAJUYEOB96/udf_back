package com.undefinedus.backend.service;

import com.undefinedus.backend.util.VerificationCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class EmailService {
    private final JavaMailSender mailSender;
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    
    @Value("${mail.username}")
    private String fromEmail;
    
    public void sendVerificationEmail(String toEmail) {
        String verificationCode = generateVerificationCode();
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("공책(Gong-chaek) 이메일 인증 코드");
            
            String mailContent = String.format(
                    "<div style='margin:20px;'>" +
                            "<h2>이메일 인증</h2>" +
                            "<p>아래의 인증 코드를 입력해주세요:</p>" +
                            "<div style='background-color:#f8f9fa;padding:15px;font-size:18px;'>" +
                            "<strong>%s</strong>" +
                            "</div>" +
                            "<p>이 인증 코드는 5분간 유효합니다.</p>" +
                            "</div>",
                    verificationCode
            );
            
            helper.setText(mailContent, true); // HTML 메일 사용
            
            mailSender.send(message);
            
            verificationCodes.put(toEmail,
                    new VerificationCode(verificationCode,
                            LocalDateTime.now().plusMinutes(3)));
            
        } catch (MessagingException e) {
            throw new RuntimeException("메일 발송 중 오류가 발생했습니다.", e);
        }
    }
    
    private String generateVerificationCode() {
        return String.format("%04d", new Random().nextInt(10000));
    }
    
    public boolean verifyCode(String email, String code) {
        VerificationCode savedCode = verificationCodes.get(email);
        
        if (savedCode == null || savedCode.isExpired()) {
            return false;
        }
        
        boolean isValid = code.equals(savedCode.getCode());
        if (isValid) {
            verificationCodes.remove(email);
        }
        return isValid;
    }
}
