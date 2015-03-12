package edu.stevens.cs522.simplecloudchatapp.Callbacks;

import java.util.List;

/**
 * Created by wyf920621 on 3/12/15.
 */
public interface ISimpleQueryListener<T> {
    public void handleResults(List<T> results);
}
