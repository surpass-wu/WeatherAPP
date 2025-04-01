package com.example.weather.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.weather.R;

public class RealtimeWeatherView extends View {

    private Paint paint = new Paint();

    private Bitmap aqBitmap;
    private Bitmap tempBitmap;
    private Bitmap wsBitmap;
    private Bitmap humBitmap;

    private final int defaultWidth = 610;
    private final int defaultHeight = 500;

    private float rotationCenterX = 0f;
    private float rotationCenterY = 0f;

    private Integer temperature = null;
    private String skycon = null;
    private Integer airQuality = null;
    private Integer apparentTemp = null;
    private Integer humidity = null;
    private float windDir = 0f;
    private Integer windScale = null;

    public RealtimeWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        paint.setAntiAlias(true);

        aqBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_air_quality);
        tempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_temperature);
        wsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_wind_direction);
        humBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_humidity);

        rotationCenterX = wsBitmap.getWidth() / 2 + 245f;
        rotationCenterY = wsBitmap.getHeight() / 2 + 375f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = defaultWidth;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = defaultHeight;
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setTextSize(225f);
        canvas.drawText(temperature + "°", 150f, 250f, paint);
        paint.setTextSize(40f);
        canvas.drawText(skycon, 450f, 250f, paint);
        canvas.drawBitmap(aqBitmap, 170f, 305f, paint);
        paint.setTextSize(38f);
        canvas.drawText("空气指数:   " + airQuality, 220f, 335f, paint);
        canvas.drawBitmap(tempBitmap, 70f, 360f, paint);
        paint.setTextSize(40f);
        canvas.drawText(apparentTemp + "°", 140f, 410f, paint);
        canvas.save();
        canvas.rotate(windDir + 145, rotationCenterX, rotationCenterY);
        canvas.drawBitmap(wsBitmap, 245f, 375f, paint);
        canvas.restore();
        canvas.drawText(windScale + "级", 310f, 410f, paint);
        canvas.drawBitmap(humBitmap, 390f, 360f, paint);
        canvas.drawText(humidity + "%", 460f, 410f, paint);
    }

    public void setRealtimeWeather(int temperature, String skycon, int airQuality,
                                   int apparentTemp, float windDir, int windScale, int humidity) {
        this.temperature = temperature;
        this.skycon = skycon;
        this.airQuality = airQuality;
        this.apparentTemp = apparentTemp;
        this.windDir = windDir;
        this.windScale = windScale;
        this.humidity = humidity;
        invalidate();
    }
}

