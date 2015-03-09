package edu.stevens.cs522.chatapp.separatedprocess.Managers;

import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.chatapp.separatedprocess.Entities.Peer;
import edu.stevens.cs522.chatapp.separatedprocess.IEntityCreator;
import edu.stevens.cs522.chatapp.separatedprocess.IQueryListener;

/**
 * Created by wyf920621 on 3/8/15.
 */
public class PeerManager extends Manager<Peer> {
    public PeerManager(Context context, IEntityCreator<Peer> creator, int loaderID) {
        super(context, creator, loaderID);
    }

    public void QueryAsync(Uri uri, IQueryListener<Peer> listener) {
        this.executeQuery(uri, listener);
    }
}
