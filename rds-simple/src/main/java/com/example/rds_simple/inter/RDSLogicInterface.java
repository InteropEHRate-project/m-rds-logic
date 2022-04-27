package com.example.rds_simple.inter;

import android.content.Context;

import com.example.rds_simple.model.RDSResearchStudy;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface RDSLogicInterface {
    void setOptInStatus(boolean optIn);
    boolean getOptInStatus();
    boolean withdrawFromStudy(String studyId, String reason, String reason_text, Context context) throws Exception;
    List<RDSResearchStudy> checkNewStudies() throws IOException, JSONException;
    List<RDSResearchStudy> fetchStudies();
    void enrollInStudy(String host,String studyId, String referenceRC, String pseudotype, String enrollment_criteria, int sehrappid, Context context) throws IOException, JSONException, ExecutionException, InterruptedException;

}
