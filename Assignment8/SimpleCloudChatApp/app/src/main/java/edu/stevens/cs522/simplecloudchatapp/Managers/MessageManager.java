package edu.stevens.cs522.simplecloudchatapp.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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

/**
 * Created by wyf920621 on 3/12/15.
 */
public class MessageManager extends Manager<Message> {
    public MessageManager(Context context, IEntityCreator<Message> creator, int loaderID) {
        super(context, creator, loaderID);
    }

    public void persistSync(Client client) {
        Cursor searchClients = getSyncResolver().query(ClientContract.CONTENT_URI(String.valueOf(client.id)), new String[] {ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                null, null, null);
        if (searchClients.moveToFirst()) {
            searchClients.close();
        } else {
            ContentValues values = new ContentValues();
            client.writeToProvider(values);
            Uri instanceUri = getSyncResolver().insert(ClientContract.CONTENT_URI, values);
            client.id = ClientContract.getClientId(instanceUri);
            searchClients.close();
        }
    }

    public void persistSync(Chatroom chatroom) {
        Cursor searchChatrooms = getSyncResolver().query(ChatroomContract.CONTENT_URI(String.valueOf(chatroom.id)), null, null, null, null);
        if (searchChatrooms.moveToFirst()) {
            searchChatrooms.close();
        } else {
            ContentValues values = new ContentValues();
            chatroom.writeToProvider(values);
            Uri instanceUri = getSyncResolver().insert(ChatroomContract.CONTENT_URI, values);
            chatroom.id = ChatroomContract.getId(instanceUri);
            searchChatrooms.close();
        }
    }

    public void persistSync(Message message, Client client, Chatroom chatroom) {
        ContentValues values = new ContentValues();
        Cursor searchClients = getSyncResolver().query(ClientContract.CONTENT_URI(String.valueOf(client.id)), new String[] {ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                null, null, null, null);
        if (searchClients.moveToFirst()) {
            client.id = ClientContract.getClientId(searchClients);
        }
        searchClients.close(); // get Client id
        if (client.id != 0) {
            message.writeToProvider(values, client.id, chatroom.id);
            Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
            message.messageID = MessageContract.getMessageId(uri);
        } else {
            client.writeToProvider(values);
            Uri clientUri = getSyncResolver().insert(ClientContract.CONTENT_URI, values);
            client.id = ClientContract.getClientId(clientUri);
            values.clear();
            message.writeToProvider(values, client.id, chatroom.id);
            Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
            message.messageID = MessageContract.getMessageId(uri);
        }
        getSyncResolver().notifyChange(MessageContract.CONTENT_URI, null);
    }

    public void persistAsync(final Message message, final Client client, final Chatroom chatroom) {
        getAsyncResolver().queryAsync(ChatroomContract.CONTENT_URI(String.valueOf(chatroom.id)), null, null, null, null, new IContinue<Cursor>() {
            @Override
            public void kontinue(Cursor value) {
                if (value.moveToFirst()) { // Chatroom exists
                    value.close();
                    getAsyncResolver().queryAsync(ClientContract.CONTENT_URI(String.valueOf(client.id)), new String[]{ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                            null, null, null, new IContinue<Cursor>() {
                                @Override
                                public void kontinue(Cursor value) {
                                    if (value.moveToFirst()) { // search client
                                        client.id = ClientContract.getClientId(value);
                                    }
                                    value.close();
                                    if (client.id != 0) { // client exists
                                        ContentValues values = new ContentValues();
                                        message.senderID = client.id;
                                        message.writeToProvider(values, client.id, chatroom.id);
                                        getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, values, new IContinue<Uri>() {
                                            @Override
                                            public void kontinue(Uri value) {
                                                message.messageID = MessageContract.getMessageId(value);
                                            }
                                        });
                                    } else { // client not exists
                                        ContentValues values = new ContentValues();
                                        client.writeToProvider(values);
                                        getAsyncResolver().insertAsync(ClientContract.CONTENT_URI, values, new IContinue<Uri>() {
                                            @Override
                                            public void kontinue(Uri value) {
                                                ContentValues messageValues = new ContentValues();
                                                client.id = ClientContract.getClientId(value);
                                                message.writeToProvider(messageValues, ClientContract.getClientId(value), chatroom.id);
                                                getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, messageValues, new IContinue<Uri>() {
                                                    @Override
                                                    public void kontinue(Uri value) {
                                                        message.messageID = MessageContract.getMessageId(value);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            });
                } else { // Chatroom not exist
                    ContentValues values = new ContentValues();
                    chatroom.writeToProvider(values);
                    getAsyncResolver().insertAsync(ChatroomContract.CONTENT_URI, values, new IContinue<Uri>() {
                        @Override
                        public void kontinue(Uri value) {
                            chatroom.id = ChatroomContract.getId(value);
                            getAsyncResolver().queryAsync(ClientContract.CONTENT_URI(String.valueOf(client.id)), new String[]{ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                                    null, null, null, new IContinue<Cursor>() {
                                        @Override
                                        public void kontinue(Cursor value) {
                                            if (value.moveToFirst()) { // search client
                                                client.id = ClientContract.getClientId(value);
                                            }
                                            value.close();
                                            if (client.id != 0) { // client exists
                                                ContentValues values = new ContentValues();
                                                message.senderID = client.id;
                                                message.writeToProvider(values, client.id, chatroom.id);
                                                getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, values, new IContinue<Uri>() {
                                                    @Override
                                                    public void kontinue(Uri value) {
                                                        message.messageID = MessageContract.getMessageId(value);
                                                    }
                                                });
                                            } else { // client not exists
                                                ContentValues values = new ContentValues();
                                                client.writeToProvider(values);
                                                getAsyncResolver().insertAsync(ClientContract.CONTENT_URI, values, new IContinue<Uri>() {
                                                    @Override
                                                    public void kontinue(Uri value) {
                                                        ContentValues messageValues = new ContentValues();
                                                        client.id = ClientContract.getClientId(value);
                                                        message.writeToProvider(messageValues, ClientContract.getClientId(value), chatroom.id);
                                                        getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, messageValues, new IContinue<Uri>() {
                                                            @Override
                                                            public void kontinue(Uri value) {
                                                                message.messageID = MessageContract.getMessageId(value);
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    });
                }
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
}
