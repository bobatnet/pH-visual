package com.csir.csio.instapH;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by bob on 22/1/16.
 */
public class PhColorModel {
    private final String dtag = "PhColorModel";
    private static String modelFName = "model";

    private float thresh_R;
    private float green_left_M, green_left_C;
    private float green_right_M, green_right_C;
    private boolean isCalibrated;
    public boolean hasChanged;
    private Activity main;
    private String fullpath;
    private Calibrations.dataModel datamodel;
    private boolean editable;

    private Runnable islearning, learningover;

    PhColorModel(Activity act, Runnable learning, Runnable donelearning, Calibrations.dataModel dmodel) {
        main = act;
        fullpath = new File(main.getFilesDir().toString(), modelFName).toString();
        islearning = learning; learningover = donelearning;
        datamodel = dmodel;
        Log.d(dtag, fullpath);
        editable = true;
        learnR = new ArrayList<>();learnG = new ArrayList<>(); learnB = new ArrayList<>(); learnPh = new ArrayList<>();
        markRemoved = new ArrayList<>();
    }

    PhColorModel(Activity act) {
        main = act;
        editable = false;
        fullpath = new File(main.getFilesDir().toString(), modelFName).toString();
        Log.d(dtag, fullpath);
        learnR = new ArrayList<>();learnG = new ArrayList<>(); learnB = new ArrayList<>(); learnPh = new ArrayList<>();
        markRemoved = new ArrayList<>();
    }

    ArrayList<Boolean> markRemoved;

    private int getMinimumPhIndex(ArrayList<Boolean> from) {
        int minInd = 0;
        for (int i=0; i<learnPh.size();i++) {
            if (!from.get(minInd))
                minInd = i;
            if (!from.get(i))
                if (learnPh.get(i) < learnPh.get(minInd))
                    minInd = i;
        }
        return minInd;
    }

    public void save_model() {
        File file = new File(fullpath);
        if (file.exists()) file.delete();
        Log.d(dtag, "Init save_model");
        try {
            FileOutputStream fout = new FileOutputStream(fullpath);
            String dataout = String.format("green_left_M:%f\ngreen_right_M:%f\ngreen_left_C:%f\ngreen_right_C:%f\nthresh_R:%f\nsufficient_data:%d\n",
                                            green_left_M,green_right_M,green_left_C,green_right_C,thresh_R,isSufficient);
            //Log.d(dtag,dataout);
            fout.write(dataout.getBytes());

            ArrayList<Boolean> dataFlags = new ArrayList<>(markRemoved);

            for (int i=0; i<learnPh.size(); i++) {
                if (!markRemoved.get(i)) {
                    Log.d(dtag, String.valueOf(i));

                    dataout = String.format("data_point:%d:%d:%d:%d:%d:%d:%f\n",
                            learnR.get(i).first, learnR.get(i).second,
                            learnG.get(i).first, learnG.get(i).second,
                            learnB.get(i).first, learnB.get(i).second,
                            learnPh.get(i));
                    //Log.d(dtag,dataout);
                    fout.write(dataout.getBytes());
                    dataFlags.set(i, true);
                }
            }
            fout.close();
            Log.d(dtag,"Saved");
        } catch (FileNotFoundException e) {
            Log.d(dtag, "Cannot save model");
        } catch (IOException e){
            Log.d(dtag, "IOException in save model");
        }
    }

    public void addDataPoint(int r, int rr, int g, int gr, int b, int br, float ph){
        hasChanged = true;
        learnR.add(Pair.create(r,rr));
        learnG.add(Pair.create(g,gr));
        learnB.add(Pair.create(b,br));
        learnPh.add(ph);
        if (editable)   datamodel.addItem(ph);
        markRemoved.add(false);
        Log.d(dtag, "Added data point");
    }

    static final int SUFF_NOT = 0, SUFF_DATA_LEFT = 1,SUFF_DATA_RIGHT = 2,SUFF_DATA = 3;
    int isSufficient;

    public boolean load_model() {
        hasChanged = false;
        int calibrated = 0;

        try{
            learnR.clear(); learnG.clear(); learnB.clear(); learnPh.clear();
            markRemoved.clear();

            BufferedReader buf = new BufferedReader(new FileReader(fullpath));

            String[] str;
            try {
                while (true) {
                    String ln = buf.readLine();
                    if (ln == null) break;
                    str = ln.split("\\:");
                    Log.d(dtag,str[0]);
                    switch (str[0]){
                        case "green_left_M" :
                            green_left_M = Float.parseFloat(str[1]); calibrated += (1 << 0);
                            Log.d(dtag,"green_left_M " + String.valueOf(green_left_M));
                            break;
                        case "green_right_M" :
                            green_right_M = Float.parseFloat(str[1]); calibrated += (1 << 1);
                            Log.d(dtag,"green_right_M " + String.valueOf(green_right_M));
                            break;
                        case "green_left_C" :
                            green_left_C = Float.parseFloat(str[1]); calibrated += (1 << 2);
                            Log.d(dtag,"green_left_C " + String.valueOf(green_left_C));
                            break;
                        case "green_right_C" :
                            green_right_C = Float.parseFloat(str[1]); calibrated += (1 << 3);
                            Log.d(dtag,"green_right_C " + String.valueOf(green_right_C));
                            break;
                        case "thresh_R":
                            thresh_R = Float.parseFloat(str[1]); calibrated += (1 << 4);
                            Log.d(dtag,"thresh_R " + String.valueOf(thresh_R));
                            break;
                        case "sufficient_data":
                            isSufficient = Integer.parseInt(str[1]);
                            break;
                        case "data_point":
                            addDataPoint(Integer.parseInt(str[1]),Integer.parseInt(str[2]),
                                         Integer.parseInt(str[3]),Integer.parseInt(str[4]),
                                         Integer.parseInt(str[5]),Integer.parseInt(str[6]),
                                         Float.parseFloat(str[7]));
                    }
                }
            } catch (IOException e) {

            } catch (NumberFormatException e) {
                try {
                    buf.close();
                    File file = new File(fullpath);
                    file.delete();
                    Log.d(dtag, "Error while reading model, deleted.");
                } catch (IOException f) {}
            }

            isCalibrated = (calibrated == 0b11111);
        } catch (FileNotFoundException e) {
            green_left_C = -1;
            green_left_M = 0;
            green_right_M = 0;
            green_right_C = -1;
            thresh_R = 0;
            isCalibrated = false;
        }
        Log.d(dtag, "Loaded model " + String.valueOf(isCalibrated) + " " + Integer.toBinaryString(calibrated));
        return isSufficient == SUFF_DATA;
    }

