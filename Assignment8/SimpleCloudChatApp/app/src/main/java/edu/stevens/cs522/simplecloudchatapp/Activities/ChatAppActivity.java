package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import edu.stevens.cs522.simplecloudchatapp.AckReceiverWrapper;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.ISimpleQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Entities.Synchronize;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;
import edu.stevens.cs522.simplecloudchatapp.R;
import edu.stevens.cs522.simplecloudchatapp.Services.RequestService;


public class ChatAppActivity extends ActionBarActivity {
    public static final String TAG = ChatAppActivity.class.getCanonicalName();
    public static final String DEFAULT_HOST = SettingActivity.DESTINATION_HOST_DEFAULT;
    public static final int DEFAULT_PORT = SettingActivity.DESTINATION_PORT_DEFAULT;
    public static Client client = null;

    public static final int CHAT_APP_LOADER_ID = 1;
    public static final int REQUEST_CODE = 1;
    private ServiceHelper serviceHelper = null;
    private MessageManager manager = null;
    private SimpleCursorAdapter cursorAdapter = null;

    private ListView messageList;
    private EditText messageText;
    private Button sendButton;
    private TextView warningView;

    private String clientName;
    private long clientID;
    private String host;
    private int port;
    private UUID registrationID;
    private AckReceiverWrapper.IReceiver receiver = null;
    private AckReceiverWrapper wrapper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_app);

        messageList = (ListView)findViewById(R.id.message_list);
        messageText = (EditText)findViewById(R.id.message_text);
        warningView = (TextView)findViewById(R.id.warning_view);
        messageText.setText("");
        sendButton = (Button)findViewById(R.id.send_button);
        manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, CHAT_APP_LOADER_ID);
        String[] from = new String[] {MessageContract.MESSAGE_TEXT, ClientContract.NAME};
        int[] to = new int[] {R.id.message_row, R.id.sender_row};
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.message_row, null, from, to, 0);
        messageList.setAdapter(cursorAdapter);

        manager.QueryAsync(MessageContract.CONTENT_URI, new IQueryListener<Message>() {
            @Override
            public void handleResults(TypedCursor<Message> cursor) {
                cursorAdapter.swapCursor(cursor.getCursor());
            }

            @Override
            public void closeResults() {
                cursorAdapter.swapCursor(null);
            }
        });
        serviceHelper = new ServiceHelper(this);
        resetInfo();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = messageText.getText().toString();
                final Message message = new Message("_default", text, new Timestamp(new Date().getTime()));
                manager.persistAsync(message, client);
                messageText.setText("");
                manager.QueryDetail(MessageContract.CONTENT_URI, new ISimpleQueryListener<Message>() {
                    @Override
                    public void handleResults(List<Message> results) {
                        if (results != null) {
                            // Synchrnoize with server
                            long seqnum = 0; // Get sequnum
                            List<Message> messages = new ArrayList<Message>(); // Store messages to be saved
                            for (int i = 0; i < results.size(); i++) {
                                long tempNum = results.get(i).seqnum;
                                if (tempNum == 0) {
                                    messages.add(results.get(i));
                                }
                                if (tempNum > seqnum) {
                                    seqnum = tempNum;
                                }
                            }
                            Synchronize request = new Synchronize(host, port, registrationID, clientID, seqnum, messages);
                            receiver = new AckReceiverWrapper.IReceiver() {
                                @Override
                                public void onReceiveResult(int resultCode, Bundle resultData) {
                                    if (resultCode == RequestService.RESULT_SYNC_OK) {
                                        getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                                        Toast.makeText(getApplicationContext(), "Synchronize successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Failed to synchronize", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            };
                            wrapper = new AckReceiverWrapper(new Handler());
                            wrapper.setReceiver(receiver);
                            serviceHelper.RefreshMessage(request, wrapper);
                        }
                    }
                });
            }
        });
    }

    private void resetInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingActivity.MY_SHARED_PREF, MODE_PRIVATE);
        clientName = sharedPreferences.getString(SettingActivity.PREF_USERNAME, "");
        clientID = sharedPreferences.getLong(SettingActivity.PREF_IDENTIFIER, -1);
        host = sharedPreferences.getString(SettingActivity.PREF_HOST, DEFAULT_HOST);
        port = sharedPreferences.getInt(SettingActivity.PREF_PORT, DEFAULT_PORT);
        registrationID = new UUID(sharedPreferences.getLong(SettingActivity.PREF_REGID_MOST, 0), sharedPreferences.getLong(SettingActivity.PREF_REGID_LEAST, 0));
        if (clientID == -1) {
            Log.i(TAG, "NEED TO REGISTER APP");
            sendButton.setEnabled(false);
            warningView.setText("Please first register the app");

        } else {
            Log.i(TAG, "APP INSTALLATION VALID");
            client = new Client(clientID, clientName, registrationID);
            sendButton.setEnabled(true);
            warningView.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }

        if (id == R.id.clients) {
            Intent intent = new Intent(this, ClientsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Register successfully");
                Toast.makeText(getApplicationContext(), "Register successfully", Toast.LENGTH_SHORT).show();
                resetInfo();
            } else {
                Log.v(TAG, "Register failed");
                Toast.makeText(getApplicationContext(), "Register failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}