package edu.stevens.cs522.bookstore.managers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import edu.stevens.cs522.bookstore.IEntityCreator;
import edu.stevens.cs522.bookstore.IQueryListener;

/**
 * Created by wyf920621 on 2/22/15.
 */
public class QueryBuilder<T> implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String PROJECTION = "bookstore.books.projection";
    public static final String SELECTION = "bookstore.books.selection";
    public static final String SELECTIONARGS = "bookstore.books.selectionArgs";
    private String tag;
    private Context context;
    private Uri uri;
    private int loaderID;
    private IEntityCreator<T> creator;
    private IQueryListener<T> listener;
    private QueryBuilder(String tag,
                         Context context,
                         Uri uri,
                         int loaderID,
                         IEntityCreator<T> creator,
                         IQueryListener<T> listener) {
        this.tag = tag;
        this.context = context;
        this.uri = uri;
        this.loaderID = loaderID;
        this.creator = creator;
        this.listener = listener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (bundle == null) {
            if (id == loaderID) {
                return new CursorLoader(context, uri, null, null, null, null);
            }
        }
        else {
            String[] projection = bundle.getStringArray(PROJECTION);
            String selection = bundle.getString(SELECTION);
            String[] selectionArgs = bundle.getStringArray(SELECTIONARGS);
            if (id == loaderID) {
                return new CursorLoader(context, uri, projection, selection, selectionArgs, null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == loaderID) {
            listener.handleResults(new TypedCursor<T>(cursor, creator));
        } else {
            throw new IllegalStateException("Unexpected loader callback");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (cursorLoader.getId() == loaderID) {
            listener.closeResults();
        } else {
            throw new IllegalStateException("Unexpected loader callback");
        }
    }

    public static <T> void executeQuery(String tag, Activity context, Uri uri, int loaderID, IEntityCreator<T> creator, IQueryListener<T> listener) {
        QueryBuilder<T> builder = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager lm = context.getLoaderManager();
        if(lm.getLoader(loaderID) == null) {
            lm.initLoader(loaderID, null, builder);
        }

    }

    public static <T> void executeQuery(String tag, Activity context, Uri uri, int loaderID, String[] projection, String selection, String[] selectionArgs, IEntityCreator<T> creator, IQueryListener<T> listener) {
        QueryBuilder<T> builder = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager lm = context.getLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putStringArray(PROJECTION, projection);
        bundle.putString(SELECTION, selection);
        bundle.putStringArray(SELECTIONARGS, selectionArgs);
        if(lm.getLoader(loaderID) == null) {
            lm.initLoader(loaderID, null, builder);
        }
    }

    public static <T> void reexecuteQuery(String tag, Activity context, Uri uri, int loaderID, IEntityCreator<T> creator, IQueryListener<T> listener) {
        QueryBuilder<T> builder = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager lm = context.getLoaderManager();
        if(lm.getLoader(loaderID) != null) {
            lm.restartLoader(loaderID, null, builder);
        }
    }

    public static <T> void reexecuteQuery(String tag, Activity context, Uri uri, int loaderID, String[] projection, String selection, String[] selectionArgs, IEntityCreator<T> creator, IQueryListener<T> listener) {
        QueryBuilder<T> builder = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager lm = context.getLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putStringArray(PROJECTION, projection);
        bundle.putString(SELECTION, selection);
        bundle.putStringArray(SELECTIONARGS, selectionArgs);
        if(lm.getLoader(loaderID) != null) {
            lm.restartLoader(loaderID, bundle, builder);
        }
    }
}