    public ArrayList<Pair<Integer,Integer>> learnR, learnG, learnB;
    public ArrayList<Float> learnPh;
    float learnThreshR;

    public void clearAllData() {
        hasChanged = true;
        learnR.clear(); learnG.clear(); learnB.clear(); learnPh.clear(); markRemoved.clear();
    }

    public void deleteIndex(int i){
        hasChanged = true;
        /* learnR.remove(i); learnG.remove(i); learnB.remove(i); learnPh.remove(i); */
        markRemoved.set(i,true);
    }

    private Pair<ArrayList<Integer>,ArrayList<Float>> threshPhGreen(boolean below, boolean setThreshR) {
        ArrayList<Integer> newgreen = new ArrayList<>();
        ArrayList<Float> newph = new ArrayList<>();
        learnThreshR = 0;

        for (int i=0; i < learnG.size(); i++){
            if (!markRemoved.get(i))
                if ((below && learnPh.get(i) < 7) || (!below && learnPh.get(i) > 7)) {
                    if (setThreshR)
                        learnThreshR = ((learnThreshR)*newgreen.size() + learnR.get(i).first - learnR.get(i).second)/(newgreen.size() + 1);
                    newgreen.add(learnG.get(i).first - learnG.get(i).second);
                    newph.add(learnPh.get(i));
                }
        }
        Log.d(dtag, "Threshold = " + String.valueOf(learnThreshR));
        Log.d(dtag, "Above/below thresh points = " + String.valueOf(newgreen.size()));
        return Pair.create(newgreen,newph);
    }

    public boolean learnModel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isSufficient = SUFF_NOT;
                main.runOnUiThread(islearning);
                Log.d(dtag, "Start Linear regress 1");
                Pair<ArrayList<Integer>, ArrayList<Float>> belowGreenPh = threshPhGreen(true, true);
                Pair<Float, Float> linmodel;

                if (belowGreenPh.first.size() > 2) {
                    linmodel = LinearRegress(belowGreenPh.first, belowGreenPh.second);
                    green_left_M = linmodel.first;
                    green_left_C = linmodel.second;
                    isSufficient = SUFF_DATA_LEFT;
                }

                Log.d(dtag, "Start Linear regress 2");
                Pair<ArrayList<Integer>, ArrayList<Float>> aboveGreenPh = threshPhGreen(false, false);

                if (aboveGreenPh.first.size() > 2) {
                    linmodel = LinearRegress(aboveGreenPh.first, aboveGreenPh.second);
                    green_right_M = linmodel.first;
                    green_right_C = linmodel.second;
                    if (isSufficient == SUFF_DATA_LEFT)
                        isSufficient = SUFF_DATA;
                    else
                        isSufficient = SUFF_DATA_RIGHT;
                }
                save_model();
                main.runOnUiThread(learningover);

                Log.d(dtag, "Stop Linear regress");
                Log.d(dtag, "Sufficient data = " + String.valueOf(isSufficient));
            }
        }).start();

        return true;
    }

    private static Pair<Float,Float> LinearRegress(ArrayList<Integer> x, ArrayList<Float> y) {
        float alpha = 0.01f, m, c;

        Random rnd = new Random();
        m = rnd.nextFloat(); c = rnd.nextFloat();

        int n = 10000;
        while (n > 0) {
            n--;
            float error = 0, errorProd = 0;

            for (int i = 0; i < x.size(); i++){
                float thisError = m*x.get(i) + c - y.get(i);
                error = (error*i + thisError)/(i+1);
                errorProd = (errorProd*i + thisError*x.get(i))/(i+1);
            }
            c -= alpha*error;
            m -= alpha*errorProd;
        }
        return Pair.create(m,c);
    }

    public float testModel(Pair<Integer,Integer> rr, Pair<Integer,Integer> gr, Pair<Integer,Integer> br){
        int r = rr.first - rr.second;
        int g = gr.first - gr.second;
        if (r < thresh_R)
            return g * green_left_M + green_left_C;
        else
            return g * green_right_M + green_right_C;
    }
}
