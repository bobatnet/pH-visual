/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.csir.csio.instapH;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class CameraActivity extends Activity {
    private final String dlog = "PhCameraAct";
    Camera2BasicFragment camera;
    TextView txt;
    ViewScale phBar;
    Viewfinder viewfinder;

    final boolean storeSamples = false;
    RectF refnBounds,testBounds;

    ArrayList<String> takenPhs;
    EditText phLabelText;
    TextView isTaken;
    Button butBack, butCalibrate;
    AdView bannerAd;
    PhColorModel phmodel;
    boolean isPicking;
    final boolean customModel = false;
    TextView phview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.csir.csio.instapH.R.layout.activity_camera);
        final ImageView crosshair = (ImageView) findViewById(com.csir.csio.instapH.R.id.imageView);
        crosshair.setImageResource(com.csir.csio.instapH.R.drawable.crosshair_img);
        crosshair.setVisibility(View.INVISIBLE);

        final ImageView splash = (ImageView) findViewById(com.csir.csio.instapH.R.id.imageSplash);
        splash.setImageResource(com.csir.csio.instapH.R.mipmap.splash);

        txt = (TextView) findViewById(com.csir.csio.instapH.R.id.textView);
        phBar = (ViewScale) findViewById(com.csir.csio.instapH.R.id.viewphscale);
        viewfinder = (Viewfinder) findViewById(com.csir.csio.instapH.R.id.view);
        phLabelText = (EditText) findViewById(com.csir.csio.instapH.R.id.editph);
        isTaken = (TextView) findViewById(com.csir.csio.instapH.R.id.takenView);
        butBack = (Button) findViewById(com.csir.csio.instapH.R.id.buttonBack);
        butCalibrate = (Button) findViewById(com.csir.csio.instapH.R.id.butCalibrate);
        bannerAd = (AdView) findViewById(com.csir.csio.instapH.R.id.adView);
        phview = (TextView) findViewById(com.csir.csio.instapH.R.id.textph);
        final TextView needcalib = (TextView) findViewById(com.csir.csio.instapH.R.id.need_calib);
        isTaken.setVisibility(View.INVISIBLE);
        needcalib.setVisibility(View.INVISIBLE);

        phmodel = new PhColorModel(this);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(com.csir.csio.instapH.R.id.viewfinder, Camera2BasicFragment.newInstance(), "CameraView")
                    .commit();
        }

        Intent intent = getIntent();

        if (customModel) butCalibrate.setVisibility(View.INVISIBLE);
        needcalib.setVisibility(View.VISIBLE);
        phview.setVisibility(View.VISIBLE);

        if (Intent.ACTION_PICK.equals(intent.getAction())) {

            viewfinder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    camera.continuousPredict = false;

                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        camera.captureStillPicture();
                        return true;
                    }
                    return false;
                }
            });

            phBar.setVisibility(View.INVISIBLE);
            phLabelText.setVisibility(View.INVISIBLE);
            butBack.setVisibility(View.VISIBLE);
            butCalibrate.setVisibility(View.INVISIBLE);
            isPicking = true;

        } else {
            isPicking = false;
            phBar.setVisibility(View.VISIBLE);
            phLabelText.setVisibility(View.INVISIBLE);
            butBack.setVisibility(View.INVISIBLE);

            if (customModel) {
                if (!phmodel.load_model()) {
                    phview.setVisibility(View.GONE);
                    needcalib.setVisibility(View.VISIBLE);
                }
                butCalibrate.setVisibility(View.VISIBLE);
            }

            /*
            Timer splashOut = new Timer();
            splashOut.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            splash.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }, 3000); */

            viewfinder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        averagephcount = 0;
                        camera.continuousPredict = true;
                        return true;
                    }
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                        averagephcount = -1;
                        camera.continuousPredict = false;
                        return true;
                    }
                    return false;
                }
            });

            takenPhs = new ArrayList<>();
            label = 1;
        }
    }

    public void calibrateClicked(View v){
        Intent intent = new Intent(this, Calibrations.class);
        startActivity(intent);
    }

    public void cameraFragmentCreated() {
        camera = (Camera2BasicFragment) getFragmentManager().findFragmentByTag("CameraView");

        camera.colorAvailable = new IntCall() {
            @Override
            public void callWithInt(int a) {
                changeRGB(a, false);
            }
        };
        testBounds = new RectF(0.47f,0.47f,0.53f,0.53f);
        refnBounds = new RectF(testBounds);
        refnBounds.offset(0f,-0.2f);

        Viewfinder view = (Viewfinder) findViewById(com.csir.csio.instapH.R.id.view);
        view.refn = new RectF(refnBounds);
        view.test = new RectF(testBounds);
    }

    public void changeRGB(final int a, boolean continuous){
        //String info = String.format("R: %d, G: %d, B: %d;  pH: %f", Color.red(a),Color.green(a),Color.blue(a), output);

        final double output = MatrixMath.predict(a);

        phBar.mark = ((float) output);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                phBar.invalidate();
            }
        });

        if (!continuous){
            float[] hsv = new float[3];
            Color.colorToHSV(a,hsv);

            String info = String.format("H: %.3f, S: %.3f, V: %.3f", hsv[0],hsv[1],hsv[2]);
            //Log.d("HSV",info);

            info = String.format("R: %d, G: %d, B: %d;  pH: %.3f", Color.red(a),Color.green(a),Color.blue(a), output);

            //Log.d("RGB",info);
        }

    }

    double averageph = 0.0; int averagephcount = -1;

    public void newObserveWithSampleStore(Bitmap img, boolean sampleStore, String store_ph){
        try {
            sampleStore = false;
            store_ph = phLabelText.getText().toString();

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            Log.d("PHpred/Path",String.format("%s",path.toString()));
            File file = new File(path,"ph_training");
            path.mkdirs();

            //if (file.exists()) file.delete();
            OutputStream fos = new FileOutputStream(file,true);
            String out = "";

            RectF bound;
            int xydiv = 20, n=0, pix, a = 0, b = 0;
            int[][] rgb = new int[2][3];

            for (int isref = 1; isref >= 0; isref--){
                if (isref == 0) bound = testBounds;
                else bound = refnBounds;

                n = 0;
                for (float x = bound.left; x < bound.right; x += Math.abs(bound.left - bound.right)/(float) xydiv)
                    for (float y = bound.top; y < bound.bottom; y += Math.abs(bound.top - bound.bottom)/(float) xydiv){
                        a = (int) (x * img.getWidth());
                        b = (int) (y * img.getHeight());
                        pix = img.getPixel(a,b);

                        if (sampleStore)
                            if (isref == 1) out += String.format("%d,%d,%d,-1.0\n", Color.red(pix), Color.green(pix), Color.blue(pix));
                            else out += String.format("%d,%d,%d,%s\n", Color.red(pix), Color.green(pix), Color.blue(pix), store_ph);

                        rgb[isref][0] = (n*rgb[isref][0] + Color.red(pix))/(n+1);
                        rgb[isref][1] = (n*rgb[isref][1] + Color.green(pix))/(n+1);
                        rgb[isref][2] = (n*rgb[isref][2] + Color.blue(pix))/(n+1);
                        n++;
                    }
            }

            double G_left_a = 0.061594409270157344, G_left_b = 7.4622720249389909;
            double G_right_a = -0.082507996398683531, G_right_b = 6.5146521312026922;
            double ph;

            int compR = rgb[0][0] - rgb[1][0];
            if (compR > -51)
                ph = G_left_a * (rgb[0][1] - rgb[1][1]) + G_left_b;
            else
                ph = G_right_a * (rgb[0][1] - rgb[1][1]) + G_right_b;

            if (sampleStore) fos.write(out.getBytes());

            fos.close();
            //Log.d("File", file.getAbsolutePath());
            //Log.d("File", String.format("Saved %d pixels", n));
            //Log.d("File", String.format("Last pixel: %d x %d", a,b));
            Log.d("File", String.format("RGB %d,%d,%d", rgb[0][0],rgb[0][1],rgb[0][2]));
            Log.d("File", String.format("RGB ref %d,%d,%d", rgb[1][0],rgb[1][1],rgb[1][2]));
            Log.d("File", String.format("ph = %f",ph));

            phBar.mark = ((float) ph);
            if (averagephcount > -1){
                averageph = (averagephcount*averageph + ph)/(averagephcount+1);
                averagephcount++;
            }

            final TextView phview = (TextView) findViewById(com.csir.csio.instapH.R.id.textph);

            takenPhs.add(store_ph);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    phBar.invalidate();
                    if (averagephcount > -1) phview.setText(String.format("%.2f",averageph));
                    isTaken.setVisibility(View.VISIBLE);
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ph_training_save","Cannot write to media !");
        }
    }

    public void newObserve(Bitmap img, final boolean isContinuous){
        Log.d(dlog,"newObserve entered");
        RectF bound;
        int xydiv = 20, n=0, pix, a = 0, b = 0;
        int[][] rgb = new int[2][3];

        for (int isref = 1; isref >= 0; isref--){
            if (isref == 0) bound = testBounds;
            else bound = refnBounds;

            n = 0;
            for (float x = bound.left; x < bound.right; x += Math.abs(bound.left - bound.right)/(float) xydiv)
                for (float y = bound.top; y < bound.bottom; y += Math.abs(bound.top - bound.bottom)/(float) xydiv){
                    a = (int) (x * img.getWidth());
                    b = (int) (y * img.getHeight());
                    pix = img.getPixel(a,b);

                    rgb[isref][0] = (n*rgb[isref][0] + Color.red(pix))/(n+1);
                    rgb[isref][1] = (n*rgb[isref][1] + Color.green(pix))/(n+1);
                    rgb[isref][2] = (n*rgb[isref][2] + Color.blue(pix))/(n+1);
                    n++;
                }
        }

        if (isPicking) {
            if (!isContinuous) {
                Log.d(dlog, "Calibration captured");
                Intent data = new Intent();
                data.putExtra("Red", rgb[0][0]);
                data.putExtra("Green", rgb[0][1]);
                data.putExtra("Blue", rgb[0][2]);
                data.putExtra("RedRefn", rgb[1][0]);
                data.putExtra("GreenRefn", rgb[1][1]);
                data.putExtra("BlueRefn", rgb[1][2]);
                setResult(RESULT_OK, data);
                finish();
            }
        } else {
            double G_left_a = 0.061594409270157344, G_left_b = 7.4622720249389909;
            double G_right_a = -0.082507996398683531, G_right_b = 6.5146521312026922;
            double ph;

            int compR = rgb[0][0] - rgb[1][0];
            if (compR > -51)
                ph = G_left_a * (rgb[0][1] - rgb[1][1]) + G_left_b;
            else
                ph = G_right_a * (rgb[0][1] - rgb[1][1]) + G_right_b;

            if (customModel) {
                ph = phmodel.testModel(Pair.create(rgb[0][0], rgb[1][0]),
                        Pair.create(rgb[0][1], rgb[1][1]),
                        Pair.create(rgb[0][2], rgb[1][2]));

                if (Double.isNaN(ph)) return;
            }

            ph = (ph < 1) ? 1: ((ph > 14) ? 14 : ph);

            Log.d("File", String.format("RGB %d,%d,%d", rgb[0][0], rgb[0][1], rgb[0][2]));
            Log.d("File", String.format("RGB ref %d,%d,%d", rgb[1][0], rgb[1][1], rgb[1][2]));
            Log.d("File", String.format("ph = %f", ph));

            phBar.mark = ((float) ph);
            if (averagephcount > -1) {
                averageph = (averagephcount * averageph + ph) / (averagephcount + 1);
                averagephcount++;
            }

            if (true) {
                takenPhs.add(String.format("%.2f", ph));
                AdRequest.Builder adRequest = new AdRequest.Builder();

                if (averageph < 6) {
                    adRequest.addKeyword("acid");
                } else if (averageph > 8) {
                    adRequest.addKeyword("base");
                } else {
                    adRequest.addKeyword("purifier");
                    adRequest.addKeyword("water");
                }

                final AdRequest adRequest1 = adRequest.build();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bannerAd.loadAd(adRequest1);
                    }
                });
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    phBar.invalidate();
                    if (averagephcount > -1) phview.setText(String.format("%.2f", averageph));
                }
            });
        }
    }

    public void backClicked(View v){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void crosshairClicked(View v){
        camera.captureStillPicture();
    }

    int label;
    public void incrementLabel(View v) {
        label++;
        String a = String.format("%d", label);
        phLabelText.setText(a);
        if (takenPhs.contains(a)) isTaken.setVisibility(View.VISIBLE);
        else isTaken.setVisibility(View.INVISIBLE);
    }

    public void decrementLabel(View v) {
        label--;
        String a = String.format("%d", label);
        phLabelText.setText(a);
        if (takenPhs.contains(a)) isTaken.setVisibility(View.VISIBLE);
        else isTaken.setVisibility(View.INVISIBLE);
    }

    public void showAboutBox(View v){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.about_box)
                        .setTitle(R.string.about_box_title)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

        // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();

                dialog.show();
    }
}
