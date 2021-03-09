package com.anonymous.latticeaid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.anonymous.latticeaid.ui.Connect.ConnectFragment;
import com.anonymous.latticeaid.ui.Connect.MyRecyclerViewAdapter;

import static android.content.Context.WIFI_SERVICE;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    //private MainActivity mActivity;
    private WifiManager wifiManager;
    private AlertDialog dialog;
    private TextView search_status_tv;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;


    public WifiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MyRecyclerViewAdapter myRecyclerViewAdapter, WifiManager wifiManager, TextView search_status_tv) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.myRecyclerViewAdapter = myRecyclerViewAdapter;
        this.wifiManager = wifiManager;
        this.search_status_tv = search_status_tv;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        //Handling wifi state
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            //if wifi is on
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "Wifi is ON", Toast.LENGTH_SHORT).show();
                if (dialog != null)
                    dialog.dismiss();
            } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {//if wifi is off
                Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();

                showDialog(context);
            }

        }//Handling connection peers list
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //  if (mActivity.peerListListener != null)
                mManager.requestPeers(mChannel, myRecyclerViewAdapter.peerListListener);
            }

        }// Handling connections
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            assert networkInfo != null;
            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, myRecyclerViewAdapter.connectionInfoListener);
            } else {
                mManager.removeGroup(mChannel, null);

                search_status_tv.setText("Disconnected!");
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }

    }

    public void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Turn On Wi-Fi!").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                wifiManager.setWifiEnabled(true);

            }
        }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity) context).finish();
                System.exit(0);
            }
        });
        dialog = builder.create();
        if (MainActivity.shouldShowDialog)
            dialog.show();
    }


}
