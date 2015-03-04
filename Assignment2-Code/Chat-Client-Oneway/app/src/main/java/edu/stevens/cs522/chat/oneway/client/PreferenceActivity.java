package edu.stevens.cs522.chat.oneway.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PreferenceActivity extends Activity {
    public static final String USER_KEY = "myPrefUsername";

    public static final String MY_PREFS = "myPreferences";
    public static final String USERNAME = "username";

    EditText userText;
    Button buttonOk;
    Button buttonCancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        userText = (EditText)findViewById(R.id.text_username);
        buttonOk = (Button)findViewById(R.id.button_ok);
        buttonCancel = (Button)findViewById(R.id.button_cancel);
        userText.setText(loadPreferences());
        buttonOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String username = userText.getText().toString();
                savePreferences(username);
                Intent intent = new Intent(PreferenceActivity.this, ChatClient.class);
                intent.putExtra(USER_KEY, username);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    protected void savePreferences(String chatUsername) {
        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // store new object
        editor.putString(USERNAME, chatUsername);

        // commit
        editor.apply();
    }

    public String loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS, Activity.MODE_PRIVATE);
        return sharedPreferences.getString(USERNAME, "client");
    }
}
