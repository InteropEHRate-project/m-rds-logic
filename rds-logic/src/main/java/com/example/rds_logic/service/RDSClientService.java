package com.example.rds_logic.service;

import android.util.Pair;

import com.example.rd_core.interaction.GetAllStudies;
import com.example.rd_core.repository.ResearchStudyRepository;
import com.example.rddi_client.datasource.retrofit.RetrofitResearchStudyDataSource;
import com.example.rds_logic.inter.RDSClientInterface;
import com.example.rds_logic.utils.AppPreferenceService;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RDSClientService {

    private RDSClientInterface listener;
    private GetAllStudies getAllStudies = new GetAllStudies(new ResearchStudyRepository(new RetrofitResearchStudyDataSource()));

    private static final RDSClientService rdsClientService = new RDSClientService();

    public RDSClientService(){

    }

    public static RDSClientService getInstance(RDSClientInterface listener){

        rdsClientService.listener = listener;
        return rdsClientService;
    }

<<<<<<< Updated upstream
    public List<ResearchStudy> checkNewStudies() throws IOException {

        if(!getOptIn()) {
            return null;
        }
        ResearchStudy researchStudy = getAllStudies.getAllStudies();

        if(listener == null){
            return null;
        }

        boolean checkCriteria = listener.checkCriteria(null);

//        return checkCriteria?researchStudy : null;
        return  null;
    }
=======
//    public List<ResearchStudy> checkNewStudies() throws IOException, JSONException {
//
//        if(!getOptIn()) {
//            return null;
//        }
//
//        ArrayList<Pair<Group, ResearchStudy>> researchStudies;
//        researchStudies = (ArrayList<Pair<Group, ResearchStudy>>) getAllStudies.getAllStudies();
//
//        if(listener == null){
//            return null;
//        }
//
//        boolean checkCriteria = listener.checkCriteria(null);
//
//        return checkCriteria?researchStudies : null;
//    }
>>>>>>> Stashed changes

    public void setOptIn(boolean optIn){

        AppPreferenceService.INSTANCE.setOptinResearch(optIn);

    }

    public boolean getOptIn(){

        return AppPreferenceService.INSTANCE.isOptinResearch();
    }


}
