/*********************************************************************

 Chat server: accept chat messages from clients.

 Sender name and GPS coordinates are encoded
 in the messages, and stripped off upon receipt.

 Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/
package edu.stevens.cs522.chat.oneway.server;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Pattern;

import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.MessageManager;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;

public class ChatServer extends Activity implements OnClickListener {

    public static final int CHAT_SERVER_LOADER_ID = 1;
    public static final String TAG = ChatServer.class.getCanonicalName();
    private static final String SEPARATE_CHAR = "|";
    private static final Pattern SEPARATOR = Pattern.compile(Character.toString(SEPARATE_CHAR.charAt(0)), Pattern.LITERAL);

    /*
     * Socket used both for sending and receiving
     */
    private DatagramSocket serverSocket;

    /*
     * True as long as we don't get socket errors
     */
    private boolean socketOK = true;

	/*
     * TODO: Declare UI.
	 */

    private SimpleCursorAdapter cursorAdapter = null;
    private MessageManager manager = null;

	/*
	 * End Todo
	 */

    Button next;

    /*
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /**
         * Let's be clear, this is a HACK to allow you to do network communication on the main thread.
         * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
         * this right in a future assignment (using a Service managing background threads).
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (serverSocket == null) {
            try {
			/*
			 * Get port information from the resources.
			 */
                int port = Integer.parseInt(this.getString(R.string.app_port));
                serverSocket = new DatagramSocket(port);
            } catch (Exception e) {
                Log.e(TAG, "Cannot open socket" + e.getMessage());
                return;
            }
        }

		/*
		 * TODO: Initialize the UI.
		 */
        ListView messageList = (ListView)findViewById(R.id.msgList);
        next = (Button)findViewById(R.id.next);

        manager = new MessageManager(this, new IEntityCreator<Message>() {
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, CHAT_SERVER_LOADER_ID);

        String[] from = new String[] {PeerContract.NAME, MessageContract.MESSAGE_TEXT};
        int[] to = new int[] {R.id.peer_row, R.id.message_row};
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.peer_row, null, from, to, 0);
        messageList.setAdapter(cursorAdapter);

        manager.QueryAsync(MessageContract.CONTENT_URI, new IQueryListener<Message>() {
            public void handleResults(TypedCursor<Message> cursor) {
                cursorAdapter.swapCursor(cursor.getCursor());
                cursor.getCursor().setNotificationUri(getContentResolver(), MessageContract.CONTENT_URI);
            }

            public void closeResults() {
                cursorAdapter.changeCursor(null);
            }
        });
		/*
		 * End Todo
		 */

    }

    public void onClick(View v) {

        byte[] receiveData = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {

            serverSocket.receive(receivePacket);
            Log.i(TAG, "Received a packet");

            InetAddress sourceIPAddress = receivePacket.getAddress();
            Log.i(TAG, "Source IP Address: " + sourceIPAddress);
			
			/*
			 * TODO: Extract sender and receiver from message and display.
			 */
            receiveData = receivePacket.getData();
            String temp = new String(receiveData, 0, receivePacket.getLength());
            if (!temp.isEmpty()) {
                String[] nameAndContent = SEPARATOR.split(temp);
                Peer peer = new Peer(nameAndContent[0], sourceIPAddress, serverSocket.getLocalPort());
                Message message = new Message(nameAndContent[1], nameAndContent[0]);
                manager.persistAsync(peer, message);
                getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
            }
			/*
			 * End Todo
			 */

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            socketOK = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_server, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.peers) {
            Intent intent = new Intent(this, PeerActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onDestroy() {
        if (socketIsOK()) {
            closeSocket();
        }
        super.onDestroy();
    }

    /*
         * Close the socket before exiting application
         */
    public void closeSocket() {
        serverSocket.close();
        serverSocket = null;
    }

    /*
     * If the socket is OK, then it's running
     */
    boolean socketIsOK() {
        return socketOK;
    }

}