package com.example.rds_simple.service;

import org.hl7.fhir.r4.model.Bundle;

public class HealthDataRequest {
    final String health_data;

    public HealthDataRequest(String health_data) {
        this.health_data = health_data;
    }
}
