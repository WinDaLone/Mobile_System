package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

import edu.stevens.cs522.simplecloudchatapp.AckReceiverWrapper;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IHandleResult;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IRegisterListener;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Fragments.ChatroomCreateFragment;
import edu.stevens.cs522.simplecloudchatapp.Fragments.MainChatFragment;
import edu.stevens.cs522.simplecloudchatapp.Fragments.SettingDialogFragment;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.Managers.ChatroomManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.R;
import edu.stevens.cs522.simplecloudchatapp.Services.RequestService;


public class ChatAppActivity extends ActionBarActivity implements IRegisterListener {
    public static final String TAG = ChatAppActivity.class.getCanonicalName();
    public static final String DEFAULT_HOST = SettingDialogFragment.DESTINATION_HOST_DEFAULT;
    public static final int DEFAULT_PORT = SettingDialogFragment.DESTINATION_PORT_DEFAULT;
    public static Client client = null;

    public static final int CHAT_APP_LOADER_ID = 1;

    public static String clientName;
    public static long clientID;
    public static String host;
    public static int port;
    public static UUID registrationID;


    private ServiceHelper serviceHelper = null;
    private AckReceiverWrapper.IReceiver receiver = null;
    private AckReceiverWrapper wrapper = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_app);
        serviceHelper = new ServiceHelper(this);
        reGetInfo();
        final boolean MultiPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        Chatroom chatroom = null;
        if (getIntent().hasExtra(MainChatActivity.TAG)) {
            chatroom = getIntent().getExtras().getParcelable(MainChatActivity.TAG);
        }
        if (chatroom != null && MultiPane) {
            Fragment fragment = new MainChatFragment();
            Bundle args = new Bundle();
            args.putParcelable(MainChatFragment.TAG, chatroom);
            fragment.setArguments(args);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_chatroom_container, fragment);
            ft.commit();
            getFragmentManager().executePendingTransactions();
        }
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
            SettingDialogFragment.launch(this, TAG);
        }

        if (id == R.id.clients) {
            Intent intent = new Intent(this, ClientsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.create_chatroom) {
            if (clientID < 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Please register first!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            } else {
                ChatroomCreateFragment.launch(this, TAG);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void reGetInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingDialogFragment.MY_SHARED_PREF, MODE_PRIVATE);
        long mst = sharedPreferences.getLong(SettingDialogFragment.PREF_REGID_MOST, 0);
        if (mst == 0) {
            registrationID = UUID.randomUUID();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(SettingDialogFragment.PREF_REGID_MOST, registrationID.getMostSignificantBits());
            editor.putLong(SettingDialogFragment.PREF_REGID_LEAST, registrationID.getLeastSignificantBits());
            editor.apply();
        } else {
            registrationID = new UUID(sharedPreferences.getLong(SettingDialogFragment.PREF_REGID_MOST, 0), sharedPreferences.getLong(SettingDialogFragment.PREF_REGID_LEAST, 0));
        }
        Log.i(TAG, "UUID: " + registrationID.toString());
        clientName = sharedPreferences.getString(SettingDialogFragment.PREF_USERNAME, "");
        clientID = sharedPreferences.getLong(SettingDialogFragment.PREF_IDENTIFIER, -1);
        host = sharedPreferences.getString(SettingDialogFragment.PREF_HOST, ChatAppActivity.DEFAULT_HOST);
        port = sharedPreferences.getInt(SettingDialogFragment.PREF_PORT, ChatAppActivity.DEFAULT_PORT);
        if (clientID < 0) {
            Log.i(TAG, "NEED TO REGISTER APP");
        } else {
            Log.i(TAG, "APP INSTALLATION VALID");
            client = new Client(clientID, clientName, registrationID);
        }
    }

    @Override
    public void register(String host, int port, String name) {
        if (name.equals("")) {
            Toast.makeText(ChatAppActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
            return;
        }
        Register register = new Register(host, port, name, registrationID);
        receiver = new AckReceiverWrapper.IReceiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RequestService.RESULT_REGISTER_OK) {
                    reGetInfo();
                    Toast.makeText(ChatAppActivity.this, "Register success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatAppActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
        wrapper = new AckReceiverWrapper(new Handler());
        wrapper.setReceiver(receiver);
        serviceHelper.RegisterUser(register, wrapper);
    }

    @Override
    public void createChatroom(String name) {
        Chatroom chatroom = new Chatroom(name);
        ChatroomManager manager = new ChatroomManager(this, new IEntityCreator<Chatroom>() {
            @Override
            public Chatroom create(Cursor cursor) {
                return new Chatroom(cursor);
            }
        }, CHAT_APP_LOADER_ID);
        manager.InsertAsync(chatroom, new IHandleResult() {
            @Override
            public void onHandleResult(Chatroom chatroom) {
                if (chatroom == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatAppActivity.this);
                    builder.setTitle("Chatroom already exists!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                } else {
                    final boolean MultiPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
                    if (!MultiPane) { // Portrait mode
                        Intent intent = new Intent(ChatAppActivity.this, MainChatActivity.class);
                        intent.putExtra(MainChatActivity.TAG, chatroom);
                        startActivity(intent);
                    } else {
                        FragmentTransaction ft;
                        Fragment fragment = new MainChatFragment();
                        Bundle args = new Bundle();
                        args.putParcelable(MainChatFragment.TAG, chatroom);
                        fragment.setArguments(args);
                        ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.main_chatroom_container, fragment);
                        ft.commit();
                        getFragmentManager().executePendingTransactions();
                    }
                }
            }
        });
    }

    @Override
    public void send(Message message, Chatroom chatroom) {
        MessageManager manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, CHAT_APP_LOADER_ID);
        manager.persistAsync(message, client, chatroom);
    }
}