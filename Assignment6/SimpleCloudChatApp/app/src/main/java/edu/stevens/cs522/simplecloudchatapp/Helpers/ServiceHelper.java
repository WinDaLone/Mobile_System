package edu.stevens.cs522.simplecloudchatapp.Helpers;

import android.content.Context;
import android.content.Intent;

import edu.stevens.cs522.simplecloudchatapp.AckReceiverWrapper;
import edu.stevens.cs522.simplecloudchatapp.Entities.PostMessage;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Entities.Synchronize;
import edu.stevens.cs522.simplecloudchatapp.Services.RequestService;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class ServiceHelper {
    public static final String REQUEST_KEY = "edu.stevens.cs522.simplecloudchatapp.request_key";
    public static final String ACK = "edu.stevens.cs522.simplecloudchatapp.ACK";
    public Context context;

    public ServiceHelper(Context context) {
        this.context = context;
    }


    public void RegisterUser(Register register, AckReceiverWrapper wrapper) {
        Intent intent = new Intent(context, RequestService.class);
        intent.setAction(RequestService.ACTION_REGISTER);
        intent.putExtra(REQUEST_KEY, register);
        intent.putExtra(ACK, wrapper);
        context.startService(intent);
    }

    public void PostMessage(PostMessage postMessage, AckReceiverWrapper wrapper) {
        Intent intent = new Intent(context, RequestService.class);
        intent.setAction(RequestService.ACTION_POST_MESSAGE);
        intent.putExtra(REQUEST_KEY, postMessage);
        intent.putExtra(ACK, wrapper);
        context.startService(intent);
    }

    public void RefreshMessage(Synchronize request, AckReceiverWrapper wrapper) {
        Intent intent = new Intent(context, RequestService.class);
        intent.setAction(RequestService.ACTION_REFRESH);
        intent.putExtra(REQUEST_KEY, request);
        intent.putExtra(ACK, wrapper);
        context.startService(intent);
    }
}
