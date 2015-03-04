package edu.stevens.cs522.chat.oneway.server.managers;

import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.chat.oneway.server.IEntityCreator;
import edu.stevens.cs522.chat.oneway.server.IQueryListener;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;

/**
 * Created by wyf920621 on 2/23/15.
 */
public class PeerManager extends Manager<Peer> {
    public PeerManager(Context context, IEntityCreator<Peer> creator, int loaderID) {
        super(context, creator, loaderID);
    }

    public void QueryAsync(Uri uri, IQueryListener<Peer> listener) {
        this.executeQuery(uri, listener);
    }
}
