package com.example.testappforgoescan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.net.*;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    static String host = "192.168.4.1";
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
                connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new ReceiveFromNetwork().execute();
            }
        });
        JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickLeft);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                data[1] = (byte) (127 * strength / 100 * Math.cos(Math.toRadians(angle)));
                data[2] = (byte) (127 * strength / 100 * Math.sin(Math.toRadians(angle)));
                tvLeft.setText(String.format("Angle = %d , Strength = %d", angle, strength));
                tvData.setText(String.format("Data: %s", Arrays.toString(data)));
                if (connection != null){
                    connection.SendDataToNetwork(data);
                }
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
                if (connection != null){
                    connection.SendDataToNetwork(data);
                }
            }
        });
    }

    public class Connection extends AsyncTask<Void, byte[], Boolean> {
        Socket socket;
        DatagramSocket datagramSocket;
        byte[] buffer = new byte[5273];

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            try {
                socket = new Socket(InetAddress.getByName(host), port);
                datagramSocket = new DatagramSocket(socket.getLocalPort());
                if (socket.isConnected()) {
                    DatagramPacket pack = new DatagramPacket(buffer, buffer.length);
                    while (socket.isConnected()) {
                        datagramSocket.receive(pack);
                        publishProgress(pack.getData());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = true;
            } finally {
                try {
                    socket.close();
                    datagramSocket.close();
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
            Bitmap bmp= BitmapFactory.decodeByteArray(data[0],0,data[0].length);
            ImageView image = findViewById(R.id.image);
            image.setImageBitmap(bmp);
        }
    }
    public class ReceiveFromNetwork extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... params) {
            StringBuilder buf = new StringBuilder();
            BufferedReader reader = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://"+host+":8889/info");
                connection = (HttpURLConnection) url.openConnection();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line=reader.readLine()) != null) {
                    buf.append(line).append("\n");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return buf.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            tvServerStatus.setText(result);
        }
    }
}