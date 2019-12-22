package com.chat.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chat.app.R;
import com.chat.app.adapters.ChatAdapter;
import com.chat.app.interfaces.MyOnClickListener;
import com.chat.app.models.ChatMessage;
import com.chat.app.viewmodels.ChatViewModel;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity implements View.OnClickListener, MyOnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ChatViewModel chatViewModel;
    private MyOnClickListener myOnClickListener;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatAdapter chatAdapter;

    private EditText messageEditText;
    private RecyclerView chatRecyclerView;
    private String fromId, toId, textMessage;
    private int skipLimit = 1, limit = 5;
    private List<ChatMessage> chatMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ProgressBar progressBar = findViewById(R.id.progressbar);
        ImageButton sendButton = findViewById(R.id.send_image_button);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        messageEditText = findViewById(R.id.message_edit_text);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        TextView noMessageTextView = findViewById(R.id.no_message_text_view);
        myOnClickListener = this;
        sendButton.setOnClickListener(this);
        messageEditText.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);

        chatMessageList = new ArrayList<>();
        chatRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        if (getIntent() != null) {
            fromId = getIntent().getStringExtra("from user id");
            toId = getIntent().getStringExtra("to user id");
        }

        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        chatViewModel.readMessages(fromId, toId, limit, skipLimit);

        chatViewModel.getCreatedResponse().observe(this, createdResponse -> {
            if (createdResponse.equals("created")) {
                messageEditText.setText("");
                chatViewModel.readMessages(fromId, toId, limit, skipLimit);
            }
        });

        chatViewModel.getErrorResponse().observe(this, errorResponse -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, errorResponse, Toast.LENGTH_SHORT).show();
        });

        chatViewModel.getChatMessageResponse().observe(this, chatMessages -> {
            progressBar.setVisibility(View.GONE);

            if (chatMessages.size() > 0) {
               /* skipLimit += chatMessages.size();

                Log.d("fkjdlfjd", skipLimit+"");

                this.chatMessageList = chatMessages;
                swipeRefreshLayout.setRefreshing(false);
                noMessageTextView.setVisibility(View.GONE);

                if (skipLimit <= limit) {
                    chatAdapter = new ChatAdapter(this, this.chatMessageList);
                    chatRecyclerView.setAdapter(chatAdapter);
                } else {
                    chatAdapter.notifyDataSetChanged();
                }*/

                chatAdapter = new ChatAdapter(this, chatMessages);
                chatRecyclerView.setAdapter(chatAdapter);

                chatAdapter.setMyOnClickListener(myOnClickListener);
                //chatRecyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
            } else {
                String noMessage = "No messages found. Please send a message";
                noMessageTextView.setText(noMessage);
                noMessageTextView.setVisibility(View.VISIBLE);
            }
        });

        chatViewModel.getCheckLatestMessage().observe(this, objectId -> {
            if (objectId != null) {
                chatViewModel.createOrUpdateLatestMessage(objectId, textMessage, fromId, toId);
            } else {
                chatViewModel.createOrUpdateLatestMessage(null, textMessage, fromId, toId);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_image_button) {
            textMessage = messageEditText.getText().toString();
            chatViewModel.createMessage(textMessage, fromId, toId, fromId + toId);
            chatViewModel.checkLatestMessage(fromId, toId);
        }
    }

    @Override
    public void myOnClick(String objectId) {
        if (objectId != null) {
            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Message");
            parseQuery.getInBackground(objectId, (object, e) -> {
                if (e == null) {
                    object.deleteInBackground(deleteError -> {
                        if (deleteError == null) {
                            chatViewModel.readMessages(fromId, toId, limit, skipLimit);
                        } else {
                            Toast.makeText(this, deleteError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        if (fromId != null && toId != null && skipLimit >= limit) {
            chatViewModel.readMessages(fromId, toId, limit, skipLimit);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
