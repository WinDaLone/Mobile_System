package edu.stevens.cs522.bookstore;

import java.util.List;

/**
 * Created by wyf920621 on 2/22/15.
 */
public interface ISimpleQueryListener<T> {
    public void handleResults(List<T> results);
}
