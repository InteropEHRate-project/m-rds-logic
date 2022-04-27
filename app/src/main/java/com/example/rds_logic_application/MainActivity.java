package com.example.rds_logic_application;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.rds_simple.controller.ResearchStudyController;
import com.example.rds_simple.model.RDSResearchStudy;
import com.example.rds_simple.service.RDSClientService;
import com.example.rds_simple.utils.AppPreferenceService;
import com.example.rds_simple.utils.DBadapter;
import com.google.gson.Gson;

import org.hl7.fhir.r4.model.DataRequirement;
import org.hl7.fhir.r4.model.Extension;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import eu.interopehrate.rdsanoni.PrivacyMechanisms;
import okhttp3.HttpUrl;

import android.util.Log;

import eu.interopehrate.rdsanoni.RDSAnonI;

public class MainActivity extends AppCompatActivity {
    DBadapter helper;
    private final HttpUrl url = new HttpUrl.Builder()
            .scheme("http")
            .host("213.249.46.208")
            .port(443).build();

    RDSClientService clientService = RDSClientService.getInstance(url, new RDSClientInterfaceImplementation());
    ResearchStudyController controller = new ResearchStudyController(url);

    private Button btnSetOptInTrue, btnGetOptIn, btnCheckNewStudies, btnEnrollInStudy, btnWithdrawFromStudy, btnSendHealthData;
    private TextView text;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppPreferenceService.INSTANCE.init(this);

        //Almost all network actions should be performed in a thread or
        //as an asynchronous task. However, I remove this restriction and override the default behavior
        //for testing purposes of the rds-logic library.
        StrictMode.ThreadPolicy gfgPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(gfgPolicy);

        //this is the helper for the db
        helper = new DBadapter(this);

        text = findViewById(R.id.text);

        btnSetOptInTrue = findViewById(R.id.setOptInTrue);
        btnSetOptInTrue.setOnClickListener(v -> MainActivity.this.setOptInTrue());

        btnGetOptIn = findViewById(R.id.getOptIn);
        btnGetOptIn.setOnClickListener(v -> MainActivity.this.getOptIn());

