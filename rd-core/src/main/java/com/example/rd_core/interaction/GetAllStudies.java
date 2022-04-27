package com.example.rd_core.interaction;

import android.util.Pair;

import com.example.rd_core.repository.ResearchStudyRepository;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class GetAllStudies {

    private ResearchStudyRepository researchStudyRepository;


    public GetAllStudies(ResearchStudyRepository researchStudyRepository) {
        this.researchStudyRepository = researchStudyRepository;
    }

    public List<? extends Pair<Group, ResearchStudy>> getAllStudies() throws IOException, JSONException {
        return researchStudyRepository.fetchAll();
    }
}
