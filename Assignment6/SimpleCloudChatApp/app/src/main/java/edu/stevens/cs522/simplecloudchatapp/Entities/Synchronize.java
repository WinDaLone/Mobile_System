package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.net.Uri;
import android.os.Parcel;
import android.util.JsonReader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by wyf920621 on 4/6/15.
 */
// TODO
public class Synchronize extends Request{
    @Override
    public Map<String, String> getRequestHeaders() {
        return null;
    }

    @Override
    public Uri getRequestUri() {
        return null;
    }

    @Override
    public String getRequestEntity() throws IOException {
        return null;
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader reader) throws IOException {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

}
