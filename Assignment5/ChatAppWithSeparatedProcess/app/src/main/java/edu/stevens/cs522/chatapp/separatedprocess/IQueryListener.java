package edu.stevens.cs522.chatapp.separatedprocess;

import edu.stevens.cs522.chatapp.separatedprocess.Managers.TypedCursor;

/**
 * Created by wyf920621 on 3/8/15.
 */
public interface IQueryListener<T> {
    public void handleResults(TypedCursor<T> cursor);
    public void closeResults();
}