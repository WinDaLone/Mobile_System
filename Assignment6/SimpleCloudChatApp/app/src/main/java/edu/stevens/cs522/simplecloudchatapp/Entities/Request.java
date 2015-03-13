package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.net.Uri;
import android.os.Parcelable;
import android.util.JsonReader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wyf920621 on 3/12/15.
 */
public abstract class Request implements Parcelable {
    protected static final String encoding = "UTF-8";
    public long clientID;
    public static final int UUIDFlag = 1;
    public UUID registrationID; // sanity check
    public abstract URL getRequestUrl();
    // App-specific HTTP requst headers
    public abstract Map<String, String> getRequestHeaders();
    // Chat service URI with parameters e.g. query string parameters
    public abstract Uri getRequestUri();
    // JSON body (if not null) for request data not passed in headers
    public abstract String getRequestEntity() throws IOException;
    // Define your own Response class including HTTP response code.
    public abstract Response getResponse(HttpURLConnection connection, JsonReader reader); /* Null for streaming */
}
