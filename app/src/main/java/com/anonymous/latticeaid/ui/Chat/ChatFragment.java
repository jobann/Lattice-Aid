package com.anonymous.latticeaid.ui.Chat;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.MainActivity;
import com.anonymous.latticeaid.R;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.nlopez.smartlocation.SmartLocation;

public class ChatFragment extends Fragment {


    Button sendBT;
    ImageButton chatRefreshBT;
    EditText msgET;
    RecyclerView recycler_gchat;
    Thread thread = null;
    Context context = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        ((MainActivity) requireActivity()).readFromJSON();

        msgET = root.findViewById(R.id.msgET);
        sendBT = root.findViewById(R.id.sendBT);
        recycler_gchat = root.findViewById(R.id.recycler_gchat);
        chatRefreshBT = root.findViewById(R.id.chatRefreshBT);
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
                        ((MainActivity) requireActivity()).saveToLocationFile(MainActivity.android_id, location);
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

                //writing to JSON file
                JSONObject chatJSONObject = new JSONObject();

                try {

                    chatJSONObject.put("message", userMessage.getMessage());
                    chatJSONObject.put("date", userMessage.getDate());
                    chatJSONObject.put("android_id", userMessage.getAndroid_id());


                    //((MainActivity) requireActivity()).writeToJSONFile(chatJSONObject);

                    //writeToJSONFile(chatJSONObject);
                    long tsLong = System.currentTimeMillis() / 1000;
                    String ts = Long.toString(tsLong);

                    String line = ts + "," + userMessage.getMessage() + "," + userMessage.getDate().toString() + "," + userMessage.getAndroid_id() + "\n";
                    ((MainActivity) requireActivity()).writeToFile(line);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                recycler_gchat.scrollToPosition(messageListAdapter.getItemCount() - 1);
                msgET.setText("");
            } else {
                Toast.makeText(requireContext(), "Sending failed! Refresh your connection!", Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).closeConnection();
            }

        });

        chatRefreshBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    UserMessage userMessage = new UserMessage(((MainActivity) requireActivity()).getFileObject(), MainActivity.TYPE_MERGE);
                    Log.d("KAHLONN", ((MainActivity) requireActivity()).getFileObject());
                    ((MainActivity) requireActivity()).getSendReceive().write(SerializationUtils.serialize(userMessage));
                } catch (Exception e) {
                    Log.d("JOBANN", e.getMessage());
                }
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