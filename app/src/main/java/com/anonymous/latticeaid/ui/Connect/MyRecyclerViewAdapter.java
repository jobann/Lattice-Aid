package com.anonymous.latticeaid.ui.Connect;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.MainActivity;
import com.anonymous.latticeaid.R;

import java.net.InetAddress;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private WifiP2pDevice device;
    private final List<String> deviceNameArray;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    List<WifiP2pDevice> deviceArray;
    TextView search_status_tv;
    //WifiP2pDevice device;
    WifiP2pConfig config;
    public WifiP2pManager.PeerListListener peerListListener;

    // data is passed into the constructor


    public MyRecyclerViewAdapter(List<String> deviceNameArray, WifiP2pManager mManager, WifiP2pManager.Channel mChannel,
                                 List<WifiP2pDevice> deviceArray, TextView search_status_tv, WifiP2pManager.PeerListListener peerListListener) {
        this.deviceNameArray = deviceNameArray;
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.deviceArray = deviceArray;
        this.search_status_tv = search_status_tv;
        this.peerListListener = peerListListener;
    }

    @NonNull
    @Override
    public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewAdapter.ViewHolder holder, int position) {
        String name = position + 1 + ". " + deviceNameArray.get(position).replace("6488253637", "");
        holder.deviceNamesTV.setText(name);
        holder.deviceNamesTV.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                device = deviceArray.get(position);
                config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                connect();

            }
        });
        holder.deviceNamesTV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.deviceNamesTV.getContext());
                final CharSequence[] items = {"Reset Connection"};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        WifiManager wifiManager = (WifiManager) holder.deviceNamesTV.getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
                        MainActivity.shouldShowDialog = false;
                        wifiManager.setWifiEnabled(false);
                        wifiManager.setWifiEnabled(true);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.shouldShowDialog = true;
                            }
                        }, 500);

                    }
                });
                builder.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceNameArray.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView deviceNamesTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNamesTV = itemView.findViewById(R.id.deviceNamesTV);

        }
    }

    @SuppressLint("MissingPermission")
    public void connect() {
        try {
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    search_status_tv.setText("Connecting...");
                    //Toast.makeText(requireActivity(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    search_status_tv.setText("Connection Error!");
                    //Toast.makeText(, "Connection Error!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(search_status_tv.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //WifiP2p connectionInfoListener to handle connection between nodes
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;
            //search_status_tv = root.findViewById(R.id.search_status_tv);


            //String connectionInfo = "Connected to " + device.deviceName;
            if (info.groupFormed && info.isGroupOwner) {
                search_status_tv.setText("Host");

            } else if (info.groupFormed) {
                search_status_tv.setText("Client");

            }


        }
    };


}
