package com.example.remotecontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    String serIpAddress;       // адрес сервера
    int port = 10000;           // порт
    String msg;                 // Сообщение
    final byte codeMsg = 1;     // Оправить сообщение
    final byte codeRotate = 2;  // Повернуть экран
    final byte codePoff = 3;    // Выключить компьютер
    byte codeCommand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        // получаем строку в поле ip адреса
        EditText etIPaddress = (EditText) findViewById(R.id.edIPaddress);
        serIpAddress = etIPaddress.getText().toString();
        // если поле не заполнено, то выводим сообщение об ошибке
        if (serIpAddress.isEmpty()) {
            Toast msgToast = Toast.makeText(this, "Введите ip адрес", Toast.LENGTH_SHORT);
            msgToast.show();
            return;
        }
        SenderThread sender = new SenderThread(); // объект представляющий поток отправки сообщений
        switch (v.getId()) // id кнопок
        {
            case R.id.btnSMsg: // отправить сообщение
                EditText etMsg = (EditText) findViewById(R.id.etMsg);
                msg = etMsg.getText().toString();
                if (!msg.isEmpty()) {
                    codeCommand = codeMsg;
                    sender.execute();
                }
                else { // Если сообщение не задано, то сообщаем об этом
                    Toast msgToast = Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT);
                    msgToast.show();
                }
                break;
            case R.id.btnRotate: // поворот
                codeCommand = codeRotate;
                sender.execute();
                break;
            case R.id.btnPowerOff: // выключить
                codeCommand = codePoff;
                sender.execute();
                break;
            case R.id.btn_touchpad:
                Intent intent = new Intent(this, TouchpadActivity.class);
                intent.putExtra("serIpAdress", serIpAddress);
                intent.putExtra("port", port);
                startActivity(intent);
                break;
        }
    }


    class SenderThread extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // ip адрес сервера
                InetAddress ipAddress = InetAddress.getByName(serIpAddress);
                // Создаем сокет
                Socket socket = new Socket(ipAddress, port);
                // Получаем потоки ввод/вывода
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream out = new DataOutputStream(outputStream);
                switch (codeCommand) { // В зависимости от кода команды посылаем сообщения
                    case codeMsg:	// Сообщение
                        out.write(codeMsg);
                        // Устанавливаем кодировку символов UTF-8
                        byte[] outMsg = msg.getBytes("UTF8");
                        out.write(outMsg);
                        break;
                    case codeRotate: // Поворот экрана
                        out.write(codeRotate);
                        break;
                    case codePoff: // Выключить
                        out.write(codePoff);
                        break;
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            return null;
        }
    }

}

