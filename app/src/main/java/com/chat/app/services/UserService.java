package com.chat.app.services;

import com.chat.app.interfaces.OnObjectResponse;
import com.chat.app.interfaces.OnUserResponse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    public void getUser(String searchedKey, OnUserResponse response) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        if (searchedKey != null) {
            query.whereEqualTo("email", searchedKey);
        }

        query.findInBackground((objects, e) -> {
            if (e == null) {
                response.onUser(objects, null);
            } else {
                response.onUser(null, e.getMessage());
            }
        });
    }

    public void lastMessage(OnObjectResponse response) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ChattingData");
        query.findInBackground((objects, e) -> {
            if (e == null) {
                response.onObject(objects, null);
            } else {
                response.onObject(null, e.getMessage());
            }
        });
    }
}
