package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

import edu.stevens.cs522.simplecloudchatapp.AckReceiverWrapper;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.R;
import edu.stevens.cs522.simplecloudchatapp.Services.RequestService;

public class SettingActivity extends ActionBarActivity {
    public static final String TAG = SettingActivity.class.getCanonicalName();
    public static final String MY_SHARED_PREF = "edu.stevens.cs522.simplecloudchatapp.SharedPreference";
    public static final String PREF_USERNAME = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.username";
    public static final String PREF_IDENTIFIER = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.identifier";
    public static final String PREF_REGID_MOST = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.regid.most";
    public static final String PREF_REGID_LEAST = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.regid.least";
    public static final String PREF_HOST = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.host";
    public static final String PREF_PORT = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.port";

    public static final String DESTINATION_HOST_DEFAULT = "127.0.0.1";
    public static final int DESTINATION_PORT_DEFAULT = 8080;
    public static final String CLIENT_NAME_DEFAULT = "";

    private static UUID uuid;
    private EditText textHost;
    private EditText textPort;
    private EditText textClientName;
    private Button saveButton;

    private ServiceHelper serviceHelper = null;
    private AckReceiverWrapper.IReceiver receiver = null;
    private AckReceiverWrapper wrapper = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        SharedPreferences sharedPreferences = getSharedPreferences(MY_SHARED_PREF, MODE_PRIVATE);
        long mst = sharedPreferences.getLong(PREF_REGID_MOST, 0);
        if (mst == 0) {
            uuid = UUID.randomUUID();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(PREF_REGID_MOST, uuid.getMostSignificantBits());
            editor.putLong(PREF_REGID_LEAST, uuid.getLeastSignificantBits());
            editor.apply();
        } else {
            uuid = new UUID(sharedPreferences.getLong(PREF_REGID_MOST, 0), sharedPreferences.getLong(PREF_REGID_LEAST, 0));
        }
        Log.i(TAG, "UUID: " + uuid.toString());
        textHost = (EditText)findViewById(R.id.destination_host);
        textPort = (EditText)findViewById(R.id.destination_port);
        textClientName = (EditText)findViewById(R.id.client_name);
        saveButton = (Button)findViewById(R.id.save_setting_button);
        textHost.setText(sharedPreferences.getString(PREF_HOST, DESTINATION_HOST_DEFAULT));
        textPort.setText(String.valueOf(sharedPreferences.getInt(PREF_PORT, DESTINATION_PORT_DEFAULT)));
        textClientName.setText(sharedPreferences.getString(PREF_USERNAME, CLIENT_NAME_DEFAULT));
        serviceHelper = new ServiceHelper(this);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = textHost.getText().toString();
                int port = Integer.parseInt(textPort.getText().toString());
                String clientName = textClientName.getText().toString();
                Register register = new Register(host, port, clientName, uuid);
                receiver = new AckReceiverWrapper.IReceiver() {
                    @Override
                    public void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == RequestService.RESULT_REGISTER_OK) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }
                };
                wrapper = new AckReceiverWrapper(new Handler());
                wrapper.setReceiver(receiver);
                serviceHelper.RegisterUser(register, wrapper);
            }
        });
    }
}
