package com.chat.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.chat.app.R;
import com.chat.app.adapters.ChatAdapter;
import com.chat.app.models.ChatMessage;
import com.chat.app.viewmodels.ChatViewModel;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private final int CAMERA_REQUEST_CODE = 111;
    private final int GALLERY_REQUEST_CODE = 222;

    private ChatViewModel chatViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatAdapter chatAdapter;

    private FrameLayout frameLayout;
    private RecyclerView recyclerView;
    private EditText messageEditText;
    private String fromId, toId, textMessage;
    private int skipLimit = 0, limit = 10;
    private List<ChatMessage> chatMessageList;
    private ImageView selectedPreviewImageView;
    private ProgressBar sendProgressBar;
    private InputStream inputStream;
    private File tempImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        frameLayout = findViewById(R.id.frame_layout);
        messageEditText = findViewById(R.id.message_edit_text);
        sendProgressBar = findViewById(R.id.send_progressbar);
        ProgressBar progressBar = findViewById(R.id.progressbar);
        recyclerView = findViewById(R.id.chat_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        ImageButton sendButton = findViewById(R.id.send_image_button);
        ImageButton cameraButton = findViewById(R.id.camera_image_button);
        selectedPreviewImageView = findViewById(R.id.selected_preview_image_view);
        ImageView cancelPreviewImageView = findViewById(R.id.cancel_preview_image_view);
        sendButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        messageEditText.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);
        cancelPreviewImageView.setOnClickListener(this);

        chatMessageList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
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
                chatViewModel.checkLatestMessage(fromId, toId);
            }
        });

        chatViewModel.getErrorResponse().observe(this, errorResponse -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, errorResponse, Toast.LENGTH_SHORT).show();
        });

        chatViewModel.getReadMessageResponse().observe(this, chatMessages -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);

            if (chatMessages.size() > 0) {
                skipLimit += limit;

                if (skipLimit <= limit) {
                    this.chatMessageList = chatMessages;
                    chatAdapter = new ChatAdapter(this, this.chatMessageList);
                    recyclerView.setAdapter(chatAdapter);
                } else {
                    int position = this.chatMessageList.size();
                    this.chatMessageList.addAll(chatMessages);
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(position);
                }
            }
        });

        chatViewModel.getCheckLatestMessage().observe(this, objectId -> {
            String currentUserName = ParseUser.getCurrentUser().getUsername();
            if (objectId != null) {
                chatViewModel.createOrUpdateLatestMessage(objectId, textMessage, currentUserName, fromId, toId);
            } else {
                chatViewModel.createOrUpdateLatestMessage(null, textMessage, currentUserName, fromId, toId);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_image_button:
                textMessage = messageEditText.getText().toString();
                if (inputStream != null || !TextUtils.isEmpty(textMessage)) {
                    chatViewModel.createMessage(textMessage, inputStream, fromId, toId, fromId + toId);
                    sendProgressBar.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.GONE);
                    messageEditText.setText("");
                }
                break;

            case R.id.camera_image_button:
                String[] items = {"Camera", "Gallery"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose an action");
                builder.setItems(items, (dialog, i) -> {
                    int permissionStatus;
                    if (items[i].equals("Camera")) {
                        permissionStatus = checkUserPermission(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE);
                        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                            capturedImage();
                        }
                    } else if (items[i].equals("Gallery")) {
                        permissionStatus = checkUserPermission(Manifest.permission.READ_EXTERNAL_STORAGE, GALLERY_REQUEST_CODE);
                        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                            pickImageFromGallery();
                        }
                    }
                });
                builder.create().show();
                break;

            case R.id.cancel_preview_image_view:
                frameLayout.setVisibility(View.GONE);
                if (inputStream != null) {
                    inputStream = null;
                }
                if (tempImageFile != null) {
                    tempImageFile = null;
                }
                break;
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "choose an image"), GALLERY_REQUEST_CODE);
    }

    private void capturedImage() {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            tempImageFile = File.createTempFile("IMG_" + System.currentTimeMillis(), ".jpg", storageDir);
            Uri photoURI = FileProvider.getUriForFile(this, "com.chat.app.fileprovider", this.tempImageFile);

            if (photoURI != null) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capturedImage();
            }
        } else if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                try {
                    Uri imageUri = Uri.fromFile(tempImageFile);
                    if (imageUri != null) {
                        inputStream = getContentResolver().openInputStream(imageUri);
                        Glide.with(this).load(imageUri).into(selectedPreviewImageView);
                        frameLayout.setVisibility(View.VISIBLE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null) {
                    Uri imageUri = data.getData();
                    try {
                        if (imageUri != null) {
                            inputStream = getContentResolver().openInputStream(imageUri);
                            Glide.with(this).load(imageUri).into(selectedPreviewImageView);
                            frameLayout.setVisibility(View.VISIBLE);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (tempImageFile != null) {
                tempImageFile = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
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
