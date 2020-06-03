package com.easyfitness.DAO;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.DAO.cardio.DAOOldCardio;
import com.easyfitness.DAO.record.DAOCardio;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.UnitConverter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


// Uses http://javacsv.sourceforge.net/com/csvreader/CsvReader.html //
public class CVSManager {

    static private String TABLE_HEAD = "table";
    static private String ID_HEAD = "id";

    private Context mContext = null;

    public CVSManager(Context pContext) {
        mContext = pContext;
    }

    public boolean exportDatabase(Profile pProfile) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        /**First of all we check if the external storage of the device is available for writing.
         * Remember that the external storage is not necessarily the sd card. Very often it is
         * the device storage.
         */
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s_", Locale.getDefault());
            Date date = new Date();

            //We use the FastNFitness directory for saving our .csv file.
            File exportDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/export/" + dateFormat.format(date) + pProfile.getName());
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            PrintWriter printWriter = null;
            try {
                exportBodyMeasures(exportDir, pProfile);
                exportRecords(exportDir, pProfile);
                exportExercise(exportDir, pProfile);
                exportBodyParts(exportDir, pProfile);
            } catch (Exception e) {
                //if there are any exceptions, return false
                e.printStackTrace();
                return false;
            } finally {
                if (printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            return true;
        }
    }

    private boolean exportRecords(File exportDir, Profile pProfile) {
        try {
            // FONTE
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
            Date date = new Date();

            CsvWriter csvOutputFonte = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Records_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));

            /**This is our database connector class that reads the data from the database.
             * The code of this class is omitted for brevity.
             */
            DAORecord dbc = new DAORecord(mContext);
            dbc.open();

            /**Let's read the first table of the database.
             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
             * containing all records of the table (all fields).
             * The code of this class is omitted for brevity.
             */
            List<Record> records = null;
            Cursor cursor = dbc.getAllRecordsByProfile(pProfile);
            records = dbc.fromCursorToList(cursor);


            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutputFonte.write(TABLE_HEAD);
            csvOutputFonte.write(ID_HEAD);
            csvOutputFonte.write(DAORecord.DATE);
            csvOutputFonte.write(DAORecord.TIME);
            csvOutputFonte.write(DAORecord.EXERCISE);
            csvOutputFonte.write(DAORecord.EXERCISE_TYPE);
            csvOutputFonte.write(DAORecord.PROFILE_KEY);
            csvOutputFonte.write(DAORecord.SETS);
            csvOutputFonte.write(DAORecord.REPS);
            csvOutputFonte.write(DAORecord.WEIGHT);
            csvOutputFonte.write(DAORecord.WEIGHT_UNIT);
            csvOutputFonte.write(DAORecord.SECONDS);
            csvOutputFonte.write(DAORecord.DISTANCE);
            csvOutputFonte.write(DAORecord.DISTANCE_UNIT);
            csvOutputFonte.write(DAORecord.DURATION);
            csvOutputFonte.write(DAORecord.NOTES);
            csvOutputFonte.write(DAORecord.RECORD_TYPE);
            csvOutputFonte.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutputFonte.write(DAORecord.TABLE_NAME);
                csvOutputFonte.write(Long.toString(records.get(i).getId()));

                Date dateRecord = records.get(i).getDate();

