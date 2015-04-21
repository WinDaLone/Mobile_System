package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.R;


public class ChatAppActivity extends ActionBarActivity {
    public static final String TAG = ChatAppActivity.class.getCanonicalName();
    public static final String DEFAULT_HOST = SettingActivity.DESTINATION_HOST_DEFAULT;
    public static final int DEFAULT_PORT = SettingActivity.DESTINATION_PORT_DEFAULT;
    public static Client client = null;

    public static final int CHAT_APP_LOADER_ID = 1;
    public static final int REQUEST_CODE = 1;



    public static String clientName;
    public static long clientID;
    public static String host;
    public static int port;
    public static UUID registrationID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_app);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_app, menu);
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
            Intent intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }

        if (id == R.id.clients) {
            Intent intent = new Intent(this, ClientsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Register successfully");
                Toast.makeText(getApplicationContext(), "Register successfully", Toast.LENGTH_SHORT).show();
                resetInfo();
            } else {
                Log.v(TAG, "Register failed");
                Toast.makeText(getApplicationContext(), "Register failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingActivity.MY_SHARED_PREF, MODE_PRIVATE);
        clientName = sharedPreferences.getString(SettingActivity.PREF_USERNAME, "");
        clientID = sharedPreferences.getLong(SettingActivity.PREF_IDENTIFIER, -1);
        host = sharedPreferences.getString(SettingActivity.PREF_HOST, DEFAULT_HOST);
        port = sharedPreferences.getInt(SettingActivity.PREF_PORT, DEFAULT_PORT);
        registrationID = new UUID(sharedPreferences.getLong(SettingActivity.PREF_REGID_MOST, 0), sharedPreferences.getLong(SettingActivity.PREF_REGID_LEAST, 0));
    }
}