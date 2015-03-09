package edu.stevens.cs522.chatapp.separatedprocess.Managers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.chatapp.separatedprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.separatedprocess.IQueryListener;
import edu.stevens.cs522.chatapp.separatedprocess.ISimpleQueryListener;
import edu.stevens.cs522.chatapp.separatedprocess.Providers.AsyncContentResolver;
import edu.stevens.cs522.chatapp.separatedprocess.Providers.QueryBuilder;
import edu.stevens.cs522.chatapp.separatedprocess.Providers.SimpleQueryBuilder;

/**
 * Created by wyf920621 on 3/8/15.
 */
public abstract class Manager<T> {
    private ContentResolver syncResolver;
    private AsyncContentResolver asyncResolver;
    private final Context context;
    private final IEntityCreator<T> creator;
    private final int loaderID;
    private final String tag;

    protected Manager(Context context, IEntityCreator<T> creator, int loaderID) {
        this.context = context;
        this.creator = creator;
        this.loaderID = loaderID;
        this.tag = this.getClass().getCanonicalName();
    }

    protected ContentResolver getSyncResolver() {
        if (syncResolver == null)
            syncResolver = context.getContentResolver();
        return syncResolver;
    }

    protected AsyncContentResolver getAsyncResolver() {
        if (asyncResolver == null)
            asyncResolver = new AsyncContentResolver(context.getContentResolver());
        return asyncResolver;
    }

    protected void executeSimpleQuery(Uri uri, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder.executeQuery((Activity) context, uri, creator, listener);
    }

    protected void executeSimpleQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder.executeQuery((Activity)context, uri, projection, selection, selectionArgs, creator, listener);
    }

    protected void executeQuery(Uri uri, IQueryListener<T> listener) {
        QueryBuilder.executeQuery(tag, (Activity) context, uri, loaderID, creator, listener);
    }

    protected void executeQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, IQueryListener<T> listener) {
        QueryBuilder.executeQuery(tag, (Activity)context, uri, loaderID, projection, selection, selectionArgs, creator, listener);
    }

    protected void reexecuteQuery(Uri uri, IQueryListener<T> listener) {
        QueryBuilder.reexecuteQuery(tag, (Activity)context, uri, loaderID, creator, listener);
    }

    protected void reexecuteQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, IQueryListener<T> listener) {
        QueryBuilder.reexecuteQuery(tag, (Activity)context, uri, loaderID, projection, selection, selectionArgs, creator, listener);
    }
}
