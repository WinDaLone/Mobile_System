package edu.stevens.cs522.simplecloudchatapp.Fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import edu.stevens.cs522.simplecloudchatapp.AckReceiverWrapper;
import edu.stevens.cs522.simplecloudchatapp.Activities.ChatAppActivity;
import edu.stevens.cs522.simplecloudchatapp.Activities.SettingActivity;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.ISimpleQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Entities.Synchronize;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;
import edu.stevens.cs522.simplecloudchatapp.R;
import edu.stevens.cs522.simplecloudchatapp.Services.RequestService;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainChatFragment extends Fragment {
    public static final String TAG = MainChatFragment.class.getCanonicalName();

    private ServiceHelper serviceHelper = null;
    private MessageManager manager = null;
    private SimpleCursorAdapter cursorAdapter = null;

    private Chatroom chatroom;

    private ListView messageList;
    private EditText messageText;
    private Button sendButton;
    private Button backButton;
    private TextView warningView;

    private AckReceiverWrapper.IReceiver receiver = null;
    private AckReceiverWrapper wrapper = null;

    public MainChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messageList = (ListView)view.findViewById(R.id.message_list);
        messageText = (EditText)view.findViewById(R.id.message_text);
        sendButton = (Button)view.findViewById(R.id.send_button);
        backButton = (Button)view.findViewById(R.id.back_button);
        warningView = (TextView)view.findViewById(R.id.warning_view);
        messageText.setText("");
        manager = new MessageManager(getActivity(), new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, ChatAppActivity.CHAT_APP_LOADER_ID);

        chatroom = getArguments().getParcelable(TAG);

        String[] from = new String[] {MessageContract.MESSAGE_TEXT, ClientContract.NAME};
        int[] to = new int[] {R.id.message_row, R.id.sender_row};
        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.message_row, null, from, to, 0);
        messageList.setAdapter(cursorAdapter);
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
        serviceHelper = new ServiceHelper(getActivity());
        resetInfo();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = messageText.getText().toString();
                //final Message message = new Message(chatroom.id, text, new Timestamp(new Date().getTime()));
                Message message = new Message(text, new Timestamp(new Date().getTime()));
                manager.persistAsync(message, ChatAppActivity.client, chatroom);
                messageText.setText("");
                manager.QueryDetail(MessageContract.CONTENT_URI, new ISimpleQueryListener<Message>() {
                    @Override
                    public void handleResults(List<Message> results) {
                        if (results != null) {
                            // Synchrnoize with server
                            long seqnum = 0; // Get sequnum
                            List<Message> messages = new ArrayList<Message>(); // Store messages to be saved
                            for (int i = 0; i < results.size(); i++) {
                                long tempNum = results.get(i).seqnum;
                                if (tempNum == 0) {
                                    messages.add(results.get(i));
                                }
                                if (tempNum > seqnum) {
                                    seqnum = tempNum;
                                }
                            }
                            Synchronize request = new Synchronize(ChatAppActivity.host, ChatAppActivity.port, ChatAppActivity.registrationID, ChatAppActivity.clientID, seqnum, messages);
                            receiver = new AckReceiverWrapper.IReceiver() {
                                @Override
                                public void onReceiveResult(int resultCode, Bundle resultData) {
                                    if (resultCode == RequestService.RESULT_SYNC_OK) {
                                        getActivity().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                                        Toast.makeText(getActivity(), "Synchronize successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), "Failed to synchronize", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            };
                            wrapper = new AckReceiverWrapper(new Handler());
                            wrapper.setReceiver(receiver);
                            serviceHelper.RefreshMessage(request, wrapper);
                        }
                    }
                });
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View navigationView = getActivity().findViewById(R.id.main_navigation_drawer);
                boolean MultiPane = navigationView != null && navigationView.getVisibility() == View.VISIBLE;
                if (MultiPane) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.main_chatroom_container, null);
                    ft.commit();
                    getFragmentManager().executePendingTransactions();
                } else {
                    getActivity().finish();
                }
            }
        });
    }


    private void resetInfo() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SettingActivity.MY_SHARED_PREF, getActivity().MODE_PRIVATE);
        ChatAppActivity.clientName = sharedPreferences.getString(SettingActivity.PREF_USERNAME, "");
        ChatAppActivity.clientID = sharedPreferences.getLong(SettingActivity.PREF_IDENTIFIER, -1);
        ChatAppActivity.host = sharedPreferences.getString(SettingActivity.PREF_HOST, ChatAppActivity.DEFAULT_HOST);
        ChatAppActivity.port = sharedPreferences.getInt(SettingActivity.PREF_PORT, ChatAppActivity.DEFAULT_PORT);
        ChatAppActivity.registrationID = new UUID(sharedPreferences.getLong(SettingActivity.PREF_REGID_MOST, 0), sharedPreferences.getLong(SettingActivity.PREF_REGID_LEAST, 0));
        if (ChatAppActivity.clientID == -1) {
            Log.i(TAG, "NEED TO REGISTER APP");
            sendButton.setEnabled(false);
            warningView.setText("Please first register the app");

        } else {
            Log.i(TAG, "APP INSTALLATION VALID");
            ChatAppActivity.client = new Client(ChatAppActivity.clientID, ChatAppActivity.clientName, ChatAppActivity.registrationID);
            sendButton.setEnabled(true);
            warningView.setText("");
        }
    }
}
