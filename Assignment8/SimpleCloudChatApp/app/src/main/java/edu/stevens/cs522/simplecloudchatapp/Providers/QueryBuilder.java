package edu.stevens.cs522.simplecloudchatapp.Providers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class QueryBuilder<T> implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String PROJECTION = "edu.stevens.cs522.simplecloudchatapp.PROJECTION";
    public static final String SELECTION = "edu.stevens.cs522.simplecloudchatapp.SELECTION";
    public static final String SELECTIONARGS = "edu.stevens.cs522.simplecloudchatapp.SELECTIONARGS";
    private final String tag;
    private final Context context;
    private final Uri uri;
    private final int loaderID;
    private final IEntityCreator<T> creator;
    private final IQueryListener<T> listener;
    private QueryBuilder(String tag, Context context, Uri uri, int loaderID, IEntityCreator<T> creator, IQueryListener<T> listener) {
        this.tag = tag;
        this.context = context;
        this.uri = uri;
        this.loaderID = loaderID;
        this.creator = creator;
        this.listener = listener;
    }

    public static <T> void executeQuery(String tag, final Activity context, Uri uri, final int loaderID, IEntityCreator<T> creator, IQueryListener<T> listener) {
        final QueryBuilder<T> queryBuilder = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager manager = context.getLoaderManager();
        manager.initLoader(loaderID, null, queryBuilder);
    }

    public static <T> void executeQuery(String tag, Activity context, Uri uri, int loaderID, String[] projection, String selection, String[] selectionArgs, IEntityCreator<T> creator, IQueryListener<T> listener) {
        QueryBuilder<T> queryBuilder = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager manager = context.getLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putStringArray(PROJECTION, projection);
        bundle.putString(SELECTION, selection);
        bundle.putStringArray(SELECTIONARGS, selectionArgs);
        manager.initLoader(loaderID, bundle, queryBuilder);
    }

    public static <T> void reexecuteQuery(String tag, Activity context, Uri uri, int loaderID, IEntityCreator<T> creator, IQueryListener<T> listener) {
        QueryBuilder<T> queryBuilder = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager manager = context.getLoaderManager();
        manager.restartLoader(loaderID, null, queryBuilder);
    }

    public static <T> void reexecuteQuery(String tag, Activity context, Uri uri, int loaderID, String[] projection, String selection, String[] selectionArgs, IEntityCreator<T> creator, IQueryListener<T> listener) {
        QueryBuilder<T> queryBuilder = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager manager = context.getLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putStringArray(PROJECTION, projection);
        bundle.putString(SELECTION, selection);
        bundle.putStringArray(SELECTIONARGS, selectionArgs);
        manager.restartLoader(loaderID, bundle, queryBuilder);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection;
        String selection;
        String[] selectionArgs;
        if (bundle == null) {
            projection = null;
            selection = null;
            selectionArgs = null;
        } else {
            projection = bundle.getStringArray(PROJECTION);
            selection = bundle.getString(SELECTION);
            selectionArgs = bundle.getStringArray(SELECTIONARGS);
        }
        if (id == loaderID) {
            return new CursorLoader(context, uri, projection, selection, selectionArgs, null);
        }
        throw new IllegalArgumentException("Unsupported loaderID");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == loaderID) {
            listener.handleResults(new TypedCursor<T>(data, creator));
        } else {
            throw  new IllegalStateException("Unexpected loader callback");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == loaderID) {
            listener.closeResults();
        } else {
            throw new IllegalStateException("Unexpected loader callback");
        }
    }
}
