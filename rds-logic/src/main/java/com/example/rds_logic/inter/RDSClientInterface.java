package com.example.rds_logic.inter;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;

import java.util.List;

public interface RDSClientInterface {

    public boolean checkCriteria(Group group);

    public void notifyPendingStudies(List<ResearchStudy> listResearchStudy);
}
