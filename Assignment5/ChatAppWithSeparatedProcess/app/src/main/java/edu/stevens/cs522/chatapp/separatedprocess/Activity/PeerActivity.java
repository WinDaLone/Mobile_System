package edu.stevens.cs522.chatapp.separatedprocess.Activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import edu.stevens.cs522.chatapp.separatedprocess.Contracts.PeerContract;
import edu.stevens.cs522.chatapp.separatedprocess.Entities.Peer;
import edu.stevens.cs522.chatapp.separatedprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.separatedprocess.IQueryListener;
import edu.stevens.cs522.chatapp.separatedprocess.Managers.PeerManager;
import edu.stevens.cs522.chatapp.separatedprocess.Managers.TypedCursor;
import edu.stevens.cs522.chatapp.separatedprocess.R;

public class PeerActivity extends ListActivity {
    public static final int PEER_ACTIVITY_LOADER_ID = 2;
    public static final String PEER_ACTIVITY_KEY = "edu.stevens.cs522.chatapp.separatedprocess.PeerActivity";
    SimpleCursorAdapter cursorAdapter;
    PeerManager manager = null;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer);
        manager = new PeerManager(this,
                new IEntityCreator<Peer>() {
                    public Peer create(Cursor cursor) {
                        return new Peer(cursor);
                    }
                }, PEER_ACTIVITY_LOADER_ID);

        listView = (ListView)findViewById(android.R.id.list);
        String[] from = new String[] {PeerContract.NAME};
        int[] to = new int[] {R.id.peer_list};
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.peer_list, null, from, to, 0);
        setListAdapter(cursorAdapter);
        manager.QueryAsync(PeerContract.CONTENT_URI,
                new IQueryListener<Peer>() {
                    public void handleResults(TypedCursor<Peer> cursor) {
                        cursorAdapter.swapCursor(cursor.getCursor());
                        //cursor.getCursor().setNotificationUri(getContentResolver(), PeerContract.CONTENT_URI);
                    }

                    public void closeResults() {
                        cursorAdapter.swapCursor(null);
                    }
                });

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                Cursor cursor = cursorAdapter.getCursor();
                cursor.moveToPosition(position);
                Peer peer = new Peer(cursor);
                peer.id = PeerContract.getId(cursor);
                Intent mIntent = new Intent(PeerActivity.this, PeerDetailActivity.class);
                mIntent.putExtra(PEER_ACTIVITY_KEY, peer);
                startActivity(mIntent);
            }
        });
    }
}
