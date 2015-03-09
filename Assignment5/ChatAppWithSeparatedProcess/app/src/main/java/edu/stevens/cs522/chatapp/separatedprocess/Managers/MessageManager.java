package edu.stevens.cs522.chatapp.separatedprocess.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.chatapp.separatedprocess.Contracts.MessageContract;
import edu.stevens.cs522.chatapp.separatedprocess.Contracts.PeerContract;
import edu.stevens.cs522.chatapp.separatedprocess.Entities.Message;
import edu.stevens.cs522.chatapp.separatedprocess.Entities.Peer;
import edu.stevens.cs522.chatapp.separatedprocess.IContinue;
import edu.stevens.cs522.chatapp.separatedprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.separatedprocess.IQueryListener;
import edu.stevens.cs522.chatapp.separatedprocess.ISimpleQueryListener;

/**
 * Created by wyf920621 on 3/8/15.
 */
public class MessageManager extends Manager<Message> {
    public MessageManager(Context context, IEntityCreator<Message> creator, int loaderID) {
        super(context, creator, loaderID);
    }

    public void persistSync(Peer peer, Message message) {
        ContentValues values = new ContentValues();
        peer.writeToProvider(values);
        String[] projection = new String[] {PeerContract.ID, PeerContract.NAME, PeerContract.ADDRESS, PeerContract.PORT};
        String selection = PeerContract.NAME + "=? AND " + PeerContract.ADDRESS + "=? AND " + PeerContract.PORT + "=?";
        String[] selectionArgs = new String[] {peer.name, peer.address.getHostAddress(), String.valueOf(peer.port)};
        Cursor cursor = getSyncResolver().query(PeerContract.CONTENT_URI(String.valueOf(peer.id)), projection, selection, selectionArgs, null);
        if (cursor.moveToFirst()) {
            peer.id = PeerContract.getId(cursor);
            values.clear();
            message.writeToProvider(values, peer.id);
            Uri uri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
            message.id = MessageContract.getId(uri);
        }
        else {
            Uri peerUri = getSyncResolver().insert(PeerContract.CONTENT_URI, values);
            peer.id = PeerContract.getId(peerUri);
            values.clear();
            message.writeToProvider(values, peer.id);
            Uri messageUri = getSyncResolver().insert(MessageContract.CONTENT_URI, values);
            message.id = MessageContract.getId(messageUri);
        }
        cursor.close();
    }

    public void persistAsync(final Peer peer, final Message message) {
        final ContentValues values = new ContentValues();
        peer.writeToProvider(values);
        String[] projection = new String[] {PeerContract.ID, PeerContract.NAME, PeerContract.ADDRESS, PeerContract.PORT};
        String selection = PeerContract.NAME + "=? AND " + PeerContract.ADDRESS + "=? AND " + PeerContract.PORT + "=?";
        String[] selectionArgs = new String[] {peer.name, peer.address.getHostAddress(), String.valueOf(peer.port)};
        getAsyncResolver().queryAsync(PeerContract.CONTENT_URI(String.valueOf(peer.id)), projection, selection, selectionArgs, null, new IContinue<Cursor>() {
            public void kontinue(Cursor value) {
                if (value.moveToFirst()) { // Peer exists
                    peer.id = PeerContract.getId(value);
                    values.clear();
                    message.writeToProvider(values, peer.id);
                    getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, values, new IContinue<Uri>() {
                        public void kontinue(Uri value) {
                            message.id = MessageContract.getId(value);
                        }
                    });
                }
                else {
                    getAsyncResolver().insertAsync(PeerContract.CONTENT_URI, values, new IContinue<Uri>() {
                        public void kontinue(Uri value) {
                            peer.id = PeerContract.getId(value);
                            values.clear();
                            message.writeToProvider(values, peer.id);
                            getAsyncResolver().insertAsync(MessageContract.CONTENT_URI, values, new IContinue<Uri>() {
                                public void kontinue(Uri value) {
                                    message.id = MessageContract.getId(value);
                                }
                            });
                        }
                    });
                }
                value.close();
            }
        });
    }

    public void QueryAsync(Uri uri, IQueryListener<Message> listener) {
        this.executeQuery(uri, listener);
    }

    public void QueryDetail(Uri uri, ISimpleQueryListener<Message> listener) {
        this.executeSimpleQuery(uri, listener);
    }
}
