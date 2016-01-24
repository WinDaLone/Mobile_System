package edu.stevens.cs522.simplecloudchatapp.Services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.stevens.cs522.simplecloudchatapp.Activities.ChatAppActivity;
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
import edu.stevens.cs522.simplecloudchatapp.Entities.Unregister;
import edu.stevens.cs522.simplecloudchatapp.Fragments.SettingDialogFragment;
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
    public static final String ACTION_UNREGISTER = "edu.stevens.cs522.simplecloudchatapp.Services.action.UNREGISTER";
    public static final String ACTION_REFRESH =  "edu.stevens.cs522.simplecloudchatapp.Services.action.REFRESH";

    public static final int RESULT_REGISTER_OK = 0;
    public static final int RESULT_UNREGISTER_OK = 1;
    public static final int RESULT_SYNC_OK = 2;
    public static final int RESULT_FAILED = 3;

    public static final int REQUEST_SERVICE_LOADER_ID = 2;
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
        }, REQUEST_SERVICE_LOADER_ID);
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
            } else if (ACTION_UNREGISTER.equals(intent.getAction())) {
                handleUnregister(intent);
            } else if (ACTION_REFRESH.equals(intent.getAction())) {
                handleRefresh(intent);
            } else {
                throw new UnsupportedOperationException("Unsupported Action");
            }
        }
        stopSelf();
    }

    private void handleUnregister(Intent intent) {
        final Unregister request = intent.getParcelableExtra(ServiceHelper.REQUEST_KEY);
        requestProcessor.perform(request, new IContinue<Boolean>() {
            @Override
            public void kontinue(Boolean value) {
                if (value) {
                    int rowId = getContentResolver().delete(ClientContract.CONTENT_URI(String.valueOf(ChatAppActivity.client.id)), null, null);
                    if (rowId > 0) {
                        Log.i(TAG, "Unregister successfully");
                        resultReceiver.send(RESULT_UNREGISTER_OK, null);
                    } else {
                        Log.i(TAG, "Unregister successfully but can't delete database");
                        resultReceiver.send(RESULT_UNREGISTER_OK, null);
                    }
                } else {
                    Log.i(TAG, "Unregister failed");
                    resultReceiver.send(RESULT_FAILED, null);
                }
            }
        });
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
                    SharedPreferences preferences = getSharedPreferences(SettingDialogFragment.MY_SHARED_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SettingDialogFragment.PREF_USERNAME, request.clientName);
                    editor.putLong(SettingDialogFragment.PREF_IDENTIFIER, request.clientID);
                    editor.putString(SettingDialogFragment.PREF_HOST, request.host);
                    editor.putInt(SettingDialogFragment.PREF_PORT, request.port);
                    editor.apply();
                    Client client = new Client(request.clientID, request.clientName, request.registrationID, ChatAppActivity.longitude, ChatAppActivity.latitude);
                    client.address = getAddress(client);
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
                    ArrayList<ContentValues> clients = response.clientValues;
                    ArrayList<ContentValues> messages = response.messageValues;
                    ArrayList<Client> clientEntities = new ArrayList<Client>();
                    Log.i(TAG, "Start synchronize clients");
                    Cursor clientCursor = getContentResolver().query(ClientContract.CONTENT_URI, null, null, null, null);
                    if (clientCursor.moveToFirst()) { // Store all clients
                        do {
                            for (int i = 0; i < clients.size(); i++) {
                                ContentValues client = clients.get(i);
                                if (client.getAsString(ClientContract.NAME).equals(ClientContract.getName(clientCursor))) {
                                    Log.i(TAG, "client exists, add to clientEntities");
                                    Client temp = new Client(clientCursor);
                                    clientEntities.add(temp);
                                } else {
                                    Log.i(TAG, "client doesn't exist, insert and add to clientEntities");
                                    Client temp = new Client(client.getAsString(ClientContract.NAME), client.getAsDouble(ClientContract.LONGITUDE), client.getAsDouble(ClientContract.LATITUDE));
                                    temp.address = getAddress(temp);
                                    temp.id = i;
                                    manager.persistSync(temp);
                                    if (temp.id != 0) {
                                        clientEntities.add(temp);
                                    }
                                }
                            }
                        } while (clientCursor.moveToNext());
                    } else { // No client in the database
                        Log.i(TAG, "No client in database, insert and add to clientEntities");
                        int count = 1;
                        for (ContentValues client : clients) {
                            Client temp = new Client(client.getAsString(ClientContract.NAME), client.getAsDouble(ClientContract.LONGITUDE), client.getAsDouble(ClientContract.LATITUDE));
                            temp.id = count;
                            temp.address = getAddress(temp);
                            manager.persistSync(temp);
                            if (temp.id != 0) {
                                clientEntities.add(temp);
                            }
                            count++;
                        }
                    }
                    clientCursor.close();
                    Log.i(TAG, "Client update finished, start synchrnize message");
                    for (ContentValues values : messages) {
                        String name = values.getAsString(ClientContract.NAME);
                        for (Client client : clientEntities) {
                            if (client.name.equals(name)) { // find match
                                Log.i(TAG, "Search if message exists");
                                Cursor cursor = getContentResolver().query(MessageContract.CONTENT_URI, new String[] {MessageContract.MESSAGE_ID, MessageContract.TIMESTAMP, MessageContract.CHATROOM_FK, MessageContract.MESSAGE_TEXT},
                                        MessageContract.TIMESTAMP + "=? AND " + MessageContract.MESSAGE_TEXT + "=? AND " + MessageContract.SENDER_ID + "=?",
                                        new String[] {String.valueOf(values.getAsLong(MessageContract.TIMESTAMP)), values.getAsString(MessageContract.MESSAGE_TEXT), String.valueOf(client.id)},
                                        null);
                                if (cursor.moveToFirst()) { // match a message, update the message
                                    Log.i(TAG, "Message exists");
                                    ContentValues valueToUpdate = new ContentValues();
                                    valueToUpdate.put(MessageContract.SEQNUM, values.getAsLong(MessageContract.SEQNUM));
                                    long rowId = getContentResolver().update(MessageContract.CONTENT_URI(String.valueOf(MessageContract.getMessageId(cursor))), valueToUpdate, null, null);
                                    if (rowId > 0) {
                                        Log.i(TAG, "Update a message");
                                    }
                                } else { // find no match, insert the message
                                    Log.i(TAG, "Message doesn't exist");
                                    String chatroom_str = values.getAsString(ChatroomContract.NAME);
                                    String text = values.getAsString(MessageContract.MESSAGE_TEXT);
                                    long timestamp = values.getAsLong(MessageContract.TIMESTAMP);
                                    long seqnum = values.getAsLong(MessageContract.SEQNUM);
                                    Message message = new Message(text, new Timestamp(timestamp), client.longitude, client.latitude);
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

    private String getAddress(final Client client) {
        if (client.latitude != 0 && client.longitude != 0) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(client.latitude, client.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null) {
                Address address = addresses.get(0);
                String addr = "";
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addr += address.getAddressLine(i);
                    addr += "\n";
                }
                return addr;
            } else {
                return "Can't find address";
            }
        } else {
            return "Can't find address";
        }

    }
}
