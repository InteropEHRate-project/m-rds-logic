package com.example.rds_simple.controller;

import android.util.Log;
import android.util.Pair;

import com.example.rds_simple.model.RDSResearchStudy;
import com.example.rds_simple.retrofit.ResearchStudyService;
import com.example.rds_simple.utils.DBadapter;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ca.uhn.fhir.context.FhirContext;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResearchStudyController {
    private FhirContext ctx = FhirContext.forR4();

    private String host;

    private OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private HttpUrl url;
    private Retrofit retrofit;
    private ResearchStudyService service;

    DBadapter helper;

    public ResearchStudyController(HttpUrl url) {
        this.url = url;
        this.retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(this.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ResearchStudyService.class);
    }


    public List<RDSResearchStudy> fetchAll() throws IOException, JSONException {
        DBadapter helper = null;
        //make the request and get the response
        final Response<ResponseBody> responseBody = service.fetchResearchStudy().execute();
        Log.d("LOGIC_OF_RDS","RESPONSE BODY: "+responseBody.toString());

        if (responseBody == null || !responseBody.isSuccessful()) {
            return null;
        }

        //get the body of the request
        String json = responseBody.body().string();
        JSONObject jObject = new JSONObject(json);
        //Log.d("LOGIC_OF_RDS","ID of research study :"+jObject.getString("RDD"));
//        String researchStudyID = json.substring(json.indexOf("\"resourceType\":\"ResearchStudy\",") + 1, json.indexOf("\"meta\""));
//        Log.d("LOGIC_OF_RDS","ID of research study :"+researchStudyID);
        JSONObject rdd_json = jObject.getJSONObject("RDD");
        JSONArray entry1 = rdd_json.getJSONArray("entry");
        JSONObject entry1_result = entry1.getJSONObject(0);
        JSONObject resource1 = entry1_result.getJSONObject("resource");
        JSONArray entry2 = resource1.getJSONArray("entry");
        JSONObject entry2_result = entry2.getJSONObject(0);
        JSONObject resource2 = entry2_result.getJSONObject("resource");
        String researchStudyID = resource2.getString("id");
        Log.d("LOGIC_OF_RDS","ID of research study :"+researchStudyID);

        Log.d("LOGIC_OF_RDS","RESPONSE FROM THE RESEARCH NODE :"+json);
        if (json.equals("")) {
            return null;
        }

        String rdd = retreiveRDD(json);

        Bundle bundle = ctx.newJsonParser().parseResource(Bundle.class, rdd);
        if (bundle == null) {
            return null;
        }

        return getResearchStudyList(bundle);

    }

    private String retreiveRDD(String result) throws JSONException {
        return String.valueOf(new JSONObject(result).get("RDD"));
    }

    private List<RDSResearchStudy> getResearchStudyList(Bundle bundle){

        ArrayList<RDSResearchStudy> list = new ArrayList<>();
        RDSResearchStudy rdsResearchStudy = new RDSResearchStudy();

        //loop inside the Bundle
        //check if the entry of the Bundle contain a Group and a Research Study or an Array of Bundle
        for (Bundle.BundleEntryComponent bundleEntryComponent : bundle.getEntry()) {
            if (!bundleEntryComponent.hasResource())
                continue;
            if (ResourceType.ResearchStudy.equals(bundleEntryComponent.getResource().getResourceType())) {
                rdsResearchStudy.setResearchStudy(bundleEntryComponent.getResource() instanceof ResearchStudy ? (ResearchStudy) bundleEntryComponent.getResource() : null);
                continue;
            }
            if (ResourceType.Group.equals(bundleEntryComponent.getResource().getResourceType())) {
                rdsResearchStudy.setGroup(bundleEntryComponent.getResource() instanceof Group ? (Group) bundleEntryComponent.getResource() : null);
                continue;
            }
            if (ResourceType.Location.equals(bundleEntryComponent.getResource().getResourceType())) {
                rdsResearchStudy.setLocation(bundleEntryComponent.getResource() instanceof Location ? (Location) bundleEntryComponent.getResource() : null);
                continue;
            }
            if (ResourceType.Bundle.equals(bundleEntryComponent.getResource().getResourceType())) {
                Bundle b = (Bundle) bundleEntryComponent.getResource();
                if (b != null)
                    list.addAll(getResearchStudyList(b));
            }
        }

        if (rdsResearchStudy.getGroup() != null && rdsResearchStudy.getResearchStudy() != null) {
            return Collections.singletonList(rdsResearchStudy);
        }
        //we are in the end of the Bundle
        return list;
    }

}
