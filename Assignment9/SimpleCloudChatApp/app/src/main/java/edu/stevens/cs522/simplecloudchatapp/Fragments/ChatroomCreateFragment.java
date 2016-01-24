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

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IRegisterListener;
import edu.stevens.cs522.simplecloudchatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatroomCreateFragment extends DialogFragment {
    EditText chatroomText;
    Button createButton;
    Button cancelButton;
    private IRegisterListener listener;

    public static void launch(Activity context, String tag) {
        ChatroomCreateFragment dialog = new ChatroomCreateFragment();
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

    public ChatroomCreateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chatroom_create, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatroomText = (EditText)view.findViewById(R.id.chatroom_name);
        chatroomText.setText("");
        createButton = (Button)view.findViewById(R.id.chatroom_create_button);
        cancelButton = (Button)view.findViewById(R.id.chatroom_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatroomCreateFragment.this.getDialog().cancel();
            }
        });
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = chatroomText.getText().toString();
                listener.createChatroom(name);
                ChatroomCreateFragment.this.getDialog().cancel();
            }
        });
    }

}
