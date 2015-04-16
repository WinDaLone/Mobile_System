package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;
import edu.stevens.cs522.simplecloudchatapp.R;

public class MessagesActivity extends ActionBarActivity {
    public static final int MESSAGE_ACTIVITY_LOADER_ID = 3;
    public static final String TAG = MessagesActivity.class.getCanonicalName();
    MessageManager messageManager;
    SimpleCursorAdapter adapter;
    ListView listView;
    TextView nameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Intent intent = getIntent();
        final Client client = intent.getParcelableExtra(ClientsActivity.CLIENT_ACTIVITY_KEY);
        nameView = (TextView)findViewById(R.id.messages_client_name);
        nameView.setText("Name: " + client.name);
        listView = (ListView)findViewById(R.id.messages_list);
        messageManager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, MESSAGE_ACTIVITY_LOADER_ID);

        String[] from = new String[] {MessageContract.MESSAGE_TEXT};
        int[] to = new int[] {R.id.messages_client_row};
        adapter = new SimpleCursorAdapter(this, R.layout.client_message_row, null, from, to, 0);
        listView.setAdapter(adapter);
        messageManager.QueryAsync(ClientContract.CONTENT_URI(String.valueOf(client.id)), new IQueryListener<Message>() {
            @Override
            public void handleResults(TypedCursor<Message> cursor) {
                adapter.swapCursor(cursor.getCursor());
                cursor.getCursor().setNotificationUri(getContentResolver(), ClientContract.CONTENT_URI(String.valueOf(client.id)));
            }

            @Override
            public void closeResults() {
                adapter.swapCursor(null);
            }
        });
    }

}
