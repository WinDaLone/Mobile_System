package edu.stevens.cs522.simplecloudchatapp.Fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.sql.Timestamp;
import java.util.Date;

import edu.stevens.cs522.simplecloudchatapp.Activities.ChatAppActivity;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IRegisterListener;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SendDialogFragment extends DialogFragment {
    public static final String TAG = SendDialogFragment.class.getCanonicalName();

    private EditText messageText;
    private Button sendButton;
    private Button cancelButton;
    private IRegisterListener listener;


    public SendDialogFragment() {
        // Required empty public constructor
    }

    public static void launch(Activity context, String tag, Chatroom chatroom) {
        SendDialogFragment dialog = new SendDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(TAG, chatroom);
        dialog.setArguments(args);
        dialog.show(context.getFragmentManager(), tag);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IRegisterListener)) {
            throw new IllegalStateException("Activity mush implement IRegisterListener.");
        }
        listener = (IRegisterListener)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_dialog, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messageText = (EditText)view.findViewById(R.id.message_text);
        messageText.setText("");
        sendButton = (Button)view.findViewById(R.id.send_button);
        cancelButton = (Button)view.findViewById(R.id.send_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendDialogFragment.this.getDialog().cancel();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = messageText.getText().toString();
                Message message = new Message(text, new Timestamp(new Date().getTime()), ChatAppActivity.client.longitude, ChatAppActivity.client.latitude);
                Chatroom chatroom = getArguments().getParcelable(TAG);
                listener.send(message, chatroom);
                SendDialogFragment.this.getDialog().cancel();
            }
        });
    }
}
