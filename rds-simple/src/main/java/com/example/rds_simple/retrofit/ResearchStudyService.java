package com.example.rds_simple.retrofit;

import com.example.rds_simple.service.CertificateDataRequest;
import com.example.rds_simple.service.EnrollmentConsentRequest;
import com.example.rds_simple.service.ExitNotificationRequest;
import com.example.rds_simple.service.HealthDataRequest;

import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ResearchStudyService {

    @GET("open-studies")
    public Call<ResponseBody> fetchResearchStudy();

    @GET("retrievePseudoIdentity")
    public Call<ResponseBody> retrievePseudoIdentity(@Query("studyId") String studyId);

    //This is for retrieving the certificate data. It is included in the security lib but due to an error
    //with httpclient it is implemented here.
    @POST("getUserCertificateInfo")
    public Call<ResponseBody> getUserCertificateInfo(@Body CertificateDataRequest body);

    //This is for sending the enrollment consent to the RRC.
    //It should be a POST request - to be updated when Simone changes it on the RRC side
    //todo change GET to POST when ready
    @Headers("Content-Type: text/plain")
    @POST("sendEnrollmentConsent")
    public Call<ResponseBody> sendEnrollmentConsent(@Query("studyId") String studyId, @Body EnrollmentConsentRequest body);
    //public Call<ResponseBody> sendEnrollmentConsent(@Query("studyID") int studyID);
//    @FormUrlEncoded
//    @POST("sendEnrollmentConsent")
//    public Call<ResponseBody> sendEnrollmentConsent(@Query("studyID") String studyID,
//                                                    @Field("signedConsent") String signedConsent,
//                                                    @Field("citizenPseudo") String citizenPseudo,
//                                                    @Field("citizenCertificate") String citizenCertificate);

    //This is for sending the exit notification to the RRC
    @POST("sendExitNotification")
    public Call<ResponseBody> sendExitNotification(@Query("studyID") String studyID, @Body ExitNotificationRequest body);

    //This is for sending the citizen's health data to the RRC
    @Headers({"Content-Type: application/json","Accept: application/xml"})
    @POST("sendHealthData")
    public Call<ResponseBody> sendHealthData(@Query("studyId") String studyId, @Query("citizenPseudo") String citizenPseudo, @Query("pkey") String pkey, @Body HealthDataRequest body);

}