        btnCheckNewStudies = findViewById(R.id.checkNewStudies);
        btnCheckNewStudies.setOnClickListener(v -> {
            try {
                MainActivity.this.checkNewStudies();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        btnEnrollInStudy = findViewById(R.id.enrollInStudy);
        btnEnrollInStudy.setOnClickListener(v -> {

            try {
                MainActivity.this.enrollInStudy();
            } catch (IOException | JSONException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        });

        btnWithdrawFromStudy = findViewById(R.id.withdrawFromStudy);
        btnWithdrawFromStudy.setOnClickListener(v -> {

            try {
                MainActivity.this.withdraw();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        btnSendHealthData = findViewById(R.id.sendHealthData);
        btnSendHealthData.setOnClickListener(v -> {

            try {
                MainActivity.this.sendHealthData();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });


    }

    private void setOptInTrue() {
        clientService.setOptInStatus(true);
    }

    private void getOptIn() {
       Boolean preference = clientService.getOptInStatus();
       text.setText(preference.toString());
    }

    private void checkNewStudies() throws IOException, JSONException {
        Log.d("LOGIC_OF_RDS","ENTERED NEW STUDIES");
        List<RDSResearchStudy> availableStudyList = new ArrayList<>();
        List<DataRequirement> dataRequirements = new ArrayList<>();

        availableStudyList = clientService.checkNewStudies();
        //print the size of availableStudyList (i.e., the number of available researches)
        Log.d("LOGIC_OF_RDS", String.valueOf(availableStudyList.size()));
        //print the available lists in logcat
        for(int i=0; i<availableStudyList.size(); i++)
        {
            if (availableStudyList.get(i).getResearchStudy().hasExtension("http://interopehrate.eu/fhir/StructureDefinition/DataSetDefinitionExtension-IEHR")) {
                Extension extension = availableStudyList.get(i).getResearchStudy().getExtensionByUrl("http://interopehrate.eu/fhir/StructureDefinition/DataSetDefinitionExtension-IEHR");
                List<Extension> dataRequirement = extension.getExtensionsByUrl("DataRequirement");
                for(Extension ext : dataRequirement) {
                    if (ext.getValue() instanceof DataRequirement)
                        dataRequirements.add((DataRequirement) ext.getValue());
                }

            }
            Log.d("LOGIC_OF_RDS","ID OF STUDY: "+String.valueOf(availableStudyList.get(i).getResearchStudy().getId()));
            //Log.d("LOGIC_OF_RDS",availableStudyList.get(i).getResearchStudy().getId());
            //Log.d("LOGIC_OF_RDS",availableStudyList.get(i).getResearchStudy().getSite().get(0).getReference());
            //check if the study needs pseudonymization or anonymization
            //Log.d("LOGIC_OF_RDS", String.valueOf(availableStudyList.get(i).getResearchStudy().getExtensionByUrl("http://interopehrate.eu/fhir/StructureDefinition/AnonymizationExtension-IEHR").getValue().getNamedProperty("code").getValues()));
            //Log.d("LOGIC_OF_RDS", ReflectionToStringBuilder.toString(availableStudyList.get(i),new RecursiveToStringStyle()));
            //dummy insert of research study in db
//            Gson gson = new Gson();
//            String researchStudyObjectToStore = gson.toJson(availableStudyList.get(i), RDSResearchStudy.class);
//            Log.d("LOGIC_OF_RDS",researchStudyObjectToStore);
//            RDSResearchStudy researchStudyObject = gson.fromJson(researchStudyObjectToStore, RDSResearchStudy.class);
//            Log.d("LOGIC_OF_RDS", "Research study object is: "+researchStudyObject);
            helper.insertResearchStudy("researchStudy","s456","320993","pseudonymization","exitCriteria","dataRequirements","researchStudyConsent");
        }
        Log.d("LOGIC_OF_RDS", String.valueOf(dataRequirements));
        Log.d("LOGIC_OF_RDS",helper.getResearchStudies());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enrollInStudy() throws IOException, JSONException, ExecutionException, InterruptedException {
        //store the alias.dat file (this is done for testing purposes in the rds-logic lib) -> it has to be done for the keystore.p12 file as well
        try {
            InputStream in = getAssets().open("alias.dat");
            OutputStream out = null;

            try {
                out = new FileOutputStream("/data/user/0/com.example.rds_logic_application/files/alias");
                byte[] buf = new byte[1024];
                int len;
                while((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                // Ensure that the InputStreams are closed even if there's an exception.
                try {
                    if ( out != null ) {
                        out.close();
                    }

                    // If you want to close the "in" InputStream yourself then remove this
                    // from here but ensure that you close it yourself eventually.
                    in.close();
                }
                catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }

        //store the keystore.p12
        try {
            InputStream in = getAssets().open("keystore.p12");
            OutputStream out = null;

            try {
                out = new FileOutputStream("/data/user/0/com.example.rds_logic_application/files/keystore.p12");
                byte[] buf = new byte[1024];
                int len;
                while((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                // Ensure that the InputStreams are closed even if there's an exception.
                try {
                    if ( out != null ) {
                        out.close();
                    }

                    // If you want to close the "in" InputStream yourself then remove this
                    // from here but ensure that you close it yourself eventually.
                    in.close();
                }
                catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }

        Log.d("LOGIC_OF_RDS","ENTERED ENROLL IN STUDY");
        //get the consent to show it to the citizen
        RDSClientService getConsentTest = new RDSClientService();
        Log.d("LOGIC_OF_RDS",getConsentTest.getConsent());
        //Call the method responsible for enrolling in study
        RDSClientService enrollInStudyTest = new RDSClientService();
        enrollInStudyTest.enrollInStudy("213.249.46.208","s456","12","pseudo-identity","enrollment_criteria",123, this.getApplicationContext());
        PrivacyMechanisms pm = PrivacyMechanisms.PrivacyMechanisms();
        //pm.setPseudo("pseudo-identity", "World42944300d358bf5b98b935482ba0a58bf0d7442e49e2a29e86322b6d92e0d5f76", "123", this.getApplicationContext());
        //get the pseudo for testing
        String get_pseudo = pm.getPseudo("s456", this.getApplicationContext());
        if (!get_pseudo.equals("")) {
            Log.d("LOGIC_OF_RDS","THE STORED PSEUDO IS: "+get_pseudo);
        }
        else {
            Log.d("LOGIC_OF_RDS","NOT FOUND");
        }
        //check whether there is a countersigned consent for a study with a specific id (i.e., to check if the enrollment was successful)
        RDSClientService getCountersignedConsentTest = new RDSClientService();
        if(getCountersignedConsentTest.retrieveCountersignedConsent("s456",this.getApplicationContext())==false){
            Log.d("LOGIC_OF_RDS","Enrollment not successful");
        }
        else{Log.d("LOGIC_OF_RDS","Enrollment successful"); }
    }

    private void withdraw() throws Exception{
        //Call the method responsible for withdrawal from a study
        RDSClientService withdrawTest = new RDSClientService();
        if(withdrawTest.withdrawFromStudy("3208433257313","lala", "lala", this.getApplicationContext()))
        {
            Log.d("LOGIC_OF_RDS","Withdrawal was successful");
        }
        else
        {
            Log.d("LOGIC_OF_RDS","Withdrawal was not successful");
        }
    }

    private void sendHealthData() throws Exception{
        //Call the method responsible for sending health data
        RDSClientService sendDataTest = new RDSClientService();
        //test the calculate date method
        //the method needs the studyID, the date from which to start calculating the dates, the period (in number of days, eg. if its per week
        //then it should be 7) and the application context
        sendDataTest.calculateDates("s456","22-02-2022",7,this.getApplicationContext());
        //test checkDate method (if it returns true then the health data can be sent (THE DATE MUST BE IN dd-MM-yyyy format)
        if(sendDataTest.checkDate("s456","01-03-2022",this.getApplicationContext()))
        {
            Log.d("LOGIC_OF_RDS","DATA CAN BE SENT TODAY");
            //so we call the sendHealthData
            if(sendDataTest.sendHealthData("213.249.46.208","s456", "{\n" +
                    "  \"resourceType\" : \"Patient\",\n" +
                    "  \"id\" : \"3466\",\n" +
                    "  \"language\" : \"it-IT\",\n" +
                    "  \"text\" : {\n" +
                    "    \"status\" : \"generated\",\n" +
                    "    \"div\" : \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\" xml:lang=\\\"it-IT\\\" lang=\\\"it-IT\\\"><p><b>Generated Narrative</b></p><p><b>identifier</b>: id: Patient/MS01</p><p><b>name</b>: Markus Smith </p><p><b>gender</b>: male</p><p><b>birthDate</b>: 2013-12-05</p><p><b>address</b>: Rome IT (HOME)</p><p><b>generalPractitioner</b>: <a href=\\\"Organization-34432.html\\\">Generated Summary: language: it-IT; id: Organization/FTGM01; name: Fondazione Gabriele Monasterio</a></p></div>\"\n" +
                    "  },\n" +
                    "  \"identifier\" : [\n" +
                    "    {\n" +
                    "      \"system\" : \"http://interopEHRate.eu/fhir-resource/\",\n" +
                    "      \"value\" : \"Patient/MS01\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"name\" : [\n" +
                    "    {\n" +
                    "      \"family\" : \"Smith\",\n" +
                    "      \"given\" : [\n" +
                    "        \"Markus\"\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"gender\" : \"male\",\n" +
                    "  \"birthDate\" : \"2013-12-05\",\n" +
                    "  \"address\" : [\n" +
                    "    {\n" +
                    "      \"use\" : \"home\",\n" +
                    "      \"type\" : \"physical\",\n" +
                    "      \"city\" : \"Rome\",\n" +
                    "      \"country\" : \"IT\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"generalPractitioner\" : [\n" +
                    "    {\n" +
                    "      \"reference\" : \"Organization/34432\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}", this.getApplicationContext()).equals("200"))
            {
                Log.d("LOGIC_OF_RDS","Data were sent successfully");
            }
//        else if(sendDataTest.sendHealthData("s788", "citizen_pseudo", null, this.getApplicationContext()).equals("EXIT"))
//        {
//            Log.d("LOGIC_OF_RDS","EXIT CRITERIA WERE MET, CITIZEN EXITED THE STUDY.");
//        }
            else
            {
                Log.d("LOGIC_OF_RDS","Data were not sent");
            }
        }
        else
        {
            Log.d("LOGIC_OF_RDS","DATA CANNOT BE SENT TODAY");
        }
    }


}

