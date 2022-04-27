package com.example.rddi_client.datasource.retrofit;

import android.util.Pair;

import com.example.rd_core.datasource.ResearchStudyDataSource;
import com.example.rddi_client.datasource.retrofit.service.ResearchStudyService;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResourceType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ca.uhn.fhir.context.FhirContext;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitResearchStudyDataSource implements ResearchStudyDataSource {
    private FhirContext ctx = FhirContext.forR4();

    private OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private HttpUrl url = new HttpUrl.Builder()
            .scheme("http")
            .host("213.249.46.208")
            .port(443).build();

    private Retrofit retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private ResearchStudyService service = retrofit.create(ResearchStudyService.class);

    @Override
    public /*List<? extends Pair<Group, ResearchStudy>>*/ ResearchStudy fetchAll() throws IOException, JSONException {
=======
    public List<? extends Pair<Group, ResearchStudy>> fetchAll() throws IOException, JSONException {
    //public ResearchStudy fetchAll() throws IOException, JSONException {
>>>>>>> Stashed changes
        final ResponseBody responseBody = service.fetchResearchStudy();
        if (responseBody == null) {
            return null;
        }
        String json = responseBody.string();
        if (json.equals("")) {
            return null;
        }

        String rdd = retreiveRDD(json);

        Bundle bundle = ctx.newJsonParser().parseResource(Bundle.class, rdd);
        if (bundle == null) {
            return null;
        }

//        return getResearchStudyList(bundle);
        return null;

    }

    private String retreiveRDD(String result) throws JSONException {
        return String.valueOf(new JSONObject(result).get("RDD"));
    }

    private List<? extends Pair<Group, ResearchStudy>> getResearchStudyList(Bundle bundle){

        ArrayList<Pair<Group, ResearchStudy>> list = new ArrayList<>();
        Group group = null;
        ResearchStudy fhirStudy = null;
        //loop inside the Bundle
        //check if the entry of the Bundle contain a Group and a Research Study or an Array of Bundle
        for (Bundle.BundleEntryComponent bundleEntryComponent : bundle.getEntry()) {
            if (!bundleEntryComponent.hasResource())
                continue;
            if (ResourceType.ResearchStudy.equals(bundleEntryComponent.getResource().getResourceType())) {
                fhirStudy = (ResearchStudy) bundleEntryComponent.getResource();
                continue;
            }
            if (ResourceType.Group.equals(bundleEntryComponent.getResource().getResourceType())) {
                group =  (Group) bundleEntryComponent.getResource();
                continue;
            }
            if (ResourceType.Bundle.equals(bundleEntryComponent.getResource().getResourceType())) {
                Bundle b = (Bundle) bundleEntryComponent.getResource();
                if (b != null)
                    list.addAll(getResearchStudyList(b));
            }
        }
        if (group != null && fhirStudy != null) {
            return new ArrayList<Pair<Group, ResearchStudy>>(Arrays.asList(new Pair<Group, ResearchStudy>(group, fhirStudy)));
        }
        //we are in the end of the Bundle
        return list;
    }

}
