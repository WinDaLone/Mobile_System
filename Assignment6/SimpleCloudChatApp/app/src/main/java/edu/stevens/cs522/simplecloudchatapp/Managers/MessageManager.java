package edu.stevens.cs522.simplecloudchatapp.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IContinue;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.ISimpleQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class MessageManager extends Manager<Message> {
    protected MessageManager(Context context, IEntityCreator<Message> creator, int loaderID) {
        super(context, creator, loaderID);
    }

    public void persistSync(Message message) {
        ContentValues values = new ContentValues();
        message.writeToProvider(values);
        Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
    }

    public void persistAsync(final Message message) {
        ContentValues values = new ContentValues();
        message.writeToProvider(values);
        getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, values, new IContinue<Uri>() {
            @Override
            public void kontinue(Uri value) {
            }
        });
    }

    public void QueryAsync(Uri uri, IQueryListener<Message> listener) {
        this.executeQuery(uri, listener);
    }

    public void QueryDetail(Uri uri, ISimpleQueryListener<Message> listener) {
        this.executeSimpleQuery(uri, listener);
    }
}
