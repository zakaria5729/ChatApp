package com.chat.app.viewmodels;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chat.app.activities.BaseActivity;
import com.chat.app.models.User;
import com.chat.app.services.UserService;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UserViewModel extends ViewModel {
    private UserService userService = new UserService();
    private MutableLiveData<String> errorResponse = new MutableLiveData<>();
    private MutableLiveData<List<User>> usersResponse = new MutableLiveData<>();

    public void allUsers() {
        userService.getUser(null, (users, error) -> {
            if (users != null && users.size() > 0) {
                List<User> userList = new ArrayList<>();
                String currentUserId = ParseUser.getCurrentUser().getObjectId();

                userService.lastMessage((objects, error1) -> {
                    if (objects != null) {
                        String time = null, text = null, fileName = null;

                        for (ParseUser user : users) {
                            for (ParseObject object : objects) {
                                String lastMessageId = object.getString("lastMessageId");
                                if (lastMessageId != null && !currentUserId.equals(user.getObjectId()) && (lastMessageId.equals(currentUserId + user.getObjectId()) || lastMessageId.equals(user.getObjectId() + currentUserId))) {
                                    time = object.getUpdatedAt().toString();
                                    text = object.getString("text");
                                    fileName = object.getString("fileName");
                                    break;
                                } else {
                                    time = null;
                                    text = null;
                                }
                            }

                            if (!currentUserId.equals(user.getObjectId())) {
                                userList.add(new User(user.getObjectId(), user.getUsername(), user.getEmail(), text, fileName, getDateTime(time)));
                            }
                        }
                        usersResponse.setValue(userList);

                    } else if (error1 != null) {
                        errorResponse.setValue(error1);
                    }
                });
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

    public void searchedUser(String searchedKey) {
        if (TextUtils.isEmpty(searchedKey) || !BaseActivity.isValidEmail(searchedKey)) {
            return;
        }

        userService.getUser(searchedKey, (users, error) -> {
            if (users != null && users.size() > 0) {
                List<User> userList = new ArrayList<>();

                for (ParseUser user : users) {
                    if (!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        userList.add(new User(user.getObjectId(), user.getUsername(), user.getEmail(), "Hello, how are you?", "", "3:35AM"));
                    }
                }
                usersResponse.setValue(userList);
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    public LiveData<List<User>> getAllUsersResponse() {
        return usersResponse;
    }

    public LiveData<String> getErrorResponse() {
        return errorResponse;
    }
}