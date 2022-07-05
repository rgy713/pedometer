package com.dependa.pedometer.base;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class FileReadWrite {

    public void writeLine(ArrayList<HashMap<String, String>> stepData){

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "mypedometerdata.csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            for (HashMap<String, String> oneData : stepData) {
                String startTime = oneData.get(Constants.FLD_startTime);
                String stepCount = oneData.get(Constants.FLD_numberOfSteps);
                String distance = oneData.get(Constants.FLD_distance);
                String stepSize = oneData.get(Constants.FLD_step_size);
                String stime = oneData.get(Constants.FLD_stime);
                String etime = oneData.get(Constants.FLD_etime);
                String latitude = oneData.get(Constants.FLD_latitude);
                String longitude = oneData.get(Constants.FLD_longitude);
                String arrStr[] ={startTime,stepCount,distance,stepSize,stime,etime,latitude,longitude};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
        }
        catch(Exception sqlEx)
        {
        }
    }
}