package com.example.rds_simple.service;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.rds_simple.inter.RDSLogicInterface;
import com.example.rds_simple.model.RDSResearchStudy;
import com.example.rds_simple.retrofit.ResearchStudyService;
import com.example.rds_simple.utils.AppPreferenceService;
import com.example.rds_simple.controller.ResearchStudyController;
import com.example.rds_simple.inter.RDSClientInterface;
import com.example.rds_simple.utils.DBadapter;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;

import ca.uhn.fhir.context.FhirContext;
import eu.interopehrate.m_rds_sm.CryptoManagementFactory;
import eu.interopehrate.m_rds_sm.api.CryptoManagement;
import eu.interopehrate.rdsanoni.PrivacyMechanisms;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import the security library


public class RDSClientService implements RDSLogicInterface {

    private RDSClientInterface listener;
    private ResearchStudyController researchStudyController;
    private HttpUrl url;

    private static final RDSClientService rdsClientService = new RDSClientService();

    public static RDSClientService getInstance(HttpUrl url, RDSClientInterface listener){
        rdsClientService.listener = listener;
        rdsClientService.url = url;
        rdsClientService.researchStudyController = new ResearchStudyController(url);
        return rdsClientService;
    }

    public List<RDSResearchStudy> checkNewStudies() throws IOException, JSONException {

        if(!getOptInStatus()) {
            return null;
        }

        if(listener == null){
            return null;
        }

        List<RDSResearchStudy> studyList = researchStudyController.fetchAll();

        List<RDSResearchStudy> availableStudyList = new ArrayList<>();
        for (RDSResearchStudy rdsResearchStudy : studyList) {
            if (listener.checkCriteria(rdsResearchStudy.getGroup()))
                availableStudyList.add(rdsResearchStudy);
        }

        return availableStudyList;
    }

    public void setOptInStatus(boolean optIn){

        AppPreferenceService.INSTANCE.setOptinResearch(optIn);

    }

    public boolean getOptInStatus(){

        return AppPreferenceService.INSTANCE.isOptinResearch();
    }


    public boolean withdrawFromStudy(String studyId, String reason, String reason_text, Context context) throws Exception {
        //check if the citizen has opted in for a study, if not throw an exception
        if(!getOptInStatus()) {
            throw new Exception("NotPartOfResearchNetworkException: the Citizen has not opted in to the Research Network.");
        }
        //Todo get the parameters of sendExitNotification from where it should
        //get the citizen's pseudo that is stored inside the mobile
        PrivacyMechanisms pm = PrivacyMechanisms.PrivacyMechanisms();
        String citizen_pseudo = pm.getPseudo(studyId, context);
        //if there is no pseudo, that means that the citizen is not enrolled. As a result, throw the exception.
        if(citizen_pseudo.equals(""))
        {
            throw new Exception("NotEnrolledException: the Citizen has not enrolled into the study.");
        }
        String citizen_signature = "lala";
        String exitNotificationStatus = sendExitNotification(studyId, citizen_pseudo, reason, reason_text, citizen_signature);
        //if statement to check the status of the response from the rrc. If it is successful return true.
        if(exitNotificationStatus.equals("200"))
        {
            return true;
        }

        return false;
    }

