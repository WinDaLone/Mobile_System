package edu.stevens.cs522.simplecloudchatapp.Providers;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IContinue;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.ISimpleQueryListener;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class SimpleQueryBuilder<T> implements IContinue<Cursor> {
    private IEntityCreator<T> creator;
    private ISimpleQueryListener<T> listener;

    public SimpleQueryBuilder(IEntityCreator<T> creator, ISimpleQueryListener<T> listener) {
        this.creator = creator;
        this.listener = listener;
    }

    @Override
    public void kontinue(Cursor value) {
        List<T> instances = new ArrayList<T>();
        if (value.moveToFirst()) {
            do {
                T instance = creator.create(value);
                instances.add(instance);
            } while (value.moveToNext());
        }
        value.close();
        listener.handleResults(instances);
    }

    public static <T> void executeQuery(Activity context, Uri uri, IEntityCreator<T> creator, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder<T> queryBuilder = new SimpleQueryBuilder<T>(creator, listener);
        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
        resolver.queryAsync(uri, null, null, null, null, queryBuilder);
    }

    public static <T> void executeQuery(Activity context, Uri uri, String[] projection, String selection, String[] selectionArgs, IEntityCreator<T> creator, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder<T> queryBuilder = new SimpleQueryBuilder<T>(creator, listener);
        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
        resolver.queryAsync(uri, projection, selection, selectionArgs, null, queryBuilder);
    }
}
