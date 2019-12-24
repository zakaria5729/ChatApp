package com.chat.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chat.app.R;
import com.chat.app.activities.ChatActivity;
import com.chat.app.models.User;
import com.parse.ParseUser;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final String defaultAvatar = "https://www.pngkey.com/png/full/230-2301779_best-classified-apps-default-user-profile.png";
    private Context context;
    private List<User> userList;

    public UserAdapter() {}

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(context).inflate(R.layout.item_users_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        if (user != null) {
            holder.lastMessageLinearLayout.setVisibility(View.GONE);
            holder.nameTextView.setText(user.getName());
            String lastSeen = " \u2022 " + user.getLastSeen();

            if (user.getLastMessage() != null && !TextUtils.isEmpty(user.getLastMessage().trim())) {
                holder.lastMessageLinearLayout.setVisibility(View.VISIBLE);
                holder.lastMessageOrFileTextView.setText(user.getLastMessage());
                holder.lastSeenTimeTextView.setText(lastSeen);
            } else if (user.getFileFromWho() != null && !TextUtils.isEmpty(user.getFileFromWho().trim())) {
                holder.lastMessageLinearLayout.setVisibility(View.VISIBLE);
                holder.lastMessageOrFileTextView.setText(user.getFileFromWho());
                holder.lastSeenTimeTextView.setText(lastSeen);
            }

            Glide.with(context)
                    .load(defaultAvatar)
                    .fitCenter()
                    .into(holder.userImageView);
        }
    }

    @Override
    public int getItemCount() {
        return (userList != null && userList.size() > 0) ? userList.size() : 0;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView userImageView;
        private LinearLayout lastMessageLinearLayout;
        private TextView nameTextView, lastMessageOrFileTextView, lastSeenTimeTextView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);

            lastMessageLinearLayout = itemView.findViewById(R.id.last_message_linear_layout);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            userImageView = itemView.findViewById(R.id.user_image_view);
            lastMessageOrFileTextView = itemView.findViewById(R.id.last_message_or_file_text_view);
            lastSeenTimeTextView = itemView.findViewById(R.id.last_seen_time_text_view);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("from_user_id", ParseUser.getCurrentUser().getObjectId());
                intent.putExtra("to_user_id", userList.get(getAdapterPosition()).getId());
                context.startActivity(intent);
            });
        }
    }
}
