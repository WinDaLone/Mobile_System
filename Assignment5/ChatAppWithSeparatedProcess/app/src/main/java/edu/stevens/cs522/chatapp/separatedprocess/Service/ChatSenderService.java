package edu.stevens.cs522.chatapp.separatedprocess.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;

import edu.stevens.cs522.chatapp.separatedprocess.Activity.ChatAppActivity;

public class ChatSenderService extends Service {
    private static final String TAG = ChatSenderService.class.getCanonicalName();

    public static final int MESSAGE_OBTAIN_KEY = 1;
    public static final String DESTINATION_KEY = "edu.stevens.cs522.chatapp.destination";
    public static final String PORT_KEY = "edu.stevens.cs522.chatapp.port";
    public static final String MESSAGE_KEY = "edu.stevens.cs522.chatapp.message";
    public static final String SOURCE_KEY = "edu.stevens.cs522.chatapp.source";

    private Messenger messenger;
    private Handler handler;
    private DatagramSocket clientSocket = null;
    private boolean socketOK = true;

    private static final String SEPARATE_CHAR = "|";
    private static final int DEFAULT_SENDER_PORT = 6667;

    private ResultReceiver resultReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        resultReceiver = intent.getParcelableExtra(ChatAppActivity.ACK);
        return messenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread messengerThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        messengerThread.start();
        Looper messengerLooper = messengerThread.getLooper();
        handler = new MessageHandler(messengerLooper);
        messenger = new Messenger(handler);
    }

    private class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String dest = data.getString(DESTINATION_KEY);
            String source = data.getString(SOURCE_KEY);
            String message = data.getString(MESSAGE_KEY);
            int port = data.getInt(PORT_KEY);
            if (clientSocket == null) {
                try {
                    clientSocket = new DatagramSocket(DEFAULT_SENDER_PORT);
                } catch (Exception e) {
                    Log.e(TAG, "Cannot open socket: " + e.getMessage());
                    socketOK = false;
                    return;
                }
            }
            // TODO: Handle action send
            try {
                InetAddress destAddr = InetAddress.getByName(dest);
                String toSend = source + SEPARATE_CHAR + message;
                byte[] sendData = toSend.getBytes(Charset.forName("UTF-8"));
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destAddr, port);
                clientSocket.send(sendPacket);
                Log.i(TAG, "Send packet: " + message);
                resultReceiver.send(ChatAppActivity.RESULT_ACK_OK, null);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        if (socketOK && clientSocket != null) {
            clientSocket.close();
            clientSocket = null;
        }
        super.onDestroy();
    }
}