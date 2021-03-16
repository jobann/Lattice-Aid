package com.anonymous.latticeaid.ui.Connect;

import android.annotation.SuppressLint;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.MainActivity;
import com.anonymous.latticeaid.R;
import com.anonymous.latticeaid.ui.Chat.UserMessage;

import org.apache.commons.lang3.SerializationUtils;

import java.util.Date;

import static android.content.Context.WIFI_P2P_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class ConnectFragment extends Fragment {

    public TextView search_status_tv;
    ImageButton connectRefreshBT;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    RecyclerView peerRecyclerView;
    WifiManager wifiManager;


    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_connect, container, false);
        search_status_tv = root.findViewById(R.id.search_status_tv);

        peerRecyclerView = root.findViewById(R.id.peerRecyclerView);
        connectRefreshBT = root.findViewById(R.id.connectRefreshBT);
        peerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        ConnectListAdapter connectListAdapter = ((MainActivity) requireActivity()).getConnectListAdapter();
        //Setting Adapter
        peerRecyclerView.setAdapter(connectListAdapter);

        mManager = (WifiP2pManager) requireContext().getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(requireContext(), requireActivity().getMainLooper(), null);
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess() {
                search_status_tv.setText("Discovery Started...");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(int reason) {
                Log.d("JOBANN", String.valueOf(reason));
                search_status_tv.setText("Discovery Error...");

            }
        });


        connectRefreshBT.setOnClickListener(v -> {
            if (((MainActivity) requireActivity()).getSendReceive() != null) {
                UserMessage userMessage = new UserMessage(MainActivity.MESSAGE_CLOSE, new Date(), MainActivity.android_id);
                ((MainActivity) requireActivity()).getSendReceive().write(SerializationUtils.serialize(userMessage));
                Log.d("JOBANN", "IF");
            } else {
                Log.d("JOBANN", "ELSE");
                mManager.removeGroup(mChannel, null);
                mManager.cancelConnect(mChannel, null);


                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess() {
                        search_status_tv.setText("Discovery Started...");
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(int reason) {
                        search_status_tv.setText("Discovery Error...");
                    }
                });
            }
        });

        return root;
    }


    public void setSearchStatusText(String s) {
        search_status_tv.setText(s);
    }

}