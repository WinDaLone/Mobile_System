package edu.stevens.cs522.simplecloudchatapp.Services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.ResultReceiver;
import android.util.Log;

import java.sql.Timestamp;
import java.util.ArrayList;

import edu.stevens.cs522.simplecloudchatapp.Activities.SettingActivity;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IContinue;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ChatroomContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Entities.Response;
import edu.stevens.cs522.simplecloudchatapp.Entities.Synchronize;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.RequestProcessor;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class RequestService extends IntentService {
    public static final String TAG = RequestService.class.getCanonicalName();
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_REGISTER = "edu.stevens.cs522.simplecloudchatapp.Services.action.REGISTER";
    public static final String ACTION_POST_MESSAGE = "edu.stevens.cs522.simplecloudchatapp.Services.action.POST_MESSAGE";
    public static final String ACTION_REFRESH =  "edu.stevens.cs522.simplecloudchatapp.Services.action.REFRESH";

    private static int messageCount = 1;
    public static final int RESULT_REGISTER_OK = 0;
    public static final int RESULT_MESSAGE_OK = 1;

    public static final int RESULT_SYNC_OK = 2;
    public static final int RESULT_FAILED = 3;

    public static final int REQUEST_SERVICE_LOADERID = 2;
    private RequestProcessor requestProcessor = null;
    private MessageManager manager;
    private ResultReceiver resultReceiver;

    @Override
    public void onCreate() {
        manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, REQUEST_SERVICE_LOADERID);
        super.onCreate();
    }

    public RequestService() {
        super("RequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            resultReceiver = intent.getParcelableExtra(ServiceHelper.ACK);
            requestProcessor = new RequestProcessor(this);
            if (ACTION_REGISTER.equals(intent.getAction())) {
                handleRegister(intent);
            } else if (ACTION_POST_MESSAGE.equals(intent.getAction())) {
                stopSelf();
            } else if (ACTION_REFRESH.equals(intent.getAction())) {
                handleRefresh(intent);
            } else {
                throw new UnsupportedOperationException("Unsupported Action");
            }
        }
        stopSelf();
    }

    private void handleRegister(Intent intent) {
        final Register request = intent.getParcelableExtra(ServiceHelper.REQUEST_KEY);
        requestProcessor.perform(request, new IContinue<Response>() {
            @Override
            public void kontinue(Response value) {
                if (value != null && value.isValid()) {
                    Response.RegisterResponse response = (Response.RegisterResponse)value;
                    request.clientID = response.id;
                    Log.i("Client to be saved: ", request.clientName + " " + request.host + ":" + String.valueOf(request.port) + " " + request.clientID);
                    SharedPreferences preferences = getSharedPreferences(SettingActivity.MY_SHARED_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SettingActivity.PREF_USERNAME, request.clientName);
                    editor.putLong(SettingActivity.PREF_IDENTIFIER, request.clientID);
                    editor.putString(SettingActivity.PREF_HOST, request.host);
                    editor.putInt(SettingActivity.PREF_PORT, request.port);
                    editor.apply();
                    Client client = new Client(request.clientID, request.clientName, request.registrationID);
                    ContentValues values = new ContentValues();
                    client.writeToProvider(values);
                    Uri uri = getContentResolver().insert(ClientContract.CONTENT_URI, values);
                    if (ClientContract.getClientId(uri) > 0) {
                        Log.i(TAG, "Register save successfully");
                        resultReceiver.send(RESULT_REGISTER_OK, null);
                    } else {
                        Log.i(TAG, "Register success but failed to store in database");
                        resultReceiver.send(RESULT_REGISTER_OK, null);
                    }
                } else {
                    Log.i(TAG, "Register save failed");
                    resultReceiver.send(RESULT_FAILED, null);
                }
            }
        });
    }

    private void handleRefresh(Intent intent) {
        Synchronize request = intent.getParcelableExtra(ServiceHelper.REQUEST_KEY);
        requestProcessor.perform(request, new IContinue<Response>() {
            @Override
            public void kontinue(Response value) {
                if (value != null && value.isValid()) {
                    Response.SyncResponse response = (Response.SyncResponse)value;
                    ArrayList<String> clients = response.client;
                    ArrayList<ContentValues> messages = response.messageValues;
                    ArrayList<Client> clientEntities = new ArrayList<Client>();
                    Log.i(TAG, "Start synchronize clients");
                    Cursor clientCursor = getContentResolver().query(ClientContract.CONTENT_URI, null, null, null, null);
                    if (clientCursor.moveToFirst()) { // Store all clients
                        do {
                            for (int i = 0; i < clients.size(); i++) {
                                String client = clients.get(i);
                                if (client.equals(ClientContract.getName(clientCursor))) {
                                    Client temp = new Client(clientCursor);
                                    clientEntities.add(temp);
                                } else {
                                    Client temp = new Client(client);
                                    temp.id = i;
                                    manager.persistSync(temp);
                                    if (temp.id != 0) {
                                        clientEntities.add(temp);
                                    }
                                }
                            }
                        } while (clientCursor.moveToNext());
                    } else { // No client in the database
                        for (String client : clients) {
                            Client temp = new Client(client);
                            manager.persistSync(temp);
                            if (temp.id != 0) {
                                clientEntities.add(temp);
                            }
                        }
                    }
                    clientCursor.close();
                    Log.i(TAG, "Client update finished, start synchrnize message");
                    for (ContentValues values : messages) {
                        String name = values.getAsString(ClientContract.NAME);
                        for (Client client : clientEntities) {
                            if (client.name.equals(name)) { // find match
                                Cursor cursor = getContentResolver().query(MessageContract.CONTENT_URI, new String[] {MessageContract.MESSAGE_ID, MessageContract.TIMESTAMP, MessageContract.CHATROOM_FK, MessageContract.MESSAGE_TEXT},
                                        MessageContract.TIMESTAMP + "=? AND " + MessageContract.MESSAGE_TEXT + "=? AND " + MessageContract.SENDER_ID + "=?",
                                        new String[] {String.valueOf(values.getAsLong(MessageContract.TIMESTAMP)), values.getAsString(MessageContract.MESSAGE_TEXT), String.valueOf(client.id)},
                                        null);
                                if (cursor.moveToFirst()) { // match a message, update the message
                                    ContentValues valueToUpdate = new ContentValues();
                                    valueToUpdate.put(MessageContract.SEQNUM, values.getAsLong(MessageContract.SEQNUM));
                                    long rowId = getContentResolver().update(MessageContract.CONTENT_URI(String.valueOf(MessageContract.getMessageId(cursor))), valueToUpdate, null, null);
                                    if (rowId > 0) {
                                        Log.i(TAG, "Update a message");
                                    }
                                } else { // find no match, insert the message
                                    String chatroom_str = values.getAsString(ChatroomContract.NAME);
                                    String text = values.getAsString(MessageContract.MESSAGE_TEXT);
                                    long timestamp = values.getAsLong(MessageContract.TIMESTAMP);
                                    long seqnum = values.getAsLong(MessageContract.SEQNUM);
                                    Message message = new Message(text, new Timestamp(timestamp));
                                    message.seqnum = seqnum;
                                    message.chatroom = chatroom_str;
                                    Chatroom chatroom = new Chatroom(chatroom_str);
                                    manager.persistSync(message, client, chatroom);
                                }
                                cursor.close();
                            }
                        }
                    }
                    Log.i(TAG, "Messages update finished");
                    resultReceiver.send(RESULT_SYNC_OK, null);
                    // TODO
                } else {
                    Log.i(TAG, "SYNCHRONIZE FAILED, NO RESPONSE");
                    resultReceiver.send(RESULT_FAILED, null);
                }
            }
        });
    }
}
