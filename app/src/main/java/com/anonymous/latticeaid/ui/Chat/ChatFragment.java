package com.anonymous.latticeaid.ui.Chat;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
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
import java.util.concurrent.atomic.AtomicReference;

import io.nlopez.smartlocation.SmartLocation;

public class ChatFragment extends Fragment {


    Button sendBT;
    EditText msgET;
    RecyclerView recycler_gchat;
    Thread thread = null;
    Context context = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        msgET = root.findViewById(R.id.msgET);
        sendBT = root.findViewById(R.id.sendBT);
        recycler_gchat = root.findViewById(R.id.recycler_gchat);
        context = requireContext();

        recycler_gchat.setLayoutManager(new LinearLayoutManager(getContext()));

        MessageListAdapter messageListAdapter = ((MainActivity) requireActivity()).getMessageListAdapter();
        recycler_gchat.setAdapter(messageListAdapter);

        sendBT.setOnClickListener(v -> {

            //Initialising Location Object
            SmartLocation.with(context).location().oneFix()
                    .start(location -> {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        //Toast.makeText(requireContext(), "Latitude: " + lat + "\nLongitude:" + lng, Toast.LENGTH_SHORT).show();
                        UserMessage userMessage = new UserMessage(MainActivity.android_id, lat, lng, MainActivity.TYPE_GPS);
                        ((MainActivity) requireActivity()).getUserLocations().put(MainActivity.android_id, createLocation(lat, lng));
                        if (((MainActivity) requireActivity()).getSendReceive() != null) {
                            ((MainActivity) requireActivity()).getSendReceive().write(SerializationUtils.serialize(userMessage));
                        }
                    });


            String msg = msgET.getText().toString();

            if (TextUtils.isEmpty(msg)) {
                msgET.setError("Enter a message!");
                return;
            }


            UserMessage userMessage = new UserMessage(msg, new Date(), MainActivity.android_id, MainActivity.TYPE_MESSAGE);
            if (((MainActivity) requireActivity()).getSendReceive() != null) {
                ((MainActivity) requireActivity()).getSendReceive().write(SerializationUtils.serialize(userMessage));
                recycler_gchat.scrollToPosition(messageListAdapter.getItemCount() - 1);
                msgET.setText("");
            } else {
                Toast.makeText(requireContext(), "Sending failed! Refresh your connection!", Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).closeConnection();
            }

        });

        return root;
    }

    public Location createLocation(double lat, double lng) {
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(lat);
        loc.setLongitude(lng);

        return loc;
    }
}