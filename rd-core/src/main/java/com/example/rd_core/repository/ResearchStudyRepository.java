package com.example.rd_core.repository;

import android.util.Pair;

import com.example.rd_core.datasource.ResearchStudyDataSource;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class ResearchStudyRepository {

    private ResearchStudyDataSource researchDataSource;

    public ResearchStudyRepository(ResearchStudyDataSource researchDataSource) {
        this.researchDataSource = researchDataSource;
    }

<<<<<<< Updated upstream
    public ResearchStudy fetchAll() throws IOException, JSONException {
=======
    public List<? extends Pair<Group, ResearchStudy>> fetchAll() throws IOException, JSONException {
>>>>>>> Stashed changes

        return researchDataSource.fetchAll();

    }
}
