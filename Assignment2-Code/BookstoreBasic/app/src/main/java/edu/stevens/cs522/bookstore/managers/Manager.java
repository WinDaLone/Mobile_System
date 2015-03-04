package edu.stevens.cs522.bookstore.managers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.bookstore.IEntityCreator;
import edu.stevens.cs522.bookstore.IQueryListener;
import edu.stevens.cs522.bookstore.ISimpleQueryListener;
import edu.stevens.cs522.bookstore.providers.AsyncContentResolver;

/**
 * Created by wyf920621 on 2/22/15.
 */
public abstract class Manager<T> {
    private final Context context;
    private final IEntityCreator<T> creator;
    private final int loaderID;
    private final String tag;
    private ContentResolver syncContentResolver;
    private AsyncContentResolver asyncContentResolver;

    protected Manager(Context context, IEntityCreator<T> creator, int loaderID) {
        this.context = context;
        this.creator = creator;
        this.loaderID = loaderID;
        this.tag = this.getClass().getCanonicalName();
        Activity activity = (Activity)context;
    }
    protected ContentResolver getSyncContentResolver() {
        if (syncContentResolver == null)
            syncContentResolver = context.getContentResolver();
        return syncContentResolver;
    }

    protected AsyncContentResolver getAsyncContentResolver() {
        if (asyncContentResolver == null)
            asyncContentResolver = new AsyncContentResolver(context.getContentResolver());
        return asyncContentResolver;
    }
    protected void executeSimpleQuery(Uri uri, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder.executeQuery((Activity)context, uri, creator, listener);
    }

    protected void executeSimpleQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder.executeQuery((Activity)context, uri, projection, selection, selectionArgs, creator, listener);
    }

    protected void executeQuery(Uri uri, IQueryListener<T> listener) {
        QueryBuilder.executeQuery(tag, (Activity)context, uri, loaderID, creator, listener);
    }

    protected void executeQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, IQueryListener<T> listener) {
        QueryBuilder.executeQuery(tag, (Activity)context, uri, loaderID, projection, selection, selectionArgs, creator, listener);
    }

    protected void reexcuteQuery(Uri uri, IQueryListener<T> listener) {
        QueryBuilder.reexecuteQuery(tag, (Activity)context, uri, loaderID, creator, listener);
    }

    protected void reexecuteQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, IQueryListener<T> listener) {
        QueryBuilder.reexecuteQuery(tag, (Activity)context, uri, loaderID, projection, selection, selectionArgs, creator, listener);
    }
}