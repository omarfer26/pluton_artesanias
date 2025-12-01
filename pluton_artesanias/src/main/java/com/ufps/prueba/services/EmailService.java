package com.ufps.prueba.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;



@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${admin.email}")
    private String adminEmail;

    public void enviarAlerta(String asunto, String mensaje) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(adminEmail);
        email.setSubject(asunto);
        email.setText(mensaje);
        mailSender.send(email);
    }
}
