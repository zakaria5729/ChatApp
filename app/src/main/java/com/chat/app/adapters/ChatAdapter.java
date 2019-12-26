package com.chat.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chat.app.R;
import com.chat.app.activities.PreviewActivity;
import com.chat.app.models.ChatMessage;
import com.parse.ParseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private final String defaultAvatar = "https://www.pngkey.com/png/full/230-2301779_best-classified-apps-default-user-profile.png";
    private final String fromId = ParseUser.getCurrentUser().getObjectId();

    private Context context;
    private List<ChatMessage> chatMessageList;

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
        holder.fromMessageTextView.setVisibility(View.GONE);
        holder.fromRelativeLayout.setVisibility(View.GONE);
        holder.toMessageTextView.setVisibility(View.GONE);
        holder.toRelativeLayout.setVisibility(View.GONE);
        holder.fromCardView.setVisibility(View.GONE);
        holder.toCardView.setVisibility(View.GONE);

        if (this.fromId.equals(fromId)) {
            ChatMessage fromChatMessage = chatMessageList.get(position);
            holder.fromRelativeLayout.setVisibility(View.VISIBLE);
            holder.fromTime.setText(fromChatMessage.getCreatedAt());

            if (fromChatMessage.getText() != null && !TextUtils.isEmpty(fromChatMessage.getText())) {
                holder.fromMessageTextView.setVisibility(View.VISIBLE);
                holder.fromMessageTextView.setText(fromChatMessage.getText());
            }

            Glide.with(context)
                    .load(defaultAvatar)
                    .fitCenter()
                    .error(R.color.colorLighterGray)
                    .into(holder.fromProfileImageView);

            if (fromChatMessage.getImageUrl() != null && !TextUtils.isEmpty(fromChatMessage.getImageUrl())) {
                holder.fromCardView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(fromChatMessage.getImageUrl())
                        .apply(new RequestOptions()
                                .format(DecodeFormat.PREFER_ARGB_8888)
                                .override(Target.SIZE_ORIGINAL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.fromProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.fromProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .error(R.color.colorLighterGray)
                        .into(holder.fromImageView);
            }

            holder.fromImageView.setOnClickListener(v -> {
                if (fromChatMessage.getImageUrl() != null && !TextUtils.isEmpty(fromChatMessage.getImageUrl())) {
                    Intent intent = new Intent(context, PreviewActivity.class);
                    intent.putExtra("image_url", fromChatMessage.getImageUrl());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    context.startActivity(intent);
                }
            });

        } else {
            ChatMessage toChatMessage = chatMessageList.get(position);
            holder.toRelativeLayout.setVisibility(View.VISIBLE);
            holder.toTime.setText(toChatMessage.getCreatedAt());

            if (toChatMessage.getText() != null && !TextUtils.isEmpty(toChatMessage.getText())) {
                holder.toMessageTextView.setVisibility(View.VISIBLE);
                holder.toMessageTextView.setText(toChatMessage.getText());
            }

            Glide.with(context)
                    .load(defaultAvatar)
                    .fitCenter()
                    .error(R.color.colorLighterGray)
                    .into(holder.toProfileImageView);

            if (toChatMessage.getImageUrl() != null && !TextUtils.isEmpty(toChatMessage.getImageUrl())) {
                holder.toCardView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(toChatMessage.getImageUrl())
                        .apply(new RequestOptions()
                                .format(DecodeFormat.PREFER_ARGB_8888)
                                .override(Target.SIZE_ORIGINAL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.toProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.toProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .error(R.color.colorLighterGray)
                        .into(holder.toImageView);
            }

            holder.fromImageView.setOnClickListener(v -> {
                if (toChatMessage.getImageUrl() != null && !TextUtils.isEmpty(toChatMessage.getImageUrl())) {
                    Intent intent = new Intent(context, PreviewActivity.class);
                    intent.putExtra("image_url", toChatMessage.getImageUrl());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size() > 0 ? chatMessageList.size() : 0;
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private CardView fromCardView, toCardView;
        private ProgressBar fromProgressBar, toProgressBar;
        private RelativeLayout fromRelativeLayout, toRelativeLayout;
        private TextView fromMessageTextView, toMessageTextView, toTime, fromTime;
        private ImageView fromProfileImageView, toProfileImageView, fromImageView, toImageView;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            toCardView = itemView.findViewById(R.id.to_card_view);
            toTime = itemView.findViewById(R.id.to_time_text_view);
            toImageView = itemView.findViewById(R.id.to_image_view);
            fromCardView = itemView.findViewById(R.id.from_card_view);
            toProgressBar = itemView.findViewById(R.id.to_progressbar);
            fromTime = itemView.findViewById(R.id.from_time_text_view);
            fromImageView = itemView.findViewById(R.id.from_image_view);
            fromProgressBar = itemView.findViewById(R.id.from_progressbar);
            toRelativeLayout = itemView.findViewById(R.id.to_relative_layout);
            toMessageTextView = itemView.findViewById(R.id.to_message_text_view);
            fromRelativeLayout = itemView.findViewById(R.id.from_relative_layout);
            toProfileImageView = itemView.findViewById(R.id.to_profile_image_view);
            fromMessageTextView = itemView.findViewById(R.id.from_message_text_view);
            fromProfileImageView = itemView.findViewById(R.id.from_profile_image_view);
        }
    }
}
