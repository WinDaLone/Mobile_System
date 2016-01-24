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

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IStreamingOutput;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Entities.Request;
import edu.stevens.cs522.simplecloudchatapp.Entities.Response;
import edu.stevens.cs522.simplecloudchatapp.Entities.Synchronize;
import edu.stevens.cs522.simplecloudchatapp.Entities.Unregister;

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

    public boolean perform(Unregister request) {
        URL url = request.getRequestUrl();
        if (isOnline()) {
            try {
                // prepare request
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("USER_AGENT", TAG);
                connection.setRequestMethod("DELETE");
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
                if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) {
                    connection.disconnect();
                    return false;
                } else {
                    connection.disconnect();
                    return true;
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return false;
    }

    public static class StreamingResponse {
        public HttpURLConnection connection;
        public Response response;

        public  StreamingResponse(HttpURLConnection connection, Response response) {
            this.connection = connection;
            this.response = response;
        }
        // TODO
        public Response getResponse() {
            return this.response;
        }
    }

    public StreamingResponse perform(Synchronize request, IStreamingOutput streamingOutput) {
        URL url = request.getRequestUrl();
        Log.i(TAG, "URL to synchronize: " + url.toString());
        if (isOnline()) {
            try {
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("USER_AGENT", TAG);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setRequestProperty("CONNECTION", "Keep-Alive");
                //connection.setConnectTimeout(15000);
                //connection.setReadTimeout(10000);
                Map<String, String> headers = request.getRequestHeaders();
                for(Map.Entry<String, String> header : headers.entrySet()) {
                    connection.addRequestProperty(header.getKey(), header.getValue());
                }
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoInput(true);
                connection.setChunkedStreamingMode(0);
                streamingOutput.write(connection, request);
                connection.connect();
                throwErrors(connection);
                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(connection.getInputStream())));
                return new StreamingResponse(connection, request.getResponse(connection, reader));
            } catch (IOException e) {
                e.printStackTrace();
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
