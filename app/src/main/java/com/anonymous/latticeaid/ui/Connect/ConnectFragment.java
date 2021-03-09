package com.anonymous.latticeaid.ui.Connect;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.MainActivity;
import com.anonymous.latticeaid.R;
import com.anonymous.latticeaid.WifiDirectBroadcastReceiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_P2P_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class ConnectFragment extends Fragment {

    public TextView search_status_tv;
    ImageButton connectRefreshBT;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    List<WifiP2pDevice> peers = new ArrayList<>();
    List<String> deviceNameArray = new ArrayList<>();
    List<WifiP2pDevice> deviceArray = new ArrayList<>();
    RecyclerView peerRecyclerView;
    static BroadcastReceiver mReceiver;
    WifiManager wifiManager;
    IntentFilter mIntentFilter;
    MyRecyclerViewAdapter myRecyclerViewAdapter;


    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ConnectViewModel connectViewModel = new ViewModelProvider(this).get(ConnectViewModel.class);
        View root = inflater.inflate(R.layout.fragment_connect, container, false);
        search_status_tv = root.findViewById(R.id.search_status_tv);

        peerRecyclerView = root.findViewById(R.id.peerRecyclerView);
        connectRefreshBT = root.findViewById(R.id.connectRefreshBT);
        peerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        initialWork();

        setDeviceName(Build.MANUFACTURER + " " + android.os.Build.MODEL + "6488253637");

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


        connectViewModel.getText().observe(getViewLifecycleOwner(), search_status_tv::setText);


        connectRefreshBT.setOnClickListener(v -> {
            peers.clear();
            deviceNameArray.clear();
            deviceArray.clear();
            myRecyclerViewAdapter.notifyDataSetChanged();
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
                    MainActivity.shouldShowDialog = false;
                    connectRefreshBT.setEnabled(false);
                    wifiManager.setWifiEnabled(false);
                    wifiManager.setWifiEnabled(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.shouldShowDialog = true;
                            connectRefreshBT.setEnabled(true);
                        }
                    }, 500);

                }
            });

        });


        return root;
    }

    private void initialWork() {
        mManager = (WifiP2pManager) requireContext().getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getContext(), requireActivity().getMainLooper(), null);
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(WIFI_SERVICE);

        myRecyclerViewAdapter = new MyRecyclerViewAdapter(deviceNameArray, mManager, mChannel, deviceArray, search_status_tv, peerListListener);
        peerRecyclerView.setAdapter(myRecyclerViewAdapter);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, myRecyclerViewAdapter, wifiManager, search_status_tv);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        requireActivity().registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(mReceiver);
        peers.clear();
        deviceNameArray.clear();
        deviceArray.clear();
    }


    //WifiP2p Peer listener
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceArray.clear();
                deviceNameArray.clear();

                //populating device info to array
                int index = 0;
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    if (device.deviceName.contains("6488253637")) {
                        deviceNameArray.add(device.deviceName);
                        deviceArray.add(device);
                        index++;
                        myRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }

                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, deviceNameArray);

                //peerRecyclerView.setAdapter(new MyRecyclerViewAdapter(deviceNameArray));
                myRecyclerViewAdapter.notifyDataSetChanged();

                if (peerList.getDeviceList().size() == 0) {
                    Toast.makeText(getContext(), "No Device Found!", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    public void setDeviceName(String devName) {
        try {
            @SuppressWarnings("rawtypes") Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            Method setDeviceName = mManager.getClass().getMethod(
                    "setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);

            Object[] arglist = new Object[3];
            arglist[0] = mChannel;
            arglist[1] = devName;
            arglist[2] = new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                }
            };

            setDeviceName.invoke(mManager, arglist);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    public WifiP2pDevice getDevice(int position) {
        return deviceArray.get(position);
    }


}