package edu.stevens.cs522.chatapp.singleprocess.Providers;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.chatapp.singleprocess.IContinue;
import edu.stevens.cs522.chatapp.singleprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.singleprocess.ISimpleQueryListener;

/**
 * Created by wyf920621 on 3/3/15.
 */
public class SimpleQueryBuilder<T> implements IContinue<Cursor> {
    private IEntityCreator<T> helper;
    private ISimpleQueryListener<T> listener;

    private SimpleQueryBuilder(IEntityCreator<T> helper, ISimpleQueryListener<T> listener) {
        this.helper = helper;
        this.listener = listener;
    }

    public static <T> void executeQuery(Activity context, Uri uri, IEntityCreator<T> helper, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder<T> queryBuilder = new SimpleQueryBuilder<T>(helper, listener);
        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
        resolver.queryAsync(uri, null, null, null, null, queryBuilder);
    }

    public static <T> void executeQuery(Activity context, Uri uri, String[] projection, String selection, String[] selectionArgs, IEntityCreator<T> creator, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder<T> queryBuilder = new SimpleQueryBuilder<T>(creator, listener);
        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
        resolver.queryAsync(uri, projection, selection, selectionArgs, null, queryBuilder);
    }

    public void kontinue(Cursor value) {
        List<T> instances = new ArrayList<T>();
        if (value.moveToFirst()) {
            do {
                T instance = helper.create(value);
                instances.add(instance);
            } while (value.moveToNext());
        }
        value.close();
        listener.handleResults(instances);
    }
}
