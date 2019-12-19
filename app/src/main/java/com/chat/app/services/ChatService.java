package com.chat.app.services;

import com.chat.app.interfaces.OnObjectResponse;
import com.chat.app.interfaces.OnResponse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ChatService {

    public void createMessage(String message, String fromId, String toId, OnResponse response) {
        ParseObject parseObject = new ParseObject("ChattingData");
        parseObject.put("fromId", fromId);
        parseObject.put("toId", toId);
        parseObject.put("message", message);
        parseObject.saveInBackground(e -> {
            if (e != null) {
                response.onComplete(null, e.getMessage());
            } else {
                response.onComplete("created", null);
            }
        });
    }

    public void createLatestMessage(String message, String fromId, String toId, OnResponse response) {
        /*ParseQuery<ParseObject> query = ParseQuery.getQuery("LatestMessages");
        query.getInBackground("hUbdsnX3aD", (object, e) -> {
            if (e == null) {
                object.put("fromId", fromId);
                object.put("toId", toId);
                object.put("message", message);
                object.saveInBackground(e1 -> {
                    if (e1 != null) {
                        response.onComplete(null, e1.getMessage());
                    }
                });
            }
        });*/

        /*ParseObject parseObject = new ParseObject("LatestMessages");
        parseObject.put("fromId", fromId);
        parseObject.put("toId", toId);
        parseObject.put("message", message);
        parseObject.saveInBackground(e -> {
            if (e != null) {
                response.onComplete(null, e.getMessage());
            } else {
                response.onComplete("created", null);
            }
        });*/
    }

    public void readMessages(String fromId, String toId, OnObjectResponse response) {
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("ChattingData");
        query1.whereEqualTo("fromId", fromId);
        query1.whereEqualTo("toId", toId);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("ChattingData");
        query2.whereEqualTo("fromId", toId);
        query2.whereEqualTo("toId", fromId);

        List<ParseQuery<ParseObject>> parseQueries = new ArrayList<>();
        parseQueries.add(query1);
        parseQueries.add(query2);

        ParseQuery<ParseObject> combineQuery = ParseQuery.or(parseQueries);
        combineQuery.findInBackground((objects, e) -> {
            if (e == null) {
                response.onObject(objects, null);
            } else {
                response.onObject(null, e.getMessage());
            }
        });
    }
}
