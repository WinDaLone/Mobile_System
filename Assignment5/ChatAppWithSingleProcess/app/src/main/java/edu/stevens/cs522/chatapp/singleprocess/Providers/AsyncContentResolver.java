package edu.stevens.cs522.chatapp.singleprocess.Providers;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import edu.stevens.cs522.chatapp.singleprocess.IContinue;

/**
 * Created by wyf920621 on 3/3/15.
 */
public class AsyncContentResolver extends AsyncQueryHandler {

    public AsyncContentResolver(ContentResolver cr) {
        super(cr);
    }

    public void insertAsync(Uri uri, ContentValues values, IContinue<Uri> callback) {
        this.startInsert(0, callback, uri, values);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        if (cookie != null) {
            @SuppressWarnings("unchecked")
            IContinue<Uri> callback = (IContinue<Uri>)cookie;
            callback.kontinue(uri);
        }
    }

    public void queryAsync(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, IContinue<Cursor> callback) {
        this.startQuery(0, callback, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        Log.v("Check","callback");
        if (cookie != null) {
            @SuppressWarnings("unchecked")
            IContinue<Cursor> callback = (IContinue<Cursor>)cookie;
            Log.v("Callback", "created");
            callback.kontinue(cursor);
        }
    }

    public void deleteAsync(Uri uri, String selection, String[] selectionArgs) {
        this.startDelete(0, null, uri, selection, selectionArgs);
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
    }

    public void updateAsync(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        this.startUpdate(0, null, uri, values, selection, selectionArgs);
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
    }

}