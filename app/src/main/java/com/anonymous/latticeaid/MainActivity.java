package com.anonymous.latticeaid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.Toast;

import com.anonymous.latticeaid.ui.Chat.ChatFragment;
import com.anonymous.latticeaid.ui.Connect.ConnectFragment;
import com.anonymous.latticeaid.ui.Connect.MyRecyclerViewAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    AlertDialog dialog;

    NavController navController;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiManager wifiManager;
    IntentFilter mIntentFilter;
    BroadcastReceiver mReceiver;
    MyRecyclerViewAdapter myRecyclerViewAdapter;
    ConnectFragment connectFragment;
    ChatFragment chatFragment;
    NavHostFragment nav_host_fragment;

    List<WifiP2pDevice> peers = new ArrayList<>();
    List<String> deviceNameArray = new ArrayList<>();
    List<WifiP2pDevice> deviceArray = new ArrayList<>();

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        checkPermission();
        initialWork();
        setDeviceName(Build.MANUFACTURER + " " + android.os.Build.MODEL + "6488253637");


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_connect, R.id.navigation_chat, R.id.navigation_notifications)
                .build();


        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        nav_host_fragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert nav_host_fragment != null;
        navController = nav_host_fragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.navigate(R.id.navigation_connect);

    }


    private void initialWork() {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        nav_host_fragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert nav_host_fragment != null;
        Fragment currentFragment = nav_host_fragment.getChildFragmentManager().getFragments().get(0);
        if (currentFragment instanceof ConnectFragment) {
            connectFragment = (ConnectFragment) currentFragment;
            myRecyclerViewAdapter = new MyRecyclerViewAdapter(deviceNameArray, deviceArray, getPeerListListener(), this);
        }
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, wifiManager, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(mReceiver, mIntentFilter);

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
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    if (device.deviceName.contains("6488253637")) {
                        deviceNameArray.add(device.deviceName);
                        deviceArray.add(device);
                        myRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }

                myRecyclerViewAdapter.notifyDataSetChanged();

                if (peerList.getDeviceList().size() == 0) {
                    Toast.makeText(getBaseContext(), "No Device Found!", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };


    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 400);
            Toast.makeText(MainActivity.this, "Access fine location failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        if (dialog != null) {
            if (!dialog.isShowing()) {
                checkPermission();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    //Handling access fine location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 400) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Please allow all permissions to continue!").setCancelable(false).setPositiveButton("OK", (dialog, which) -> startActivity(intent));
                dialog = builder.create();
                dialog.show();

            }
        }
    }

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

    //WifiP2p connectionInfoListener to handle connection between nodes
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;
            //search_status_tv = root.findViewById(R.id.search_status_tv);


            //String connectionInfo = "Connected to " + device.deviceName;
            if (info.groupFormed && info.isGroupOwner) {
                connectFragment.setSearchStatusText("Host");

                serverClass = new ServerClass();
                serverClass.start();
                navController.navigate(R.id.navigation_chat);

            } else if (info.groupFormed) {
                connectFragment.setSearchStatusText("Client");
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
                navController.navigate(R.id.navigation_chat);
            }

        }
    };

    @SuppressLint("MissingPermission")
    public void connect(WifiP2pConfig config) {
        try {
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    connectFragment.setSearchStatusText("Connecting...");
                    //Toast.makeText(requireActivity(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    connectFragment.setSearchStatusText("Connection Error!");
                    //Toast.makeText(, "Connection Error!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public WifiP2pManager.PeerListListener getPeerListListener() {
        return peerListListener;
    }

    public MyRecyclerViewAdapter getMyRecyclerViewAdapter() {
        return myRecyclerViewAdapter;
    }

    public NavController getNavController() {
        return navController;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            if (msg.what == MESSAGE_READ) {
                byte[] readBuff = (byte[]) msg.obj;
                String tempMsg = new String(readBuff, 0, msg.arg1);
                //read_msg_box.setText(tempMsg);
                nav_host_fragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                assert nav_host_fragment != null;
                Fragment currentFragment = nav_host_fragment.getChildFragmentManager().getFragments().get(0);
                if (currentFragment instanceof ChatFragment) {
                    chatFragment = (ChatFragment) currentFragment;
                    chatFragment.setMessageText(tempMsg);
                }
            }

            return true;
        }
    });


    //Server class
    public class ServerClass extends Thread {
        ServerSocket serverSocket;
        Socket socket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Client class
    public class ClientClass extends Thread {

        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //SendReceive class to handle data transfer
    public class SendReceive extends Thread {
        private final Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        //Constructor
        public SendReceive(Socket skt) {
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }// end of constructor

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }//end of run

        public void write(final byte[] bytes) {

            new Thread(() -> {
                try {
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

    public SendReceive getSendReceive() {
        return sendReceive;
    }
}