package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Fragments.MainChatFragment;
import edu.stevens.cs522.simplecloudchatapp.R;

public class MainChatActivity extends ActionBarActivity {
    public static final String TAG = MainChatActivity.class.getCanonicalName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        Chatroom chatroom = getIntent().getParcelableExtra(TAG);
        Fragment fragment = new MainChatFragment();
        Bundle args = new Bundle();
        args.putParcelable(MainChatFragment.TAG, chatroom);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.main_chatroom_container, fragment).commit();
        getFragmentManager().executePendingTransactions();
    }

}
