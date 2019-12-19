package com.chat.app.viewmodels;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chat.app.activities.BaseActivity;
import com.chat.app.models.ChatMessage;
import com.chat.app.models.User;
import com.chat.app.services.UserService;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends ViewModel {
    private UserService userService = new UserService();
    private MutableLiveData<String> errorResponse = new MutableLiveData<>();
    private MutableLiveData<List<User>> usersResponse = new MutableLiveData<>();

    public void allUsers() {
        userService.getUser(null, (users, error) -> {
            if (users != null && users.size() > 0) {
                List<User> userList = new ArrayList<>();

                for (ParseUser user : users) {
                    if (!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        userList.add(new User(user.getObjectId(), user.getUsername(), user.getEmail(), "Message", "3:35AM"));
                    }
                }
                usersResponse.setValue(userList);
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
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
                        userList.add(new User(user.getObjectId(), user.getUsername(), user.getEmail(), "Hello, how are you?", "3:35AM"));
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
