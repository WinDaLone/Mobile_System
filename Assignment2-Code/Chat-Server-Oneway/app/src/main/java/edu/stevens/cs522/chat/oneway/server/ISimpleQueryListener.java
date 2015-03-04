package edu.stevens.cs522.chat.oneway.server;

import java.util.List;

/**
 * Created by wyf920621 on 2/23/15.
 */
public interface ISimpleQueryListener<T> {
    public void handleResults(List<T> results);
}
