package edu.stevens.cs522.chatapp.singleprocess.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.chatapp.singleprocess.Contracts.MessageContract;
import edu.stevens.cs522.chatapp.singleprocess.Contracts.PeerContract;
import edu.stevens.cs522.chatapp.singleprocess.Entities.Message;
import edu.stevens.cs522.chatapp.singleprocess.Entities.Peer;
import edu.stevens.cs522.chatapp.singleprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.singleprocess.IQueryListener;
import edu.stevens.cs522.chatapp.singleprocess.Managers.MessageManager;
import edu.stevens.cs522.chatapp.singleprocess.Managers.TypedCursor;
import edu.stevens.cs522.chatapp.singleprocess.R;

public class PeerDetailActivity extends Activity {
    public static final int PEER_DETIAL_ACTIVITY_LOADER_ID = 3;
    MessageManager messageManager;
    SimpleCursorAdapter cursorAdapter;
    ListView listView;
    TextView addressView;
    TextView portView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_detail);
        Intent intent = getIntent();
        final Peer peer = intent.getParcelableExtra(PeerActivity.PEER_ACTIVITY_KEY);
        addressView = (TextView) findViewById(R.id.peer_ip);
        portView = (TextView) findViewById(R.id.peer_port);
        addressView.setText("IP Address: " + peer.address.getHostAddress());
        portView.setText("Port: " + peer.port);
        messageManager = new MessageManager(this,
                new IEntityCreator<Message>() {
                    public Message create(Cursor cursor) {
                        return new Message(cursor);
                    }
                }, PEER_DETIAL_ACTIVITY_LOADER_ID);

        String[] from = new String[]{MessageContract.MESSAGE_TEXT};
        int[] to = new int[]{R.id.peer_message};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.peer_message, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView = (ListView) findViewById(R.id.peer_messages);
        listView.setAdapter(cursorAdapter);
        messageManager.QueryAsync(PeerContract.CONTENT_URI(String.valueOf(peer.id)), new IQueryListener<Message>() {
            public void handleResults(TypedCursor<Message> cursor) {
                cursorAdapter.swapCursor(cursor.getCursor());
                cursor.getCursor().setNotificationUri(getContentResolver(), PeerContract.CONTENT_URI(String.valueOf(peer.id)));
            }

            public void closeResults() {
                cursorAdapter.swapCursor(null);
            }
        });
    }
}
