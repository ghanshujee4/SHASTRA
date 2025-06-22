package com.library.sdl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.SocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@RestController
public class SmtpTestController {

    @GetMapping("/smtp-test")
    public String testSmtpGreeting() {
        String smtpHost = "smtp.idfcbank.com";
        int smtpPort = 587;

        try (Socket socket = SocketFactory.getDefault().createSocket(smtpHost, smtpPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String response = in.readLine();  // Read server greeting (should start with 220)
            return "SMTP server responded: " + response;

        } catch (Exception e) {
            return "Failed to connect to SMTP server: " + e.getMessage();
        }
    }
}
