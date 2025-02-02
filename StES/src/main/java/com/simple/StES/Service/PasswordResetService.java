package com.simple.StES.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.simple.StES.repository.PasswordResetTokenRepository;
import com.simple.StES.repository.memRepository;
import com.simple.StES.vo.PasswordResetToken;
import com.simple.StES.vo.memVo;

@Service
public class PasswordResetService {
	
	@Autowired
    private memRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    public void createPasswordResetToken(String userEmail) {
        memVo user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("User not found1");
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUserEmail(userEmail);
        resetToken.setExpirationDate(LocalDateTime.now().plusHours(1));
        tokenRepository.save(resetToken);

        sendResetEmail(userEmail, token);
    }

    private void sendResetEmail(String email, String token) {
        String resetUrl = "http://localhost:8080/mem/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText(resetUrl);

        mailSender.send(message);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired token11");
        }

        memVo user = userRepository.findByEmail(resetToken.getUserEmail());
        user.setPw(newPassword);  // 비밀번호는 해싱하여 저장하는 것이 좋습니다.
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

}
