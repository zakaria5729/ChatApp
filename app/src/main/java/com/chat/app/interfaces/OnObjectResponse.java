package com.chat.app.interfaces;

import com.parse.ParseObject;

import java.util.List;

public interface OnObjectResponse {
    void onObject(List<ParseObject> objects, String error);
}
