package com.anonymous.latticeaid.ui.Connect;

import android.annotation.SuppressLint;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.R;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private final List<String> deviceNameArray;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    List<WifiP2pDevice> deviceArray;
    TextView search_status_tv;

    // data is passed into the constructor


    public MyRecyclerViewAdapter(List<String> deviceNameArray, WifiP2pManager mManager, WifiP2pManager.Channel mChannel, List<WifiP2pDevice> deviceArray, TextView search_status_tv) {
        this.deviceNameArray = deviceNameArray;
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.deviceArray = deviceArray;
        this.search_status_tv = search_status_tv;
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
                WifiP2pDevice device = deviceArray.get(position);
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        search_status_tv.setText("Connected to " + device.deviceName);
                        //Toast.makeText(requireActivity(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        search_status_tv.setText("Connection Error!");
                        //Toast.makeText(, "Connection Error!", Toast.LENGTH_SHORT).show();
                    }
                });
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

}
