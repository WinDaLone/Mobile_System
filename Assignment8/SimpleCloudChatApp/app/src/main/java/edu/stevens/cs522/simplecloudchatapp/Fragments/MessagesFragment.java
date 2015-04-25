package edu.stevens.cs522.simplecloudchatapp.Fragments;


import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ChatroomContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;
import edu.stevens.cs522.simplecloudchatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {
    public static final String TAG = MessagesFragment.class.getCanonicalName();
    public static int MESSAGES_FRAGMENT_LOADER_ID;
    MessageManager messageManager;
    SimpleCursorAdapter adapter;
    ListView listView;
    TextView nameView;
    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Client client = getArguments().getParcelable(TAG);
        MESSAGES_FRAGMENT_LOADER_ID = (int)client.id + 10;

        nameView = (TextView)view.findViewById(R.id.messages_client_name);
        nameView.setText("Name: " + client.name);
        listView = (ListView)view.findViewById(R.id.messages_list);
        messageManager = new MessageManager(getActivity(), new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, MESSAGES_FRAGMENT_LOADER_ID);
        String[] from = new String[] {ChatroomContract.NAME, MessageContract.MESSAGE_TEXT};
        int[] to = new int[] {R.id.chatroom_client_row, R.id.messages_client_row};
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.client_message_row, null, from, to, 0);
        listView.setAdapter(adapter);
        messageManager.QueryAsync(ClientContract.CONTENT_URI(String.valueOf(client.id)), new IQueryListener<Message>() {
            @Override
            public void handleResults(TypedCursor<Message> cursor) {
                adapter.swapCursor(cursor.getCursor());
                cursor.getCursor().setNotificationUri(getActivity().getContentResolver(), ClientContract.CONTENT_URI(String.valueOf(client.id)));
            }

            @Override
            public void closeResults() {
                adapter.swapCursor(null);
            }
        });

    }
}
