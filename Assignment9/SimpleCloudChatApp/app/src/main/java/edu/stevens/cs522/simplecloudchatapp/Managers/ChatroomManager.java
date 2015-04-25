package edu.stevens.cs522.simplecloudchatapp.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IContinue;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IHandleResult;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.ISimpleQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ChatroomContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;

/**
 * Created by wyf920621 on 4/21/15.
 */
public class ChatroomManager extends Manager<Chatroom> {
    public static final String TAG = ChatroomManager.class.getCanonicalName();
    public ChatroomManager(Context context, IEntityCreator<Chatroom> creator, int loaderID) {
        super(context, creator, loaderID);
    }


    public void QueryAsync(Uri uri, IQueryListener<Chatroom> listener) {
        this.executeQuery(uri, listener);
    }

    public void QueryDetail(Uri uri, ISimpleQueryListener<Chatroom> listener) {
        this.executeSimpleQuery(uri, listener);
    }

    public void InsertAsync(final Chatroom chatroom, final IHandleResult handleResult) {
        String[] projecton = new String[] {ChatroomContract.ID, ChatroomContract.NAME};
        String selection = ChatroomContract.NAME + "=?";
        String[] selectionArgs = new String[] {chatroom.name};
        Log.i(TAG, "Search a chatroom");
        getAsyncResolver().queryAsync(ChatroomContract.CONTENT_URI, projecton, selection, selectionArgs, null, new IContinue<Cursor>() {
            @Override
            public void kontinue(Cursor value) {
                if (!value.moveToFirst()) { // Chatroom doesn't exist
                    Log.i(TAG, "Chatroom doesn't exist, insert it");
                    ContentValues values = new ContentValues();
                    chatroom.writeToProvider(values);
                    getAsyncResolver().insertAsync(ChatroomContract.CONTENT_URI, values, new IContinue<Uri>() {
                        @Override
                        public void kontinue(Uri value) {
                            chatroom.id = ChatroomContract.getId(value);
                            handleResult.onHandleResult(chatroom);
                        }
                    });
                } else {
                    Log.i(TAG, "Chatroom has existed");
                    handleResult.onHandleResult(null);
                }
            }
        });
    }
}
