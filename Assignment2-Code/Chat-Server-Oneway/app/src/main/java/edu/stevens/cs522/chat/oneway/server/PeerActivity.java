package edu.stevens.cs522.chat.oneway.server;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.PeerManager;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;

public class PeerActivity extends ListActivity {
    public static final int PEER_ACTIVITY_LOADER_ID = 2;
    public static final String PEER_ACTIVITY_KEY = "edu.stevens.cs522.chat.oneway.server.PeerActivity";
    SimpleCursorAdapter cursorAdapter;
    PeerManager manager = null;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
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
                        cursor.getCursor().setNotificationUri(getContentResolver(), PeerContract.CONTENT_URI);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_peer, menu);
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

}
