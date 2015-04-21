package edu.stevens.cs522.simplecloudchatapp.Managers;

import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.ISimpleQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;

/**
 * Created by wyf920621 on 4/21/15.
 */
public class ChatroomManager extends Manager<Chatroom> {
    public ChatroomManager(Context context, IEntityCreator<Chatroom> creator, int loaderID) {
        super(context, creator, loaderID);
    }


    public void QueryAsync(Uri uri, IQueryListener<Chatroom> listener) {
        this.executeQuery(uri, listener);
    }

    public void QueryDetail(Uri uri, ISimpleQueryListener<Chatroom> listener) {
        this.executeSimpleQuery(uri, listener);
    }

}
