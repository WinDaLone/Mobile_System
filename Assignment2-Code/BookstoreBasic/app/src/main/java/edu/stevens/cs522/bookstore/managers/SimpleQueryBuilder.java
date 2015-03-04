package edu.stevens.cs522.bookstore.managers;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.bookstore.providers.AsyncContentResolver;
import edu.stevens.cs522.bookstore.IContinue;
import edu.stevens.cs522.bookstore.IEntityCreator;
import edu.stevens.cs522.bookstore.ISimpleQueryListener;

/**
 * Created by wyf920621 on 2/22/15.
 */
public class SimpleQueryBuilder<T> implements IContinue<Cursor> {
    private IEntityCreator<T> helper;
    private ISimpleQueryListener<T> listener;

    private SimpleQueryBuilder(
            IEntityCreator<T> helper,
            ISimpleQueryListener<T> listener) {
        this.helper = helper;
        this.listener = listener;
    }

    public static <T> void executeQuery(Activity context, Uri uri, IEntityCreator<T> helper, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder<T> builder = new SimpleQueryBuilder<T>(helper, listener);
        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
        resolver.queryAsync(uri, null, null, null, null, builder);
    }

    public static <T> void executeQuery(Activity context, Uri uri, String[] projection, String selection, String[] selectionArgs, IEntityCreator<T> helper, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder<T> builder = new SimpleQueryBuilder<T>(helper, listener);
        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
        resolver.queryAsync(uri, projection, selection, selectionArgs, null, builder);
    }

    @Override
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
