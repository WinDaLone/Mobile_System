package edu.stevens.cs522.simplecloudchatapp.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IContinue;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.ISimpleQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
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
            return;
        } else {
            ContentValues values = new ContentValues();
            client.writeToProvider(values);
            Uri instanceUri = getSyncResolver().insert(ClientContract.CONTENT_URI, values);
            client.id = ClientContract.getClientId(instanceUri);
        }
    }

    public void persistSync(Message message, Client client) {
        ContentValues values = new ContentValues();
        Cursor searchClients = getSyncResolver().query(ClientContract.CONTENT_URI(String.valueOf(client.id)), new String[] {ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                null, null, null, null);
        if (searchClients.moveToFirst()) {
            client.id = ClientContract.getClientId(searchClients);
        }
        searchClients.close();
        if (client.id != 0) {
            message.writeToProvider(values, client.id);
            Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
            message.messageID = MessageContract.getMessageId(uri);
        } else {
            client.writeToProvider(values);
            Uri clientUri = getSyncResolver().insert(ClientContract.CONTENT_URI, values);
            client.id = ClientContract.getClientId(clientUri);
            values.clear();
            message.writeToProvider(values, client.id);
            Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
            message.messageID = MessageContract.getMessageId(uri);
        }
        getSyncResolver().notifyChange(MessageContract.CONTENT_URI, null);
    }

    public void persistAsync(final Message message, final Client client) {
        getAsyncResolver().queryAsync(ClientContract.CONTENT_URI(String.valueOf(client.id)), new String[] {ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID},
                null, null, null, new IContinue<Cursor>() {
            @Override
            public void kontinue(Cursor value) {
                if (value.moveToFirst()) { // search client
                    client.id = ClientContract.getClientId(value);
                }
                value.close();
                if (client.id != 0) {
                    ContentValues values = new ContentValues();
                    message.senderID = client.id;
                    message.writeToProvider(values, client.id);
                    getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, values, new IContinue<Uri>() {
                        @Override
                        public void kontinue(Uri value) {
                            message.messageID = MessageContract.getMessageId(value);
                        }
                    });
                } else {
                    ContentValues values = new ContentValues();
                    client.writeToProvider(values);
                    getAsyncResolver().insertAsync(ClientContract.CONTENT_URI, values, new IContinue<Uri>() {
                        @Override
                        public void kontinue(Uri value) {
                            ContentValues messageValues = new ContentValues();
                            client.id = ClientContract.getClientId(value);
                            message.writeToProvider(messageValues, ClientContract.getClientId(value));
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
        getSyncResolver().notifyChange(MessageContract.CONTENT_URI, null);
    }

    public void UpdateAsync(Message message, ContentValues values) {
        getAsyncResolver().updateAsync(MessageContract.CONTENT_URI(String.valueOf(message.messageID)), values, null, null);
    }

    public void UpdateSync(Message message, ContentValues values) {
        getSyncResolver().update(MessageContract.CONTENT_URI(String.valueOf(message.messageID)), values, null, null);
    }

    public void QueryAsync(Uri uri, IQueryListener<Message> listener) {
        this.executeQuery(uri, listener);
    }

    public void QueryDetail(Uri uri, ISimpleQueryListener<Message> listener) {
        this.executeSimpleQuery(uri, listener);
    }
}
