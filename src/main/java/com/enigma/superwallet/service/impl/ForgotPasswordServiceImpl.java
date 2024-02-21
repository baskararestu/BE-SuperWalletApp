package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.dto.request.ForgotPasswordRequest;
import com.enigma.superwallet.dto.response.ForgetPasswordResponse;
import com.enigma.superwallet.entity.Customer;
import com.enigma.superwallet.entity.UserCredential;
import com.enigma.superwallet.repository.CustomerRepository;
import com.enigma.superwallet.repository.UserCredentialRepository;
import com.enigma.superwallet.service.ForgotPasswordService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;


    @Override
    public ForgetPasswordResponse resetPassword(ForgotPasswordRequest forgotPasswordRequest) {
        String newPassword = randomPassword(12);
        System.out.println(forgotPasswordRequest.getEmail());
        Optional<UserCredential> optionalCustomer = userCredentialRepository.findByEmail(forgotPasswordRequest.getEmail());
        String email = optionalCustomer.map(UserCredential::getEmail).orElse(null);
        System.out.println(email);
        System.out.println(optionalCustomer + "ini find email");
        if(optionalCustomer.isPresent()) {
            UserCredential user = optionalCustomer.get(); // Ambil UserCredential dari Optional
            user.setPassword(passwordEncoder.encode(newPassword));
            userCredentialRepository.save(user); // Simpan perubahan pada password

            sendNewPassword(forgotPasswordRequest.getEmail(), newPassword);

            return ForgetPasswordResponse.builder()
                    .email(forgotPasswordRequest.getEmail())
                    .message("New Password sent to your email").build();
        } else {
            throw new NotFoundException("Email not found"); // Lebih baik menggunakan NotFoundException untuk kasus ini
        }
    }

    @Override
    public void sendNewPassword(String email, String newPassword) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setFrom(new InternetAddress("truecuks19@gmail.com"));
            mimeMessageHelper.setSubject("Password Reset Confirmation");
            mimeMessageHelper.setText("Your new password has been successfully reset. Your updated password is: " + newPassword, true);
            mimeMessageHelper.setTo(email);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error sending email with new password.", e);}
    }

    @Override
    public String randomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));}
        return password.toString();
    }
}
