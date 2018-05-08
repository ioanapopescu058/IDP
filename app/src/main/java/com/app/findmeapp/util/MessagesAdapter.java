package com.app.findmeapp.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.findmeapp.R;
import com.app.findmeapp.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    public static final int SENT = 0;
    public static final int RECEIVED = 1;

    private List<ChatMessage> messageList;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;

        public View layout;

        public ViewHolder(View view) {
            super(view);
            layout = view;
            messageTextView = view.findViewById(R.id.text_message_body);
        }
    }

    public MessagesAdapter(List<ChatMessage> myDataset, Context context) {
        messageList = myDataset;
        mContext = context;
    }

    public void add(int position, ChatMessage message) {
        messageList.add(position, message);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        messageList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return SENT;
        } else {
            return RECEIVED;
        }
    }

    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == SENT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.sent_message, null);
        } else if (viewType == RECEIVED) {
            view = LayoutInflater.from(mContext).inflate(R.layout.received_message, null);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        holder.messageTextView.setText(msg.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

}