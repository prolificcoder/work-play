package com.computemachines.android.workplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by tparker on 4/24/16.
 */
public class TimeButton extends Button {
    private static Context context;

    private String label;
    public void setLabel(String label) {
        this.label = label;
    }

    public TimeButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TimeButton.context = context;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.set(getPaint());
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(30);
        paint.setLetterSpacing(1);
        canvas.drawText(label, (getLeft()+getRight())/2 - 25, paint.getTextSize()+15, paint);

        long number = 1;
        if(label == "Work") {
//            number = ((ClockingActivity) context).getGoalRatioDenom();
        }
        Paint ratioPaint = new Paint();
        ratioPaint.set(getPaint());
        ratioPaint.setTextAlign(Paint.Align.RIGHT);
        ratioPaint.setTextSize(30);
        canvas.drawText(String.format("(%d)", number), getRight() - 35, ratioPaint.getTextSize()+15, ratioPaint);
    }
}