                csvOutputFonte.write(DateConverter.dateToDBDateStr(dateRecord));
                csvOutputFonte.write(records.get(i).getTime());
                csvOutputFonte.write(records.get(i).getExercise());
                csvOutputFonte.write(Integer.toString(ExerciseType.STRENGTH.ordinal()));
                csvOutputFonte.write(Long.toString(records.get(i).getProfileId()));
                csvOutputFonte.write(Integer.toString(records.get(i).getSets()));
                csvOutputFonte.write(Integer.toString(records.get(i).getReps()));
                csvOutputFonte.write(Float.toString(records.get(i).getWeight()));
                csvOutputFonte.write(Integer.toString(records.get(i).getWeightUnit().ordinal()));
                csvOutputFonte.write(Integer.toString(records.get(i).getSecond()));
                csvOutputFonte.write(Float.toString(records.get(i).getDistance()));
                csvOutputFonte.write(Integer.toString(records.get(i).getDistanceUnit().ordinal()));
                csvOutputFonte.write(Long.toString(records.get(i).getDuration()));
                if (records.get(i).getNote() == null) csvOutputFonte.write("");
                else csvOutputFonte.write(records.get(i).getNote());
                csvOutputFonte.write(Integer.toString(records.get(i).getRecordType().ordinal()));
                csvOutputFonte.endRecord();
            }
            csvOutputFonte.close();
            dbc.closeAll();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    private boolean exportBodyMeasures(File exportDir, Profile pProfile) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
            Date date = new Date();

            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_BodyMeasures_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));
            DAOBodyMeasure daoBodyMeasure = new DAOBodyMeasure(mContext);
            daoBodyMeasure.open();

            DAOBodyPart daoBodyPart = new DAOBodyPart(mContext);

            List<BodyMeasure> bodyMeasures;
            bodyMeasures = daoBodyMeasure.getBodyMeasuresList(pProfile);

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(ID_HEAD);
            cvsOutput.write(DAOBodyMeasure.DATE);
            cvsOutput.write("bodypart_label");
            cvsOutput.write(DAOBodyMeasure.MEASURE);
            cvsOutput.write(DAOBodyMeasure.PROFIL_KEY);
            cvsOutput.endRecord();

            for (int i = 0; i < bodyMeasures.size(); i++) {
                cvsOutput.write(DAOBodyMeasure.TABLE_NAME);
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getId()));
                Date dateRecord = bodyMeasures.get(i).getDate();
                cvsOutput.write(DateConverter.dateToDBDateStr(dateRecord));
                BodyPart bp = daoBodyPart.getBodyPart(bodyMeasures.get(i).getBodyPartID());
                cvsOutput.write(bp.getName(mContext)); // Write the full name of the BodyPart
                cvsOutput.write(Float.toString(bodyMeasures.get(i).getBodyMeasure()));
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getProfileID()));

                cvsOutput.endRecord();
            }
            cvsOutput.close();
            daoBodyMeasure.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    private boolean exportBodyParts(File exportDir, Profile pProfile) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
            Date date = new Date();

            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_CustomBodyPart_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));
            DAOBodyPart daoBodyPart = new DAOBodyPart(mContext);
            daoBodyPart.open();


            List<BodyPart> bodyParts;
            bodyParts = daoBodyPart.getList();

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(DAOBodyPart.KEY);
            cvsOutput.write(DAOBodyPart.CUSTOM_NAME);
            cvsOutput.write(DAOBodyPart.CUSTOM_PICTURE);
            cvsOutput.endRecord();

            for (BodyPart bp : bodyParts ) {
                if (bp.getBodyPartResKey()==-1) { // Only custom BodyPart are exported
                    cvsOutput.write(DAOBodyMeasure.TABLE_NAME);
                    cvsOutput.write(Long.toString(bp.getId()));
                    cvsOutput.write(bp.getName(mContext));
                    cvsOutput.write(bp.getCustomPicture());
                    cvsOutput.endRecord();
                }
            }
            cvsOutput.close();
            daoBodyPart.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    private boolean exportExercise(File exportDir, Profile pProfile) {
        try {
            // FONTE
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
            Date date = new Date();

            CsvWriter csvOutput = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Exercises_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));

            /**This is our database connector class that reads the data from the database.
             * The code of this class is omitted for brevity.
             */
            DAOMachine dbcMachine = new DAOMachine(mContext);
            dbcMachine.open();

            /**Let's read the first table of the database.
             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
             * containing all records of the table (all fields).
             * The code of this class is omitted for brevity.
             */
            List<Machine> records = null;
            records = dbcMachine.getAllMachinesArray();

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutput.write(TABLE_HEAD);
            csvOutput.write(ID_HEAD);
            csvOutput.write(DAOMachine.NAME);
            csvOutput.write(DAOMachine.DESCRIPTION);
            csvOutput.write(DAOMachine.TYPE);
            csvOutput.write(DAOMachine.BODYPARTS);
            csvOutput.write(DAOMachine.FAVORITES);
            //csvOutput.write(DAOMachine.PICTURE_RES);
            csvOutput.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutput.write(DAOMachine.TABLE_NAME);
                csvOutput.write(Long.toString(records.get(i).getId()));
                csvOutput.write(records.get(i).getName());
                csvOutput.write(records.get(i).getDescription());
                csvOutput.write(Integer.toString(records.get(i).getType().ordinal()));
                csvOutput.write(records.get(i).getBodyParts());
                csvOutput.write(Boolean.toString(records.get(i).getFavorite()));
                //write the record in the .csv file
                csvOutput.endRecord();
            }
            csvOutput.close();
            dbcMachine.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    public boolean importDatabase(String file, Profile pProfile) {

        boolean ret = true;

        try {
            CsvReader csvRecords = new CsvReader(file, ',', Charset.forName("UTF-8"));

            csvRecords.readHeaders();

            ArrayList<Record> recordsList = new ArrayList<>() ;

            DAOMachine dbcMachine = new DAOMachine(mContext);

            while (csvRecords.readRecord()) {
                switch (csvRecords.get(TABLE_HEAD)) {
                    case DAORecord.TABLE_NAME: {
                        Date date;
                        date = DateConverter.DBDateStrToDate(csvRecords.get(DAORecord.DATE));
                        String time = csvRecords.get(DAORecord.TIME);
                        String exercise = csvRecords.get(DAORecord.EXERCISE);
                        if ( dbcMachine.getMachine(exercise) != null ) {
                            long exerciseId = dbcMachine.getMachine(exercise).getId();
                            ExerciseType exerciseType = dbcMachine.getMachine(exercise).getType();

                            float poids = Float.valueOf(csvRecords.get(DAORecord.WEIGHT));
                            int repetition = Integer.valueOf(csvRecords.get(DAORecord.REPS));
                            int serie = Integer.valueOf(csvRecords.get(DAORecord.SETS));
                            WeightUnit unit = WeightUnit.KG;
                            if (!csvRecords.get(DAORecord.WEIGHT_UNIT).isEmpty()) {
                                unit = WeightUnit.fromInteger(Integer.valueOf(csvRecords.get(DAORecord.WEIGHT_UNIT)));
                            }
                            int second = Integer.valueOf(csvRecords.get(DAORecord.SECONDS));
                            float distance = Float.valueOf(csvRecords.get(DAORecord.DISTANCE));
                            int duration = Integer.valueOf(csvRecords.get(DAORecord.DURATION));
                            DistanceUnit distance_unit = DistanceUnit.KM;
                            if (!csvRecords.get(DAORecord.DISTANCE_UNIT).isEmpty()) {
                                distance_unit = DistanceUnit.fromInteger(Integer.valueOf(csvRecords.get(DAORecord.DISTANCE_UNIT)));
                            }
                            String notes = csvRecords.get(DAORecord.NOTES);

                            Record record = new Record(date, time, exercise, exerciseId, pProfile.getId(), serie, repetition, poids, unit, second, distance, distance_unit, duration, notes, exerciseType, -1);
                            recordsList.add(record);
                        } else {
                            return false;
                        }

                        break;
                    }
                    case DAOOldCardio.TABLE_NAME: {
                        DAOCardio dbcCardio = new DAOCardio(mContext);
                        dbcCardio.open();
                        Date date;

                        date =DateConverter.DBDateStrToDate(csvRecords.get(DAOCardio.DATE));

                        String exercice = csvRecords.get(DAOOldCardio.EXERCICE);
                        float distance = Float.valueOf(csvRecords.get(DAOOldCardio.DISTANCE));
                        int duration = Integer.valueOf(csvRecords.get(DAOOldCardio.DURATION));
                        dbcCardio.addCardioRecord(date, "", exercice, distance, duration, pProfile.getId(), DistanceUnit.KM, -1);
                        dbcCardio.close();

                        break;
                    }
                    case DAOProfileWeight.TABLE_NAME: {
                        DAOBodyMeasure dbcWeight = new DAOBodyMeasure(mContext);
                        dbcWeight.open();
                        Date date;
                        date = DateConverter.DBDateStrToDate(csvRecords.get(DAOProfileWeight.DATE));

                        float poids = Float.valueOf(csvRecords.get(DAOProfileWeight.POIDS));
                        dbcWeight.addBodyMeasure(date, BodyPartExtensions.WEIGHT, poids, pProfile.getId());

                        break;
                    }
                    case DAOBodyMeasure.TABLE_NAME: {
                        DAOBodyMeasure dbcBodyMeasure = new DAOBodyMeasure(mContext);
                        dbcBodyMeasure.open();
                        Date date;
                        date = DateConverter.DBDateStrToDate(csvRecords.get(DAOBodyMeasure.DATE));
                        String bodyPartName = csvRecords.get("bodypart_label");
                        DAOBodyPart dbcBodyPart = new DAOBodyPart(mContext);
                        dbcBodyPart.open();
                        List<BodyPart> bodyParts;
                        bodyParts = dbcBodyPart.getList();
                        for (BodyPart bp : bodyParts) {
                            if (bp.getName(mContext).equals(bodyPartName)) {
                                float measure = Float.valueOf(csvRecords.get(DAOBodyMeasure.MEASURE));
                                dbcBodyMeasure.addBodyMeasure(date, bp.getId(), measure, pProfile.getId());
                                dbcBodyPart.close();
                                break;
                            }
                        }
                        break;
                    }
                    case DAOBodyPart.TABLE_NAME: {
                        DAOBodyPart dbcBodyPart = new DAOBodyPart(mContext);
                        dbcBodyPart.open();
                        int bodyPartId = -1;
                        String customName = csvRecords.get(DAOBodyPart.CUSTOM_NAME);
                        String customPicture = csvRecords.get(DAOBodyPart.CUSTOM_PICTURE);
                        dbcBodyPart.add(bodyPartId, customName, customPicture, 0, BodyPartExtensions.TYPE_MUSCLE);
                        break;
                    }
                    case DAOProfile.TABLE_NAME:
                        // TODO : import profiles
                        break;
                    case DAOMachine.TABLE_NAME:
                        DAOMachine dbc = new DAOMachine(mContext);
                        String name = csvRecords.get(DAOMachine.NAME);
                        String description = csvRecords.get(DAOMachine.DESCRIPTION);
                        ExerciseType type = ExerciseType.fromInteger(Integer.valueOf(csvRecords.get(DAOMachine.TYPE)));
                        boolean favorite = Boolean.valueOf(csvRecords.get(DAOMachine.FAVORITES));
                        String bodyParts = csvRecords.get(DAOMachine.BODYPARTS);

                        // Check if this machine doesn't exist
                        if (dbc.getMachine(name) == null) {
                            dbc.addMachine(name, description, type, "", favorite, bodyParts);
                        } else {
                            Machine m = dbc.getMachine(name);
                            m.setDescription(description);
                            m.setFavorite(favorite);
                            m.setBodyParts(bodyParts);
                            dbc.updateMachine(m);
                        }
                        break;
                }
            }

            csvRecords.close();

            // In case of success
            DAORecord daoRecord = new DAORecord(mContext);
            daoRecord.addList(recordsList);

        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }

        return ret;
    }
}
