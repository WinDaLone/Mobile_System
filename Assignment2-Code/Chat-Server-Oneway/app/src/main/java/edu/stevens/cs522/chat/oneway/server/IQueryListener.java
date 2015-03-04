package edu.stevens.cs522.chat.oneway.server;

import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;

/**
 * Created by wyf920621 on 2/23/15.
 */
public interface IQueryListener<T> {
    public void handleResults(TypedCursor<T> cursor);
    public void closeResults();
}
