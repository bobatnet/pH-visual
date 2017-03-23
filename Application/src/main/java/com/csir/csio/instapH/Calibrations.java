package com.csir.csio.instapH;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Calibrations extends Activity {
    final String dtag = "PhCalibrations";
    LinearLayout dataList;
    PhColorModel phmodel;
    TextView islearning;
    EditText enterPh;

    class dataModel {
        int lastIndex;

        void clearItems() {
            lastIndex = 0;
            Calibrations.this.dataList.removeAllViews();
            phmodel.clearAllData();
        }

        void addItem(final float ph) {
            Calibrations.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Calibrations sup = Calibrations.this;
                    final LinearLayout newlayout = new LinearLayout(sup);
                    newlayout.setOrientation(LinearLayout.HORIZONTAL);
                    newlayout.setBackgroundResource(com.csir.csio.instapH.R.drawable.calibrations_list_shape);

                    ImageButton delete = new ImageButton(sup);
                    delete.setImageResource(com.csir.csio.instapH.R.drawable.delete);
                    delete.setOnClickListener(new View.OnClickListener() {
                        final int myindex = lastIndex;
                        final LinearLayout mylayout = newlayout;
                        public void onClick(View v) {
                            phmodel.deleteIndex(myindex);
                            mylayout.setVisibility(View.GONE);
                        }
                    });
                    TextView value = new TextView(sup);

                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    value.setText(String.format("%.3f",ph));
                    value.setGravity(Gravity.CENTER_VERTICAL);
                    value.setTextSize(25.0f);

                    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

                    newlayout.addView(delete);
                    newlayout.addView(value,layoutParams);

                    sup.dataList.addView(newlayout,lastIndex,layoutParams1);
                    lastIndex++;
                }
            });
        }
    }

    final dataModel mydatamodel = new dataModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.csir.csio.instapH.R.layout.activity_calibrations);

        dataList = (LinearLayout) findViewById(com.csir.csio.instapH.R.id.scroll);
        islearning = (TextView) findViewById(com.csir.csio.instapH.R.id.islearning);
        enterPh = (EditText) findViewById(com.csir.csio.instapH.R.id.enterph);

        phmodel = new PhColorModel(this, new Runnable() {
            @Override
            public void run() {
                islearning.setVisibility(View.VISIBLE);
            }
        }, new Runnable() {
            @Override
            public void run() {
                islearning.setVisibility(View.INVISIBLE);
                switchBack();
            }
        }, mydatamodel);

        phmodel.load_model();
    }

    static final int INTENT_NEW_CALIBRATE = 0;
    float calibph;

    public void doAddCalibration(View v){
        try {
            calibph = Float.parseFloat(enterPh.getText().toString());
        } catch (NumberFormatException e){
            enterPh.setText("");
            return;
        }
        Intent intentNewCalibration = new Intent(this, CameraActivity.class);
        intentNewCalibration.setAction(Intent.ACTION_PICK);
        startActivityForResult(intentNewCalibration,INTENT_NEW_CALIBRATE);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        Log.d(dtag, "Back to calibrations");
        if (requestCode == INTENT_NEW_CALIBRATE){
            phmodel.addDataPoint(data.getIntExtra("Red",0), data.getIntExtra("RedRefn",0),
                    data.getIntExtra("Green",0), data.getIntExtra("GreenRefn",0),
                    data.getIntExtra("Blue",0), data.getIntExtra("BlueRefn",0),
                    calibph);
            Log.d(dtag, "Added calibration point");
        }
    }

    void switchBack() {
        Intent backToMain = new Intent(this, CameraActivity.class);
        backToMain.setAction(Intent.ACTION_MAIN);
        startActivity(backToMain);
    }

    public void clickedPredictMode(View v) {
        phmodel.learnModel();
    }

    public void clickedClear(View v) {
        mydatamodel.clearItems();
    }
}
