package com.csir.csio.instapH;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/**
 * Created by bob on 22/12/15.
 */
public final class Viewfinder extends View {
    private final Paint paint;

    public Viewfinder(Context context) {
        super(context);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public Viewfinder(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public Viewfinder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    RectF refn,test;

    @Override
    public void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(Color.argb(75, 255, 255, 255));

        canvas.drawRect(0, 0, width, height / 3, paint); // top
        canvas.drawRect(0, height * 2 / 3, width, height, paint); // bottom
        canvas.drawRect(0, height / 3, width / 3, height * 2 / 3, paint); //left
        canvas.drawRect(width * 2 / 3, height / 3, width, height * 2 / 3, paint); //right

        paint.setColor(Color.argb(150,255,255,255));
        paint.setStrokeWidth(10.0f);

        if (test != null)
            canvas.drawRect(test.left * width, test.top * height, test.right * width, test.bottom * height, paint);
        else Log.d("Viewfinder","Test is null !");
        //if (aspX != 0) canvas.drawRect(inner.centerX() - 50 * aspX,inner.centerY() + (-10) * aspY ,inner.centerX() + 50 * aspX,inner.centerY() + (10) * aspY, paint);

        paint.setColor(Color.argb(150,255,255,100));
        paint.setStrokeWidth(10.0f);

        if (refn != null)
            canvas.drawRect(refn.left * width, refn.top * height, refn.right * width, refn.bottom * height, paint);
        else Log.d("Viewfinder","Refn is null !");
        //if (aspX != 0) canvas.drawRect(inner.centerX() - 50 * aspX,inner.centerY() + (-10 - 200) * aspY,inner.centerX() + 50 *aspX,inner.centerY() + (10 - 200) * aspY, paint);

        Log.d("Viewfinder",String.format("Size: %d x %d", width, height));
    }
}
