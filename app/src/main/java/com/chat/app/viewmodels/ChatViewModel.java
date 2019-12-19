package com.chat.app.viewmodels;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chat.app.models.ChatMessage;
import com.chat.app.services.ChatService;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {

    private ChatService chatService = new ChatService();
    private MutableLiveData<String> errorResponse = new MutableLiveData<>();
    private MutableLiveData<String> createdResponse = new MutableLiveData<>();
    private MutableLiveData<List<ChatMessage>> chatMessageResponse = new MutableLiveData<>();

    public void createMessage(String message, String fromId, String toId) {
        chatService.createMessage(message, fromId, toId, (success, error) -> {
            if (success != null) {
                createdResponse.setValue(success);
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    public void createLatestMessage(String message, String fromId, String toId) {
        chatService.createLatestMessage(message, fromId, toId, (success, error) -> {
            if (success != null) {
                //createdResponse.setValue(success);
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    public void readMessages(String fromId, String toId) {
        chatService.readMessages(fromId, toId, (objects, error) -> {
            if (objects != null && objects.size() > 0) {
                List<ChatMessage> chatMessageList = new ArrayList<>();

                for (ParseObject object : objects) {
                    String fromUserId = object.getString("fromId");
                    String toUserId = object.getString("toId");
                    String message = object.getString("message");

                    if (!TextUtils.isEmpty(fromUserId) && !TextUtils.isEmpty(toUserId) && !TextUtils.isEmpty(message)) {
                        ChatMessage senderReceiver = new ChatMessage(fromUserId, toUserId, message);
                        chatMessageList.add(senderReceiver);
                    }
                }
                chatMessageResponse.setValue(chatMessageList);
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    public LiveData<String> getCreatedResponse() {
        return createdResponse;
    }

    public LiveData<List<ChatMessage>> getChatMessageResponse() {
        return chatMessageResponse;
    }

    public LiveData<String> getErrorResponse() {
        return errorResponse;
    }
}
