package com.example.remotecontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TouchpadActivity extends AppCompatActivity {

    String serIpAddress;    // адрес сервера
    int port;               // порт
    final byte codeMove = 4; // Оправить сообщение
    byte codeCommand;
    private int MouseX;
    private int MouseY;
    private int Height;
    private int Width;
    SenderThread sender;

    public class TouchpadGLSurfaceView extends GLSurfaceView {


        @Override
        public boolean onTouchEvent(MotionEvent e) {
            MouseX = (int) e.getX();
            MouseY = (int) e.getY();
            Height = getHeight();
            Width = getWidth();

            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    sender = new SenderThread();
                    sender.execute();
                    break;
            }
            return true;
        }

        public TouchpadGLSurfaceView(Context context) { super(context); }
    }

    public class TouchpadRenderer implements Renderer{
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
            gl.glClearDepthf(1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {

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
                    case codeMove:	// Сообщение
                        out.write(codeMove);
                        // Устанавливаем кодировку символов UTF-8
                        String mousePosition = MouseX + " " + MouseY;
                        byte[] outPos = mousePosition.getBytes("UTF8");
                        out.write(outPos);
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

    TouchpadGLSurfaceView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new TouchpadGLSurfaceView(this);
        TouchpadRenderer touchpadRenderer = new TouchpadRenderer();
        view.setRenderer(touchpadRenderer);
        setContentView(view);

    }
}