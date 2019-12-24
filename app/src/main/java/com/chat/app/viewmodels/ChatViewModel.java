package com.chat.app.viewmodels;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.chat.app.models.ChatMessage;
import com.chat.app.services.ChatService;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChatViewModel extends AndroidViewModel {

    private ChatService chatService = new ChatService();
    private MutableLiveData<String> errorResponse = new MutableLiveData<>();
    private MutableLiveData<String> createdResponse = new MutableLiveData<>();
    private MutableLiveData<List<ChatMessage>> readMessageResponse = new MutableLiveData<>();
    private MutableLiveData<String> checkLatestMessage = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);
    }

    public void createMessage(String text, InputStream inputStream, String fromId, String toId, String threadId) {
        if (TextUtils.isEmpty(fromId) || TextUtils.isEmpty(toId) || TextUtils.isEmpty(threadId)) {
            return;
        }

        try {
            chatService.createMessage(text, getParseFile(inputStream), fromId, toId, threadId, (success, error) -> {
                if (success != null) {
                    createdResponse.setValue(success);
                } else if (error != null) {
                    errorResponse.setValue(error);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void createOrUpdateLatestMessage(String objectId, String text, String fileFromWho, String fromId, String toId) {
        if (TextUtils.isEmpty(fromId) || TextUtils.isEmpty(toId)) {
            return;
        }

        chatService.createOrUpdateLatestMessage(objectId, text, fileFromWho, fromId, toId, (success, error) -> {
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
                List<ChatMessage> chatMessageList = new ArrayList<>();

                for (ParseObject object : objects) {
                    String objectId = object.getObjectId();
                    String fromUserId = object.getString("fromId");
                    String toUserId = object.getString("toId");
                    String text = object.getString("text");
                    String createdAt = getDateTime(object.getCreatedAt().toString());

                    String imageUrl = null;
                    ParseFile parseFile = object.getParseFile("file");
                    if (parseFile != null && parseFile.getUrl() != null) {
                        imageUrl = parseFile.getUrl();
                    }

                    if (!TextUtils.isEmpty(fromUserId) && !TextUtils.isEmpty(toUserId) && (!TextUtils.isEmpty(text) || imageUrl != null)) {
                        ChatMessage chatMessage = new ChatMessage(objectId, fromUserId, toUserId, text, imageUrl, createdAt);
                        chatMessageList.add(chatMessage);
                    }
                }
                readMessageResponse.setValue(chatMessageList);
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

    private ParseFile getParseFile(InputStream inputStream) throws IOException {
        ParseFile parseFile = null;

        if (inputStream != null) {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytearrayoutputstream);

            byte[] bytes = bytearrayoutputstream.toByteArray();
            if (bytes.length > 0) {
                parseFile = new ParseFile("image_" + System.currentTimeMillis() + ".jpg", bytes);
            }

            inputStream.close();
            bytearrayoutputstream.close();
        }

        return parseFile;
    }

    public LiveData<String> getCreatedResponse() {
        return createdResponse;
    }

    public LiveData<List<ChatMessage>> getReadMessageResponse() {
        return readMessageResponse;
    }

    public LiveData<String> getCheckLatestMessage() {
        return checkLatestMessage;
    }

    public LiveData<String> getErrorResponse() {
        return errorResponse;
    }
}
