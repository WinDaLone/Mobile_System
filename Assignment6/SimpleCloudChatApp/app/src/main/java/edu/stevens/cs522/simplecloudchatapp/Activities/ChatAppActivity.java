package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Entities.PostMessage;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;
import edu.stevens.cs522.simplecloudchatapp.R;


public class ChatAppActivity extends ActionBarActivity {
    public static final String TAG = ChatAppActivity.class.getCanonicalName();
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8080;

    public static final int CHAT_APP_LOADER_ID = 1;
    public static final int REQUEST_CODE = 1;
    private ServiceHelper serviceHelper = null;
    private MessageManager manager = null;
    private SimpleCursorAdapter cursorAdapter = null;

    private ListView messageList;
    private EditText messageText;
    private Button sendButton;

    private String clientName;
    private long clientID;
    private String host;
    private int port;
    private UUID registrationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_app);

        messageList = (ListView)findViewById(R.id.message_list);
        messageText = (EditText)findViewById(R.id.message_text);
        sendButton = (Button)findViewById(R.id.send_button);
        manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, CHAT_APP_LOADER_ID);
        String[] from = new String[] {MessageContract.MESSAGE_TEXT};
        int[] to = new int[] {R.id.message_row};
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
                String message = messageText.getText().toString();
                Date date = new Date();
                PostMessage postMessage = new PostMessage(host, port, registrationID, clientID, "_default", new Timestamp(date.getTime()), message);
                boolean res = serviceHelper.PostMessage(postMessage);
                if (res) {
                    getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                    Log.v(TAG, "Message post success");
                } else {
                    Log.e(TAG, "Message post failed");
                }
            }
        });
    }

    private void resetInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingActivity.MY_SHARED_PREF, MODE_PRIVATE);
        clientName = sharedPreferences.getString(SettingActivity.PREF_USERNAME, "");
        clientID = sharedPreferences.getLong(SettingActivity.PREF_IDENTIFIER, 0);
        host = sharedPreferences.getString(SettingActivity.PREF_HOST, DEFAULT_HOST);
        port = sharedPreferences.getInt(SettingActivity.PREF_PORT, DEFAULT_PORT);
        registrationID = new UUID(sharedPreferences.getLong(SettingActivity.PREF_REGID_MOST, 0), sharedPreferences.getLong(SettingActivity.PREF_REGID_LEAST, 0));
        if (clientID == 0) {
            sendButton.setEnabled(false);
        } else {
            sendButton.setEnabled(true);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.v(SettingActivity.TAG, "Register successfully");
                resetInfo();
            } else {
                Log.v(SettingActivity.TAG, "Register failed");
            }
        }
    }
}
