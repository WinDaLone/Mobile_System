package edu.stevens.cs522.chatapp.singleprocess.Service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Pattern;

import edu.stevens.cs522.chatapp.singleprocess.Activity.ChatAppActivity;
import edu.stevens.cs522.chatapp.singleprocess.Entities.Message;
import edu.stevens.cs522.chatapp.singleprocess.Entities.Peer;
import edu.stevens.cs522.chatapp.singleprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.singleprocess.Managers.MessageManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ChatReceiverService extends IntentService {
    private static final String TAG = ChatReceiverService.class.getCanonicalName();
    private static final String SEPARATE_CHAR = "|";
    private static final Pattern SEPARATOR = Pattern.compile(Character.toString(SEPARATE_CHAR.charAt(0)), Pattern.LITERAL);
    public static final int CHAT_SERVER_LOADER_ID = 1;
    private DatagramSocket serverSocket;
    private boolean socketOK = true;
    private MessageManager manager;
    private boolean loop;

    public ChatReceiverService() {
        super("ChatReceiverService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (manager == null) {
                manager = new MessageManager(this, new IEntityCreator<Message>() {
                    @Override
                    public Message create(Cursor cursor) {
                        return new Message(cursor);
                    }
                }, CHAT_SERVER_LOADER_ID);
            }
            if (serverSocket == null) {
                try {
                    int port = ChatAppActivity.clientPort;
                    serverSocket = new DatagramSocket(port);
                } catch (Exception e) {
                    Log.e(TAG, "Cannot open socket " + e.getMessage());
                    return;
                }
            }
            loop = true;
            while (loop) {
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
                        manager.persistAsync(peer, message);
                        Intent broadcastIntent = new Intent(Intent.ACTION_PROVIDER_CHANGED);
                        sendBroadcast(broadcastIntent);
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    socketOK = false;
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        if (socketOK && serverSocket != null) {
            serverSocket.close();
            serverSocket = null;
        }
        super.onDestroy();
    }
}
