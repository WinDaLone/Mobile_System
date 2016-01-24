package edu.stevens.cs522.simplecloudchatapp.Fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.simplecloudchatapp.Activities.ChatAppActivity;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;
import edu.stevens.cs522.simplecloudchatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainChatFragment extends Fragment {
    public static final String TAG = MainChatFragment.class.getCanonicalName();
    private static int MAIN_CHAT_FRAGMENT_LOADER_ID;
    private MessageManager manager = null;
    private SimpleCursorAdapter cursorAdapter = null;

    private Chatroom chatroom;
    private TextView titleView;
    private ListView messageList;
    private Button backButton;

    public MainChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main_chat, container, false);
        messageList = (ListView)rootView.findViewById(R.id.message_list);
        backButton = (Button)rootView.findViewById(R.id.back_button);
        titleView = (TextView)rootView.findViewById(R.id.main_chatroom_title);

        String[] from = new String[] {MessageContract.MESSAGE_TEXT, ClientContract.NAME, MessageContract.LATITUDE, MessageContract.LONGITUDE};
        int[] to = new int[] {R.id.message_row, R.id.sender_row, R.id.latitude_row, R.id.longitude_row};
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.message_row, null, from, to, 0);

        messageList.setAdapter(cursorAdapter);

        chatroom = getArguments().getParcelable(TAG);
        titleView.setText(chatroom.name);
        MAIN_CHAT_FRAGMENT_LOADER_ID = (int)chatroom.id + 10;
        Log.i(TAG, "LOADER_ID in chatroom: " + String.valueOf(MAIN_CHAT_FRAGMENT_LOADER_ID));
        manager = new MessageManager(getActivity(), new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, MAIN_CHAT_FRAGMENT_LOADER_ID);

        manager.QueryAsync(MessageContract.CONTENT_URI, new String[] {chatroom.name}, new IQueryListener<Message>() {
            @Override
            public void handleResults(TypedCursor<Message> cursor) {
                cursorAdapter.swapCursor(cursor.getCursor());
            }

            @Override
            public void closeResults() {
                cursorAdapter.swapCursor(null);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View navigationView = getActivity().findViewById(R.id.main_navigation_drawer);
                boolean MultiPane = navigationView != null && navigationView.getVisibility() == View.VISIBLE;
                if (MultiPane) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    //ft.replace(R.id.main_chatroom_container, new BlankFragment());
                    ft.remove(MainChatFragment.this);
                    ft.commit();
                    getFragmentManager().executePendingTransactions();
                } else {
                    getActivity().finish();
                }
            }
        });
        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_main_chat_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.send) {
            if (ChatAppActivity.clientID < 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Warning").setMessage(R.string.warning_message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            } else {
                SendDialogFragment.launch(getActivity(), TAG, chatroom);
            }
        }


        return super.onOptionsItemSelected(item);
    }
}
