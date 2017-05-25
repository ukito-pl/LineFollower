package com.example.ukito.linefollower;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

/**
 * Created by ukito on 15.03.2017.
 */

public class DataCollector {

    public float[][] time;
    public float[][] value;

    public DataCollector(){
        time = new float[3][100];
        value = new float[3][100];
        generateData();

    }

    public void generateData(){
        for (int i = 0; i < 100; i++){
            time[0][i] = i*0.1f;
            value[0][i] = 1-(float) Math.pow(2.71,-time[0][i]);
            time[1][i] = i*0.1f;
            value[1][i] = 0.75f*(1-(float) Math.pow(2.71,-3*time[1][i]));
            time[2][i] = i*0.1f;
            value[2][i] =1 - ( (float)Math.pow(2.71,-2*time[2][i]) * (float)Math.cos(time[2][i]) - 2*(float)Math.pow(2.71,-2*time[2][i]) * (float)Math.sin(time[2][i])) ;
        }
    }

    public LineGraphSeries<DataPoint> getLineDataSeries(int dataNumber){

        DataPoint[] dataPoint;
        dataPoint = new DataPoint[100];
        for(int i =0; i <100;i++){
            dataPoint[i] = new DataPoint( time[dataNumber][i],value[dataNumber][i]);
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);
        return series;
    }
}
