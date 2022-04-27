package com.example.rd_core.datasource;

import android.util.Pair;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public interface ResearchStudyDataSource {

    public List<? extends Pair<Group, ResearchStudy>> fetchAll() throws IOException, JSONException;
}
