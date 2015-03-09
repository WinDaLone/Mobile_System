package edu.stevens.cs522.chatapp.singleprocess.Service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ChatSenderService extends Service {
    private static final String TAG = ChatSenderService.class.getCanonicalName();

    private DatagramSocket clientSocket = null;
    private boolean socketOK = true;

    private static final String SEPARATE_CHAR = "|";
    //private static final int DEFAULT_SENDER_PORT = 6667;

    private final IBinder binder = new IChatSendService();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    public class IChatSendService extends Binder {
        public ChatSenderService getService() {
            return ChatSenderService.this;
        }
    }

    public void send(final String dest, final int port, final String source, final String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (clientSocket == null) {
                    try {
                        clientSocket = new DatagramSocket();
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
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        thread.start();
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
