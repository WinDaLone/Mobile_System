package edu.stevens.cs522.simplecloudchatapp.Callbacks;

import java.io.IOException;
import java.net.HttpURLConnection;

import edu.stevens.cs522.simplecloudchatapp.Entities.Synchronize;

/**
 * Created by wyf920621 on 4/11/15.
 */
public interface IStreamingOutput {
    public void write(HttpURLConnection connection, Synchronize request) throws IOException;
}
