/*********************************************************************

 Client for sending chat messages to the server.

 Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/
package edu.stevens.cs522.chat.oneway.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/*
 * @author dduggan
 * 
 */
public class ChatClient extends Activity implements OnClickListener {

    final static private String TAG = ChatClient.class.getSimpleName();

    /*
     * Client name (should be entered in a parent activity, provided as an extra in the intent).
     */
    public static final String CLIENT_NAME_KEY = "client_name";

    public static final String DEFAULT_CLIENT_NAME = "client";

    private String clientName;

    /*
     * Client UDP port (should be entered in a parent activity, provided as an extra in the intent).
     */
    public static final String CLIENT_PORT_KEY = "client_port";

    public static final int DEFAULT_CLIENT_PORT = 6666;

    /*
     * Socket used for sending
     */
    private DatagramSocket clientSocket;


    private int clientPort;
    /*
     * True as long as we don't get socket errors
     */
    private boolean socketOK = true;

    /*
     * Widgets for dest address, message text, send button.
     */
    private EditText destinationHost;

    private EditText destinationPort;

    private EditText messageText;

    private static final String SEPARATE_CHAR = "|";

    public static final int REQUEST_USER = 1;

    /*
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent callingIntent = getIntent();
        if (callingIntent != null && callingIntent.getExtras() != null) {
            clientName = callingIntent.getExtras().getString(CLIENT_NAME_KEY, DEFAULT_CLIENT_NAME);
            clientPort = callingIntent.getExtras().getInt(CLIENT_PORT_KEY, DEFAULT_CLIENT_PORT);

        } else {
            clientName = DEFAULT_CLIENT_NAME;
            clientPort = DEFAULT_CLIENT_PORT;
        }

        /**
         * Let's be clear, this is a HACK to allow you to do network communication on the main thread.
         * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
         * this right in a future assignment (using a Service managing background threads).
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // TODO initialize the UI.
        destinationPort = (EditText)findViewById(R.id.destination_port);
        destinationHost = (EditText)findViewById(R.id.destination_host);
        messageText = (EditText)findViewById(R.id.message_text);
        // End todo

        if(clientSocket == null) {
            try {
                clientSocket = new DatagramSocket(clientPort);

            } catch (Exception e) {
                Log.e(TAG, "Cannot open socket: " + e.getMessage(), e);
                socketOK = false;
                return;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preference:
                Intent intent = new Intent(this, PreferenceActivity.class);
                startActivityForResult(intent, REQUEST_USER);
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_USER && resultCode == RESULT_OK) {
            clientName = data.getStringExtra(PreferenceActivity.USER_KEY);
        }
    }

    /*
                 * Callback for the SEND button.
                 */
    public void onClick(View v) {
        try {
            /*
			 * On the emulator, which does not support WIFI stack, we'll send to
			 * (an AVD alias for) the host loopback interface, with the server
			 * port on the host redirected to the server port on the server AVD.
			 */

            InetAddress destAddr = null;
            

            int destPort = 0;

            byte[] sendData = null;  // Combine sender and message text; default encoding is UTF-8

            // TODO get data from UI

            destPort = Integer.parseInt(destinationPort.getText().toString());
            String host = destinationHost.getText().toString();
            destAddr = InetAddress.getByName(host);

            String toSend = messageText.getText().toString();
            toSend = clientName + SEPARATE_CHAR + toSend;

            sendData = toSend.getBytes(Charset.forName("UTF-8"));

            // End todo

            DatagramPacket sendPacket = new DatagramPacket(sendData,
                    sendData.length, destAddr, destPort);

            clientSocket.send(sendPacket);

            Log.i(TAG, "Sent packet: " + messageText);


        } catch (UnknownHostException e) {
            Log.e(TAG, "Unknown host exception: ", e);
        } catch (IOException e) {
            Log.e(TAG, "IO exception: ", e);
        }


        messageText.setText("");
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
        clientSocket.close();
        clientSocket = null;
    }

    /*
     * If the socket is OK, then it's running
     */
    boolean socketIsOK() {
        return socketOK;
    }
}