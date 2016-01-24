package edu.stevens.cs522.simplecloudchatapp.Callbacks;

import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;

/**
 * Created by wyf920621 on 3/12/15.
 */
public interface IQueryListener<T> {
    public void handleResults(TypedCursor<T> cursor);
    public void closeResults();
}
