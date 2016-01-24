package edu.stevens.cs522.simplecloudchatapp.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.simplecloudchatapp.AckReceiverWrapper;
import edu.stevens.cs522.simplecloudchatapp.Activities.ChatAppActivity;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IContinue;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.ISimpleQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ChatroomContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Entities.Synchronize;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.Services.RequestService;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class MessageManager extends Manager<Message> {
    public static final String TAG = MessageManager.class.getCanonicalName();
    public MessageManager(Context context, IEntityCreator<Message> creator, int loaderID) {
        super(context, creator, loaderID);
    }

    public void persistSync(Client client) {
        Log.i(TAG, "Search client");
        Cursor searchClients = getSyncResolver().query(ClientContract.CONTENT_URI, new String[] {ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                ClientContract.NAME + "=?", new String[] {client.name}, null);
        if (searchClients.moveToFirst()) {
            Log.i(TAG, "Client exists");
            client.id = ClientContract.getClientId(searchClients);
        } else {
            Log.i(TAG, "Client doesn't exist, insert it");
            ContentValues values = new ContentValues();
            client.writeToProvider(values);
            Uri instanceUri = getSyncResolver().insert(ClientContract.CONTENT_URI, values);
            client.id = ClientContract.getClientId(instanceUri);
        }
        searchClients.close();
    }

    public void persistSync(Chatroom chatroom) {
        Log.i(TAG, "Search a chatroom");
        Cursor searchChatrooms = getSyncResolver().query(ChatroomContract.CONTENT_URI, null, ChatroomContract.NAME + "=?", new String[] {chatroom.name}, null);
        if (searchChatrooms.moveToFirst()) {
            Log.i(TAG, "Chatroom exists");
            chatroom.id = ChatroomContract.getId(searchChatrooms);
        } else {
            Log.i(TAG, "Chatroom doesn't exist, insert it");
            ContentValues values = new ContentValues();
            chatroom.writeToProvider(values);
            Uri instanceUri = getSyncResolver().insert(ChatroomContract.CONTENT_URI, values);
            chatroom.id = ChatroomContract.getId(instanceUri);
        }
        searchChatrooms.close();
    }

    public void persistSync(Message message, Client client, Chatroom chatroom) {
        Log.i(TAG, "Search a chatroom");
        Cursor searchChatrooms = getSyncResolver().query(ChatroomContract.CONTENT_URI, null, ChatroomContract.NAME + "=?", new String[] {chatroom.name}, null, null);
        if (searchChatrooms.moveToFirst()) {
            Log.i(TAG, "Chatroom exists");
            chatroom.id = ChatroomContract.getId(searchChatrooms);
        } else {
            Log.i(TAG, "Chatroom doesn't exist");
        }
        searchChatrooms.close();
        ContentValues values = new ContentValues();
        Log.i(TAG, "Search a client");
        Cursor searchClients = getSyncResolver().query(ClientContract.CONTENT_URI, new String[] {ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                ClientContract.NAME + "=?", new String[] {client.name}, null, null);
        if (searchClients.moveToFirst()) {
            Log.i(TAG, "client exists");
            client.id = ClientContract.getClientId(searchClients);
        } else {
            Log.i(TAG, "client doesn't exist");
        }
        searchClients.close(); // get Client id
        if (client.id != 0) {
            if (chatroom.id != 0) {
                Log.i(TAG, "insert message");
                values.clear();
                message.writeToProvider(values, client.id, chatroom.id);
                Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
                message.messageID = MessageContract.getMessageId(uri);
                SynchronizeMessage();
            } else {
                Log.i(TAG, "insert chatroom");
                values.clear();
                chatroom.writeToProvider(values);
                Uri chatroomUri = getSyncResolver().insert(ChatroomContract.CONTENT_URI, values);
                chatroom.id = ChatroomContract.getId(chatroomUri);

                Log.i(TAG, "insert message");
                values.clear();
                message.writeToProvider(values, client.id, chatroom.id);
                Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
                message.messageID = MessageContract.getMessageId(uri);
                SynchronizeMessage();
            }
        } else {
            Log.i(TAG, "insert client");
            values.clear();
            client.writeToProvider(values);
            Uri clientUri = getSyncResolver().insert(ClientContract.CONTENT_URI, values);
            client.id = ClientContract.getClientId(clientUri);
            if (chatroom.id != 0) {
                Log.i(TAG, "insert message");
                values.clear();
                message.writeToProvider(values, client.id, chatroom.id);
                Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
                message.messageID = MessageContract.getMessageId(uri);
                SynchronizeMessage();
            } else {
                Log.i(TAG, "insert chatroom");
                values.clear();
                chatroom.writeToProvider(values);
                Uri chatroomUri = getSyncResolver().insert(ChatroomContract.CONTENT_URI, values);
                chatroom.id = ChatroomContract.getId(chatroomUri);

                Log.i(TAG, "insert message");
                values.clear();
                message.writeToProvider(values, client.id, chatroom.id);
                Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
                message.messageID = MessageContract.getMessageId(uri);
                SynchronizeMessage();
            }
        }
        getSyncResolver().notifyChange(MessageContract.CONTENT_URI, null);
    }

    public void persistAsync(final Message message, final Client client, final Chatroom chatroom) {
        Log.i(TAG, "Search a chatroom");
        getAsyncResolver().queryAsync(ChatroomContract.CONTENT_URI, null, ChatroomContract.NAME + "=?", new String[] {chatroom.name}, null, new IContinue<Cursor>() {
            @Override
            public void kontinue(Cursor value) {
                if (value.moveToFirst()) { // Chatroom exists
                    Log.i(TAG, "Chatroom exists");
                    chatroom.id = ChatroomContract.getId(value);
                    Log.i(TAG, "Search a client");
                    getAsyncResolver().queryAsync(ClientContract.CONTENT_URI, new String[]{ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                            ClientContract.NAME + "=?", new String[] {client.name}, null, new IContinue<Cursor>() {
                                @Override
                                public void kontinue(Cursor value) {
                                    if (value.moveToFirst()) { // search client
                                        client.id = ClientContract.getClientId(value);
                                    }
                                    if (client.id != 0) { // client exists
                                        Log.i(TAG, "Client exists");
                                        ContentValues values = new ContentValues();
                                        message.senderID = client.id;
                                        message.writeToProvider(values, client.id, chatroom.id);
                                        Log.i(TAG, "Insert message");
                                        getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, values, new IContinue<Uri>() {
                                            @Override
                                            public void kontinue(Uri value) {
                                                Log.i(TAG, "Insert message OK");
                                                message.messageID = MessageContract.getMessageId(value);
                                                SynchronizeMessage();
                                            }
                                        });
                                    } else { // client not exists
                                        Log.i(TAG, "Client doesn't exist, insert it");
                                        ContentValues values = new ContentValues();
                                        client.writeToProvider(values);
                                        getAsyncResolver().insertAsync(ClientContract.CONTENT_URI, values, new IContinue<Uri>() {
                                            @Override
                                            public void kontinue(Uri value) {
                                                ContentValues messageValues = new ContentValues();
                                                client.id = ClientContract.getClientId(value);
                                                Log.i(TAG, "Insert message");
                                                message.writeToProvider(messageValues, ClientContract.getClientId(value), chatroom.id);
                                                getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, messageValues, new IContinue<Uri>() {
                                                    @Override
                                                    public void kontinue(Uri value) {
                                                        Log.i(TAG, "Insert message OK");
                                                        message.messageID = MessageContract.getMessageId(value);
                                                        SynchronizeMessage();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                    value.close();
                                }
                            });
                } else { // Chatroom not exist
                    Log.i(TAG, "Chatroom doesn't exist, insert it");
                    ContentValues values = new ContentValues();
                    chatroom.writeToProvider(values);
                    // Insert chatroom
                    getAsyncResolver().insertAsync(ChatroomContract.CONTENT_URI, values, new IContinue<Uri>() {
                        @Override
                        public void kontinue(Uri value) {
                            chatroom.id = ChatroomContract.getId(value);
                            Log.i(TAG, "Search a client");
                            getAsyncResolver().queryAsync(ClientContract.CONTENT_URI, new String[]{ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                                    ClientContract.NAME + "=?", new String[] {client.name}, null, new IContinue<Cursor>() {
                                        @Override
                                        public void kontinue(Cursor value) {
                                            if (value.moveToFirst()) { // search client
                                                client.id = ClientContract.getClientId(value);
                                            }
                                            if (client.id != 0) { // client exists
                                                Log.i(TAG, "Client exists");
                                                ContentValues values = new ContentValues();
                                                message.senderID = client.id;
                                                message.writeToProvider(values, client.id, chatroom.id);
                                                Log.i(TAG, "Insert message");
                                                getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, values, new IContinue<Uri>() {
                                                    @Override
                                                    public void kontinue(Uri value) {
                                                        Log.i(TAG, "Insert message OK");
                                                        message.messageID = MessageContract.getMessageId(value);
                                                        SynchronizeMessage();
                                                    }
                                                });
                                            } else { // client not exists
                                                Log.i(TAG, "Client doesn't exist, insert it");
                                                ContentValues values = new ContentValues();
                                                client.writeToProvider(values);
                                                getAsyncResolver().insertAsync(ClientContract.CONTENT_URI, values, new IContinue<Uri>() {
                                                    @Override
                                                    public void kontinue(Uri value) {
                                                        ContentValues messageValues = new ContentValues();
                                                        client.id = ClientContract.getClientId(value);
                                                        message.writeToProvider(messageValues, ClientContract.getClientId(value), chatroom.id);
                                                        Log.i(TAG, "Insert message");
                                                        getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, messageValues, new IContinue<Uri>() {
                                                            @Override
                                                            public void kontinue(Uri value) {
                                                                Log.i(TAG, "Insert message OK");
                                                                message.messageID = MessageContract.getMessageId(value);
                                                                SynchronizeMessage();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                            value.close();
                                        }
                                    });
                        }
                    });
                }
                value.close();
            }
        });
        getSyncResolver().notifyChange(MessageContract.CONTENT_URI, null);
    }


    // Query messages of a chatroom
    public void QueryAsync(Uri uri, String[] selectionArgs, IQueryListener<Message> listener) {
        this.executeQuery(uri, null, null, selectionArgs, listener);
    }

    // Query all messages
    public void QueryAsync(Uri uri, IQueryListener<Message> listener) {
        this.executeQuery(uri, listener);
    }


    public void QueryDetail(Uri uri, ISimpleQueryListener<Message> listener) {
        this.executeSimpleQuery(uri, listener);
    }

    public void DeleteAllAsync() {
        getAsyncResolver().deleteAsync(MessageContract.CONTENT_URI, null, null);
        getAsyncResolver().deleteAsync(ChatroomContract.CONTENT_URI, null, null);
        getAsyncResolver().deleteAsync(ClientContract.CONTENT_URI, null, null);
    }

    private void SynchronizeMessage() {
        Log.i(TAG, "Start synchronize messages");
        this.QueryDetail(MessageContract.CONTENT_URI, new ISimpleQueryListener<Message>() {
            @Override
            public void handleResults(List<Message> results) {
                if (results != null) {
                    // Synchrnoize with server
                    long seqnum = 0; // Get sequnum
                    List<Message> messages = new ArrayList<Message>(); // Store messages to be saved
                    for (int i = 0; i < results.size(); i++) {
                        long tempNum = results.get(i).seqnum;
                        if (tempNum == 0) {
                            messages.add(results.get(i));
                        }
                        if (tempNum > seqnum) {
                            seqnum = tempNum;
                        }
                    }
                    Synchronize request = new Synchronize(ChatAppActivity.host, ChatAppActivity.port, ChatAppActivity.client, seqnum, messages);
                    AckReceiverWrapper.IReceiver receiver = new AckReceiverWrapper.IReceiver() {
                        @Override
                        public void onReceiveResult(int resultCode, Bundle resultData) {
                            if (resultCode == RequestService.RESULT_SYNC_OK) {
                                getSyncResolver().notifyChange(MessageContract.CONTENT_URI, null);
                                Toast.makeText(getContext(), "Synchronize successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to synchronize", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    AckReceiverWrapper wrapper = new AckReceiverWrapper(new Handler());
                    wrapper.setReceiver(receiver);
                    ServiceHelper serviceHelper = new ServiceHelper(getContext());
                    serviceHelper.RefreshMessage(request, wrapper);
                }
            }
        });
    }
}
