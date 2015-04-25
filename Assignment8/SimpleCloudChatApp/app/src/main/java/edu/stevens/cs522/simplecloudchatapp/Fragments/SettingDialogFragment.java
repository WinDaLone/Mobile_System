package edu.stevens.cs522.simplecloudchatapp.Fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IRegisterListener;
import edu.stevens.cs522.simplecloudchatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingDialogFragment extends DialogFragment {
    public static final String TAG = SettingDialogFragment.class.getCanonicalName();
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

    private EditText textHost;
    private EditText textPort;
    private EditText textClientName;
    private Button saveButton;
    private Button cancelButton;



    public SettingDialogFragment() {
        // Required empty public constructor
    }

    public IRegisterListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IRegisterListener)) {
            throw new IllegalStateException("Activity mush implement IRegisterListener.");
        }
        listener = (IRegisterListener)activity;
    }

    public static void launch(Activity context, String tag) {
        SettingDialogFragment fragment = new SettingDialogFragment();
        fragment.show(context.getFragmentManager(), tag);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SettingDialogFragment.MY_SHARED_PREF, getActivity().MODE_PRIVATE);
        textHost = (EditText)view.findViewById(R.id.destination_host);
        textPort = (EditText)view.findViewById(R.id.destination_port);
        textClientName = (EditText)view.findViewById(R.id.client_name);
        saveButton = (Button)view.findViewById(R.id.register_save_button);
        cancelButton = (Button)view.findViewById(R.id.register_cancel_button);
        textHost.setText(sharedPreferences.getString(PREF_HOST, DESTINATION_HOST_DEFAULT));
        textPort.setText(String.valueOf(sharedPreferences.getInt(PREF_PORT, DESTINATION_PORT_DEFAULT)));
        textClientName.setText(sharedPreferences.getString(PREF_USERNAME, CLIENT_NAME_DEFAULT));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = textHost.getText().toString();
                int port = Integer.parseInt(textPort.getText().toString());
                String clientName = textClientName.getText().toString();
                listener.register(host, port, clientName);
                SettingDialogFragment.this.getDialog().cancel();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingDialogFragment.this.getDialog().cancel();
            }
        });
    }


}
