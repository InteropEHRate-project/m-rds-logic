package com.example.rds_simple.service;

public class ExitNotificationRequest {

    final String citizen_pseudo;
    final String reason;
    final String reason_text;
    final String citizen_signature;

    public ExitNotificationRequest(String citizen_pseudo, String reason, String reason_text, String citizen_signature) {
        this.citizen_pseudo = citizen_pseudo;
        this.reason = reason;
        this.reason_text = reason_text;
        this.citizen_signature = citizen_signature;
    }
}
