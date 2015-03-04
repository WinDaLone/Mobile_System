package edu.stevens.cs522.chatapp.singleprocess.Service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Pattern;

import edu.stevens.cs522.chatapp.singleprocess.Activity.ChatAppActivity;
import edu.stevens.cs522.chatapp.singleprocess.Contracts.MessageContract;
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
public class ChatReceiverService extends Service {
    private static final String TAG = ChatReceiverService.class.getCanonicalName();
    private static final String SEPARATE_CHAR = "|";
    private static final Pattern SEPARATOR = Pattern.compile(Character.toString(SEPARATE_CHAR.charAt(0)), Pattern.LITERAL);
    public static final int CHAT_SERVER_LOADER_ID = 1;
    private DatagramSocket serverSocket;
    private boolean socketOK = true;
    private MessageManager manager;
    private boolean loop;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, CHAT_SERVER_LOADER_ID);
        // TODO: Handle action Foo
        if (serverSocket == null) {
            try {
                int port = ChatAppActivity.clientPort;
                serverSocket = new DatagramSocket(port);
            } catch (Exception e) {
                Log.e(TAG, "Cannot open socket" + e.getMessage());
                return START_STICKY;
            }
        }
        loop = true;
        handleReceiver();
        return START_STICKY;
    }

    protected void handleReceiver() {
        while (loop) {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
                Log.i(TAG, "Received a packet");

                InetAddress sourceIPAddress = receivePacket.getAddress();
                Log.i(TAG, "Source IP Address: " + sourceIPAddress);

                receiveData = receivePacket.getData();
                String temp = new String(receiveData, 0, receivePacket.getLength());
                if (!temp.isEmpty()) {
                    String[] nameAndContent = SEPARATOR.split(temp);
                    Peer peer = new Peer(nameAndContent[0], sourceIPAddress, serverSocket.getLocalPort());
                    Message message = new Message(nameAndContent[1], nameAndContent[0]);
                    manager.persistAsync(peer, message);
                    getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                    Intent broadcastIntent = new Intent(Intent.ACTION_PROVIDER_CHANGED);
                    sendBroadcast(broadcastIntent); // send the broadcast
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                socketOK = false;
            }
        }
    }


    @Override
    public void onDestroy() {
        loop = false;
        if (socketOK && serverSocket != null) {
            serverSocket.close();
            serverSocket = null;
        }
        super.onDestroy();
    }
}
