package com.example.testappforgoescan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
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
        ImageView image = findViewById(R.id.image);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection = new Connection();
                connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickLeft);
        connection = new Connection();
        connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        DatagramSocket datagramSocket;
        DatagramSocket imageSocket;
        byte[] image = new byte[5273];
        byte[] buffer = new byte[5273];

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            try {
                socket = new Socket(InetAddress.getByName("10.0.2.2"), port);
                datagramSocket = new DatagramSocket();
                if (socket.isConnected()) {
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                    DatagramPacket pack = new DatagramPacket(buffer, buffer.length);
                    while (socket.isConnected()) {
//                        publishProgress(buffer);

                        datagramSocket.receive(pack);
                        publishProgress(pack.getData());
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
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                DatagramPacket dp = new DatagramPacket(data, data.length, socket.getInetAddress(), 8001);
                                tvServerStatus.setText(socket.getInetAddress().toString());
                                datagramSocket.send(dp);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(byte[]... data) {
            System.out.println("Answer: ");
            for (byte b : data[0]){
                if (b > 0) {
                    System.out.print(b + ' ');
                }
            }
            System.out.println();
            tvServerStatus.setText(String.format("Answer: %s ", data[0][0]));
//                Bitmap bmp= BitmapFactory.decodeByteArray(answer,0,answer.length);
//                image.setImageBitmap(bmp);
        }
    }
}