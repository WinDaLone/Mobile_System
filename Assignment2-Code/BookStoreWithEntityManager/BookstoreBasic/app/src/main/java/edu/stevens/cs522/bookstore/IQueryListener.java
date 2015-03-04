package edu.stevens.cs522.bookstore;

import edu.stevens.cs522.bookstore.managers.TypedCursor;

/**
 * Created by wyf920621 on 2/22/15.
 */
public interface IQueryListener<T> {
    public void handleResults(TypedCursor<T> results);
    public void closeResults();
}
