package edu.stevens.cs522.simplecloudchatapp.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.ResultReceiver;
import android.util.Log;

import edu.stevens.cs522.simplecloudchatapp.Activities.SettingActivity;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IContinue;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Entities.PostMessage;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Entities.Response;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.RequestProcessor;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class RequestService extends IntentService {
    public static final String TAG = RequestService.class.getCanonicalName();
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_REGISTER = "edu.stevens.cs522.simplecloudchatapp.Services.action.REGISTER";
    public static final String ACTION_POST_MESSAGE = "edu.stevens.cs522.simplecloudchatapp.Services.action.POST_MESSAGE";

    private static int messageCount = 1;
    public static final int RESULT_REGISTER_OK = 0;
    public static final int RESULT_MESSAGE_OK = 1;
    public static final int RESULT_FAILED = 2;

    public static final int REQUEST_SERVICE_LOADERID = 2;
    private RequestProcessor requestProcessor = null;
    private MessageManager manager;
    private ResultReceiver resultReceiver;

    @Override
    public void onCreate() {
        manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, REQUEST_SERVICE_LOADERID);
        super.onCreate();
    }

    public RequestService() {
        super("RequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            resultReceiver = intent.getParcelableExtra(ServiceHelper.ACK);
            requestProcessor = new RequestProcessor(this);
            if (ACTION_REGISTER.equals(intent.getAction())) {
                handleRegister(intent);
            } else if (ACTION_POST_MESSAGE.equals(intent.getAction())) {
                handlePostMessage(intent);
            } else {
                throw new UnsupportedOperationException("Unsupported Action");
            }
        }
        stopSelf();
    }

    private void handleRegister(Intent intent) {
        final Register request = intent.getParcelableExtra(ServiceHelper.REQUEST_KEY);
        requestProcessor.perform(request, new IContinue<Response>() {
            @Override
            public void kontinue(Response value) {
                if (value != null && value.isValid()) {
                    Response.RegisterResponse response = (Response.RegisterResponse)value;
                    request.clientID = response.id;
                    Log.i("Client to be saved: ", request.clientName + " " + request.host + ":" + String.valueOf(request.port) + " " + request.clientID);
                    SharedPreferences preferences = getSharedPreferences(SettingActivity.MY_SHARED_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SettingActivity.PREF_USERNAME, request.clientName);
                    editor.putLong(SettingActivity.PREF_IDENTIFIER, request.clientID);
                    editor.putString(SettingActivity.PREF_HOST, request.host);
                    editor.putInt(SettingActivity.PREF_PORT, request.port);
                    editor.apply();
                    Log.i(TAG, "Register save successfully");
                    resultReceiver.send(RESULT_REGISTER_OK, null);
                } else {
                    Log.i(TAG, "Register save failed");
                    resultReceiver.send(RESULT_FAILED, null);
                }
            }
        });
    }

    private void handlePostMessage(Intent intent) {
        final PostMessage postMessage = intent.getParcelableExtra(ServiceHelper.REQUEST_KEY);
        requestProcessor.perform(postMessage, new IContinue<Response>() {
            @Override
            public void kontinue(Response value) {
                if (value != null && value.isValid()) {
                    Response.RegisterResponse response = (Response.RegisterResponse)value;
                    postMessage.messageID = response.id;
                    Log.i("Message to be saved: ", postMessage.text + " " + postMessage.messageID);
                    manager.persistSync(new Message(postMessage.text, postMessage.timestamp, postMessage.clientID, postMessage.messageID));
                    resultReceiver.send(RESULT_MESSAGE_OK, null);
                } else {
                    Log.i(TAG, "message save failed");
                    resultReceiver.send(RESULT_FAILED, null);
                }
            }
        });
    }
}