    public List<RDSResearchStudy> fetchStudies(){
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void enrollInStudy(String host,String studyId, String referenceRC, String pseudoType, String enrollment_criteria, int sehrappid, Context context) throws IOException, JSONException, ExecutionException, InterruptedException {
        //call diffieHellman() to generate mobile app symmetric key
        //diffieHellman();

        String pseudo = "";
         //if statement to check whether it's a pseudonym based or a pseudo-identity based study
        if (pseudoType.equals("pseudonym"))
        {
            //the pseudonym should be retrieved. For the time being is "pseudonym"
            //Todo Make a request to the pseudonym provider
            //pseudo = "pseudonym";
            PrivacyMechanisms pm = PrivacyMechanisms.PrivacyMechanisms();
            pseudo = pm.retrievePseudonym("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdHRyaWJ1dGVzIjoiZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKU1V6STFOaUo5LmV5SmtZWFJoTXlJNklseHlYRzVjY2x4dVhISmNibHh5WEc0OElVUlBRMVJaVUVVZ2FIUnRiRDVjY2x4dVhISmNibHh5WEc1Y2NseHVQR2gwYld3Z2JHRnVaejFjSW1WdVhDSS1YSEpjYmx4eVhHNDhhR1ZoWkQ1Y2NseHVJQ0FnSUZ4eVhHNWNjbHh1UEcxbGRHRWdZMmhoY25ObGREMWNJblYwWmkwNFhDSS1YSEpjYmp4dFpYUmhJR2gwZEhBdFpYRjFhWFk5WENKWUxWVkJMVU52YlhCaGRHbGliR1ZjSWlCamIyNTBaVzUwUFZ3aVNVVTlaV1JuWlZ3aVBseHlYRzQ4YldWMFlTQnVZVzFsUFZ3aWRtbGxkM0J2Y25SY0lpQmpiMjUwWlc1MFBWd2lkMmxrZEdnOVpHVjJhV05sTFhkcFpIUm9MQ0JwYm1sMGFXRnNMWE5qWVd4bFBURmNJajVjY2x4dVBHMWxkR0VnYm1GdFpUMWNJbVJsYzJOeWFYQjBhVzl1WENJZ1kyOXVkR1Z1ZEQxY0lrTkZSaUJsU1VRZ0xTQmxTVVJCVTF3aVBseHlYRzQ4YldWMFlTQnVZVzFsUFZ3aVlYVjBhRzl5WENJZ1kyOXVkR1Z1ZEQxY0lrTkZSaUJsU1VRZ0xTQmxTVVJCVTF3aVBseHlYRzQ4YkdsdWF5QnlaV3c5WENKemRIbHNaWE5vWldWMFhDSWdhSEpsWmoxY0luSmxjMjkxY21ObGN5OXphMmx1TUM5amMzTXZZbTl2ZEhOMGNtRndMbTFwYmk1amMzTmNJajVjY2x4dVBHeHBibXNnY21Wc1BWd2ljM1I1YkdWemFHVmxkRndpSUdoeVpXWTlYQ0p5WlhOdmRYSmpaWE12YzJ0cGJqQXZZM056TDJOMWMzUnZiUzVqYzNOY0lqNWNjbHh1UEd4cGJtc2djbVZzUFZ3aWMzUjViR1Z6YUdWbGRGd2lJR2h5WldZOVhDSnlaWE52ZFhKalpYTXZjMnRwYmpBdlkzTnpMMkYzWlhOdmJXVXRZbTl2ZEhOMGNtRndMV05vWldOclltOTRMbU56YzF3aUlDOC1YSEpjYmx4eVhHNDhiR2x1YXlCeVpXdzlYQ0poY0hCc1pTMTBiM1ZqYUMxcFkyOXVYQ0lnYzJsNlpYTTlYQ0kxTjNnMU4xd2lJR2h5WldZOVhDSnlaWE52ZFhKalpYTXZjMnRwYmpBdmFXMW5MMkZ3Y0d4bExYUnZkV05vTFdsamIyNHROVGQ0TlRjdWNHNW5YQ0ktWEhKY2JqeHNhVzVySUhKbGJEMWNJbUZ3Y0d4bExYUnZkV05vTFdsamIyNWNJaUJ6YVhwbGN6MWNJall3ZURZd1hDSWdhSEpsWmoxY0luSmxjMjkxY21ObGN5OXphMmx1TUM5cGJXY3ZZWEJ3YkdVdGRHOTFZMmd0YVdOdmJpMDJNSGcyTUM1d2JtZGNJajVjY2x4dVBHeHBibXNnY21Wc1BWd2lZWEJ3YkdVdGRHOTFZMmd0YVdOdmJsd2lJSE5wZW1WelBWd2lOeko0TnpKY0lpQm9jbVZtUFZ3aWNtVnpiM1Z5WTJWekwzTnJhVzR3TDJsdFp5OWhjSEJzWlMxMGIzVmphQzFwWTI5dUxUY3llRGN5TG5CdVoxd2lQbHh5WEc0OGJHbHVheUJ5Wld3OVhDSmhjSEJzWlMxMGIzVmphQzFwWTI5dVhDSWdjMmw2WlhNOVhDSTNObmczTmx3aUlHaHlaV1k5WENKeVpYTnZkWEpqWlhNdmMydHBiakF2YVcxbkwyRndjR3hsTFhSdmRXTm9MV2xqYjI0dE56WjROell1Y0c1blhDSS1YSEpjYmp4c2FXNXJJSEpsYkQxY0ltRndjR3hsTFhSdmRXTm9MV2xqYjI1Y0lpQnphWHBsY3oxY0lqRXhOSGd4TVRSY0lpQm9jbVZtUFZ3aWNtVnpiM1Z5WTJWekwzTnJhVzR3TDJsdFp5OWhjSEJzWlMxMGIzVmphQzFwWTI5dUxURXhOSGd4TVRRdWNHNW5YQ0ktWEhKY2JqeHNhVzVySUhKbGJEMWNJbUZ3Y0d4bExYUnZkV05vTFdsamIyNWNJaUJ6YVhwbGN6MWNJakV5TUhneE1qQmNJaUJvY21WbVBWd2ljbVZ6YjNWeVkyVnpMM05yYVc0d0wybHRaeTloY0hCc1pTMTBiM1ZqYUMxcFkyOXVMVEV5TUhneE1qQXVjRzVuWENJLVhISmNianhzYVc1cklISmxiRDFjSW1Gd2NHeGxMWFJ2ZFdOb0xXbGpiMjVjSWlCemFYcGxjejFjSWpFME5IZ3hORFJjSWlCb2NtVm1QVndpY21WemIzVnlZMlZ6TDNOcmFXNHdMMmx0Wnk5aGNIQnNaUzEwYjNWamFDMXBZMjl1TFRFME5IZ3hORFF1Y0c1blhDSS1YSEpjYmp4c2FXNXJJSEpsYkQxY0ltRndjR3hsTFhSdmRXTm9MV2xqYjI1Y0lpQnphWHBsY3oxY0lqRTFNbmd4TlRKY0lpQm9jbVZtUFZ3aWNtVnpiM1Z5WTJWekwzTnJhVzR3TDJsdFp5OWhjSEJzWlMxMGIzVmphQzFwWTI5dUxURTFNbmd4TlRJdWNHNW5YQ0ktWEhKY2JqeHNhVzVySUhKbGJEMWNJbUZ3Y0d4bExYUnZkV05vTFdsamIyNWNJaUJ6YVhwbGN6MWNJakU0TUhneE9EQmNJaUJvY21WbVBWd2ljbVZ6YjNWeVkyVnpMM05yYVc0d0wybHRaeTloY0hCc1pTMTBiM1ZqYUMxcFkyOXVMVEU0TUhneE9EQXVjRzVuWENJLVhISmNianhzYVc1cklISmxiRDFjSW1samIyNWNJaUIwZVhCbFBWd2lhVzFoWjJVdmNHNW5YQ0lnYUhKbFpqMWNJbkpsYzI5MWNtTmxjeTl6YTJsdU1DOXBiV2N2Wm1GMmFXTnZiaTB6TW5nek1pNXdibWRjSWlCemFYcGxjejFjSWpNeWVETXlYQ0ktWEhKY2JqeHNhVzVySUhKbGJEMWNJbWxqYjI1Y0lpQjBlWEJsUFZ3aWFXMWhaMlV2Y0c1blhDSWdhSEpsWmoxY0luSmxjMjkxY21ObGN5OXphMmx1TUM5cGJXY3ZZVzVrY205cFpDMWphSEp2YldVdE1Ua3llREU1TWk1d2JtZGNJaUJ6YVhwbGN6MWNJakU1TW5neE9USmNJajVjY2x4dVBHeHBibXNnY21Wc1BWd2lhV052Ymx3aUlIUjVjR1U5WENKcGJXRm5aUzl3Ym1kY0lpQm9jbVZtUFZ3aWNtVnpiM1Z5WTJWekwzTnJhVzR3TDJsdFp5OW1ZWFpwWTI5dUxUazJlRGsyTG5CdVoxd2lJSE5wZW1WelBWd2lPVFo0T1RaY0lqNWNjbHh1UEd4cGJtc2djbVZzUFZ3aWFXTnZibHdpSUhSNWNHVTlYQ0pwYldGblpTOXdibWRjSWlCb2NtVm1QVndpY21WemIzVnlZMlZ6TDNOcmFXNHdMMmx0Wnk5bVlYWnBZMjl1TFRFMmVERTJMbkJ1WjF3aUlITnBlbVZ6UFZ3aU1UWjRNVFpjSWo1Y2NseHVQR3hwYm1zZ2NtVnNQVndpYldGdWFXWmxjM1JjSWlCb2NtVm1QWEpsYzI5MWNtTmxjeTl6YTJsdU1DOXBiV2N2YldGdWFXWmxjM1F1YW5OdmJsd2lQbHh5WEc1Y2NseHVQRzFsZEdFZ2FIUjBjQzFsY1hWcGRqMG5jSEpoWjIxaEp5QmpiMjUwWlc1MFBTZHVieTFqWVdOb1pTY3ZQbHh5WEc0OGJXVjBZU0JvZEhSd0xXVnhkV2wyUFNkallXTm9aUzFqYjI1MGNtOXNKeUJqYjI1MFpXNTBQU2R1YnkxallXTm9aU3dnYm04dGMzUnZjbVVzSUcxMWMzUXRjbVYyWVd4cFpHRjBaU2N2UGx4eVhHNDhiV1YwWVNCb2RIUndMV1Z4ZFdsMlBWd2lSWGh3YVhKbGMxd2lJR052Ym5SbGJuUTlYQ0l0TVZ3aUx6NWNjbHh1UEcxbGRHRWdhSFIwY0MxbGNYVnBkajFjSWtOdmJuUmxiblF0Vkhsd1pWd2lJR052Ym5SbGJuUTlYQ0owWlhoMEwyaDBiV3c3SUdOb1lYSnpaWFE5VlZSR0xUaGNJaTgtWEhKY2JseHlYRzQ4YldWMFlTQnVZVzFsUFZ3aWJYTmhjSEJzYVdOaGRHbHZiaTFVYVd4bFEyOXNiM0pjSWlCamIyNTBaVzUwUFZ3aUkyWm1ZelF3WkZ3aVBseHlYRzQ4YldWMFlTQnVZVzFsUFZ3aWJYTmhjSEJzYVdOaGRHbHZiaTFVYVd4bFNXMWhaMlZjSWlCamIyNTBaVzUwUFZ3aWNtVnpiM1Z5WTJWekwzTnJhVzR3TDJsdFp5OXRjM1JwYkdVdE1UUTBlREUwTkM1d2JtZGNJajVjY2x4dVBHMWxkR0VnYm1GdFpUMWNJblJvWlcxbExXTnZiRzl5WENJZ1kyOXVkR1Z1ZEQxY0lpTm1abVptWm1aY0lqNWNjbHh1UENFdExTQklWRTFNTlNCemFHbHRJR0Z1WkNCU1pYTndiMjVrTG1weklHWnZjaUJKUlRnZ2MzVndjRzl5ZENCdlppQklWRTFNTlNCbGJHVnRaVzUwY3lCaGJtUWdiV1ZrYVdFZ2NYVmxjbWxsY3lBdExUNWNjbHh1UENFdExWdHBaaUJzZENCSlJTQTVYVDVjY2x4dVBITmpjbWx3ZENCemNtTTlYQ0pvZEhSd2N6b3ZMMjl6Y3k1dFlYaGpaRzR1WTI5dEwyaDBiV3cxYzJocGRpOHpMamN1TWk5b2RHMXNOWE5vYVhZdWJXbHVMbXB6WENJLVBDOXpZM0pwY0hRLVhISmNianh6WTNKcGNIUWdjM0pqUFZ3aWFIUjBjSE02THk5dmMzTXViV0Y0WTJSdUxtTnZiUzl5WlhOd2IyNWtMekV1TkM0eUwzSmxjM0J2Ym1RdWJXbHVMbXB6WENJLVBDOXpZM0pwY0hRLVhISmNiandoVzJWdVpHbG1YUzB0UGx4eVhHNDhjMk55YVhCMElIUjVjR1U5WENKMFpYaDBMMnBoZG1GelkzSnBjSFJjSWlCemNtTTlYQ0p5WlhOdmRYSmpaWE12YzJ0cGJqQXZhbk12YW5GMVpYSjVMVEV1TVRFdU15NXRhVzR1YW5OY0lqNDhMM05qY21sd2RENWNjbHh1UEhOamNtbHdkQ0IwZVhCbFBWd2lkR1Y0ZEM5cVlYWmhjMk55YVhCMFhDSWdjM0pqUFZ3aWNtVnpiM1Z5WTJWekwzTnJhVzR3TDJwekwySnZiM1J6ZEhKaGNDNXRhVzR1YW5OY0lqNDhMM05qY21sd2RENWNjbHh1UEhOamNtbHdkQ0IwZVhCbFBWd2lkR1Y0ZEM5cVlYWmhjMk55YVhCMFhDSWdjM0pqUFZ3aWNtVnpiM1Z5WTJWekwzTnJhVzR3TDJwekwyWjFibU4wYVc5dUxtcHpYQ0ktUEM5elkzSnBjSFEtWEhKY2JseHlYRzRnSUNBZ1BIUnBkR3hsUG1WSlJFRlRJRUYxZEdobGJuUnBZMkYwYVc5dUlGTmxjblpwWTJVZ0tFbGtVQ2s4TDNScGRHeGxQbHh5WEc0OEwyaGxZV1EtWEhKY2JqeGliMlI1UGx4eVhHNDhJUzB0VTFSQlVsUWdTRVZCUkVWU0xTMC1YSEpjYmp4b1pXRmtaWElnWTJ4aGMzTTlYQ0pvWldGa1pYSmNJajVjY2x4dVhIUThaR2wySUdOc1lYTnpQVndpWTI5dWRHRnBibVZ5WENJLVhISmNibHgwWEhROGFERS1aVWxFUVZNZ1FYVjBhR1Z1ZEdsallYUnBiMjRnVTJWeWRtbGpaU0FvU1dSUUtUd3ZhREUtWEhKY2JseDBQQzlrYVhZLVhISmNiand2YUdWaFpHVnlQbHh5WEc0OElTMHRSVTVFSUVoRlFVUkZVaTB0UGx4eVhHNDhiV0ZwYmo1Y2NseHVQR1JwZGlCamJHRnpjejFjSW1OdmJuUmhhVzVsY2x3aVBseHlYRzRnSUNBZ1BHUnBkaUJqYkdGemN6MWNJbkp2ZDF3aVBseHlYRzRnSUNBZ0lDQWdJRHhrYVhZZ1kyeGhjM005WENKMFlXSXRZMjl1ZEdWdWRGd2lQbHh5WEc0Z0lDQWdJQ0FnSUNBZ0lDQThaR2wySUhKdmJHVTlYQ0owWVdKd1lXNWxiRndpSUdOc1lYTnpQVndpZEdGaUxYQmhibVVnWm1Ga1pTQnBiaUJoWTNScGRtVmNJaUJwWkQxY0luUmhZaTB3TWx3aVBseHlYRzRnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdQR1JwZGlCamJHRnpjejFjSW1OdmJDMXRaQzB4TWx3aVBseHlYRzRnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUR4b01qNVNaWE53YjI1elpWeHlYRzRnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUR3dmFESS1YSEpjYmlBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0E4TDJScGRqNWNjbHh1SUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJRnh5WEc0OFpHbDJJR05zWVhOelBWd2lZMjlzTFcxa0xUWmNJajVjY2x4dUlDQWdJRHhrYVhZZ1kyeGhjM005WENKcGJHeDFjM1J5WVhScGIyNWNJajVjY2x4dUlDQWdJQ0FnUEdneFBqeHpjR0Z1SUdOc1lYTnpQVndpWkdWdGJ5MXVZVzFsWENJLVNVUlFQQzl6Y0dGdVBqd3ZhREUtWEhKY2JpQWdJQ0E4TDJScGRqNWNjbHh1UEM5a2FYWS1YSEpjYmlBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0E4WkdsMklHTnNZWE56UFZ3aVkyOXNMVzFrTFRaY0lqNWNjbHh1SUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBOFptOXliU0JwWkQxY0ltZGxibVZ5YVdOR2IzSnRYQ0lnYm1GdFpUMWNJbWRsYm1WeWFXTkdiM0p0WENJLVhISmNibHh5WEc0Z0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0E4YVc1d2RYUWdkSGx3WlQxY0ltaHBaR1JsYmx3aUlHbGtQVndpWlhKeWIzSk5aWE56WVdkbFhDSWdibUZ0WlQxY0ltVnljbTl5VFdWemMyRm5aVndpSUhaaGJIVmxQVndpYm5Wc2JGd2lMejVjY2x4dUlDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnUEdsdWNIVjBJSFI1Y0dVOVhDSm9hV1JrWlc1Y0lpQnBaRDFjSW1WeWNtOXlUV1Z6YzJGblpWUnBkR3hsWENJZ2JtRnRaVDFjSW1WeWNtOXlUV1Z6YzJGblpWUnBkR3hsWENKY2NseHVJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUhaaGJIVmxQVndpYm5Wc2JGd2lMejVjY2x4dUlDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQThMMlp2Y20wLVhISmNibHh5WEc0Z0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lEeG1iM0p0SUdsa1BWd2ljbVZrYVhKbFkzUkdiM0p0WENJZ2JtRnRaVDFjSW5KbFpHbHlaV04wUm05eWJWd2lJR0ZqZEdsdmJqMWNJbWgwZEhBNkx5OXNiMk5oYkdodmMzUTZPREE0TUM5VGNHVmphV1pwWTFCeWIzaDVVMlZ5ZG1salpTOUpaSEJTWlhOd2IyNXpaVndpSUcxbGRHaHZaRDFjSW5CdmMzUmNJajVjY2x4dUlDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnUEdScGRpQmpiR0Z6Y3oxY0ltWnZjbTB0WjNKdmRYQmNJajVjY2x4dUlDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lEeHNZV0psYkNCbWIzSTlYQ0pxVTI5dVVtVnpjRzl1YzJWRVpXTnZaR1ZrWENJLVUyMXpjM0JVYjJ0bGJpQlNaWE53YjI1elpUd3ZiR0ZpWld3LVhISmNiaUFnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0E4ZEdWNGRHRnlaV0VnYm1GdFpUMWNJbXBUYjI1U1pYTndiMjV6WlVSbFkyOWtaV1JjSWlCcFpEMWNJbXBUYjI1U1pYTndiMjV6WlVSbFkyOWtaV1JjSWlCamJHRnpjejFjSW1admNtMHRZMjl1ZEhKdmJGd2lYSEpjYmlBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ2NtOTNjejFjSWpFd1hDSS1lMXh1SUNBZ1hDSnlaWE53YjI1elpWd2lJRG9nZTF4dUlDQWdJQ0FnWENKaGRIUnlhV0oxZEdWZmJHbHpkRndpSURvZ1d5QjdYRzRnSUNBZ0lDQWdJQ0JjSW5SNWNHVmNJaUE2SUZ3aWMzUnlhVzVuWDJ4cGMzUmNJaXhjYmlBZ0lDQWdJQ0FnSUZ3aWJtRnRaVndpSURvZ1hDSk1aV2RoYkU1aGJXVmNJaXhjYmlBZ0lDQWdJQ0FnSUZ3aWRtRnNkV1Z6WENJZ09pQmJJSHRjYmlBZ0lDQWdJQ0FnSUNBZ0lGd2lkbUZzZFdWY0lpQTZJRndpUTNWeWNtVnVkQ0JNWldkaGJDQk9ZVzFsWENKY2JpQWdJQ0FnSUNBZ0lIMGdYVnh1SUNBZ0lDQWdmU3dnZTF4dUlDQWdJQ0FnSUNBZ1hDSjBlWEJsWENJZ09pQmNJbk4wY21sdVoxOXNhWE4wWENJc1hHNGdJQ0FnSUNBZ0lDQmNJbTVoYldWY0lpQTZJRndpVEdWbllXeFFaWEp6YjI1SlpHVnVkR2xtYVdWeVhDSXNYRzRnSUNBZ0lDQWdJQ0JjSW5aaGJIVmxjMXdpSURvZ1d5QjdYRzRnSUNBZ0lDQWdJQ0FnSUNCY0luWmhiSFZsWENJZ09pQmNJakV5TlRRMU56UTROME52YlhCaGJubGNJbHh1SUNBZ0lDQWdJQ0FnZlNCZFhHNGdJQ0FnSUNCOUxDQjdYRzRnSUNBZ0lDQWdJQ0JjSW5SNWNHVmNJaUE2SUZ3aVlXUmtjbVZ6YzBsa1hDSXNYRzRnSUNBZ0lDQWdJQ0JjSW01aGJXVmNJaUE2SUZ3aVEzVnljbVZ1ZEVGa1pISmxjM05jSWl4Y2JpQWdJQ0FnSUNBZ0lGd2lkbUZzZFdWY0lpQTZJSHRjYmlBZ0lDQWdJQ0FnSUNBZ0lGd2lZM1pmWVdSa2NtVnpjMTloY21WaFhDSWdPaUJjSWtWMGRHVnlZbVZsYTF3aUxGeHVJQ0FnSUNBZ0lDQWdJQ0FnWENKaFpHMXBibDkxYm1sMFgyWnBjbk4wWDJ4cGJtVmNJaUE2SUZ3aVFrVmNJaXhjYmlBZ0lDQWdJQ0FnSUNBZ0lGd2lZV1J0YVc1ZmRXNXBkRjl6WldOdmJtUmZiR2x1WlZ3aUlEb2dYQ0pGVkZSRlVrSkZSVXRjSWl4Y2JpQWdJQ0FnSUNBZ0lDQWdJRndpYkc5allYUnZjbDlrWlhOcFoyNWhkRzl5WENJZ09pQmNJakk0WENJc1hHNGdJQ0FnSUNBZ0lDQWdJQ0JjSW14dlkyRjBiM0pmYm1GdFpWd2lJRG9nWENKRVNVZEpWQ0JpZFdsc1pHbHVaMXdpTEZ4dUlDQWdJQ0FnSUNBZ0lDQWdYQ0p3YjE5aWIzaGNJaUE2SUZ3aU1USXpORndpTEZ4dUlDQWdJQ0FnSUNBZ0lDQWdYQ0p3YjNOMFgyTnZaR1ZjSWlBNklGd2lNVEEwTUZ3aUxGeHVJQ0FnSUNBZ0lDQWdJQ0FnWENKd2IzTjBYMjVoYldWY0lpQTZJRndpUlZSVVJWSkNSVVZMSUVOSVFWTlRSVndpTEZ4dUlDQWdJQ0FnSUNBZ0lDQWdYQ0owYUc5eWIzVm5hR1poY21WY0lpQTZJRndpVW5WbElFSmxiR3hwWVhKa1hDSmNiaUFnSUNBZ0lDQWdJSDFjYmlBZ0lDQWdJSDBzSUh0Y2JpQWdJQ0FnSUNBZ0lGd2lkSGx3WlZ3aUlEb2dYQ0p6ZEhKcGJtZGZiR2x6ZEZ3aUxGeHVJQ0FnSUNBZ0lDQWdYQ0p1WVcxbFhDSWdPaUJjSWtaaGJXbHNlVTVoYldWY0lpeGNiaUFnSUNBZ0lDQWdJRndpZG1Gc2RXVnpYQ0lnT2lCYklIdGNiaUFnSUNBZ0lDQWdJQ0FnSUZ3aWRtRnNkV1ZjSWlBNklGd2lSMkZ5WTJsaFhDSmNiaUFnSUNBZ0lDQWdJSDBnWFZ4dUlDQWdJQ0FnZlN3Z2UxeHVJQ0FnSUNBZ0lDQWdYQ0owZVhCbFhDSWdPaUJjSW5OMGNtbHVaMTlzYVhOMFhDSXNYRzRnSUNBZ0lDQWdJQ0JjSW01aGJXVmNJaUE2SUZ3aVJtbHljM1JPWVcxbFhDSXNYRzRnSUNBZ0lDQWdJQ0JjSW5aaGJIVmxjMXdpSURvZ1d5QjdYRzRnSUNBZ0lDQWdJQ0FnSUNCY0luWmhiSFZsWENJZ09pQmNJbXBoZG1sbGNsd2lYRzRnSUNBZ0lDQWdJQ0I5SUYxY2JpQWdJQ0FnSUgwc0lIdGNiaUFnSUNBZ0lDQWdJRndpZEhsd1pWd2lJRG9nWENKa1lYUmxYQ0lzWEc0Z0lDQWdJQ0FnSUNCY0ltNWhiV1ZjSWlBNklGd2lSR0YwWlU5bVFtbHlkR2hjSWl4Y2JpQWdJQ0FnSUNBZ0lGd2lkbUZzZFdWY0lpQTZJRndpTVRrMk5TMHdNUzB3TVZ3aVhHNGdJQ0FnSUNCOUxDQjdYRzRnSUNBZ0lDQWdJQ0JjSW5SNWNHVmNJaUE2SUZ3aWMzUnlhVzVuWDJ4cGMzUmNJaXhjYmlBZ0lDQWdJQ0FnSUZ3aWJtRnRaVndpSURvZ1hDSkhaVzVrWlhKY0lpeGNiaUFnSUNBZ0lDQWdJRndpZG1Gc2RXVnpYQ0lnT2lCYklIdGNiaUFnSUNBZ0lDQWdJQ0FnSUZ3aWRtRnNkV1ZjSWlBNklGd2lUV0ZzWlZ3aVhHNGdJQ0FnSUNBZ0lDQjlJRjFjYmlBZ0lDQWdJSDBzSUh0Y2JpQWdJQ0FnSUNBZ0lGd2lkSGx3WlZ3aUlEb2dYQ0p6ZEhKcGJtZGZiR2x6ZEZ3aUxGeHVJQ0FnSUNBZ0lDQWdYQ0p1WVcxbFhDSWdPaUJjSWxCbGNuTnZia2xrWlc1MGFXWnBaWEpjSWl4Y2JpQWdJQ0FnSUNBZ0lGd2lkbUZzZFdWelhDSWdPaUJiSUh0Y2JpQWdJQ0FnSUNBZ0lDQWdJRndpZG1Gc2RXVmNJaUE2SUZ3aU1USXpORFZjSWx4dUlDQWdJQ0FnSUNBZ2ZTQmRYRzRnSUNBZ0lDQjlJRjBzWEc0Z0lDQWdJQ0JjSW1GMWRHaGxiblJwWTJGMGFXOXVYMk52Ym5SbGVIUmZZMnhoYzNOY0lpQTZJRndpUVZ3aUxGeHVJQ0FnSUNBZ1hDSmpiR2xsYm5SZlNYQmZRV1JrY21WemMxd2lJRG9nWENJeE9USXVNVFk0TGpNdU5qUmNJaXhjYmlBZ0lDQWdJRndpWTNKbFlYUmxaRjl2Ymx3aUlEb2dYQ0l5TURJeExURXlMVEUzVkRFME9qQTRPakEzTGpjME5GcGNJaXhjYmlBZ0lDQWdJRndpYVdSY0lpQTZJRndpTkdWbE5EUTRNR0l0WW1ZMU9DMDBPVGt3TFRrMFlXWXRNMlV6TnpsbU9EUmlNbVF6WENJc1hHNGdJQ0FnSUNCY0ltbHVjbVZ6Y0c5dWMyVmZkRzljSWlBNklGd2lPVFZpTjJSbFptTXRNakl3WWkwMFpERXpMV0l3WVRRdE5UTTNOREV5TVRGbU5EbG1YQ0lzWEc0Z0lDQWdJQ0JjSW1semMzVmxjbHdpSURvZ1hDSkVSVTFQTFVsRVVGd2lMRnh1SUNBZ0lDQWdYQ0p6ZEdGMGRYTmNJaUE2SUh0Y2JpQWdJQ0FnSUNBZ0lGd2ljM1JoZEhWelgyTnZaR1ZjSWlBNklGd2ljM1ZqWTJWemMxd2lYRzRnSUNBZ0lDQjlMRnh1SUNBZ0lDQWdYQ0p6ZFdKcVpXTjBYQ0lnT2lCY0lqQXhNak0wTlRaY0lpeGNiaUFnSUNBZ0lGd2lkbVZ5YzJsdmJsd2lJRG9nWENJeFhDSmNiaUFnSUgxY2JuMDhMM1JsZUhSaGNtVmhQbHh5WEc0Z0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ1BHbHVjSFYwSUhSNWNHVTlYQ0pvYVdSa1pXNWNJaUJwWkQxY0ltUnZUbTkwYlc5a2FXWjVWR2hsVW1WemNHOXVjMlZjSWlCdVlXMWxQVndpWkc5T2IzUnRiMlJwWm5sVWFHVlNaWE53YjI1elpWd2lYSEpjYmlBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnZG1Gc2RXVTlYQ0p2Ymx3aUx6NWNjbHh1WEhKY2JpQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQThhVzV3ZFhRZ2RIbHdaVDFjSW1ocFpHUmxibHdpSUdsa1BWd2lVMDFUVTFCU1pYTndiMjV6WlZ3aUlHNWhiV1U5WENKVFRWTlRVRkpsYzNCdmJuTmxYQ0l2UGx4eVhHNGdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBOEwyUnBkajVjY2x4dUlDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnUEdsMFpXMGdkSGx3WlQxY0ltSjFkSFJ2Ymx3aUlITjBlV3hsUFNka2FYTndiR0Y1T201dmJtVTdKeUJwWkQxY0ltbGtjRk4xWW0xcGRHSjFkSFJ2Ymx3aVhISmNiaUFnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUdOc1lYTnpQVndpWW5SdUlHSjBiaTFrWldaaGRXeDBJR0owYmkxc1p5QmlkRzR0WW14dlkydGNJaUJ2Ym1Oc2FXTnJQVndpY21WMGRYSnVJR0poYzJVMk5GOWxibU52WkdVb0tUdGNJajVUZFdKdGFYUmNjbHh1SUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdQQzlwZEdWdFBseHlYRzRnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUR3dlptOXliVDVjY2x4dUlDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQThibTl6WTNKcGNIUS1YSEpjYmlBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJRHhtYjNKdElHbGtQVndpYm05S1lYWmhVMk55YVhCMFJtOXliVndpSUc1aGJXVTlYQ0p1YjBwaGRtRlRZM0pwY0hSU1pXUnBjbVZqZEVadmNtMWNJaUJoWTNScGIyNDlYQ0pvZEhSd09pOHZiRzlqWVd4b2IzTjBPamd3T0RBdlUzQmxZMmxtYVdOUWNtOTRlVk5sY25acFkyVXZTV1J3VW1WemNHOXVjMlZjSWx4eVhHNGdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0J0WlhSb2IyUTlYQ0p3YjNOMFhDSS1YSEpjYmlBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBOGFXNXdkWFFnZEhsd1pUMWNJbWhwWkdSbGJsd2lJR2xrUFZ3aVUwMVRVMUJTWlhOd2IyNXpaVTV2U2xOY0lpQnVZVzFsUFZ3aVUwMVRVMUJTWlhOd2IyNXpaVndpWEhKY2JpQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ2RtRnNkV1U5WENKbGQyOW5TVU5CYVdOdFZucGpSemwxWXpKVmFVbEViMmRsZDI5blNVTkJaMGxEUVdsWldGSXdZMjFzYVdSWVVteFlNbmh3WXpOUmFVbEViMmRYZVVJM1EybEJaMGxEUVdkSlEwRm5TVU5LTUdWWVFteEphVUUyU1VOS2VtUklTbkJpYldSbVlrZHNlbVJEU1hORGFVRm5TVU5CWjBsRFFXZEpRMHAxV1ZjeGJFbHBRVFpKUTBwTldsZGthR0pGTldoaVYxVnBURUZ2WjBsRFFXZEpRMEZuU1VOQmFXUnRSbk5rVjFaNlNXbEJOa2xHYzJkbGQyOW5TVU5CWjBsRFFXZEpRMEZuU1VOQmFXUnRSbk5rVjFWcFNVUnZaMGxyVGpGamJrcHNZbTVSWjFSSFZtNVpWM2RuVkcxR2RGcFRTVXRKUTBGblNVTkJaMGxEUVdkbVUwSmtRMmxCWjBsRFFXZEpTREJ6U1VoelMwbERRV2RKUTBGblNVTkJaMGx1VWpWalIxVnBTVVJ2WjBsdVRqQmpiV3gxV2pFNWMyRllUakJKYVhkTFNVTkJaMGxEUVdkSlEwRm5TVzAxYUdKWFZXbEpSRzluU1d0NGJGb3lSbk5WUjFaNVl6STVkVk5YVW14aWJsSndXbTFzYkdOcFNYTkRhVUZuU1VOQlowbERRV2RKUTBveVdWZDRNVnBZVFdsSlJHOW5WM2xDTjBOcFFXZEpRMEZuU1VOQlowbERRV2RKUTBveVdWZDRNVnBUU1dkUGFVRnBUVlJKTVU1RVZUTk9SR2N6VVRJNWRHTkhSblZsVTBsTFNVTkJaMGxEUVdkSlEwRm5abE5DWkVOcFFXZEpRMEZuU1Vnd2MwbEljMHRKUTBGblNVTkJaMGxEUVdkSmJsSTFZMGRWYVVsRWIyZEpiVVpyV2toS2JHTXpUa3BhUTBselEybEJaMGxEUVdkSlEwRm5TVU5LZFZsWE1XeEphVUUyU1VOS1JHUllTbmxhVnpVd1VWZFNhMk50Vm5wamVVbHpRMmxCWjBsRFFXZEpRMEZuU1VOS01sbFhlREZhVTBsblQybENOME5wUVdkSlEwRm5TVU5CWjBsRFFXZEpRMHBxWkd3NWFGcEhVbmxhV0U1NldESkdlVnBYUldsSlJHOW5TV3RXTUdSSFZubFpiVlpzWVhsSmMwTnBRV2RKUTBGblNVTkJaMGxEUVdkSlEwcG9Xa2N4Y0dKc09URmliV3d3V0RKYWNHTnVUakJZTW5od1ltMVZhVWxFYjJkSmEwcEdTV2wzUzBsRFFXZEpRMEZuU1VOQlowbERRV2RKYlVacllsZHNkVmd6Vm5WaFdGSm1ZekpXYW1JeU5XdFlNbmh3WW0xVmFVbEViMmRKYTFaVlZrVldVMUZyVmtaVGVVbHpRMmxCWjBsRFFXZEpRMEZuU1VOQlowbERTbk5pTWs1b1pFYzVlVmd5VW14ak1teHVZbTFHTUdJelNXbEpSRzluU1dwSk5FbHBkMHRKUTBGblNVTkJaMGxEUVdkSlEwRm5TVzE0ZGxreVJqQmlNMHBtWW0xR2RGcFRTV2RQYVVGcFVrVnNTRk5XVVdkWmJsWndZa2RTY0dKdFkybE1RVzluU1VOQlowbERRV2RKUTBGblNVTkJhV05IT1daWmJUazBTV2xCTmtsRFNYaE5hazB3U1dsM1MwbERRV2RKUTBGblNVTkJaMGxEUVdkSmJrSjJZek5TWmxreU9XdGFVMGxuVDJsQmFVMVVRVEJOUTBselEybEJaMGxEUVdkSlEwRm5TVU5CWjBsRFNuZGlNMDR3V0RJMWFHSlhWV2xKUkc5blNXdFdWVlpGVmxOUmExWkdVM2xDUkZORlJsUlZNRlZwVEVGdlowbERRV2RKUTBGblNVTkJaMGxEUVdsa1IyaDJZMjA1TVZveWFHMVpXRXBzU1dsQk5rbERTbE5rVjFWblVXMVdjMkpIYkdoamJWRnBRMmxCWjBsRFFXZEpRMEZuU1Vnd1MwbERRV2RKUTBGblpsTjNaMlYzYjJkSlEwRm5TVU5CWjBsRFFXbGtTR3gzV2xOSlowOXBRV2xqTTFKNVlWYzFibGd5ZUhCak0xRnBURUZ2WjBsRFFXZEpRMEZuU1VOQmFXSnRSblJhVTBsblQybEJhVkp0Um5SaFYzZzFWRzFHZEZwVFNYTkRhVUZuU1VOQlowbERRV2RKUTBveVdWZDRNVnBZVFdsSlJHOW5WM2xDTjBOcFFXZEpRMEZuU1VOQlowbERRV2RKUTBveVdWZDRNVnBUU1dkUGFVRnBVakpHZVZreWJHaEpaMjluU1VOQlowbERRV2RKUTBJNVNVWXdTMGxEUVdkSlEwRm5abE4zWjJWM2IyZEpRMEZuU1VOQlowbERRV2xrU0d4M1dsTkpaMDlwUVdsak0xSjVZVmMxYmxneWVIQmpNMUZwVEVGdlowbERRV2RKUTBGblNVTkJhV0p0Um5SYVUwbG5UMmxCYVZKdGJIbGpNMUpQV1ZjeGJFbHBkMHRKUTBGblNVTkJaMGxEUVdkSmJscG9Za2hXYkdONVNXZFBhVUppU1VoelMwbERRV2RKUTBGblNVTkJaMGxEUVdkSmJscG9Za2hXYkVscFFUWkpRMHB4V1ZoYWNGcFlTV2xEYVVGblNVTkJaMGxEUVdkSlNEQm5XRkZ2WjBsRFFXZEpRMEk1VEVOQ04wTnBRV2RKUTBGblNVTkJaMGxEU2pCbFdFSnNTV2xCTmtsRFNtdFpXRkpzU1dsM1MwbERRV2RKUTBGblNVTkJaMGx0TldoaVYxVnBTVVJ2WjBsclVtaGtSMVpRV210S2NHTnVVbTlKYVhkTFNVTkJaMGxEUVdkSlEwRm5TVzVhYUdKSVZteEphVUUyU1VOSmVFOVVXVEZNVkVGNFRGUkJlRWxuYjJkSlEwRm5TVU5DT1V4RFFqZERhVUZuU1VOQlowbERRV2RKUTBvd1pWaENiRWxwUVRaSlEwcDZaRWhLY0dKdFpHWmlSMng2WkVOSmMwTnBRV2RKUTBGblNVTkJaMGxEU25WWlZ6RnNTV2xCTmtsRFNraGFWelZyV2xoSmFVeEJiMmRKUTBGblNVTkJaMGxEUVdsa2JVWnpaRmRXZWtscFFUWkpSbk5uWlhkdlowbERRV2RKUTBGblNVTkJaMGxEUVdsa2JVWnpaRmRWYVVsRWIyZEphekZvWWtkVmFVTnBRV2RKUTBGblNVTkJaMGxJTUdkWVVXOW5TVU5CWjBsRFFqbE1RMEkzUTJsQlowbERRV2RKUTBGblNVTktNR1ZZUW14SmFVRTJTVU5LZW1SSVNuQmliV1JtWWtkc2VtUkRTWE5EYVVGblNVTkJaMGxEUVdkSlEwcDFXVmN4YkVscFFUWkpRMHBSV2xoS2VtSXlOVXBhUjFaMVpFZHNiV0ZYVm5sSmFYZExTVU5CWjBsRFFXZEpRMEZuU1c1YWFHSklWbXhqZVVsblQybENZa2xJYzB0SlEwRm5TVU5CWjBsRFFXZEpRMEZuU1c1YWFHSklWbXhKYVVFMlNVTkplRTFxVFRCT1UwbExTVU5CWjBsRFFXZEpRMEZuWmxOQ1pFTnBRV2RKUTBGblNVZ3daMWhUZDB0SlEwRm5TVU5CWjBsdFJqRmtSMmhzWW01U2NGa3lSakJoVnpsMVdESk9kbUp1VW14bFNGSm1XVEo0YUdNelRXbEpSRzluU1d0RmFVeEJiMmRKUTBGblNVTkJhVmt5ZUhCYVZ6VXdXREJzZDFnd1JtdGFTRXBzWXpOTmFVbEViMmRKYWtVMVRXazBlRTVxWjNWTmVUUXlUa05KYzBOcFFXZEpRMEZuU1VOS2FtTnRWbWhrUjFacldESTVkVWxwUVRaSlEwbDVUVVJKZUV4VVJYbE1WRVV6VmtSRk1FOXFRVFJQYWtFelRHcGpNRTVHYjJsTVFXOW5TVU5CWjBsRFFXbGhWMUZwU1VSdlowbHFVbXhhVkZFd1QwUkNhVXhYU20xT1ZHZDBUa1JyTlUxRE1EVk9SMFp0VEZST2JFMTZZelZhYW1jd1dXcEthMDE1U1hORGFVRm5TVU5CWjBsRFNuQmlia3BzWXpOQ2RtSnVUbXhZTTFKMlNXbEJOa2xEU1RWT1Ywa3pXa2RXYlZsNU1IbE5ha0pwVEZSU2EwMVVUWFJaYWtKb1RrTXdNVTE2WXpCTlZFbDRUVmRaTUU5WFdXbE1RVzluU1VOQlowbERRV2xoV0U1NlpGZFdlVWxwUVRaSlEwcEZVbFV4VUV4VmJFVlZRMGx6UTJsQlowbERRV2RKUTBwNlpFZEdNR1JZVFdsSlJHOW5aWGR2WjBsRFFXZEpRMEZuU1VOQmFXTXpVbWhrU0ZaNldESk9kbHBIVldsSlJHOW5TVzVPTVZreVRteGpNMDFwUTJsQlowbERRV2RKU0RCelEybEJaMGxEUVdkSlEwcDZaRmRLY1ZwWFRqQkphVUUyU1VOSmQwMVVTWHBPUkZVeVNXbDNTMGxEUVdkSlEwRm5TVzVhYkdOdVRuQmlNalJwU1VSdlowbHFSV2xEYVVGblNVZ3dTMlpSUFQxY0lpOC1YSEpjYmlBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBOGFXNXdkWFFnZEhsd1pUMWNJbk4xWW0xcGRGd2lJR2xrUFZ3aWMzVmliV2wwUW5WMGRHOXVNVndpSUdOc1lYTnpQVndpWW5SdUlHSjBiaTF1WlhoMFhDSWdkbUZzZFdVOVhDSlRkV0p0YVhSY0lpOC1YSEpjYmlBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJRHd2Wm05eWJUNWNjbHh1SUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNBOEwyNXZjMk55YVhCMEluMC52bWduSXg5b3FYdzlneVROMnVreVd2S09Ga3hkeGI1U3VhQS1SZk1ta2V0OXh1SUo5Wkc4WG9SRVRDT1hjemxpUV9pQXBaS25FV3hqU3hVRGZvZnVuaU11R1RYWHBXZkRsNm05OUZ0aVBsUEFMazU1bkNrLUxQMy01UjItWGpXQTdZelRJcFpuMFlMNjJWR1J4NlFaYVRabHRGaWZjMnhYTk5uVDVLek1FVlkiLCJhc3NlcnRpb24iOiJleUowZVhBaU9pSktWMVFpTENKaGJHY2lPaUpTVXpJMU5pSjkuZXlKa1lYUmhNaUk2SWx3aVkzSmxZWFJsWkY5dmJsd2lJRG9nWENJeU1ESXhMVEV5TFRFM1ZERTBPakE0T2pBM0xqYzBORnBjSWl4Y2JpQWdJQ0FnSUZ3aWFXUmNJaUE2SUZ3aU5HVmxORFE0TUdJdFltWTFPQzAwT1Rrd0xUazBZV1l0TTJVek56bG1PRFJpTW1RelhDSXNYRzRnSUNBZ0lDQmNJbWx1Y21WemNHOXVjMlZmZEc5Y0lpQTZJRndpT1RWaU4yUmxabU10TWpJd1lpMDBaREV6TFdJd1lUUXROVE0zTkRFeU1URm1ORGxtWENJc1hHNGdJQ0FnSUNCY0ltbHpjM1ZsY2x3aUlEb2dYQ0pFUlUxUExVbEVVRndpTEZ4dUlDQWdJQ0FnWENKemRHRjBkWE5jSWlBNklIdGNiaUFnSUNBZ0lDQWdJRndpYzNSaGRIVnpYMk52WkdWY0lpQTZJRndpYzNWalkyVnpjMXdpWEc0Z0lDQWdJQ0I5TEZ4dUlDQWdJQ0FnWENKemRXSnFaV04wWENJZ09pQmNJakF4TWpNME5UWmNJaXhjYmlBZ0lDQWdJRndpZG1WeWMybHZibHdpSURvZ1hDSXhYQ0pjYmlBZ0lIMWNibjA4TDNSbGVIUmhjbVZoUGlKOS5WX1JBMjRzTzBud1NqUW9ueTdpR0dnTmVicWR1d0kyNHA4RV9OcXU4M2RBdG5VTV8tTUZ3UnBscDJJNHhMWWdyS3hjRE9uT3BWQk9HNTVITnloX2VRVnN1ODVoQkIwcWQySEFTQzBkelZ3S21CZGp1MEI0cVdKVVdmQlRKWW5Ca1lnZUhpZmZURlZmVkRXOVc3QlJPN3ZEYnNkQVRaNTNWWFpHam0wY1FlbncifQ.PyzK72u6MwP5VwIdSwzPzn2y7hwfaedKnox7_Tm9RBoth1K6u3yPdrqVk_RHDWYwAE7HSxcBZ4MHXDTHzl9cjq82kiOCpkUx8tQ0_0AoyrW_Jw4nH07bwgBdFCCMK4AdgjH-RazjqQ_cv0NegKTQlWYTHrZC83GMFYlQOlgSsRA", "TUlJRElUQ0NBZ21nQXdJQkFnSUVEOEo5U0RBTkJna3Foa2lHOXcwQkFRc0ZBREFUTVJFd0R3WURWUVFEREFoSApjbVZsWTJWRFFUQWVGdzB5TVRFeE16QXhNVEl6TlRoYUZ3MHlNekExTWpVd09EQXdNemRhTUJFeER6QU5CZ05WCkJBTU1Ca2RTZUdGMmFUQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQUp5bkw4ZVgKajNpbThTcVV0L1BYUlVXOGdYUm5TTEhTNkhtVC9CSDZwc01HRHpOZGJrdnpKbWU5SEE1anNCOFNFRzV3QjNzNgpKRUEzOGVtbm9RWTBxc3l6aGFWWmxHc0RBaGdURWNuUXo1T1VLbzJPRVVtTk9yZUJnRzl0aTFXK092Z3NQTHpUCnEzTjVGd3N6NWVYOUFyNEI5S2dZb3RWWW16eVhLUEdhRDYva3Q0akU5b09MOUJTK2I2dDdTbVdXNmJtZVY4NVUKc241V0d0NUhhdGt5UFNEUmFKMkg1Z3REbEJqY0ZBUjNEejNNMzNEbno0YlVtdEhiczhhT1Y1VHl0M3JuZENadQo5ckNnRzhNeW1wamhlS09JUk1wVGdUMkgxSWdKdXRHY0JibjQ5Mmg4Um5DeHdJUGZFZWpkUDQ5SzU3WTlUZEJyCnY3Tmw5bG9nME4wS21OY0NBd0VBQWFOL01IMHdEQVlEVlIwVEFRSC9CQUl3QURBZkJnTlZIU01FR0RBV2dCUi8KWDFYNjhKc3ZVd3JmZUx5Q0VWRUEyT1l6OXpBZEJnTlZIU1VFRmpBVUJnZ3JCZ0VGQlFjREFnWUlLd1lCQlFVSApBd1F3SFFZRFZSME9CQllFRkc3RXVUYzVYdjE0SktyTStrVngySmlyNmxhbU1BNEdBMVVkRHdFQi93UUVBd0lGCjREQU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FRRUFjMDl4ZlJaUDdwRVdKZ3B1dFhtL2F4akswMFhKN3VBN3QxbmYKL01pWHdvSytiYXlLSVpWZWhSeEEzVk5VYXBOR1VNTnZWZkw3MmZOUFJLNkNERENDSzMvVk1mZE5EbHFDWDZROQozZ21hUk96cVVpd2VtUFovVEpreWY5R01laVdMZXhBZEszOTFsT1BJZ3orcmFoOVZtZVpBRDJIem9xSzVaQkp3Cm5oaUlKaHVtaVgwbmtpc2phRy9TYm1KNllENm02VlZtTDY3M0VXdC9lQjZod01UalBsdXE5Q2dNRmdEWGZmZUoKZUJjUWZNd2pXa05ITG1tY3ZJSzlmcTZoYmJxNmNISFBmREpZTjRHL2Y4OHZodzZha3dFZ1J1aXRic2J4ck91agozUTdSVnR6MmhGa0NWSk1WNkJpWHU0VXpDSk5uZTV0ai9HQ3JwRWZKTVB6WWZTTEVrdz09");
            Log.d("LOGIC_OF_RDS","Pseudonym retrieved: "+pseudo);
            pm.setPseudo("pseudonym", pseudo, studyId, context);
        }
        else
        {
            pseudo = retrievePseudoIdentity(studyId,host);
            Log.d("LOGIC_OF_RDS","Pseudo-identity retrieved: "+pseudo);
            PrivacyMechanisms pm = PrivacyMechanisms.PrivacyMechanisms();
            pm.setPseudo("pseudo-identity", pseudo, studyId, context);
        }
        //call anonymization methods (the setPseudo)
        //convert studyID to String
//        String studyID = Integer.toString(studyId);
//        PrivacyMechanisms privacyMechanisms = null;
//        privacyMechanisms.setPseudo(pseudoType, pseudo, studyID, context);
//        PrivacyMechanisms privacyMechanisms = null;
//        String get_pseudo = privacyMechanisms.getPseudo("123", context);
//        if (!get_pseudo.equals("")) {
//            Log.d("LOGIC_OF_RDS","FOUND");
//        }
//        else {
//            Log.d("LOGIC_OF_RDS","NOT FOUND");
//        }

        //Code to generate the consent and sign it, as well as getting the user certificate
        String CA_URL = "http://212.101.173.84:8071";
        CryptoManagement crypto = CryptoManagementFactory.create(CA_URL);
        CryptoManagement cryptoManagement = CryptoManagementFactory.create(CA_URL);

        try {
            // mobile app
            String consent = crypto.generateConsent();
            Log.d("LOGIC_OF_RDS ", consent);
            PrivateKey privateKey = cryptoManagement.getPrivateKey(context);
            String signed = crypto.signPayload(consent,privateKey);
            Log.d("LOGIC_OF_RDS - sign", signed);
            //Todo Use the ubitech lib or the getUserCertificateInfo (Comment-Uncomment accordingly)
            //byte[] certificateData = cryptoManagement.getUserCertificate("GRxavi");
            byte[] certificateData = crypto.getCertificateFromKeystore(context);
            String encoded_certificateData = Base64.encodeToString(certificateData, android.util.Base64.DEFAULT).replaceAll("\r", "").replaceAll("\n", "");
            Log.d("LOGIC_OF_RDS ","Certificate Data: " +encoded_certificateData);
            //send the enrollment consent to the RRC selected by the citizen
            String signed_contract_string = sendEnrollmentConsent(host,context, studyId, consent, signed, pseudo, encoded_certificateData, enrollment_criteria, sehrappid);
            Log.d("LOGIC_OF_RDS ", "Signed contract retrieved: "+signed_contract_string);
            if (this.listener != null) {
                this.listener.notifyEnrollment(true, studyId, host);
            }
        }
        catch (Exception e) {
            Log.e("LOGIC_OF_RDS", e.getMessage());

            if (this.listener != null) {
                this.listener.notifyEnrollment(false, studyId, host);
            }
        }




    }

    //this is the method for sending the enrollment consent to the RRC
    public String sendEnrollmentConsent(String host,Context context, String studyId, String consent, String signed, String pseudo, String certificateData, String enrollment_criteria, int sehrappid) throws IOException, JSONException {
        url = new HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();


         Retrofit retrofit;
         ResearchStudyService service;

        retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(this.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ResearchStudyService.class);
        //dummy request to the RRC
        //encode to base64
        String encodedConsent= Base64.encodeToString(consent.getBytes(), Base64.DEFAULT).replaceAll("\r", "").replaceAll("\n", "");
        Log.d("LOGIC_OF_RDS","Encoded consent: "+encodedConsent);
        String encodedSignature = Base64.encodeToString(signed.getBytes(), Base64.DEFAULT).replaceAll("\r", "").replaceAll("\n", "");
        Log.d("LOGIC_OF_RDS","Encoded signature: "+encodedSignature);
        String encodedCerticicate = Base64.encodeToString(certificateData.getBytes(), Base64.DEFAULT).replaceAll("\r", "").replaceAll("\n", "");
        Log.d("LOGIC_OF_RDS","Encoded certificate: "+encodedCerticicate);
        final Response<ResponseBody> responseBody = service.sendEnrollmentConsent(studyId, new EnrollmentConsentRequest(encodedConsent, encodedSignature, certificateData, pseudo, enrollment_criteria, sehrappid)).execute();
        //Todo make the real request to the rrc
        Log.d("LOGIC_OF_RDS","RESPONSE BODY FROM THE RRC: "+responseBody.toString());

        //get the fields "signed-contract" and "certificate" from the response body
        String json = responseBody.body().string();
        JSONObject jObject = new JSONObject(json);
        String signed_contract = jObject.getString("signed-contract");
        String rrc_certificate = jObject.getString("certificate");
        Log.d("LOGIC_OF_RDS","Signed contact and rrc certificate: "+signed_contract.toString() +rrc_certificate.toString());
        //Todo: Store the signed contract into the database
        DBadapter helper;
        //this is the helper for the db
        helper = new DBadapter(context);
        //convert signed contact to string
        String signed_contract_string = signed_contract.toString();
        //store the contract for the study with the specific id
        helper.insertConsent(studyId,signed_contract_string);

        return signed_contract_string;

    }

    //this method is responsible for sending the exit notification to the RRC
    public String sendExitNotification(String studyId, String citizen_pseudo, String reason, String reason_text, String citizen_signature) throws IOException, JSONException {
        url = new HttpUrl.Builder()
                .scheme("http")
                .host("213.249.46.208")
                .port(80).build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit;
        ResearchStudyService service;

        retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(this.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ResearchStudyService.class);
        //make the request to the RRC
        final Response<ResponseBody> responseBody = service.sendExitNotification(studyId, new ExitNotificationRequest(citizen_pseudo,reason,reason_text,citizen_signature)).execute();
        String rrcResponse = responseBody.toString();
        Log.d("LOGIC_OF_RDS","RRC response is: "+rrcResponse);
        if(rrcResponse.contains("200"))
        {
            return "200";
        }
        else
        {
            return "500";
        }


    }


    //this method is responsible for sending the citizen's health data to the RRC
    public String sendHealthData(String host,String studyId, String health_data, Context context) throws Exception {
        Log.d("LOGIC_OF_RDS","Host address: "+host);
        url = new HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit;
        ResearchStudyService service;

        retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(this.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ResearchStudyService.class);



        //get the exit criteria of the given study
        DBadapter helper;
        helper = new DBadapter(context);
        String exitCriteria = helper.getExitCriteria(studyId);

        //check if exit criteria are met and if yes, withdraw from study
        //TODO Call the checkExitCriteria method
        boolean exitCriteriaResult = false;
        if(exitCriteriaResult)
        {
            boolean exitStudy = withdrawFromStudy(studyId,"DROPOUT","reason_text",context);
            if(exitStudy)
            {
                return "EXIT";
            }
        }
        //get the research study
        //String rdd = String.valueOf(new JSONObject(storedResearchStudy).get("RDD"));
        Log.d("LOGIC_OF_RDS","Exit criteria of the study: "+exitCriteria);


        //get the anonymization type
        String anonymization_type = helper.getAnonymizationType(studyId);
        Log.d("LOGIC_OF_RDS","Anonymization type of the study: "+anonymization_type);

        //get the data requirements of the study
        String dataRequirements = helper.getDataRequirements(studyId);
        Log.d("LOGIC_OF_RDS","Data requirements of the study: "+dataRequirements);


//
//        DBadapter helper;
//        //this is the helper for the db
//        helper = new DBadapter(context);
//        //convert signed contact to string
//        String researchStudy = helper.getStudybyID(studyId);
//        Log.d("LOGIC_OF_RDS","Research study found: "+researchStudy);
        //create PrivacyMechanisms object
        PrivacyMechanisms pm = PrivacyMechanisms.PrivacyMechanisms();
        String anonymized_data = null;
        //get the pseudo for the study with the specific ID
        String citizen_pseudo = pm.getPseudo(studyId, context);
        Log.d("LOGIC_OF_RDS","Stored citizen pseudo is: "+citizen_pseudo);
        //check the anonymization type and call the corresponding mechanism
        if(anonymization_type.equals("anonymization"))
        {
            anonymized_data = pm.anonymizeData(health_data,"none");
            Log.d("LOGIC_OF_RDS","Anonymized data: "+anonymized_data.toString());
        }
        else
        {
            anonymized_data = pm.pseudonymizeData(health_data.toString(),"none",studyId,context);
            Log.d("LOGIC_OF_RDS","Pseudonymized data: "+anonymized_data.toString());
        }

        Log.d("LOGIC_OF_RDS","Id of the study: "+studyId);

        //data encryption
        String CA_URL = "http://212.101.173.84:8071";
        CryptoManagement crypto = CryptoManagementFactory.create(CA_URL);
        //this is the symmetric key used to encrypt the data
        //String symmetric = "UiYmJk%2FiWopGS2n0YIhZsgp1auRVyahR7sgFOtEC5r4%5C%5Cu003d"; //encryptedCommunication.generateSymmtericKey();
        String symmetric = "Bos0HSxY4HWrVwEZaoywbAnP8a0BWExEfl5pyHULEXQ=";
        //dummy encrypted data
        //String encrypted_data = "G9oaffubyHPfO5eOx947VD5RQBrRZYeTUKhXZ8bBIFKAXhNqc7qr92+1yNgmhzrwaYqlAXOkajWwpUdb9kmjDoZijinju6MJ/vwrx19aGD9jyRm2uycM9vESXZKFnjRv3bH6mRfObu8nnc35HwOL51r/OKkPUIEVHl3w8UtL6djDNK2ANSG95LFmauI/mAiTQPuPJrP5qeH7IotHce+RoLSvQjSjtH6LTlPJgFmefEj1prRUzPhSX5IctzQLrPXRSUGCDHQsw8suRn6vwM7KSUdax77DNhZyY5rj0QMLmPDRqH3M3nR2mkfHqnRzRhRnk50PQAAf8Jw756bjlh/u8AWUiKlgUPjQY0egvjl35/c/YcrIV/X77NWTjurTCzyhF0pzanhPHpeT+K2ji/lrnLloxB/y+m02OKBRysaC1ah+F3hQbPyaxfiPMkVjJeTkqs41mnMj/rEEoHzmbszrgd/qJdlp83CiEFec5q4OhND8wJmhg3CGsrhCBVWpaMhBxZMPS2fgmDl93e0pRzed8wxQL9Xalukv5gLPy6OjOzm6ba/vvvOBTWbYMRUTQtE4jlxcU7tOc/JLAuF20DUGMlmafGwpJqhOMOgT7hIxXyqkAM+P2uLZMBCpVkR/nAlHnkiMN+l05euUU1ooWCo6pMBCY49w3IDKo2fUXroJmNdWpmEWBS+s08bjUjK/29Q6qkZuXqdue7ipAVAULqG+G4VcJA+jMDQoEmhXIn4TJGU=";
        String encrypted_data = crypto.encrypt(anonymized_data, symmetric);
        Log.d("LOGIC_OF_RDS","Encrypted data: "+encrypted_data.toString());

        //make the request to the RRC
        final Response<ResponseBody> responseBody = service.sendHealthData(studyId,citizen_pseudo,symmetric, new HealthDataRequest(encrypted_data)).execute();
        //Todo test that the sending of the health data is ok
        //Log.d("LOGIC_OF_RDS","RESPONSE BODY FROM THE RRC: "+responseBody.toString());
        //get the HTTP code of the response
        Response<ResponseBody> rrcResponseObject = responseBody;
        //Log info about the response
        Log.d("LOGIC_OF_RDS","RRC response code: "+rrcResponseObject.code());
        //Log.d("LOGIC_OF_RDS","RRC error response body is: "+rrcResponseObject.errorBody().string());
        Log.d("LOGIC_OF_RDS","RRC Response is: "+rrcResponseObject.toString());
        //get the response as a string and check if it is successful
        String rrcResponse = responseBody.toString();
        if(rrcResponse.contains("200")){
            Log.d("LOGIC_OF_RDS","RRC Response is 200");
            //call notifyDataRetrieval()
            if (this.listener != null) {
                this.listener.notifyDataRetrieval(studyId);
            }
            return "200";
        }
        else{
            Log.d("LOGIC_OF_RDS","RRC Response is 500");
            return "500";
        }

    }


    //this is the method for retrieving a pseudo-identity from the RRC (for pseudo-identity-based studies)
    public String retrievePseudoIdentity(String studyId, String host) throws IOException, JSONException {
        url = new HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit;
        ResearchStudyService service;

        retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(this.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ResearchStudyService.class);
        //making the request to the RRC
        final Response<ResponseBody> responseBody = service.retrievePseudoIdentity(studyId).execute();
        //get the body of the request
        String json = responseBody.body().string();
        JSONObject jObject = new JSONObject(json);
        //get the pseudo-identity
        String pseudo_identity = jObject.getString("pseudo-identity");
        Log.d("LOGIC_OF_RDS","PSEUDO-IDENTITY RETRIEVED FROM THE RRC: "+pseudo_identity);
        return pseudo_identity;
    }

    //method to just get the enrollment consent in order to show it to the citizen
    public String getConsent()
    {
        String CA_URL = "http://212.101.173.84:8071";
        CryptoManagement crypto = CryptoManagementFactory.create(CA_URL);
        String consent = crypto.generateConsent();
        return consent;
    }

    //method to retrieve the countersigned consent
    public boolean retrieveCountersignedConsent(String studyId,Context context){
        DBadapter helper;
        helper = new DBadapter(context);
        //boolean variable that is true if a specific study has a countersigned consent
        boolean search_result = helper.getCountersignedConsent(studyId);
        return search_result;
    }

    //method to calculate dates to request health data (based on number of days (eg 7 days)
    public void calculateDates(String studyId,String currentDate, int numberOfDays, Context context){
        //arraylist to store all the calculated dates
        ArrayList<String> datesToStore = new ArrayList<>();
        String dt = currentDate;  // Start date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Calendar c = Calendar.getInstance();
        //for loop to calculate multiple dates
        for (int i = 0; i < 100; i++) {
            try {
                c.setTime(sdf.parse(dt));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c.add(Calendar.DATE, numberOfDays);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
            String output = sdf1.format(c.getTime());
            dt = output;
            //store the date into the arraylist
            datesToStore.add(output);
        }
        //print the arraylist of dates
        Log.d("LOGIC_OF_RDS","ARRAYLIST OF DATES: "+datesToStore);
        //store the arraylist into the db
        DBadapter helper;
        //this is the helper for the db
        helper = new DBadapter(context);
        //store the dates into the db
        helper.insertDates(studyId,datesToStore.toString());
    }

    //method to just call the getResearchDate()
    public boolean checkDate(String studyId, String currentDate, Context context)
    {
        DBadapter helper;
        //this is the helper for the db
        helper = new DBadapter(context);
        //if date exists return true so that data can be sent
        if(helper.getResearchDate(studyId,currentDate))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    //security methods
//    private void consent(Context context) {
//        String CA_URL = "http://212.101.173.84:8071";
//        CryptoManagement crypto = CryptoManagementFactory.create(CA_URL);
//        CryptoManagement cryptoManagement = CryptoManagementFactory.create(CA_URL);
//
//        try {
//            // mobile app
//            String consent = crypto.generateConsent();
//            Log.d("LOGIC_OF_RDS ", consent);
//            PrivateKey privateKey = cryptoManagement.getPrivateKey(context, "GRxavi");
//            String signed = crypto.signPayload(consent,privateKey);
//            Log.d("LOGIC_OF_RDS - sign", signed);
//            byte[] certificateData = cryptoManagement.getUserCertificate("GRxavi");
//            Log.d("LOGIC_OF_RDS ", String.valueOf(certificateData));
//            // send signed, consent and certificateData to research center...
//            //probably need to make a call to exchangeKey endpoint
//
//        }
//        catch (Exception e) {
//            Log.e("consent", e.getMessage());
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void diffieHellman() {
//        String CA_URL = "http://212.101.173.84:8071";
//        CryptoManagement crypto = CryptoManagementFactory.create(CA_URL);
//
//        try {
//            //Mobile app
//            KeyPair mobileKpair = crypto.aliceInitKeyPair();
//            Log.d("LOGIC_OF_RDS ", String.valueOf(mobileKpair));
//            KeyAgreement mobileKpairKA = crypto.aliceKeyAgreement(mobileKpair);
//            Log.d("LOGIC_OF_RDS ", String.valueOf(mobileKpairKA));
//            byte[] mobilePubKeyEnc = crypto.alicePubKeyEnc(mobileKpair);
//            Log.d("LOGIC_OF_RDS ", String.valueOf(mobilePubKeyEnc));
//            // send mobilePubKeyEnc to research center..
//
//            //Research Center
//            KeyPair researchkeypair = crypto.bobInitKeyPair(mobilePubKeyEnc);
//            KeyAgreement researchKeyAgreement = crypto.bobKeyAgreement(researchkeypair);
//            KeyAgreement symkeyagreement = crypto.bobKeyAgreementFin(mobilePubKeyEnc, researchKeyAgreement);
//            byte[] researchSharedSecret = symkeyagreement.generateSecret();
//            SecretKeySpec symkeyspec = crypto.generateSymmtericKey(researchSharedSecret, 32);
//            String symkeys = Base64.getEncoder().encodeToString(symkeyspec.getEncoded()).replaceAll("\r", "").replaceAll("\n", "");
//            System.out.println("Research center symkey: " + symkeys);
//            byte[] researchPubKeyEnc = crypto.bobPubKeyEnc(researchkeypair);
//            // send researchPubKeyEnc to mobile app
//
//            //Mobile app
//            KeyAgreement mobileSymkeyagreement = crypto.aliceKeyAgreementFin(researchPubKeyEnc,mobileKpairKA);
//            byte[] mobileSharedSecret = mobileSymkeyagreement.generateSecret();
//            SecretKeySpec mobileSymkeyspec = crypto.generateSymmtericKey(mobileSharedSecret,32);
//            String symkeystr = Base64.getEncoder().encodeToString(mobileSymkeyspec.getEncoded());
//            System.out.println("Mobile app symkey: " + symkeystr);
//            Log.d("LOGIC_OF_RDS ", "Mobile app symkey: " + symkeystr);
//        }
//        catch(Exception e){
//            Log.e("diffieHellman", e.getMessage());
//        }
//    }

    //this is the method for retrieving the certificate data
    private String getUserCertificateInfo(String username) throws IOException, JSONException {
        url = new HttpUrl.Builder()
                .scheme("http")
                .host("interoperate-ejbca-service.euprojects.net")
                .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit;
        ResearchStudyService service;

        retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(this.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ResearchStudyService.class);

        //making the request
        final Response<ResponseBody> responseBody = service.getUserCertificateInfo(new CertificateDataRequest(username)).execute();
        //check if the request is not successful
        Log.d("LOGIC_OF_RDS", String.valueOf(responseBody));
        if(!responseBody.isSuccessful()){
            Log.d("LOGIC_OF_RDS",String.valueOf(responseBody.code()));
        }
        if(responseBody.body() == null){
            Log.d("LOGIC_OF_RDS","Response body is null!");
        }
        String json = responseBody.body().string();
        JSONArray jsonarray = new JSONArray(json);
        //get the data inside the json array
        String ca_response = jsonarray.getString(0);
        //convert the response to a json object
        JSONObject ca_response_json = new JSONObject(ca_response);
        //JSONArray entry1 = rdd_json.getJSONArray("entry");
        String certificateData = ca_response_json.getString("certificateData");
        Log.d("LOGIC_OF_RDS","CERTIFICATE DATA RETRIEVED: "+certificateData);
        return certificateData;
    }

}
