package edu.stevens.cs522.chatapp.singleprocess.Managers;

import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.chatapp.singleprocess.Entities.Peer;
import edu.stevens.cs522.chatapp.singleprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.singleprocess.IQueryListener;

/**
 * Created by wyf920621 on 3/3/15.
 */
public class PeerManager extends Manager<Peer> {
    public PeerManager(Context context, IEntityCreator<Peer> creator, int loaderID) {
        super(context, creator, loaderID);
    }

    public void QueryAsync(Uri uri, IQueryListener<Peer> listener) {
        this.executeQuery(uri, listener);
    }
}
