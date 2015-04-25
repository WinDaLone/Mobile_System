package edu.stevens.cs522.simplecloudchatapp.Fragments;


import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import edu.stevens.cs522.simplecloudchatapp.Activities.ClientsActivity;
import edu.stevens.cs522.simplecloudchatapp.Activities.MessagesActivity;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Managers.ClientManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;
import edu.stevens.cs522.simplecloudchatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClientsFragment extends Fragment {
    public static final String TAG = ClientsFragment.class.getCanonicalName();
    public static final int CLIENT_FRAGMENT_LOADER_ID = 2;

    SimpleCursorAdapter cursorAdapter;
    ClientManager manager;
    ListView listView;
    View messageView;

    public ClientsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clients, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        manager = new ClientManager(getActivity(), new IEntityCreator<Client>() {
            @Override
            public Client create(Cursor cursor) {
                return new Client(cursor);
            }
        }, CLIENT_FRAGMENT_LOADER_ID);
        listView = (ListView)view.findViewById(R.id.client_fragment_list);
        String[] from = new String[] {ClientContract.NAME};
        int[] to = new int[] {R.id.client_list};
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.client_list, null, from, to, 0);
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
                messageView = getActivity().findViewById(R.id.message_fragment);
                boolean isMultiPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
                if (isMultiPane) {
                    Fragment fragment = new MessagesFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(MessagesFragment.TAG, client);
                    fragment.setArguments(args);
                    getFragmentManager().beginTransaction().replace(R.id.message_fragment, fragment).commit();
                    getFragmentManager().executePendingTransactions();
                } else {
                    Intent intent = new Intent(getActivity(), MessagesActivity.class);
                    intent.putExtra(ClientsActivity.CLIENT_ACTIVITY_KEY, client);
                    startActivity(intent);
                }
            }
        });
    }
}
