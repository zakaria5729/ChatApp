package com.chat.app.services;

import com.chat.app.interfaces.OnResponse;
import com.chat.app.interfaces.OnUserResponse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class AuthService {

    public void signUp(String name, String email, String password, OnResponse response) {
        ParseUser parseUser = new ParseUser();
        parseUser.setUsername(name);
        parseUser.setPassword(password);
        parseUser.setEmail(email);

        parseUser.signUpInBackground(e -> {
            if (e == null) {
                response.onComplete("Successfully signed up. Please check inbox and verify your email", null);
            } else {
                response.onComplete(null, e.getMessage());
            }
        });
    }

    public void login(String email, String password, OnResponse response) {
        ParseUser.logInInBackground(email, password, (user, e) -> {
            if (user != null) {
                response.onComplete(user.getSessionToken(), null);
            } else {
                response.onComplete(null, e.getMessage());
            }
        });
    }

    public void userVerification(String email, OnUserResponse response) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("email", email);
        query.findInBackground((objects, e) -> {
            if (e == null) {
                response.onUser(objects, null);
            } else {
                response.onUser(null, e.getMessage());
            }
        });
    }
}
