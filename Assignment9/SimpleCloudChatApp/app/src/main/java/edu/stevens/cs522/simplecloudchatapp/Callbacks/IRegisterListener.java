package edu.stevens.cs522.simplecloudchatapp.Callbacks;

import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;

/**
 * Created by wyf920621 on 4/24/15.
 */
public interface IRegisterListener {
    public void register(String host, int port, String name);
    public void createChatroom(String name);
    public void send(Message message, Chatroom chatroom);
}
