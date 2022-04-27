package com.example.rds_simple.model;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.ResearchStudy;

public class RDSResearchStudy {

    private ResearchStudy researchStudy;
    private Group group;
    private Location location;

    public RDSResearchStudy() {

    }

    public ResearchStudy getResearchStudy() {
        return researchStudy;
    }

    public void setResearchStudy(ResearchStudy researchStudy) {
        this.researchStudy = researchStudy;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
