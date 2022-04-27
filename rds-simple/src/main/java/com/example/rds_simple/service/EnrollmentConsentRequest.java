package com.example.rds_simple.service;

public class EnrollmentConsentRequest {
    final String consent;
    final String signature;
    final String citizen_certificate;
    final String citizen_pseudo;
    final String enrollment_criteria;
    final int sehrapp_id;

    public EnrollmentConsentRequest(String consent, String signature, String citizen_certificate, String citizen_pseudo, String enrollment_criteria, int sehrapp_id) {
        this.consent = consent;
        this.signature = signature;
        this.citizen_certificate = citizen_certificate;
        this.citizen_pseudo = citizen_pseudo;
        this.enrollment_criteria = enrollment_criteria;
        this.sehrapp_id = sehrapp_id;
    }
}
