package com.chat.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.app.R;
import com.chat.app.adapters.ChatAdapter;
import com.chat.app.viewmodels.ChatViewModel;

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    private ChatViewModel chatViewModel;

    private EditText messageEditText;
    private RecyclerView chatRecyclerView;
    private String fromId, toId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageButton sendButton = findViewById(R.id.send_image_button);
        messageEditText = findViewById(R.id.message_edit_text);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        sendButton.setOnClickListener(this);
        messageEditText.setOnClickListener(this);
        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent() != null) {
            fromId = getIntent().getStringExtra("from user id");
            toId = getIntent().getStringExtra("to user id");
        }

        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        chatViewModel.readMessages(fromId, toId);

        chatViewModel.getCreatedResponse().observe(this, createdResponse -> {
            if (createdResponse.equals("created")) {
                messageEditText.setText("");
            }
        });

        chatViewModel.getErrorResponse().observe(this, errorResponse ->
                Toast.makeText(this, errorResponse, Toast.LENGTH_SHORT).show());

        chatViewModel.getChatMessageResponse().observe(this, chatMessages -> {
            ChatAdapter chatAdapter = new ChatAdapter(this, chatMessages);
            chatRecyclerView.setAdapter(chatAdapter);
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_image_button) {
            chatViewModel.createMessage(messageEditText.getText().toString(), fromId, toId);
            chatViewModel.createLatestMessage(messageEditText.getText().toString(), fromId, toId);
        }
    }
}
