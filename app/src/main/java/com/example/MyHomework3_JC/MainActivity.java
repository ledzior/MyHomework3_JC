package com.example.MyHomework3_JC;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public SensorManager mSensorManager;
    private Vibrator vibrator;
    private Sensor accelerometer;
    private ImageView ballImage;
    private TextView ballText;
    private TextView titleText;
    private TextView sensorText;
    private Button button;
    private boolean shaking;
    private boolean locked;
    private float maxRange;
    private String[] ballAnswers;
    private long[] pattern = {0, 50, 110};
    private ObjectAnimator startBallAnimator;
    private ObjectAnimator ballAnimator;
    private AnimatorSet animation;
    float start;
    float left;
    float right;
    float sum;
    float percent;
    float lastMeas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setContentView(R.layout.activity_main);

        ballImage = (ImageView) findViewById(R.id.magicBallImage);
        ballText = (TextView) findViewById(R.id.magicBallText);
        titleText = (TextView) findViewById(R.id.titleText);
        sensorText = (TextView) findViewById(R.id.sensorText);
        button = (Button) findViewById(R.id.button);
        button.setVisibility(View.INVISIBLE);
        ballAnswers = getResources().getStringArray(R.array.answers);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null)
        {
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            maxRange = (float) Math.sqrt(3*Math.pow((accelerometer.getMaximumRange())/3, 2.0));
        }
        else
        {
            titleText.setText(R.string.error_text);//no accelerometer detected
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locked = false;
                button.setVisibility(View.INVISIBLE);
                ballImage.setImageDrawable(getResources().getDrawable(R.drawable.magicball_8));
                ballText.setText("");
            }
        });
        start = ballImage.getX();

        left = start - 30;
        right = start + 30;

        startBallAnimator = ObjectAnimator.ofFloat(ballImage, "translationX", start, left);
        startBallAnimator.setInterpolator(new LinearInterpolator());
        startBallAnimator.setDuration(50);

        ballAnimator = ObjectAnimator.ofFloat(ballImage, "translationX", left, right);
        ballAnimator.setInterpolator(new AccelerateInterpolator());
        ballAnimator.setDuration(80);
        ballAnimator.setRepeatCount(ValueAnimator.INFINITE);
        ballAnimator.setRepeatMode(ValueAnimator.REVERSE);

        animation = new AnimatorSet();
        animation.play(startBallAnimator).before(ballAnimator);

    }
    @Override
    protected void onResume()
    {
        super.onResume();
        shaking = false;
        locked = false;
        lastMeas = 0;
        if(accelerometer !=null) mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        titleText.setText(R.string.shake_text);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(accelerometer !=null) mSensorManager.unregisterListener(this, accelerometer);
        vibrator.cancel();
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if(!locked)
        {
            sum = 0;
            for(int i=0; i<event.values.length; i++)
            {
                sum+=Math.pow(event.values[i], 2.0);
            }
            sum = Math.abs(lastMeas - (float) Math.sqrt(sum));
            lastMeas = sum;
            percent = sum/maxRange;
            sensorText.setText(String.valueOf(percent));

            if (!shaking)
            {
                if (percent > 0.18)
                {
                    titleText.setText(R.string.shaking_text);
                    vibrator.vibrate(pattern, 0);
                    animation.start();
                    shaking = true;
                }
                else
                {
                    titleText.setText(R.string.shake_text);
                }
            }
            else
            {
                if (percent < 0.003)
                {
                    animation.cancel();
                    ballImage.setTranslationX(start);
                    vibrator.cancel();
                    ballImage.setImageDrawable(getResources().getDrawable(R.drawable.magicball_empty));
                    titleText.setText(R.string.end_text);
                    ballText.setText(ballAnswers[(int) Math.floor(sum*10000)%20]);
                    locked = true;
                    shaking = false;
                    button.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
