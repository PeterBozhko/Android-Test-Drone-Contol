package com.example.testappforgoescan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickLeft);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                TextView tvLeft = findViewById(R.id.TextLeft);
                tvLeft.setText(String.format("Angle = %d , Strength = %d",angle, strength));
            }
        });
        JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickRight);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                TextView tvRight = findViewById(R.id.TextRight);
                tvRight.setText(String.format("Angle = %d , Strength = %d",angle, strength));
            }
        });
    }
}