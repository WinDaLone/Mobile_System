package edu.stevens.cs522.simplecloudchatapp;

import android.content.Context;
import android.util.Log;

import edu.stevens.cs522.simplecloudchatapp.Callbacks.IContinue;
import edu.stevens.cs522.simplecloudchatapp.Entities.PostMessage;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Entities.Response;
import edu.stevens.cs522.simplecloudchatapp.Entities.Synchronize;

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
        }
    }

    public void perform(Synchronize synchronize, IContinue<Response> iContinue) {
        Response response = restMethod.perform(synchronize); // TODO
        if (response != null) {
            iContinue.kontinue(response);
        } else {
            Log.e(TAG, "No Response");
        }
    }

    public void perform(PostMessage postMessage, IContinue<Response> iContinue) {
        Response response = restMethod.perform(postMessage);
        if (response != null) {
            iContinue.kontinue(response);
        } else {
            Log.e(TAG, "No Response");
        }
    }
}
