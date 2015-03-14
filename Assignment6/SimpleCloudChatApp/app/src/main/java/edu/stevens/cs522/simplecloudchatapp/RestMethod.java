package edu.stevens.cs522.simplecloudchatapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import edu.stevens.cs522.simplecloudchatapp.Entities.PostMessage;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Entities.Request;
import edu.stevens.cs522.simplecloudchatapp.Entities.Response;

/**
 * Created by wyf920621 on 3/13/15.
 */
public class RestMethod {
    public static final String TAG = RestMethod.class.getCanonicalName();
    private Context context;
    private HttpURLConnection connection;
    public RestMethod(Context context) {
        this.context = context;
    }
    public Response perform(Register request) {
        URL url = request.getRequestUrl();
        Log.i(TAG, "URL to request: " + url.toString());
        if(isOnline()) {
            try {
                // prepare request
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("USER_AGENT", TAG);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setRequestProperty("CONNECTION", "Keep-Alive");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(10000);

                Map<String, String> headers = request.getRequestHeaders();
                for(Map.Entry<String, String> header : headers.entrySet()) {
                    connection.addRequestProperty(header.getKey(), header.getValue());
                }

                // execute request
                outputRequestEntity(request);
                connection.setDoInput(true);
                connection.connect();
                throwErrors(connection);

                JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(connection.getInputStream())));
                Response response = request.getResponse(connection, jsonReader);
                jsonReader.close();
                connection.disconnect();
                return response;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    public Response perform(PostMessage request) {
        URL url = request.getRequestUrl();
        if (isOnline()) {
            try {
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("USER_AGENT", TAG);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setRequestProperty("CONNECTION", "Keep-Alive");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(10000);

                Map<String, String> headers = request.getRequestHeaders();
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.addRequestProperty(header.getKey(), header.getValue());
                }
                connection.setDoInput(true);
                // execute
                outputRequestEntity(request);
                connection.connect();
                throwErrors(connection);
                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(connection.getInputStream())));
                Response response = request.getResponse(connection, reader);
                reader.close();
                connection.disconnect();
                return response;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void outputRequestEntity(Request request) throws IOException {
        String requestEntity = request.getRequestEntity();
        if (requestEntity != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            byte[] outputEntity = requestEntity.getBytes("UTF-8");
            connection.setFixedLengthStreamingMode(outputEntity.length);
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(outputEntity);
            out.flush();
            out.close();
        }
    }

    private static void throwErrors (HttpURLConnection connection) throws IOException {
        final int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            String exceptionMessage = "Error response " + status + " " + connection.getResponseMessage() + " for " + connection.getURL();
            throw new IOException(exceptionMessage);
        }
    }
}