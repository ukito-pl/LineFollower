package com.example.ukito.linefollower;

import android.os.Environment;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by ukito on 15.03.2017.
 */

public class DataManager {

    public float[][] time;
    public float[][] value;
    public String currentData;

    public DataManager(){
        time = new float[3][2000];
        value = new float[3][2000];
        //generateData();

    }

    public void generateData(){
        for (int i = 0; i < 2000; i++){
            /*
            time[0][i] = i*0.1f;
            value[0][i] = 1-(float) Math.pow(2.71,-time[0][i]);
            time[1][i] = i*0.1f;
            value[1][i] = 0.75f*(1-(float) Math.pow(2.71,-3*time[1][i]));
            time[2][i] = i*0.1f;
            value[2][i] =1 - ( (float)Math.pow(2.71,-2*time[2][i]) * (float)Math.cos(time[2][i]) - 2*(float)Math.pow(2.71,-2*time[2][i]) * (float)Math.sin(time[2][i])) ;

            time[0][i] = i*0.1f;
            value[0][i] = 0;
            time[1][i] = i*0.1f;
            value[1][i] = 0;
            time[2][i] = i*0.1f;
            value[2][i] =0;*/
        }
    }

    public LineGraphSeries<DataPoint> getLineDataSeries(int dataNumber){

        DataPoint[] dataPoint;
        dataPoint = new DataPoint[2000];
        for(int i =0; i <2000;i++){
            dataPoint[i] = new DataPoint( time[dataNumber][i],value[dataNumber][i]);
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);
        return series;
    }

    public void saveData(MainActivity ma,int i){
        try {
            File dir=new File( Environment.getExternalStorageDirectory(), "DanePomiarowe");
            if(!dir.exists()){
                dir.mkdir();
            }
            Calendar cal = Calendar.getInstance();
            String nazwa = "/Dane "+ Integer.toString(i+1) + cal.getTime().toString() + ".csv";

            CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath()+ nazwa), '\t');



            for (int j = 0; j < time[i].length; j++) {
                String[] entries = (Float.toString(time[i][j]) + "#" + Float.toString(value[i][j])).split("#");
                writer.writeNext(entries);
            }
            writer.close();
            Utils.toast(ma.getApplicationContext(), "Zapisano");
        }catch(java.io.IOException e){
            Utils.toast(ma.getApplicationContext(), "Nie można zapisać");
        }

    }

    public void addData(String dataFrame){

    }

    public void receiveData(String data){
        String remData = getDataFrame(data);
        String dataFrame = data;
    }

    public String getDataFrame(String data){
        String dataFrame = "";
        String remData = "";
        int copy = 0;
        int j = 0;
        int i = 0;
        boolean endOfFrame = false;
        for (; (i<data.length() || !endOfFrame);i++){
            if (data.charAt(i) == '#' && copy == 0){	//if starting # is recognized
                copy = 1;
                i++;
            }
            if (data.charAt(i) == '*' && copy == 1){	//if ending * is recognized
                copy = 0;
                endOfFrame = true;
                data = dataFrame;
            }
            if(copy == 1){
                dataFrame = dataFrame + data.charAt(i);

                j++;
            }
        }
        if(endOfFrame) {
            for (; i < data.length(); i++) {
                if (data.charAt(i) == '#' && copy == 0) {    //if starting # is recognized
                    copy = 1;
                    i++;
                }
                if (data.charAt(i) == '*' && copy == 1) {    //if ending * is recognized
                    copy = 0;
                    endOfFrame = true;
                }
                if (copy == 1) {
                    remData = remData + data.charAt(i);

                    j++;
                }
            }
            return remData;
        }else{
            remData = data;
            data = "";

            return remData;
        }

    }
}
