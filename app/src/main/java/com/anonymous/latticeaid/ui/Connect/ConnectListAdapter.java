package com.anonymous.latticeaid.ui.Connect;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.MainActivity;
import com.anonymous.latticeaid.R;

import java.util.List;


public class ConnectListAdapter extends RecyclerView.Adapter<ConnectListAdapter.ViewHolder> {

    private WifiP2pDevice device;
    private final List<String> deviceNameArray;
    List<WifiP2pDevice> deviceArray;
    WifiP2pConfig config;
    WifiP2pManager.PeerListListener peerListListener;
    MainActivity mActivity;


    // data is passed into the constructor
    public ConnectListAdapter(List<String> deviceNameArray, List<WifiP2pDevice> deviceArray,
                              WifiP2pManager.PeerListListener peerListListener, MainActivity mActivity) {
        this.deviceNameArray = deviceNameArray;
        this.deviceArray = deviceArray;
        this.peerListListener = peerListListener;
        this.mActivity = mActivity;
    }

    @NonNull
    @Override
    public ConnectListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectListAdapter.ViewHolder holder, int position) {
        String name = position + 1 + ". " + deviceNameArray.get(position).replace("6488253637", "");
        holder.deviceNamesTV.setText(name);
        holder.deviceNamesTV.setOnClickListener(v -> {
            device = deviceArray.get(position);
            config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;

            mActivity.connect(config);

        });

        holder.deviceNamesTV.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.deviceNamesTV.getContext());
            final CharSequence[] items = {"Connect"};
            builder.setItems(items, (dialog, item) -> {
                device = deviceArray.get(position);
                config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                mActivity.connect(config);
            });
            builder.show();
            return true;
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
