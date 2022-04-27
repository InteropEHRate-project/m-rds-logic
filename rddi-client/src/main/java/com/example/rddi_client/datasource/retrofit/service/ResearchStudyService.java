package com.example.rddi_client.datasource.retrofit.service;

import okhttp3.ResponseBody;
import retrofit2.http.GET;

public interface ResearchStudyService {

    @GET("open-studies")
    public ResponseBody fetchResearchStudy();

}
