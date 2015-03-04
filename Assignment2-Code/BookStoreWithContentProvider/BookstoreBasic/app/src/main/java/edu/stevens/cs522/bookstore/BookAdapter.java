package edu.stevens.cs522.bookstore;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Author;

/**
 * Created by wyf920621 on 2/27/15.
 */
public class BookAdapter extends ResourceCursorAdapter {
    protected final static int ROW_LAYOUT = android.R.layout.simple_list_item_2;

    public BookAdapter(Context context, Cursor cursor) {
        super(context, ROW_LAYOUT, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(ROW_LAYOUT, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleLine = (TextView)view.findViewById(android.R.id.text1);
        TextView authorLine = (TextView)view.findViewById(android.R.id.text2);
        String title = BookContract.getTitle(cursor);
        Author[] authors = BookContract.getAuthors(cursor);
        titleLine.setText(title);
        authorLine.setText(authors[0].toString());
    }
}
