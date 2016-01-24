package edu.stevens.cs522.simplecloudchatapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by wyf920621 on 3/14/15.
 */
public class AckReceiverWrapper extends ResultReceiver {
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
