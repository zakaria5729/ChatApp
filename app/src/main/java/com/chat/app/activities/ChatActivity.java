package com.chat.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.chat.app.R;
import com.chat.app.adapters.ChatAdapter;
import com.chat.app.models.ChatMessage;
import com.chat.app.viewmodels.ChatViewModel;
import com.parse.ParseFile;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ChatViewModel chatViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatAdapter chatAdapter;

    private FrameLayout frameLayout;
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private String fromId, toId, textMessage, fileName;
    private int skipLimit = 0, limit = 10;
    private List<ChatMessage> chatMessageList;
    private ParseFile parseFile;
    private ImageView selectedPreviewImageView;
    private ProgressBar sendProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        frameLayout = findViewById(R.id.frame_layout);
        messageEditText = findViewById(R.id.message_edit_text);
        sendProgressBar = findViewById(R.id.send_progressbar);
        ProgressBar progressBar = findViewById(R.id.progressbar);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        ImageButton sendButton = findViewById(R.id.send_image_button);
        ImageButton cameraButton = findViewById(R.id.camera_image_button);
        //TextView noMessageTextView = findViewById(R.id.no_message_text_view);
        selectedPreviewImageView = findViewById(R.id.selected_preview_image_view);
        ImageView cancelPreviewImageView = findViewById(R.id.cancel_preview_image_view);
        sendButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        messageEditText.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);
        cancelPreviewImageView.setOnClickListener(this);

        chatMessageList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        if (getIntent() != null) {
            fromId = getIntent().getStringExtra("from_user_id");
            toId = getIntent().getStringExtra("to_user_id");
        }

        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        chatViewModel.readMessages(fromId, toId, limit, skipLimit);

        chatViewModel.getCreatedResponse().observe(this, createdResponse -> {
            sendProgressBar.setVisibility(View.GONE);

            if (createdResponse.equals("created")) {
                messageEditText.setText("");
                chatViewModel.checkLatestMessage(fromId, toId);
                parseFile = null;
                frameLayout.setVisibility(View.GONE);
            }
        });

        chatViewModel.getErrorResponse().observe(this, errorResponse -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, errorResponse, Toast.LENGTH_SHORT).show();
        });

        chatViewModel.getReadMessageResponse().observe(this, chatMessages -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);

            if (chatMessages.size() > 0) {
                skipLimit += limit;
                swipeRefreshLayout.setRefreshing(false);

                if (skipLimit <= limit) {
                    this.chatMessageList = chatMessages;
                    chatAdapter = new ChatAdapter(this, this.chatMessageList);
                    chatRecyclerView.setAdapter(chatAdapter);
                } else {
                    int position = this.chatMessageList.size();
                    this.chatMessageList.addAll(chatMessages);
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.smoothScrollToPosition(position);
                }
            }/* else {
                if (this.chatMessageList.size() <= 0) {
                    String noMessage = "No messages found. Please send a message";
                    noMessageTextView.setText(noMessage);
                    swipeRefreshLayout.setRefreshing(false);
                    noMessageTextView.setVisibility(View.VISIBLE);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }*/
        });

        chatViewModel.getCheckLatestMessage().observe(this, objectId -> {
            if (objectId != null) {
                chatViewModel.createOrUpdateLatestMessage(objectId, textMessage, fileName, fromId, toId);
            } else {
                chatViewModel.createOrUpdateLatestMessage(null, textMessage, fileName, fromId, toId);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_image_button:
                textMessage = messageEditText.getText().toString();
                if (!TextUtils.isEmpty(textMessage) || parseFile != null) {
                    chatViewModel.createMessage(textMessage, parseFile, fromId, toId, fromId + toId);
                    sendProgressBar.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.camera_image_button:
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle("Select the area which you want to crop")
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setCropMenuCropButtonTitle("Done")
                        .setCropMenuCropButtonIcon(R.drawable.ic_camera)
                        .start(this);
                break;

            case R.id.cancel_preview_image_view:
                parseFile = null;
                frameLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK && result != null) {
                Uri uri = result.getUri();

                try {
                    InputStream iStream = getContentResolver().openInputStream(uri);
                    if (iStream != null) {
                        byte[] inputFileData = getBytes(iStream);

                        if (inputFileData != null) {
                            this.fileName = "image_" + System.currentTimeMillis();
                            parseFile = new ParseFile(this.fileName, inputFileData);
                            frameLayout.setVisibility(View.VISIBLE);
                            Glide.with(this).load(uri).into(selectedPreviewImageView);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE && result != null) {
                Exception exception = result.getError();
                exception.printStackTrace();
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }

        return byteArrayOutputStream.toByteArray();
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
