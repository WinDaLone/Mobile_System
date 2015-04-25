package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IRegisterListener;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Fragments.MainChatFragment;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.R;

public class MainChatActivity extends ActionBarActivity implements IRegisterListener {
    public static final String TAG = MainChatActivity.class.getCanonicalName();

    public static final int MAIN_CHAT_ACTIVITY_LOADER_ID = 7;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        Chatroom chatroom = getIntent().getParcelableExtra(TAG);
        final boolean MultiPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (MultiPane) {
            Intent intent = new Intent(this, ChatAppActivity.class);
            intent.putExtra(TAG, chatroom);
            startActivity(intent);
            finish();
        }
        Fragment fragment = new MainChatFragment();
        Bundle args = new Bundle();
        args.putParcelable(MainChatFragment.TAG, chatroom);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.main_chatroom_container, fragment).commit();
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public void register(String host, int port, String name) {
        return;
    }

    @Override
    public void createChatroom(String name) {
        return;
    }

    @Override
    public void send(Message message, Chatroom chatroom) {
        MessageManager manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, MAIN_CHAT_ACTIVITY_LOADER_ID);
        manager.persistAsync(message, ChatAppActivity.client, chatroom);
    }
}
