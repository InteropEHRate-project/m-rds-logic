package com.example.rds_simple.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


//this is a class for creating the SQLite database to store research studies (in corresponding table)
// and the consent that is signed from the RRC (in corresponding table)
//Also, the methods for interacting with the db are implemented.
public class DBadapter {
    dbHelper dbhelper;
    public  DBadapter(Context context)
    {
        dbhelper = new dbHelper(context);
    }

    public long insertResearchStudy(String researchStudy, String researchStudyID, String rrc, String pseudonymizationType, String exitCriteria, String dataRequirements, String researchStudyConsent)
    {
        SQLiteDatabase dbb = dbhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(dbHelper.RESEARCH_STUDY, researchStudy);
        contentValues.put(dbHelper.RESEARCH_STUDY_ID, researchStudyID);
        contentValues.put(dbHelper.RRC, rrc);
        contentValues.put(dbHelper.PSEUDONYMIZATION_TYPE, pseudonymizationType);
        contentValues.put(dbHelper.EXIT_CRITERIA, exitCriteria);
        contentValues.put(dbHelper.DATA_REQUIREMENTS, dataRequirements);
        contentValues.put(dbHelper.RESEARCH_STUDY_CONSENT, researchStudyConsent);
        long id = dbb.insert(dbHelper.RESEARCH_STUDIES_TABLE_NAME, null , contentValues);
        return id;
    }

    //method to insert selected rcc from the citizen into the database
    //actually it's an update query. The primary key is needed (UID)
    public long insertRRC(int _id, int rrc)
    {
        SQLiteDatabase dbb = dbhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(dbhelper.RRC,rrc);
        String[] whereArgs= {String.valueOf(_id)};
        int count =dbb.update(dbhelper.RESEARCH_STUDIES_TABLE_NAME,contentValues, dbhelper.UID+" = ?",whereArgs );
        return count;
    }

    //method to store the consent received by the rrc (countersigned) into the database
    //actually it's an update query. The primary key is needed (UID)
    public long insertConsent(String researchStudyID, String consent)
    {
        SQLiteDatabase dbb = dbhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(dbhelper.RESEARCH_STUDY_CONSENT,consent);
        String[] whereArgs= {String.valueOf(researchStudyID)};
        int count =dbb.update(dbhelper.RESEARCH_STUDIES_TABLE_NAME,contentValues, dbhelper.RESEARCH_STUDY_ID+" = ?",whereArgs );
        return count;
    }

    //method to retrieve all the research studies that are stored inside the mobile phone
    public String getResearchStudies()
    {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String[] columns = {dbHelper.UID,dbHelper.RESEARCH_STUDY,dbHelper.RESEARCH_STUDY_ID,dbHelper.RRC,dbHelper.PSEUDONYMIZATION_TYPE, dbHelper.EXIT_CRITERIA, dbHelper.DATA_REQUIREMENTS,dbHelper.RESEARCH_STUDY_CONSENT};
        Cursor cursor =db.query(dbHelper.RESEARCH_STUDIES_TABLE_NAME,columns,null,null,null,null,null);
        StringBuffer buffer= new StringBuffer();
        while (cursor.moveToNext())
        {
            int cid =cursor.getInt(cursor.getColumnIndex(dbHelper.UID));
            String researchStudy =cursor.getString(cursor.getColumnIndex(dbHelper.RESEARCH_STUDY));
            String  researchStudyID =cursor.getString(cursor.getColumnIndex(dbHelper.RESEARCH_STUDY_ID));
            String  rrc =cursor.getString(cursor.getColumnIndex(dbHelper.RRC));
            String  pseudonymization_type =cursor.getString(cursor.getColumnIndex(dbHelper.PSEUDONYMIZATION_TYPE));
            String  exit_criteria =cursor.getString(cursor.getColumnIndex(dbHelper.EXIT_CRITERIA));
            String  data_requirements =cursor.getString(cursor.getColumnIndex(dbHelper.DATA_REQUIREMENTS));
            String researchStudyConsent = cursor.getString(cursor.getColumnIndex(dbHelper.RESEARCH_STUDY_CONSENT));
            buffer.append(cid+ "   " + researchStudy + "   " + researchStudyID +"   " + rrc +" " + pseudonymization_type +" " + exit_criteria +" " + data_requirements +" " + researchStudyConsent +"\n");
        }
        return buffer.toString();
    }

