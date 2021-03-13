package com.anonymous.latticeaid.ui.Chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.anonymous.latticeaid.MainActivity;
import com.anonymous.latticeaid.R;

import java.util.Objects;


public class ChatFragment extends Fragment {


    public TextView msgTV;
    Button sendBT;
    EditText msgET;



    static Boolean isWriting = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        msgTV = root.findViewById(R.id.msgTV);
        msgET = root.findViewById(R.id.msgET);
        sendBT = root.findViewById(R.id.sendBT);

        sendBT.setOnClickListener(v -> {
            String msg = msgET.getText().toString();
            if (((MainActivity) requireActivity()).getSendReceive() != null)
                ((MainActivity) requireActivity()).getSendReceive().write(msg.getBytes());
        });



        return root;
    }

    public void setMessageText(String msg){
        msgTV.setText(msg);
    }

}