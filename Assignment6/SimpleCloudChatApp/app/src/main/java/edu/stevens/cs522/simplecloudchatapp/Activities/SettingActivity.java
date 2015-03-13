package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.R;

public class SettingActivity extends ActionBarActivity {
    public static final String TAG = SettingActivity.class.getCanonicalName();
    public static final String MY_SHARED_PREF = "edu.stevens.cs522.simplecloudchatapp.SharedPreference";
    public static final String PREF_USERNAME = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.username";
    public static final String PREF_IDENTIFIER = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.identifier";
    public static final String PREF_REGID_MOST = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.regid.most";
    public static final String PREF_REGID_LEAST = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.regid.least";
    public static final String PREF_HOST = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.host";
    public static final String PREF_PORT = "edu.stevens.cs522.simplecloudchatapp.SharedPreference.port";

    private EditText textHost;
    private EditText textPort;
    private EditText textClientName;
    private Button saveButton;

    private ServiceHelper serviceHelper = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        textHost = (EditText)findViewById(R.id.destination_host);
        textPort = (EditText)findViewById(R.id.destination_port);
        textClientName = (EditText)findViewById(R.id.client_name);
        saveButton = (Button)findViewById(R.id.save_setting_button);
        serviceHelper = new ServiceHelper(this);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = textHost.getText().toString();
                int port = Integer.parseInt(textPort.getText().toString());
                String clientName = textClientName.getText().toString();
                Register register = new Register(host, port, clientName);
                boolean res = serviceHelper.RegisterUser(register);
                if (res) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        });
    }
}
