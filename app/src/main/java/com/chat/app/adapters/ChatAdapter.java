package com.chat.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chat.app.R;
import com.chat.app.interfaces.MyOnClickListener;
import com.chat.app.models.ChatMessage;
import com.parse.ParseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private final String defaultAvatar = "https://www.pngkey.com/png/full/230-2301779_best-classified-apps-default-user-profile.png";
    private final String fromId = ParseUser.getCurrentUser().getObjectId();

    private Context context;
    private List<ChatMessage> chatMessageList;
    private MyOnClickListener myOnClickListener;

    public ChatAdapter(Context context, List<ChatMessage> chatMessageList) {
        this.context = context;
        this.chatMessageList = chatMessageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        String fromId = chatMessageList.get(position).getFromId();
        holder.fromRelativeLayout.setVisibility(View.GONE);
        holder.toRelativeLayout.setVisibility(View.GONE);

        if (this.fromId.equals(fromId)) {
            ChatMessage sender = chatMessageList.get(position);
            holder.fromRelativeLayout.setVisibility(View.VISIBLE);
            holder.fromMessageTextView.setText(sender.getText());
            holder.fromTime.setText(sender.getCreatedAt());

            Glide.with(context)
                    .load(defaultAvatar)
                    .fitCenter()
                    .placeholder(R.drawable.custom_rounder_gray)
                    .error(R.drawable.custom_rounder_gray)
                    .into(holder.fromImageView);
        } else {
            ChatMessage receiver = chatMessageList.get(position);
            holder.toRelativeLayout.setVisibility(View.VISIBLE);
            holder.toMessageTextView.setText(receiver.getText());
            holder.toTime.setText(receiver.getCreatedAt());

            Glide.with(context)
                    .load(defaultAvatar)
                    .fitCenter()
                    .placeholder(R.drawable.custom_rounder_gray)
                    .error(R.drawable.custom_rounder_gray)
                    .into(holder.toImageView);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size() > 0 ? chatMessageList.size() : 0;
    }

    public void setMyOnClickListener(MyOnClickListener myOnClickListener) {
        this.myOnClickListener = myOnClickListener;
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout fromRelativeLayout, toRelativeLayout;
        private ImageView fromImageView, toImageView;
        private TextView fromMessageTextView, toMessageTextView, toTime, fromTime;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            toImageView = itemView.findViewById(R.id.to_image_view);
            toTime = itemView.findViewById(R.id.to_time_text_view);
            fromTime = itemView.findViewById(R.id.from_time_text_view);
            fromImageView = itemView.findViewById(R.id.from_image_view);
            fromMessageTextView = itemView.findViewById(R.id.from_message_text_view);
            toMessageTextView = itemView.findViewById(R.id.to_message_text_view);
            fromRelativeLayout = itemView.findViewById(R.id.from_relative_layout);
            toRelativeLayout = itemView.findViewById(R.id.to_relative_layout);

            toRelativeLayout.setOnLongClickListener(v -> {
                myOnClickListener.myOnClick(chatMessageList.get(getAdapterPosition()).getObjectId());
                return true;
            });

            fromRelativeLayout.setOnLongClickListener(v -> {
                myOnClickListener.myOnClick(chatMessageList.get(getAdapterPosition()).getObjectId());
                return true;
            });
        }
    }
}
