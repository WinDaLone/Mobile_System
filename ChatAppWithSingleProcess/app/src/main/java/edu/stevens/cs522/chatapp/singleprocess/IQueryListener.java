package edu.stevens.cs522.chatapp.singleprocess;

import edu.stevens.cs522.chatapp.singleprocess.Managers.TypedCursor;

/**
 * Created by wyf920621 on 3/3/15.
 */
public interface IQueryListener<T> {
    public void handleResults(TypedCursor<T> cursor);
    public void closeResults();
}