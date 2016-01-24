package edu.stevens.cs522.simplecloudchatapp;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IContinue;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IStreamingOutput;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Entities.Response;
import edu.stevens.cs522.simplecloudchatapp.Entities.Synchronize;
import edu.stevens.cs522.simplecloudchatapp.Entities.Unregister;

/**
 * Created by wyf920621 on 3/13/15.
 */
public class RequestProcessor {
    public static final String TAG = RequestProcessor.class.getCanonicalName();
    private RestMethod restMethod;
    public RequestProcessor(Context context) {
        restMethod = new RestMethod(context);
    }
    public void perform(Register register, IContinue<Response> iContinue) {
        Response response = restMethod.perform(register);
        if (response != null) {
            iContinue.kontinue(response);
        } else {
            Log.e(TAG, "No Response");
            iContinue.kontinue(null);
        }
    }

    public void perform(Unregister unregister, IContinue<Boolean> iContinue) {
        boolean isSuccess = restMethod.perform(unregister);
        if (isSuccess) {
            iContinue.kontinue(true);
        } else {
            iContinue.kontinue(false);
        }
    }

    public void perform(Synchronize request, IContinue<Response> iContinue) {
        RestMethod.StreamingResponse streamingResponse = restMethod.perform(request, new IStreamingOutput() {
            @Override
            public void write(HttpURLConnection connection, Synchronize request) throws IOException {
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(os, "UTF-8")));
                writer.setIndent("  ");
                request.setJsonWriter(writer);
                writer.flush();
                writer.close();
            }
        });
        if (streamingResponse != null) {
            Response response = streamingResponse.getResponse();
            if (response != null) {
                iContinue.kontinue(response);
            } else {
                iContinue.kontinue(null);
                Log.e(TAG, "No Response");
            }
            streamingResponse.connection.disconnect();
        } else {
            iContinue.kontinue(null);
            Log.e(TAG, "No streamingResponse");
        }
    }
}
