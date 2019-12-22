package com.chat.app.services;

import com.chat.app.interfaces.OnObjectResponse;
import com.chat.app.interfaces.OnResponse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ChatService {

    public void createMessage(String text, String fromId, String toId, String threadId, OnResponse response) {
        ParseObject parseObject = new ParseObject("Message");
        parseObject.put("text", text);
        parseObject.put("fromId", fromId);
        parseObject.put("toId", toId);
        parseObject.put("threadId", threadId);
        parseObject.saveInBackground(e -> {
            if (e != null) {
                response.onComplete(null, e.getMessage());
            } else {
                response.onComplete("created", null);
            }
        });
    }

    public void createOrUpdateLatestMessage(String objectId, String text, String fromId, String toId, OnResponse response) {
        if (objectId != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("LatestMessage");
            query.getInBackground(objectId, (object, e) -> {
                if (e == null) {
                    object.put("lastMessageId", fromId + toId);
                    object.put("fromId", fromId);
                    object.put("toId", toId);
                    object.put("text", text);
                    object.saveInBackground(error -> {
                        if (error != null) {
                            response.onComplete(null, error.getMessage());
                        } else {
                            response.onComplete("updated", null);
                        }
                    });
                }
            });
        } else {
            ParseObject parseObject = new ParseObject("LatestMessage");
            parseObject.put("lastMessageId", fromId + toId);
            parseObject.put("fromId", fromId);
            parseObject.put("toId", toId);
            parseObject.put("text", text);
            parseObject.saveInBackground(e -> {
                if (e != null) {
                    response.onComplete(null, e.getMessage());
                } else {
                    response.onComplete("created", null);
                }
            });
        }
    }

    public void readMessages(String fromId, String toId, int limit, int skipLimit, OnObjectResponse response) {
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Message");
        query1.whereEqualTo("threadId", fromId + toId);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Message");
        query2.whereEqualTo("threadId", toId + fromId);

        List<ParseQuery<ParseObject>> parseQueries = new ArrayList<>();
        parseQueries.add(query1);
        parseQueries.add(query2);

        ParseQuery<ParseObject> combineQuery = ParseQuery.or(parseQueries);
        //combineQuery.setSkip(skipLimit);
        //combineQuery.orderByDescending("createdAt").setLimit(limit);
        combineQuery.findInBackground((objects, e) -> {
            if (e == null) {
                response.onObject(objects, null);
            } else {
                response.onObject(null, e.getMessage());
            }
        });
    }

    public void checkLatestMessage(String fromId, String toId, OnObjectResponse response) {
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("LatestMessage");
        query1.whereEqualTo("lastMessageId", fromId + toId);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("LatestMessage");
        query2.whereEqualTo("lastMessageId", toId + fromId);

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
