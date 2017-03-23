package com.csir.csio.instapH;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by bob on 23/12/15.
 */
public final class ViewScale extends View{
    private final Paint paint;

    public ViewScale(Context context) {
        super(context);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public ViewScale(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public ViewScale(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    float originscale = 7.0f;
    float mark = -1.0f;
    float fullscale = 14.0f;
    float majorTick = 1.0f;
    float minorTick = 0.5f;
    float mminorTick = 0.25f;

    @Override
    public void onDraw(Canvas canvas) {

        float majorTickW = canvas.getWidth();
        float minorTickW = majorTickW * minorTick/majorTick;
        float mminorTickW = majorTickW * mminorTick/majorTick;

        paint.setColor(Color.argb(200, 255, 255, 255));
        paint.setTextSize(10);

        float majorHeights = canvas.getHeight()*majorTick/fullscale;

        for (float x = 0; x <= fullscale; x += majorTick){
            canvas.drawLine(0,x * majorHeights,majorTickW,x*majorHeights,paint);
            canvas.drawText(String.format("%d", ((int) x)),majorTickW*0.5f,x*majorHeights,paint);
        }

        float minorHeights = canvas.getHeight()*minorTick/fullscale;

        for (float x = 0; x <= fullscale/minorTick; x += minorTick){
            canvas.drawLine(0,x * minorHeights,minorTickW,x*minorHeights,paint);
        }

        float mminorHeights = canvas.getHeight()*mminorTick/fullscale;

        for (float x = 0; x <= fullscale/mminorTick; x += mminorTick) {
            canvas.drawLine(0, x * mminorHeights, mminorTickW, x * mminorHeights, paint);
        }

        if (mark > 0){
            float scale = canvas.getHeight()/fullscale;

            //Log.d("ViewScale",String.format("Mark: %f, scale: %f",mark,scale));

            paint.setColor(Color.argb(255,120,30,30));

            if (mark < originscale)
                canvas.drawRect(mminorTickW/3,scale*mark,mminorTickW*2/3,scale*originscale,paint);
            else
                canvas.drawRect(mminorTickW/3,originscale*scale,mminorTickW*2/3,scale*mark, paint);

            canvas.drawCircle(mminorTickW/2,scale*mark,mminorTickW/4,paint);

            paint.setTextSize(30);
            canvas.drawText(String.format("%.3f", mark),minorTickW,scale*mark,paint);
        }
    }

}
