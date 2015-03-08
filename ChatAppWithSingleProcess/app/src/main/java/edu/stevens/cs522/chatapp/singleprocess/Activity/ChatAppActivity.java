package edu.stevens.cs522.chatapp.singleprocess.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import edu.stevens.cs522.chatapp.singleprocess.Contracts.MessageContract;
import edu.stevens.cs522.chatapp.singleprocess.Contracts.PeerContract;
import edu.stevens.cs522.chatapp.singleprocess.Entities.Message;
import edu.stevens.cs522.chatapp.singleprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.singleprocess.IQueryListener;
import edu.stevens.cs522.chatapp.singleprocess.Managers.MessageManager;
import edu.stevens.cs522.chatapp.singleprocess.Managers.TypedCursor;
import edu.stevens.cs522.chatapp.singleprocess.R;
import edu.stevens.cs522.chatapp.singleprocess.Service.ChatReceiverService;
import edu.stevens.cs522.chatapp.singleprocess.Service.ChatSenderService;

public class ChatAppActivity extends ActionBarActivity {
    @SuppressWarnings("unused")
    private static final String TAG = ChatAppActivity.class.getCanonicalName();
    private static final int CHAT_SERVER_LOADER_ID = 1;
    private static final String CLIENT_NAME_KEY = "client_name";
    private static final String DEFAULT_CLIENT_NAME = "client";
    public static String clientName;
    public static final String CLIENT_PORT_KEY = "client_port";
    public static final int DEFAULT_CLIENT_PORT = 6666;
    public static int clientPort;
    Intent intentReceiver = null;
    private MessageManager manager = null;
    private SimpleCursorAdapter cursorAdapter = null;

    private ListView messageList;
    private EditText destinationHost;
    private EditText destinationPort;
    private EditText messageText;
    private Intent sendIntent;
    public static final int REQUEST_USER = 1;
    private ChatSenderService service;
    private boolean serviceBound;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ChatSenderService.IChatSendService)binder).getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_app);
        Intent callingIntent = getIntent();
        if (callingIntent != null && callingIntent.getExtras() != null) {
            clientName = callingIntent.getExtras().getString(CLIENT_NAME_KEY);
            clientPort = callingIntent.getExtras().getInt(CLIENT_PORT_KEY);
        } else {
            clientName = DEFAULT_CLIENT_NAME;
            clientPort = DEFAULT_CLIENT_PORT;
        }
        destinationHost = (EditText)findViewById(R.id.destination_host);
        destinationPort = (EditText)findViewById(R.id.destination_port);
        messageText = (EditText)findViewById(R.id.message_text);
        messageList = (ListView)findViewById(R.id.message_list);
        manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, CHAT_SERVER_LOADER_ID);
        String[] from = new String[] {PeerContract.NAME, MessageContract.MESSAGE_TEXT};
        int[] to = new int[] {R.id.peer_row, R.id.message_row};
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.peer_row, null, from, to, 0);
        messageList.setAdapter(cursorAdapter);

        manager.QueryAsync(MessageContract.CONTENT_URI, new IQueryListener<Message>() {
            @Override
            public void handleResults(TypedCursor<Message> cursor) {
                cursorAdapter.swapCursor(cursor.getCursor());
                //cursor.getCursor().setNotificationUri(getContentResolver(), MessageContract.CONTENT_URI);
            }

            @Override
            public void closeResults() {
                cursorAdapter.swapCursor(null);
            }
        });
        // Start receiver service
        intentReceiver = new Intent(this, ChatReceiverService.class);
        startService(intentReceiver);
    }

    @Override
    protected void onStart() {
        // Bind sender service
        sendIntent = new Intent(ChatAppActivity.this, ChatSenderService.class);
        bindService(sendIntent, connection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    public void onClick(View view) {
        String destination = destinationHost.getText().toString();
        int port = Integer.parseInt(destinationPort.getText().toString());
        String message = messageText.getText().toString();
        service.send(destination, port, clientName, message);
        messageText.setText("");
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
        Intent intent;
        switch (id) {
            case R.id.menu_preference:
                intent = new Intent(this, PreferenceActivity.class);
                startActivityForResult(intent, REQUEST_USER);
                return true;
            case R.id.peers:
                intent = new Intent(this, PeerActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_USER && resultCode == RESULT_OK) {
            clientName = data.getStringExtra(PreferenceActivity.USER_KEY);
        }
    }

    @Override
    protected void onStop() {
        if (serviceBound) {
            unbindService(connection);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (intentReceiver != null) {
            stopService(intentReceiver);
        }
        super.onDestroy();
    }

    public static class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Show the toast
            Toast.makeText(context, "A message has been received", Toast.LENGTH_LONG).show();
            context.getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
        }
    }
}
