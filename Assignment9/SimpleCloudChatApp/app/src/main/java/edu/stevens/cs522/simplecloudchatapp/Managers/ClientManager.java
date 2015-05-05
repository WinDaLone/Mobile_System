package edu.stevens.cs522.simplecloudchatapp.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;

/**
 * Created by wyf920621 on 4/14/15.
 */
public class ClientManager extends Manager<Client> {
    public ClientManager(Context context, IEntityCreator<Client> creator, int loaderID) {
        super(context, creator, loaderID);
    }
    public void QueryAsync(Uri uri, IQueryListener<Client> listener) {
        this.executeQuery(uri, listener);
    }

    public void UpdateGPSAsync(Client client) {
        ContentValues values = new ContentValues();
        values.put(ClientContract.LATITUDE, client.latitude);
        values.put(ClientContract.LONGITUDE, client.longitude);
        values.put(ClientContract.ADDRESS, client.address);
        getAsyncResolver().updateAsync(ClientContract.CONTENT_URI(String.valueOf(client.id)), values, null, null);
        getSyncResolver().notifyChange(ClientContract.CONTENT_URI, null);
    }
}
