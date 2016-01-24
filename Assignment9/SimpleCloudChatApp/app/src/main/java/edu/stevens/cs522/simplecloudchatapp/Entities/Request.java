package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.net.Uri;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wyf920621 on 3/12/15.
 */
public abstract class Request implements Parcelable {
    public static final String TAG = Request.class.getCanonicalName();
    protected static final String encoding = "UTF-8";
    public long clientID;
    public UUID registrationID; // sanity check
    public double latitude;
    public double longitude;
    public URL getRequestUrl() {
        Uri uri = this.getRequestUri();
        URL url = null;
        try {
            url = new URL(uri.toString());
            Log.v(TAG, "Request URL: " + url.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }
        return url;
    }
    // App-specific HTTP requst headers
    public Map<String, String> getRequestHeaders() {
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("X-latitude", String.valueOf(latitude));
        stringMap.put("X-longitude", String.valueOf(longitude));
        return stringMap;
    };
    // Chat service URI with parameters e.g. query string parameters
    public abstract Uri getRequestUri();
    // JSON body (if not null) for request data not passed in headers
    public abstract String getRequestEntity() throws IOException;
    // Define your own Response class including HTTP response code.
    public abstract Response getResponse(HttpURLConnection connection, JsonReader reader) throws IOException; /* Null for streaming */
}