    //method to check if the countersigned consent is saved (i.e., the enrollment was successful)
    public boolean getCountersignedConsent(String researchStudyID)
    {
        boolean counterSigned = false;
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String[] columns = {dbHelper.UID,dbHelper.RESEARCH_STUDY_ID,dbHelper.RESEARCH_STUDY_CONSENT};
        String[] whereArgs= {String.valueOf(researchStudyID)};
        Cursor cursor =db.query(dbHelper.RESEARCH_STUDIES_TABLE_NAME,columns,dbHelper.RESEARCH_STUDY_ID+ "=?",whereArgs,null,null,null);
        StringBuffer buffer= new StringBuffer();
        while (cursor.moveToNext())
        {
            int cid =cursor.getInt(cursor.getColumnIndex(dbHelper.UID));
            String  researchID =cursor.getString(cursor.getColumnIndex(dbHelper.RESEARCH_STUDY_ID));
            String researchStudyConsent = cursor.getString(cursor.getColumnIndex(dbHelper.RESEARCH_STUDY_CONSENT));
            //if statement to set the counterSigned variable to true (whether a specific study has a countersigned consent)
            if(researchStudyConsent!=null){
                counterSigned = true;
            }
        }
        return counterSigned;
    }

    //method to retrieve a research study that is stored inside the mobile phone based on its id
    public String getStudybyID(String researchStudyID)
    {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String[] columns = {dbHelper.RESEARCH_STUDY};
        String[] whereArgs= {String.valueOf(researchStudyID)};
        Cursor cursor =db.query(dbHelper.RESEARCH_STUDIES_TABLE_NAME,columns,dbHelper.RESEARCH_STUDY_ID+ "=?",whereArgs,null,null,null);
        StringBuffer buffer= new StringBuffer();
        String researchStudy = null;
        while (cursor.moveToNext())
        {
            researchStudy =cursor.getString(cursor.getColumnIndex(dbHelper.RESEARCH_STUDY));
        }
        return researchStudy;
    }

    //method to retrieve the exit criteria of a research study
    public String getExitCriteria(String researchStudyID)
    {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String[] columns = {dbHelper.EXIT_CRITERIA};
        String[] whereArgs= {String.valueOf(researchStudyID)};
        Cursor cursor =db.query(dbHelper.RESEARCH_STUDIES_TABLE_NAME,columns,dbHelper.RESEARCH_STUDY_ID+ "=?",whereArgs,null,null,null);
        StringBuffer buffer= new StringBuffer();
        String exitCriteria = null;
        while (cursor.moveToNext())
        {
            exitCriteria =cursor.getString(cursor.getColumnIndex(dbHelper.EXIT_CRITERIA));
        }
        return exitCriteria;
    }

    //method to retrieve the data requirements of a research study
    public String getDataRequirements(String researchStudyID)
    {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String[] columns = {dbHelper.DATA_REQUIREMENTS};
        String[] whereArgs= {String.valueOf(researchStudyID)};
        Cursor cursor =db.query(dbHelper.RESEARCH_STUDIES_TABLE_NAME,columns,dbHelper.RESEARCH_STUDY_ID+ "=?",whereArgs,null,null,null);
        StringBuffer buffer= new StringBuffer();
        String dataRequirements = null;
        while (cursor.moveToNext())
        {
            dataRequirements =cursor.getString(cursor.getColumnIndex(dbHelper.DATA_REQUIREMENTS));
        }
        return dataRequirements;
    }

    //method to retrieve the anonymization type of a research study
    public String getAnonymizationType(String researchStudyID)
    {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String[] columns = {dbHelper.PSEUDONYMIZATION_TYPE};
        String[] whereArgs= {String.valueOf(researchStudyID)};
        Cursor cursor =db.query(dbHelper.RESEARCH_STUDIES_TABLE_NAME,columns,dbHelper.RESEARCH_STUDY_ID+ "=?",whereArgs,null,null,null);
        StringBuffer buffer= new StringBuffer();
        String anonymizationType = null;
        while (cursor.moveToNext())
        {
            anonymizationType =cursor.getString(cursor.getColumnIndex(dbHelper.PSEUDONYMIZATION_TYPE));
        }
        return anonymizationType;
    }

