package edu.stevens.cs522.chatapp.separatedprocess.Service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Pattern;

import edu.stevens.cs522.chatapp.separatedprocess.Activity.ChatAppActivity;
import edu.stevens.cs522.chatapp.separatedprocess.Entities.Message;
import edu.stevens.cs522.chatapp.separatedprocess.Entities.Peer;
import edu.stevens.cs522.chatapp.separatedprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.separatedprocess.Managers.MessageManager;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class ChatReceiverService extends IntentService {
    private static final String TAG = ChatReceiverService.class.getCanonicalName();
    private static final String SEPARATE_CHAR = "|";
    private static final Pattern SEPARATOR = Pattern.compile(Character.toString(SEPARATE_CHAR.charAt(0)), Pattern.LITERAL);
    public static final int CHAT_SERVER_LOADER_ID = 1;
    private DatagramSocket serverSocket;
    private boolean socketOK = true;
    private MessageManager manager;
    private boolean isStopped = false;

    public ChatReceiverService() {
        super("ChatReceiverService");
    }

    @Override
    public void onCreate() {
        manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, CHAT_SERVER_LOADER_ID);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (serverSocket == null) {
                try {
                    int port = ChatAppActivity.clientPort;
                    serverSocket = new DatagramSocket(port);
                } catch (Exception e) {
                    Log.e(TAG, "Cannot open socket " + e.getMessage());
                    return;
                }
            }
            while (serverSocket != null && !serverSocket.isClosed()) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    Log.i(TAG, "Received a pakcet");
                    InetAddress sourceIPAddress = receivePacket.getAddress();
                    Log.i(TAG, "Source IP Address: " + sourceIPAddress);
                    receiveData = receivePacket.getData();
                    int serverPort = receivePacket.getPort();
                    String temp = new String(receiveData, 0, receivePacket.getLength());
                    if (!temp.isEmpty()) {
                        String[] nameAndContent = SEPARATOR.split(temp);
                        Peer peer = new Peer(nameAndContent[0], sourceIPAddress, serverPort);
                        Message message = new Message(nameAndContent[1], nameAndContent[0]);
                        // Since the IntentService is already run on a Worker Thread, just persist
                        // data directly
                        manager.persistSync(peer, message);
                        Intent broadcastIntent = new Intent(Intent.ACTION_PROVIDER_CHANGED);
                        sendBroadcast(broadcastIntent);
                    }
                } catch (IOException e) {
                    if (e.getMessage().compareTo("Socket closed") != 0) {
                        Log.e(TAG, e.getMessage());
                        socketOK = false;
                    }
                }
            }
            isStopped = true;
        }
    }
    @Override
    public void onDestroy() {
        if (socketOK && serverSocket != null) {
            serverSocket.close();
        }
        while(true){
            if (isStopped) {
                break;
            }
        }
        serverSocket = null;
        super.onDestroy();
    }
}

