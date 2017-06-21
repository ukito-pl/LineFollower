package com.example.ukito.linefollower;

import android.os.Environment;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Random;
import java.util.Vector;

/**
 * Created by ukito on 15.03.2017.
 */

public class DataManager {

    public Vector<Vector<Float>> time;
    public Vector<Vector<Float>> value;
    public String recData;
    public String remData = "";
    public int it1 = 0;
    public int it2 = 0;
    public int it3 = 0;
    public int length = 2000;
    public boolean stringToIntError = false;
    public MainActivity ma;
    public float minX = 0;
    public float maxX = 0;
    public float minY = 0;
    public float maxY = 0;

    public DataManager(MainActivity mainActivity){
        value = new Vector<>();
        time = new Vector<>();
        value.setSize(3);
        time.setSize(3);

        value.set(0,new Vector<Float>());
        value.set(1,new Vector<Float>());
        value.set(2,new Vector<Float>());
        time.set(0,new Vector<Float>());
        time.set(1,new Vector<Float>());
        time.set(2,new Vector<Float>());


        ma = mainActivity;

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
        dataPoint = new DataPoint[Math.min(value.elementAt(dataNumber).size(),value.elementAt(dataNumber).size() )];
        for(int i =0; (i <value.elementAt(dataNumber).size()) && (i <time.elementAt(dataNumber).size());i++){
            dataPoint[i] = new DataPoint( time.elementAt(dataNumber).get(i), value.elementAt(dataNumber).get(i));
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
            String nazwa = "/Dane "+ Integer.toString(i+1)+ " " + cal.getTime().toString() + ".csv";

            CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath()+ nazwa), '\t');


            for(int j =0; (j <value.elementAt(i).size()) && (j <time.elementAt(i).size());j++){
                String[] entries = (Float.toString(time.elementAt(i).get(j)) + "#" + Float.toString(value.elementAt(i).get(j))).split("#");
                writer.writeNext(entries);
            }
            writer.close();
            Utils.toast(ma.getApplicationContext(), "Zapisano w katalogu \"DanePomiarowe\" ");
        }catch(java.io.IOException e){
            Utils.toast(ma.getApplicationContext(), "Nie można zapisać");
        }

    }


    public void receiveData(String receivedData){
        recData = receivedData;
        String dataFrame;
        do {
            dataFrame = getDataFrame(recData);
            if(!dataFrame.isEmpty()) {
                String data = dataFrame.substring(1, dataFrame.length() - 1); //get data between # and *
                interpretData(data);
            }
        }while(!dataFrame.isEmpty());
    }

    public String getDataFrame(String data){
        String dataFrame = "";
        data = remData + data;
        if(!data.isEmpty()) {
            int endIndex = -1;
            int startIndex = -1;
            boolean startOfFrame = false;
            boolean endOfFrame = false;
            for (int i = 0; (i < data.length() && !endOfFrame); i++) {
                if (data.charAt(i) == '#' && startOfFrame == false) {    //if starting # is recognized
                    startOfFrame = true;
                    startIndex = i;

                } else if (data.charAt(i) == '*' && startOfFrame == true) {    //if ending * is recognized
                    startOfFrame = false;
                    endOfFrame = true;
                    endIndex = i;
                    if (endIndex + 1 < data.length()) {
                        remData = data.substring(endIndex + 1, data.length());
                    }else{
                        remData="";
                    }
                    dataFrame = data.substring(startIndex, endIndex + 1);
                    recData = "";
                    return dataFrame;
                }
            }
        }
        return dataFrame;

    }

    public void interpretData(String data){
        char id = data.charAt(0);

        data = data.substring(1, data.length());
        int val;
        if (!stringToIntError) {
            switch (id) {
                case 'L':
                    if(ma.registerData) {
                        val = stringToInt(data);
                        if (val > maxY){
                            maxY = val;
                        }else if(val < minY){
                            minY = val;
                        }
                        if (value.elementAt(0).size() >= length) {
                            value.elementAt(0).remove(0);
                            value.elementAt(0).add((float) val);
                        } else {
                            value.elementAt(0).add((float) val);
                        }
                        if (time.elementAt(0).size() >= length) {
                            time.elementAt(0).remove(0);
                            time.elementAt(0).add((float) it1 * 0.001f);
                        } else {
                            time.elementAt(0).add((float) it1 * 0.001f);
                        }
                        it1++;
                        if (it1* 0.001f > maxX){
                            maxX = it1* 0.001f;
                        }
                    }
                    break;
                case 'R':
                    if(ma.registerData) {
                        val = stringToInt(data);
                        if (val > maxY) {
                            maxY = val;
                        } else if (val < minY) {
                            minY = val;
                        }
                        if (value.elementAt(1).size() >= length) {
                            value.elementAt(1).remove(0);
                            value.elementAt(1).add((float) val);
                        } else {
                            value.elementAt(1).add((float) val);
                        }
                        if (time.elementAt(1).size() >= length) {
                            time.elementAt(1).remove(0);
                            time.elementAt(1).add((float) it2 * 0.001f);
                        } else {
                            time.elementAt(1).add((float) it2 * 0.001f);
                        }
                        it2++;
                        if (it2* 0.001f > maxX) {
                            maxX = it2* 0.001f;
                        }
                    }
                    break;
                case 'O':
                    if(ma.registerData) {
                        val = stringToInt(data);
                        if (val > maxY){
                            maxY = val;
                        }else if(val < minY){
                            minY = val;
                        }
                        if (value.elementAt(2).size() >= length) {
                            value.elementAt(2).remove(0);
                            value.elementAt(2).add((float) val);
                        } else {
                            value.elementAt(2).add((float) val);
                        }
                        if (time.elementAt(2).size() >= length) {
                            time.elementAt(2).remove(0);
                            time.elementAt(2).add((float) it3 * 0.001f);
                        } else {
                            time.elementAt(2).add((float) it3 * 0.001f);
                        }
                        it3++;
                        if (it3* 0.001f > maxX){
                            maxX = it3* 0.001f;
                        }
                    }
                    break;
                case 'T':
                    Utils.consoleNotify(ma,data);
                    break;
            }
        }
    }

    public int stringToInt(String textValue){
        int i = 0;
        if (textValue.charAt(0) == '-'){
            i = 1;

        }
        int number = 0;
        int multiplier = (int) Math.pow(10, textValue.length()-1);
        for ( ; i < textValue.length(); i++){
            if(textValue.charAt(i) >= '0' && textValue.charAt(i) <= '9') {
                number = number + (textValue.charAt(i) - '0') * multiplier;
                multiplier = multiplier / 10;
            }else{
                stringToIntError = true;
            }
        }
        if (textValue.charAt(0) == '-'){
            number = - number;

        }
        stringToIntError = false;
        return number;
    }


}
