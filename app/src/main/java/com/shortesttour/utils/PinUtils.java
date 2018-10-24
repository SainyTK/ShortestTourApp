package com.shortesttour.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.shortesttour.R;

public class PinUtils {
    private static Paint paint = new Paint();

    public static Bitmap createNumberPin(Context context,int num){

        String text = num+1+"";
        Bitmap bm = null;
        if(num==0){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_blue)
                    .copy(Bitmap.Config.ARGB_8888, true);
            paint.setColor(context.getResources().getColor(R.color.activeTint));
        }else{
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_red)
                    .copy(Bitmap.Config.ARGB_8888, true);
            paint.setColor(context.getResources().getColor(R.color.red));
        }

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(context, 14));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2)+1;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2))-2;

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }

    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }
}
