package edu.stevens.cs522.chatapp.singleprocess;

import java.util.List;

/**
 * Created by wyf920621 on 3/3/15.
 */
public interface ISimpleQueryListener<T> {
    public void handleResults(List<T> results);
}
