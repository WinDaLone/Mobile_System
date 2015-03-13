package edu.stevens.cs522.simplecloudchatapp.Helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import edu.stevens.cs522.simplecloudchatapp.Entities.PostMessage;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Entities.Request;
import edu.stevens.cs522.simplecloudchatapp.Services.RequestService;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class ServiceHelper {
    public static final String REQUEST_KEY = "edu.stevens.cs522.simplecloudchatapp.request_key";
    public static final String ACK = "edu.stevens.cs522.simplecloudchatapp.ACK";
    public Request request = null;
    public Context context;

    private AckReceiverWrapper.IReceiver receiver;
    private AckReceiverWrapper wrapper;

    public ServiceHelper(Context context) {
        this.context = context;
    }


    public boolean RegisterUser(Register register) {
        final ICheck iCheck = new ICheck();
        Intent intent = new Intent(context, RequestService.class);
        receiver = new AckReceiverWrapper.IReceiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RequestService.RESULT_OK) {
                    iCheck.isSuccess = true;
                    iCheck.hasResult = true;
                } else {
                    iCheck.isSuccess = false;
                    iCheck.hasResult = true;
                }
            }
        };
        wrapper = new AckReceiverWrapper(new Handler());
        wrapper.setReceiver(receiver);
        intent.setAction(RequestService.ACTION_REGISTER);
        intent.putExtra(REQUEST_KEY, request);
        intent.putExtra(ACK, wrapper);
        context.startService(intent);
        while(true) {
            if (iCheck.hasResult) {
                break;
            }
        }
        return iCheck.isSuccess;
    }

    public boolean PostMessage(PostMessage postMessage) {
        final ICheck iCheck = new ICheck();
        Intent intent = new Intent(context, RequestService.class);
        receiver = new AckReceiverWrapper.IReceiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RequestService.RESULT_OK) {
                    iCheck.isSuccess = true;
                    iCheck.hasResult = true;
                } else {
                    iCheck.isSuccess = false;
                    iCheck.hasResult = true;
                }
            }
        };
        wrapper = new AckReceiverWrapper(new Handler());
        wrapper.setReceiver(receiver);
        intent.setAction(RequestService.ACTION_POST_MESSAGE);
        intent.putExtra(REQUEST_KEY, request);
        intent.putExtra(ACK, wrapper);
        context.startService(intent);
        while(true) {
            if (iCheck.hasResult) {
                break;
            }
        }
        return iCheck.isSuccess;
    }

    public static class AckReceiverWrapper extends ResultReceiver {
        private IReceiver receiver;
        public AckReceiverWrapper(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (receiver != null) {
                receiver.onReceiveResult(resultCode, resultData);
            }
        }

        public void setReceiver(IReceiver receiver) {
            this.receiver = receiver;
        }

        public interface IReceiver {
            public void onReceiveResult(int resultCode, Bundle resultData);
        }
    }

    public static class ICheck {
        public boolean isSuccess;
        public boolean hasResult;
        public ICheck() {
            isSuccess = false;
            hasResult = false;
        }
    }
}
