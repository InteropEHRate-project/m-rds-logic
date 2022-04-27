package com.example.rds_simple.inter;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;

import java.util.ArrayList;
import java.util.List;

public interface RDSClientInterface {
    boolean checkCriteria(Group group);
    Bundle queryData(Group group); // params to be defined
    // Add new interfaces
    void notifyPendingStudies(); //param should be pendingStudyList: metadata of the ongoing research studies
    void notifyNewStudies(ArrayList studyList); //param should be studyList: list of research studies available to the Citizen
    String notifyEnrollment(boolean enrollementSucced, String studyId, String host); //param should be study: metadata to be displayed about the study
    void notifyExit(); //params should be study: metadata to be displayed about the study and reason (optional): human-readable reason for the exit
    void notifyDataRetrieval(String studyId); //params should be study and data (optional): human-readable list of data elements (attributes) being sent
    void notifyWithdrawal(); //param should be study: metadata to be displayed about the study




}