    //method to store the dates that were calculated by the calculateDates method
    public long insertDates(String researchStudyID, String researchDates)
    {
        SQLiteDatabase dbb = dbhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(dbhelper.RESEARCH_STUDY_DATES,researchDates);
        String[] whereArgs= {String.valueOf(researchStudyID)};
        int count =dbb.update(dbhelper.RESEARCH_STUDIES_TABLE_NAME,contentValues, dbhelper.RESEARCH_STUDY_ID+" = ?",whereArgs );
        return count;
    }

    //method to find a given date in a study
    public boolean getResearchDate(String researchStudyID, String currentDate)
    {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String[] columns = {dbHelper.RESEARCH_STUDY_DATES};
        String[] whereArgs= {String.valueOf(researchStudyID)};
        Cursor cursor =db.query(dbHelper.RESEARCH_STUDIES_TABLE_NAME,columns,dbHelper.RESEARCH_STUDY_ID+ "=?",whereArgs,null,null,null);
        StringBuffer buffer= new StringBuffer();
        String researchDates = null;
        while (cursor.moveToNext())
        {
            researchDates =cursor.getString(cursor.getColumnIndex(dbHelper.RESEARCH_STUDY_DATES));
        }
        Log.d("LOGIC_OF_RDS","DATES OF STUDY: "+researchDates);
        if(researchDates.contains(currentDate))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

//    public  int delete(String uname)
//    {
//        SQLiteDatabase db = myhelper.getWritableDatabase();
//        String[] whereArgs ={uname};
//
//        int count =db.delete(myDbHelper.TABLE_NAME ,myDbHelper.NAME+" = ?",whereArgs);
//        return  count;
//    }

//    public int updateName(String oldName , String newName)
//    {
//        SQLiteDatabase db = myhelper.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(myDbHelper.ADATE,newName);
//        String[] whereArgs= {oldName};
//        int count =db.update(myDbHelper.TABLE_NAME,contentValues, myDbHelper.ADATE+" = ?",whereArgs );
//        return count;
//    }

    static class dbHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "rds_logic";    // Database Name
        private static final String RESEARCH_STUDIES_TABLE_NAME = "researchStudies";   // Table Name (for storing research studies)
        private static final int DATABASE_Version = 1;    // Database Version
        private static final String UID="_id";     // Column I (Primary Key)
        private static final String RESEARCH_STUDY = "ResearchStudy";    //Column II (the full research study)
        private static final String RESEARCH_STUDY_ID= "ResearchStudyID";    // Column III (the ID of the research study)
        private static final String RRC= "ReferenceResearchCenter";    // Column IV (the location that the citizen chose for the research study)
        private static final String PSEUDONYMIZATION_TYPE= "PseudonymizationType"; //The type of pseudonymization
        private static final String EXIT_CRITERIA= "ExitCriteria"; //The exit criteria of the study
        private static final String DATA_REQUIREMENTS= "DataRequirements"; //The data requirements (used for queryData)
        private static final String RESEARCH_STUDY_DATES = "ResearchStudyDates";//the dates when to send data
        private static final String RESEARCH_STUDY_CONSENT= "ResearchStudyConsent";    // Column V (the consent for the particular research study)
        private static final String CREATE_REASEATCH_STUDIES_TABLE = "CREATE TABLE "+RESEARCH_STUDIES_TABLE_NAME+
                " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+RESEARCH_STUDY+" BLOB ,"+ RESEARCH_STUDY_ID+" BLOB,"+ RRC+" BLOB,"+ RESEARCH_STUDY_CONSENT+" BLOB,"+ PSEUDONYMIZATION_TYPE+" BLOB,"+ RESEARCH_STUDY_DATES+" BLOB,"+EXIT_CRITERIA+" BLOB,"+DATA_REQUIREMENTS+" BLOB);";
        private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+RESEARCH_STUDIES_TABLE_NAME;
        private Context context;

        public dbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context=context;
        }

        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL(CREATE_REASEATCH_STUDIES_TABLE);
            } catch (Exception e) {
                //Message.message(context,""+e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                //Message.message(context,"OnUpgrade");
                db.execSQL(DROP_TABLE);
                onCreate(db);
            }catch (Exception e) {
                //Message.message(context,""+e);
            }
        }
    }
}
