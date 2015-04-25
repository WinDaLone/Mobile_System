package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Fragments.MessagesFragment;
import edu.stevens.cs522.simplecloudchatapp.R;

public class MessagesActivity extends ActionBarActivity {
    public static final String TAG = MessagesActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Intent intent = getIntent();
        final Client client = intent.getParcelableExtra(ClientsActivity.CLIENT_ACTIVITY_KEY);
        final boolean MultiPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (MultiPane) {
            Intent back = new Intent(this, ClientsActivity.class);
            back.putExtra(TAG, client);
            startActivity(back);
            finish();
        }
        Fragment fragment = new MessagesFragment();
        Bundle args = new Bundle();
        args.putParcelable(MessagesFragment.TAG, client);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.message_fragment, fragment).commit();
        getFragmentManager().executePendingTransactions();
    }

}
