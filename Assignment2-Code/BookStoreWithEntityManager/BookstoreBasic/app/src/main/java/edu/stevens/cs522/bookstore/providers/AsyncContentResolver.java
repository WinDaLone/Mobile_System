package edu.stevens.cs522.bookstore.providers;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.bookstore.IContinue;

/**
 * Created by wyf920621 on 2/21/15.
 */
// AsyncContentResolver
public class AsyncContentResolver extends AsyncQueryHandler {
    public AsyncContentResolver(ContentResolver cr) {
        super(cr);
    }
    public void insertAsync(Uri uri, ContentValues values, IContinue<Uri> callback) {
        this.startInsert(0, callback, uri, values);
    }

    public void deleteAsync(Uri uri, String selection, String[] selectionArgs) {
        this.startDelete(0, null, uri, selection, selectionArgs);
    }

    public void updateAsync(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        startUpdate(0, null, uri, values, selection, selectionArgs);
    }

    public void queryAsync(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy, IContinue<Cursor> callback) {
        this.startQuery(0, callback, uri, projection, selection, selectionArgs, orderBy);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (cookie != null) {
            @SuppressWarnings("unchecked")
            IContinue<Cursor> callback = (IContinue<Cursor>)cookie;
            callback.kontinue(cursor);
        }
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        if (cookie != null) {
            @SuppressWarnings("unchecked")
            IContinue<Uri> callback = (IContinue<Uri>)cookie;
            callback.kontinue(uri);
        }
    }
}
