package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Managers.ClientManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;
import edu.stevens.cs522.simplecloudchatapp.R;

public class ClientsActivity extends ActionBarActivity {
    public static final String TAG = ClientsActivity.class.getCanonicalName();
    public static final String CLIENT_ACTIVITY_KEY = TAG;
    public static final int CLIENT_ACTIVITY_LOADER_ID = 2;
    SimpleCursorAdapter cursorAdapter;
    ClientManager manager;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients);
        manager = new ClientManager(this, new IEntityCreator<Client>() {
            @Override
            public Client create(Cursor cursor) {
                return new Client(cursor);
            }
        }, CLIENT_ACTIVITY_LOADER_ID);
        listView = (ListView)findViewById(android.R.id.list);
        String[] from = new String[] {ClientContract.NAME};
        int[] to = new int[] {R.id.client_list};
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.client_list, null, from, to, 0);
        listView.setAdapter(cursorAdapter);
        manager.QueryAsync(ClientContract.CONTENT_URI, new IQueryListener<Client>() {
            @Override
            public void handleResults(TypedCursor<Client> cursor) {
                cursorAdapter.swapCursor(cursor.getCursor());
            }

            @Override
            public void closeResults() {
                cursorAdapter.swapCursor(null);
            }
        });
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = cursorAdapter.getCursor();
                cursor.moveToPosition(position);
                Client client = new Client(cursor);
                client.id = ClientContract.getClientId(cursor);
                Intent intent = new Intent(ClientsActivity.this, MessagesActivity.class);
                intent.putExtra(CLIENT_ACTIVITY_KEY, client);
                startActivity(intent);
            }
        });
    }
}
