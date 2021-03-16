package com.anonymous.latticeaid.ui.Chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.MainActivity;
import com.anonymous.latticeaid.R;

import org.apache.commons.lang3.SerializationUtils;

import java.util.Date;

public class ChatFragment extends Fragment {


    Button sendBT;
    EditText msgET;
    RecyclerView recycler_gchat;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        msgET = root.findViewById(R.id.msgET);
        sendBT = root.findViewById(R.id.sendBT);
        recycler_gchat = root.findViewById(R.id.recycler_gchat);

        recycler_gchat.setLayoutManager(new LinearLayoutManager(getContext()));

        MessageListAdapter messageListAdapter = ((MainActivity) requireActivity()).getMessageListAdapter();
        recycler_gchat.setAdapter(messageListAdapter);

        sendBT.setOnClickListener(v -> {

            String msg = msgET.getText().toString();
            UserMessage userMessage = new UserMessage(msg, new Date(), MainActivity.android_id);

            if(TextUtils.isEmpty(msg)){
                msgET.setError("Enter a message!");
                return;
            }

            if (((MainActivity) requireActivity()).getSendReceive() != null) {
                ((MainActivity) requireActivity()).getSendReceive().write(SerializationUtils.serialize(userMessage));
                recycler_gchat.scrollToPosition(messageListAdapter.getItemCount() - 1);
                msgET.setText("");
            } else {
                Toast.makeText(requireContext(), "Sending failed! Refresh your connection!", Toast.LENGTH_SHORT).show();
                ((MainActivity)requireActivity()).closeConnection();
            }
        });

        return root;
    }

}