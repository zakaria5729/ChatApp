package com.chat.app.viewmodels;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chat.app.models.ChatMessage;
import com.chat.app.services.ChatService;
import com.parse.ParseObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChatViewModel extends ViewModel {

    private ChatService chatService = new ChatService();
    private MutableLiveData<String> errorResponse = new MutableLiveData<>();
    private MutableLiveData<String> createdResponse = new MutableLiveData<>();
    private MutableLiveData<List<ChatMessage>> chatMessageResponse = new MutableLiveData<>();
    private MutableLiveData<String> checkLatestMessage = new MutableLiveData<>();
    private List<ChatMessage> chatMessageList = new ArrayList<>();

    public void createMessage(String text, String fromId, String toId, String threadId) {
        chatService.createMessage(text, fromId, toId, threadId, (success, error) -> {
            if (success != null) {
                createdResponse.setValue(success);
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    public void checkLatestMessage(String fromId, String toId) {
        chatService.checkLatestMessage(fromId, toId, (objects, error) -> {
            if (objects != null) {
                if (objects.size() > 0) {
                    String objectId = objects.get(0).getObjectId();
                    checkLatestMessage.setValue(objectId);
                } else {
                    checkLatestMessage.setValue(null);
                }
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    public void createOrUpdateLatestMessage(String objectId, String message, String fromId, String toId) {
        chatService.createOrUpdateLatestMessage(objectId, message, fromId, toId, (success, error) -> {
            if (success != null) {
                createdResponse.setValue(success);
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    public void readMessages(String fromId, String toId, int limit, int skipLimit) {
        chatService.readMessages(fromId, toId, limit, skipLimit, (objects, error) -> {
            if (objects != null) {

                for (ParseObject object : objects) {
                    String objectId = object.getObjectId();
                    String fromUserId = object.getString("fromId");
                    String toUserId = object.getString("toId");
                    String text = object.getString("text");
                    String createdAt = getDateTime(object.getCreatedAt().toString());

                    if (!TextUtils.isEmpty(fromUserId) && !TextUtils.isEmpty(toUserId) && !TextUtils.isEmpty(text)) {
                        ChatMessage chatMessage = new ChatMessage(objectId, fromUserId, toUserId, text, createdAt);
                        chatMessageList.add(chatMessage);
                    }
                }
                chatMessageResponse.setValue(chatMessageList);
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    private String getDateTime(String time) {
        String formattedTime = "";

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
            Date past = simpleDateFormat.parse(time);
            Date now = new Date();

            if (past != null) {
                long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
                SimpleDateFormat sdf;

                if (days > 365) {
                    sdf = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
                } else if (days > 7) {
                    sdf = new SimpleDateFormat("MM dd", Locale.getDefault());
                } else if (days >= 1) {
                    sdf = new SimpleDateFormat("EE", Locale.getDefault());
                } else {
                    sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                }

                Date date = simpleDateFormat.parse(time);
                if (date != null) {
                    formattedTime = sdf.format(date);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return formattedTime;
    }

    public LiveData<String> getCreatedResponse() {
        return createdResponse;
    }

    public LiveData<List<ChatMessage>> getChatMessageResponse() {
        return chatMessageResponse;
    }

    public LiveData<String> getCheckLatestMessage() {
        return checkLatestMessage;
    }

    public LiveData<String> getErrorResponse() {
        return errorResponse;
    }
}
