package com.example.testappforgoescan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.net.*;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    static String host = "localhost";
    static int port = 8888;
    TextView tvServerStatus = null;
    Connection connection = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        byte[] data = new byte[6];
        data[0] = 127;
        data[5] = 1;
        TextView tvData = findViewById(R.id.data);
        tvServerStatus = findViewById(R.id.serverStatus);
        TextView tvLeft = findViewById(R.id.TextLeft);
        TextView tvRight = findViewById(R.id.TextRight);
        Button btn = findViewById(R.id.connection);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection = new Connection();
                connection.execute();
            }
        });
        JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickLeft);
        connection = new Connection();
        connection.execute();
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                data[1] = (byte) (127 * strength / 100 * Math.cos(Math.toRadians(angle)));
                data[2] = (byte) (127 * strength / 100 * Math.sin(Math.toRadians(angle)));
                tvLeft.setText(String.format("Angle = %d , Strength = %d", angle, strength));
                tvData.setText(String.format("Data: %s", Arrays.toString(data)));
                connection.SendDataToNetwork(data);
            }
        });
        JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickRight);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                data[3] = (byte) (127 * strength / 100 * Math.cos(Math.toRadians(angle)));
                data[4] = (byte) (127 * strength / 100 * Math.sin(Math.toRadians(angle)));
                tvRight.setText(String.format("Angle = %d , Strength = %d", angle, strength));
                tvData.setText(String.format("Data: %s", Arrays.toString(data)));
                connection.SendDataToNetwork(data);
            }
        });
    }

    public class Connection extends AsyncTask<Void, byte[], Boolean> {
        Socket socket;
        InputStream is;
        OutputStream os;

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            try {
                socket = new Socket(InetAddress.getByName(host), port);
                if (socket.isConnected()) {
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                    byte[] buffer = new byte[4096];
                    int read = is.read(buffer, 0, 4096);
                    while(read != -1){
                        byte[] tempdata = new byte[read];
                        System.arraycopy(buffer, 0, tempdata, 0, read);
                        publishProgress(tempdata);
                        read = is.read(buffer, 0, 4096);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = true;
            } finally {
                try {
                    is.close();
                    os.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        public void SendDataToNetwork(byte[] data) {
            try {
                if (socket.isConnected()) {
                    os.write(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        @Override
//        protected void onProgressUpdate(byte[]... values) {
//            if (values.length > 0) {
//                textStatus.setText(new String(values[0]));
//            }
//        }
//        @Override
//        protected void onPostExecute(Boolean result) {
//            if (result) {
//                tvServerStatus.setText("There was a connection error.");
//            } else {
//                tvServerStatus.setText("Выполнено");
//            }
//        }
    }
}