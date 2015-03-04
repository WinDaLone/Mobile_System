package edu.stevens.cs522.chatapp.singleprocess.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    private static final String TAG = ChatAppActivity.class.getCanonicalName();
    private static final int CHAT_SERVER_LOADER_ID = 1;
    private static final String CLIENT_NAME_KEY = "client_name";
    private static final String DEFAULT_CLIENT_NAME = "client";
    private String clientName;
    public static final String CLIENT_PORT_KEY = "client_port";
    public static final int DEFAULT_CLIENT_PORT = 6666;
    private int clientPort;
    Intent intentReceiver = null;
    private MessageManager manager = null;
    private SimpleCursorAdapter cursorAdapter = null;

    private ListView messageList;
    private EditText destinationHost;
    private EditText destinationPort;
    private EditText messageText;
    private Button sendButton;
    public static final int REQUEST_USER = 1;
    private ChatSenderService service;
    IntentFilter filter = new IntentFilter(Intent.ACTION_PROVIDER_CHANGED); // TODO
    Receiver receiver;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ChatSenderService.IChatSendService)binder).getService();
            String destination = destinationHost.getText().toString();
            int port = Integer.parseInt(destinationPort.getText().toString());
            String message = messageText.getText().toString();
            service.send(destination, port, clientName, message);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
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
        sendButton = (Button)findViewById(R.id.send_button);
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
                cursor.getCursor().setNotificationUri(getContentResolver(), MessageContract.CONTENT_URI);
            }

            @Override
            public void closeResults() {
                cursorAdapter.swapCursor(null);
            }
        });
        registerReceiver(receiver, filter);

        intentReceiver = new Intent(this, ChatReceiverService.class);
        startService(intentReceiver);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(ChatAppActivity.this, ChatSenderService.class);
                bindService(sendIntent, connection, Context.BIND_AUTO_CREATE);
            }
        });
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(ChatAppActivity.this, "A message has received", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (intentReceiver != null) {
            stopService(intentReceiver);
        }
        super.onDestroy();
    }
}
