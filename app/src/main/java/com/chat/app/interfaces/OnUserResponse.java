package com.chat.app.interfaces;

import com.parse.ParseUser;

import java.util.List;

public interface OnUserResponse {
    void onUser(List<ParseUser> users, String error);
}
