package com.example.rds_logic_application;

import com.example.rds_simple.inter.RDSClientInterface;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;

import java.util.ArrayList;
import java.util.List;


public class RDSClientInterfaceImplementation implements RDSClientInterface {

    @Override
    public boolean checkCriteria(Group group) {
        return true;
    }

    @Override
    public Bundle queryData(Group group) {
        return null;
    }

    @Override
    public void notifyPendingStudies() {

    }

    @Override
    public void notifyNewStudies(ArrayList studyList) {

    }

    @Override
    public String notifyEnrollment(boolean enrollementSucced, String studyId, String host) {
        return host;
    }



    @Override
    public void notifyExit() {

    }

    @Override
    public void notifyDataRetrieval(String studyId) {

    }

    @Override
    public void notifyWithdrawal() {

    }
}
