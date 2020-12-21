package com.example.bombgame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.example.bombgame.Constants.*;
import static com.example.bombgame.Enums.*;

public class PlayScreen extends AppCompatActivity implements View.OnClickListener {

    BluetoothDevice HM_10;

    BluetoothManager blManager;
    BluetoothAdapter blAdapter;

    BluetoothGatt blGatt;
    BluetoothGattCallback blGattCallback;

    List<BluetoothGattService> blGattServicesList;
    List<BluetoothGattCharacteristic> blGattCharacteristicsList;
    BluetoothGattService blGattService = null;
    BluetoothGattCharacteristic blGattCharacteristic = null;

    int messagesSent = 0;

    EditText etCodeLength, etTimeToDefuse;
    TextView tvTitle, tvGameResult;

    Button  btnKey1, btnKey2, btnKey3,
            btnKey4, btnKey5, btnKey6,
            btnKey7, btnKey8, btnKey9,
                     btnKey0;

    Button  btnStartGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);
        getSupportActionBar().hide();

        tvTitle = findViewById(R.id.tvTitle);
        tvGameResult = findViewById(R.id.tvGameResult);

        etCodeLength = findViewById(R.id.etCodeLength);
        etTimeToDefuse = findViewById(R.id.etTimeToDefuse);

        btnStartGame = findViewById(R.id.btnStartGame);
        btnStartGame.setOnClickListener(this);

        btnKey0 = findViewById(R.id.btnKey0);
        btnKey0.setOnClickListener(this);
        btnKey1 = findViewById(R.id.btnKey1);
        btnKey1.setOnClickListener(this);
        btnKey2 = findViewById(R.id.btnKey2);
        btnKey2.setOnClickListener(this);
        btnKey3 = findViewById(R.id.btnKey3);
        btnKey3.setOnClickListener(this);
        btnKey4 = findViewById(R.id.btnKey4);
        btnKey4.setOnClickListener(this);
        btnKey5 = findViewById(R.id.btnKey5);
        btnKey5.setOnClickListener(this);
        btnKey6 = findViewById(R.id.btnKey6);
        btnKey6.setOnClickListener(this);
        btnKey7 = findViewById(R.id.btnKey7);
        btnKey7.setOnClickListener(this);
        btnKey8 = findViewById(R.id.btnKey8);
        btnKey8.setOnClickListener(this);
        btnKey9 = findViewById(R.id.btnKey9);
        btnKey9.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartGame:
                int codeLength, timeToDefuse;

                try {
                    codeLength = Integer.parseInt(etCodeLength.getText().toString());

                    if (codeLength < MIN_CODE_LENGTH) {
                        codeLength = 1;
                    } else if (codeLength > MAX_CODE_LENGTH) {
                        codeLength = MAX_CODE_LENGTH;
                    }
                } catch (NumberFormatException e) {
                    codeLength = DEFAULT_CODE_LENGTH;
                }

                try {
                    timeToDefuse = Integer.parseInt(etTimeToDefuse.getText().toString());

                    if (timeToDefuse < MIN_TIME_TO_DEFUSE) {
                        timeToDefuse = MIN_TIME_TO_DEFUSE;
                    } else if (timeToDefuse > MAX_TIME_TO_DEFUSE) {
                        timeToDefuse = MAX_TIME_TO_DEFUSE;
                    }
                } catch (NumberFormatException e) {
                    timeToDefuse = DEFAULT_TIME_TO_DEFUSE;
                }

                startGame(codeLength, timeToDefuse);
                break;
            case R.id.btnKey0:
                sendDigitToArduino(0);
                break;
            case R.id.btnKey1:
                sendDigitToArduino(1);
                break;
            case R.id.btnKey2:
                sendDigitToArduino(2);
                break;
            case R.id.btnKey3:
                sendDigitToArduino(3);
                break;
            case R.id.btnKey4:
                sendDigitToArduino(4);
                break;
            case R.id.btnKey5:
                sendDigitToArduino(5);
                break;
            case R.id.btnKey6:
                sendDigitToArduino(6);
                break;
            case R.id.btnKey7:
                sendDigitToArduino(7);
                break;
            case R.id.btnKey8:
                sendDigitToArduino(8);
                break;
            case R.id.btnKey9:
                sendDigitToArduino(9);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        blManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        blAdapter = blManager.getAdapter();

        // Включен ли блютуз
        if (!blAdapter.isEnabled()) {
            Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btEnableIntent, 0);
        }

        // Предоставлен ли доступ к геолокации
        if (!(ContextCompat.checkSelfPermission(PlayScreen.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(PlayScreen.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        HM_10 = blAdapter.getRemoteDevice(HM_10_MAC);

        initializeBluetoothGattCallback();
        HM_10.connectGatt(PlayScreen.this, false, blGattCallback);
    }

    private void initializeBluetoothGattCallback() {
        blGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        blGatt = gatt;
                        blGatt.discoverServices();
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    debugMsg("Связь с бомбой потеряна, перезапустите приложение");
                    gatt.close();
                }
            }

            @Override
            public void onServicesDiscovered (BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    blGattServicesList = blGatt.getServices();

                    // Ищем нужный нам сервис
                    for (BluetoothGattService blS : blGattServicesList) {
                        if (blS.getUuid().equals(HM_10_CUSTOM_SERVICE)) {
                            blGattService = blS;
                            break;
                        }
                    }

                    // Ищем нужную нам характеристику
                    blGattCharacteristicsList = blGattService.getCharacteristics();
                    for (BluetoothGattCharacteristic blC : blGattCharacteristicsList) {
                        if (blC.getUuid().equals(HM_10_CUSTOM_CHARACTERISTIC)) {
                            blGattCharacteristic = blC;
                            break;
                        }
                    }

                    sendHandshakeToArduino();
                }
            }

            @Override
            public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] bytes = characteristic.getValue();
                if (bytes[0] == (byte) 0xF0 && bytes[1] == (byte) 0x0F && bytes[2] == (byte) 0xAA) {
                    if (bytes[4] == (byte) MESSAGES_CODES.MSG_WIN.ordinal()) {
                        endGame(true);
                    } else if (bytes[4] == (byte) MESSAGES_CODES.MSG_LOSE.ordinal()) {
                        endGame(false);
                    }
                }
            }
        };
    }

    public void startGame(int codeLength, int timeToDefuse) {
        if (blGattCharacteristic == null) {
            debugMsg("Нет подключения к ардуино");
            return;
        }

        tvTitle.setText("Бомба...");
        tvGameResult.setText("");

        etCodeLength.setVisibility(View.GONE);
        etTimeToDefuse.setVisibility(View.GONE);
        btnStartGame.setVisibility(View.GONE);

        btnKey0.setVisibility(View.VISIBLE);
        btnKey1.setVisibility(View.VISIBLE);
        btnKey2.setVisibility(View.VISIBLE);
        btnKey3.setVisibility(View.VISIBLE);
        btnKey4.setVisibility(View.VISIBLE);
        btnKey5.setVisibility(View.VISIBLE);
        btnKey6.setVisibility(View.VISIBLE);
        btnKey7.setVisibility(View.VISIBLE);
        btnKey8.setVisibility(View.VISIBLE);
        btnKey9.setVisibility(View.VISIBLE);

        sendParamsToArduino(timeToDefuse, codeLength);
    }

    public void fillByteArrayPreamble(byte[] arr) {
        arr[0] = (byte) 0xF0;
        arr[1] = (byte) 0x0F;
        arr[2] = (byte) 0xAA;
        arr[3] = (byte) messagesSent++;
    }

    public byte getCRC(byte[] arr) {
        byte CRC = 0;

        int arrLength = arr.length;
        for (int i = 0; i < arrLength - 1; ++i) {
            CRC += arr[i];
        }

        return CRC;
    }

    public void writeCharacteristic(byte[] valueToWrite) {

        blGatt.setCharacteristicNotification(blGattCharacteristic, true);

        blGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        blGattCharacteristic.setValue(valueToWrite);

        blGatt.writeCharacteristic(blGattCharacteristic);
    }

    public void sendHandshakeToArduino() {
        byte[] valueToWrite = new byte[PACKET_MIN_LENGTH];

        fillByteArrayPreamble(valueToWrite);

        valueToWrite[4] = (byte) MESSAGES_CODES.MSG_HANDSHAKE.ordinal();

        valueToWrite[5] = getCRC(valueToWrite);

        writeCharacteristic(valueToWrite);
    }

    public void sendDigitToArduino(int digit) {
        byte[] valueToWrite = new byte[PACKET_MIN_LENGTH + 1];

        fillByteArrayPreamble(valueToWrite);

        valueToWrite[4] = (byte) MESSAGES_CODES.MSG_INPUT.ordinal();
        valueToWrite[5] = (byte) digit;

        valueToWrite[6] = getCRC(valueToWrite);

        writeCharacteristic(valueToWrite);
    }

    public void sendParamsToArduino(int timeToDefuse, int codeLength) {
        byte[] valueToWrite = new byte[PACKET_MIN_LENGTH + 2];

        fillByteArrayPreamble(valueToWrite);

        valueToWrite[4] = (byte) MESSAGES_CODES.MSG_START.ordinal();
        valueToWrite[5] = (byte) timeToDefuse;
        valueToWrite[6] = (byte) codeLength;

        valueToWrite[7] = getCRC(valueToWrite);

        writeCharacteristic(valueToWrite);
    }

    public void endGame(boolean win) {
        final Boolean b = win;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (b) {
                    tvGameResult.setTextColor(Color.GREEN);
                    tvGameResult.setText("...обезврежена!");
                } else {
                    tvGameResult.setTextColor(Color.RED);
                    tvGameResult.setText("...взорвалась");
                }

                etCodeLength.setVisibility(View.VISIBLE);
                etTimeToDefuse.setVisibility(View.VISIBLE);
                btnStartGame.setVisibility(View.VISIBLE);

                btnKey0.setVisibility(View.GONE);
                btnKey1.setVisibility(View.GONE);
                btnKey2.setVisibility(View.GONE);
                btnKey3.setVisibility(View.GONE);
                btnKey4.setVisibility(View.GONE);
                btnKey5.setVisibility(View.GONE);
                btnKey6.setVisibility(View.GONE);
                btnKey7.setVisibility(View.GONE);
                btnKey8.setVisibility(View.GONE);
                btnKey9.setVisibility(View.GONE);
            }
        });
    }

    public void debugMsg(String msg) {
        final String str = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PlayScreen.this, str, Toast.LENGTH_LONG).show();
            }
        });
    }
}