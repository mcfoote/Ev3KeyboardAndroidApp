package com.example.Ev3Proj3;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

//// HERE
import java.io.InputStream;
import java.io.OutputStream;

import com.example.Ev3Proj3.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private final int MOTOR_A = 1;
    private final int MOTOR_B = 2;
    private final int MOTOR_C = 4;
    private final int MOTOR_BC = 6;
    private final int MOTOR_D = 8;
    private final int MOTOR_ALL = 15;
    private long startTime;
    Stack<Press> pressList = new Stack<>();
    private final String CV_ROBOTNAME = "MFJRW24";
    private BluetoothAdapter cv_btInterface = null;
    private Set<BluetoothDevice> cv_pairedDevices = null;
    private BluetoothDevice cv_btDevice = null;
    private BluetoothSocket cv_btSocket = null;

    // Data stream to/from NXT bluetooth
    private InputStream cv_is = null;
    private OutputStream cv_os = null;

    BottomNavigationView cv_bottomNavigationView;
    TextView cv_txtDebug;
    SeekBar cv_speedBar;
    SeekBar cv_speedBarThird;
    ImageButton cv_btnForward;
    ImageButton cv_btnBackward;
    ImageButton cv_btnLeft;
    ImageButton cv_btnRight;
    ImageButton cv_btnForwardThird;
    ImageButton cv_btnBackwardThird;
    TextView cv_txtSpeedBar;
    TextView cv_txtSpeedBarThird;
    Button cv_btnReplay;
    Switch cv_switchSpin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ui elements
        cv_txtDebug = findViewById(R.id.vv_txtDebug);
        cv_txtSpeedBar = findViewById(R.id.vv_txtSpeedBar);
        cv_txtSpeedBarThird = findViewById(R.id.vv_txtSpeedBarThird);
        cv_speedBar = findViewById(R.id.vv_speedBar);
        cv_speedBarThird = findViewById(R.id.vv_speedBarThird);
        cv_btnForward = findViewById(R.id.vv_btnForward);
        cv_btnBackward = findViewById(R.id.vv_btnBackward);
        cv_btnLeft = findViewById(R.id.vv_btnLeft);
        cv_btnRight = findViewById(R.id.vv_btnRight);
        cv_btnForwardThird = findViewById(R.id.vv_btnForwardThird);
        cv_btnBackwardThird = findViewById(R.id.vv_btnBackwardThird);
        cv_btnReplay = findViewById(R.id.vv_btnReplay);
        cv_switchSpin = findViewById(R.id.vv_switchSpin);

        // Have to check for permission every time
        cpf_checkBTPermissions();

        // Bottom menu
        cv_bottomNavigationView = findViewById(R.id.vv_bottomNavigationView);
        cv_bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.drive) {
                cpf_EV3PlayTone();
                return true;
            } else if (itemId == R.id.connect) {
                cpf_connectToEV3(cv_btDevice);
                return true;
            } else if (itemId == R.id.search) {
                cv_btDevice = cpf_locateInPairedBTList(CV_ROBOTNAME);
                return true;
            } else if (itemId == R.id.allow) {
                cpf_requestBTPermissions();
                return true;
            }
            return false;
        });
        cv_bottomNavigationView.setSelectedItemId(R.id.search); // Default bottom nav menu to search

        ///////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////// BUTTON TOUCH METHODS //////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////

        // Forward Button
        cv_btnForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            cpf_EV3MoveWheels(cv_speedBar.getProgress(), MOTOR_BC);
                            cv_btnForward.setBackgroundColor(getColor(R.color.green));
                            cv_btnForward.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start();
                            startTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            cpf_EV3MoveWheels(0, MOTOR_BC);
                            cv_btnForward.setBackgroundColor(getColor(R.color.white));
                            cv_btnForward.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            pressList.push(new Press("cv_btnForward", cv_speedBar.getProgress(), MOTOR_BC, cv_switchSpin.isChecked(),System.currentTimeMillis() - startTime));
                            break;
                    }
                return false;
            }
        });

        // Backward Button
        cv_btnBackward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cpf_EV3MoveWheels(-cv_speedBar.getProgress(), MOTOR_BC);
                        cv_btnBackward.setBackgroundColor(getColor(R.color.green));
                        cv_btnBackward.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start();
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cpf_EV3MoveWheels(0, MOTOR_BC);
                        cv_btnBackward.setBackgroundColor(getColor(R.color.white));
                        cv_btnBackward.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        pressList.push(new Press("cv_btnBackward", -cv_speedBar.getProgress(), MOTOR_BC, cv_switchSpin.isChecked(), System.currentTimeMillis() - startTime));
                        break;
                }
                return false;
            }
        });

        // Left Button
        cv_btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cpf_EV3MoveWheels(cv_speedBar.getProgress(), MOTOR_C);
                        if (cv_switchSpin.isChecked()){
                            cpf_EV3MoveWheels(-cv_speedBar.getProgress(), MOTOR_B);
                        }
                        cv_btnLeft.setBackgroundColor(getColor(R.color.green));
                        cv_btnLeft.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start();
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cpf_EV3MoveWheels(0, MOTOR_BC);
                        cv_btnLeft.setBackgroundColor(getColor(R.color.white));
                        cv_btnLeft.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        pressList.push(new Press("cv_btnLeft", cv_speedBar.getProgress(), MOTOR_C, cv_switchSpin.isChecked(),System.currentTimeMillis() - startTime));
                        break;
                }
                return false;
            }
        });

        // Right Button
        cv_btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cpf_EV3MoveWheels(cv_speedBar.getProgress(), MOTOR_B);
                        if (cv_switchSpin.isChecked()){
                            cpf_EV3MoveWheels(-cv_speedBar.getProgress(), MOTOR_C);
                        }
                        cv_btnRight.setBackgroundColor(getColor(R.color.green));
                        cv_btnRight.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start();
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cpf_EV3MoveWheels(0, MOTOR_BC);
                        cv_btnRight.setBackgroundColor(getColor(R.color.white));
                        cv_btnRight.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        pressList.push(new Press("cv_btnRight", cv_speedBar.getProgress(), MOTOR_B, cv_switchSpin.isChecked(),System.currentTimeMillis() - startTime));
                        break;
                }
                return false;
            }
        });

        // 3rd Wheel Forward Button
        cv_btnForwardThird.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cpf_EV3MoveWheels(cv_speedBarThird.getProgress(), MOTOR_D);
                        cv_btnForwardThird.setBackgroundColor(getColor(R.color.green));
                        cv_btnForwardThird.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start();
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cpf_EV3MoveWheels(0, MOTOR_D);
                        cv_btnForwardThird.setBackgroundColor(getColor(R.color.white));
                        cv_btnForwardThird.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        pressList.push(new Press("cv_btnForwardThird", cv_speedBar.getProgress(), MOTOR_D, cv_switchSpin.isChecked(),System.currentTimeMillis() - startTime));
                        break;
                }
                return false;
            }
        });

        // 3rd Wheel Backward Button
        cv_btnBackwardThird.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cpf_EV3MoveWheels(-cv_speedBarThird.getProgress(), MOTOR_D);
                        cv_btnBackwardThird.setBackgroundColor(getColor(R.color.green));
                        cv_btnBackwardThird.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start();
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cpf_EV3MoveWheels(0, MOTOR_D);
                        cv_btnBackwardThird.setBackgroundColor(getColor(R.color.white));
                        cv_btnBackwardThird.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        pressList.push(new Press("cv_btnBackwardThird", cv_speedBar.getProgress(), MOTOR_D, cv_switchSpin.isChecked(),System.currentTimeMillis() - startTime));
                        break;
                }
                return false;
            }
        });

        // Replay Button
        cv_btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               replayPresses();
            }
        });

        // Speed bar
        cv_speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cv_txtSpeedBar.setText("Speed: " + String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Speed bar
        cv_speedBarThird.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cv_txtSpeedBarThird.setText("Speed: " + String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void replayPresses() {
        while(!pressList.empty()){
            Press press = pressList.pop();

            // Handle second wheel if spin turning
            if (press.getSpin()){
                if (press.getButtonId().equals("cv_btnLeft")) cpf_EV3MoveWheels(press.getSpeed(), MOTOR_B);
                if (press.getButtonId().equals("cv_btnRight")) cpf_EV3MoveWheels(press.getSpeed(), MOTOR_C);
            }

            // Do reverse of movement
            cpf_EV3MoveWheels(-press.getSpeed(), press.getMotors());

            // Allow for the duration
            try {
                Thread.sleep(press.getDuration());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Stop movement
            cpf_EV3MoveWheels(0, MOTOR_ALL);

            // Short wait in between each movement
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// EV3 COMMANDS //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////

    // Move specified wheels in the same direction
    private void cpf_EV3MoveWheels(int speed, int motors){
        try{

            byte[] buffer = new byte[15];

            buffer[0] = (byte) (15 - 2);
            buffer[1] = 0;

            buffer[2] = 13;
            buffer[3] = 42;

            buffer[4] = (byte) 0x80;

            buffer[5] = 0;
            buffer[6] = 0;

            buffer[7] = (byte) 0xa4;
            buffer[8] = 0;
            buffer[9] = (byte) motors;

            buffer[10] = (byte) 0x81;
            buffer[11] = (byte) speed;

            buffer[12] = (byte) 0xa6;
            buffer[13] = 0;
            buffer[14] = (byte) motors;

            cv_os.write(buffer);
            cv_os.flush();

        }
        catch(Exception e){
            cv_txtDebug.setText("Error in Move(" + e.getMessage() + ")");
        }
    }

    // 4.2.5 Play a 1Kz tone at level 2 for 1 sec.
    private void cpf_EV3PlayTone() {
        try {
            byte[] buffer = new byte[17];       // 0x0f command length

            buffer[0] = (byte) (17-2);
            buffer[1] = 0;

            buffer[2] = 34;
            buffer[3] = 12;

            buffer[4] = (byte) 0x80;

            buffer[5] = 0;
            buffer[6] = 0;

            buffer[7] = (byte) 0x94;
            buffer[8] = 1;

            buffer[9] = (byte) 0x81;
            buffer[10] = (byte) 0x02;

            buffer[11] = (byte) 0x82;
            buffer[12] = (byte) 0xd0;
            buffer[13] = (byte) 0x07;

            buffer[14] = (byte) 0x82;
            buffer[15] = (byte) 0xe8;
            buffer[16] = (byte) 0x03;

            cv_os.write(buffer);
            cv_os.flush();
        }
        catch (Exception e) {
            cv_txtDebug.setText("Error in MoveForward(" + e.getMessage() + ")");
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// BLUE TOOTH CONNECTION MENU METHODS ///////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////

    private void cpf_checkBTPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            cv_txtDebug.setText("BLUETOOTH_SCAN already granted.\n");
        } else {
            cv_txtDebug.setText("BLUETOOTH_SCAN NOT granted.\n");
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            cv_txtDebug.setText("BLUETOOTH_CONNECT NOT granted.\n");
        } else {
            cv_txtDebug.setText("BLUETOOTH_CONNECT already granted.\n");
        }
    }

    // https://www.geeksforgeeks.org/android-how-to-request-permissions-in-android-application/
    // https://stackoverflow.com/questions/67722950/android-12-new-bluetooth-permissions
    private void cpf_requestBTPermissions() {
        // We can give any value but unique for each permission.
        final int BLUETOOTH_SCAN_CODE = 100;
        final int BLUETOOTH_CONNECT_CODE = 101;

        // Android version < 12, "android.permission.BLUETOOTH" just fine
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Toast.makeText(MainActivity.this,
                    "BLUETOOTH granted for earlier Android", Toast.LENGTH_SHORT).show();
            return;
        }

        // Android 12+ has to go through the process
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.BLUETOOTH_SCAN},
                    BLUETOOTH_SCAN_CODE);
        } else {
            Toast.makeText(MainActivity.this,
                    "BLUETOOTH_SCAN already granted", Toast.LENGTH_SHORT).show();
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    BLUETOOTH_CONNECT_CODE);
        } else {
            Toast.makeText(MainActivity.this,
                    "BLUETOOTH_CONNECT already granted", Toast.LENGTH_SHORT).show();
        }
    }

    // Modify from chap14, pp390 findRobot()
    private BluetoothDevice cpf_locateInPairedBTList(String name) {
        BluetoothDevice lv_bd = null;
        try {
            cv_btInterface = BluetoothAdapter.getDefaultAdapter();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            cv_pairedDevices = cv_btInterface.getBondedDevices();
            Iterator<BluetoothDevice> lv_it = cv_pairedDevices.iterator();
            while (lv_it.hasNext()) {
                lv_bd = lv_it.next();
                if (lv_bd.getName().equalsIgnoreCase(name)) {
                    cv_txtDebug.setText(name + " is in paired list");
                    return lv_bd;
                }
            }
            cv_txtDebug.setText(name + " is NOT in paired list");
        } catch (Exception e) {
            cv_txtDebug.setText("Failed in findRobot() " + e.getMessage());
        }
        return null;
    }

    // Modify frmo chap14, pp391 connectToRobot()
    private void cpf_connectToEV3(BluetoothDevice bd) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cv_btSocket = bd.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            cv_btSocket.connect();

            //// HERE
            cv_is = cv_btSocket.getInputStream();
            cv_os = cv_btSocket.getOutputStream();
            cv_txtDebug.setText("Connected to " + bd.getName());
        } catch (Exception e) {
            cv_txtDebug.setText("Error interacting with remote device [" + e.getMessage() + "]");
        }
    }
}