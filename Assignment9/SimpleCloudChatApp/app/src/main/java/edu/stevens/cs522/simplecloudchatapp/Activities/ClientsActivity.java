package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Fragments.MessagesFragment;
import edu.stevens.cs522.simplecloudchatapp.R;

public class ClientsActivity extends ActionBarActivity {
    public static final String TAG = ClientsActivity.class.getCanonicalName();
    public static final String CLIENT_ACTIVITY_KEY = TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients);
        Client client = null;
        if (getIntent().hasExtra(MessagesActivity.TAG)) {
            client = getIntent().getParcelableExtra(MessagesActivity.TAG);
        }
        boolean isMultiPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (isMultiPane && client != null) {
            Fragment fragment = new MessagesFragment();
            Bundle args = new Bundle();
            args.putParcelable(MessagesFragment.TAG, client);
            fragment.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.message_fragment, fragment).commit();
            getFragmentManager().executePendingTransactions();
        }
    }
}
