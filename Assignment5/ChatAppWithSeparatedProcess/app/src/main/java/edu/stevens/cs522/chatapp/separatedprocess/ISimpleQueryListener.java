package edu.stevens.cs522.chatapp.separatedprocess;

import java.util.List;

/**
 * Created by wyf920621 on 3/8/15.
*/
public interface ISimpleQueryListener<T> {
    public void handleResults(List<T> results);
}
