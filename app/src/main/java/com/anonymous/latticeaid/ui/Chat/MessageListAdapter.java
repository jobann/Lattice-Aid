package com.anonymous.latticeaid.ui.Chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.MainActivity;
import com.anonymous.latticeaid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    List<UserMessage> userMessagesList;

    public MessageListAdapter(List<UserMessage> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_me, parent, false);
            return new SentMessageHolder(view);

        } else {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_other, parent, false);
            return new ReceivedMessageHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String message = userMessagesList.get(position).getMessage();
        Date date = userMessagesList.get(position).getDate();
        Date previousDate = null;

        if (position != 0) {
            previousDate = userMessagesList.get(position - 1).getDate();
        }
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                if (previousDate == null) {
                    ((SentMessageHolder) holder).bind(message, getFormatTime(date), getFormatDate(date));
                } else {// if previous date is not null
                    if (getFormatDate(date).equals(getFormatDate(previousDate))) {//if current and previous date is same
                        ((SentMessageHolder) holder).bind(message, getFormatTime(date));
                    } else {
                        ((SentMessageHolder) holder).bind(message, getFormatTime(date), getFormatDate(date));
                    }
                }


                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                if (previousDate == null) {
                    ((ReceivedMessageHolder) holder).bind(message, getFormatTime(date), getFormatDate(date));
                } else {// if previous date is not null
                    if (getFormatDate(date).equals(getFormatDate(previousDate))) {//if current and previous date is same
                        ((ReceivedMessageHolder) holder).bind(message, getFormatTime(date));
                    } else {
                        ((ReceivedMessageHolder) holder).bind(message, getFormatTime(date), getFormatDate(date));
                    }
                }

        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (userMessagesList.get(position).getAndroid_id().equalsIgnoreCase(MainActivity.android_id)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else
            return VIEW_TYPE_MESSAGE_RECEIVED;
    }


    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, text_gchat_date_me, text_gchat_timestamp_me;
        ConstraintLayout item_chat_me_CL;

        SentMessageHolder(View itemView) {
            super(itemView);
            item_chat_me_CL = itemView.findViewById(R.id.item_chat_me_CL);
            messageText = itemView.findViewById(R.id.text_gchat_message_me);
            text_gchat_date_me = itemView.findViewById(R.id.text_gchat_date_me);
            text_gchat_timestamp_me = itemView.findViewById(R.id.text_gchat_timestamp_me);
        }

        void bind(String message, String time, String date) {
            messageText.setText(message);
            text_gchat_timestamp_me.setText(time);
            text_gchat_date_me.setText(date);
        }

        void bind(String message, String time) {
            messageText.setText(message);
            text_gchat_timestamp_me.setText(time);
            item_chat_me_CL.removeView(text_gchat_date_me);
        }
    }

    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, text_gchat_date_other, text_gchat_timestamp_other;
        ConstraintLayout item_chat_other_CL;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            item_chat_other_CL = itemView.findViewById(R.id.item_chat_other_CL);
            messageText = itemView.findViewById(R.id.text_gchat_message_other);
            text_gchat_timestamp_other = itemView.findViewById(R.id.text_gchat_timestamp_other);
            text_gchat_date_other = itemView.findViewById(R.id.text_gchat_date_other);
        }

        void bind(String message, String time, String date) {
            messageText.setText(message);
            text_gchat_timestamp_other.setText(time);
            text_gchat_date_other.setText(date);
        }

        void bind(String message, String time) {
            messageText.setText(message);
            text_gchat_timestamp_other.setText(time);
            item_chat_other_CL.removeView(text_gchat_date_other);
        }
    }

    String getFormatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    String getFormatTime(Date date) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return timeFormat.format(date);
    }

}
