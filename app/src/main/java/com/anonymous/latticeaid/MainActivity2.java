 package com.anonymous.latticeaid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MainActivity2 extends AppCompatActivity {

    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView read_msg_box, connectionStatus;
    EditText writeMsg;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    static Boolean isWriting = false;

    Location location = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initialWork();

        //discovering peers
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                    Toast.makeText(MainActivity2.this, "Access fine location failed [Main Activity]", Toast.LENGTH_SHORT).show();
                }
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Discovery Started...");
                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionStatus.setText("Discovery Error...");

                    }
                });
            }
        });

        //onClicking List item (Peer list)
        listView.setOnItemClickListener((parent, view, position, id) -> {
            final WifiP2pDevice device = deviceArray[position];
           /* WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            mManager.createGroup(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(MainActivity.this, "Connection Error!", Toast.LENGTH_SHORT).show();
                }
            });*/


            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity2.this, "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(MainActivity2.this, "Connection Error!", Toast.LENGTH_SHORT).show();
                }
            });

        });

        // onClicking send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = writeMsg.getText().toString();
                if (isWriting) {
                    Toast.makeText(MainActivity2.this, "Please Wait...", Toast.LENGTH_SHORT).show();
                    return;
                }
                isWriting = true;
                //connectionStatus.setText(msg);
                if (sendReceive != null)
                    sendReceive.write(msg.getBytes());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isWriting = false;
                    }
                }, 1000);
            }
        });//end of Send button

        //OnOff button
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SmartLocation.with(getApplicationContext()).location().start(locationListener);

                //new GeoLocation(getApplicationContext()).getAddress();
                //mManager.removeGroup(mChannel, null);
                //btnOnOff.setEnabled(false);
            }
        });

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            if (msg.what == MESSAGE_READ) {
                byte[] readBuff = (byte[]) msg.obj;
                String tempMsg = new String(readBuff, 0, msg.arg1);
                read_msg_box.setText(tempMsg);
            }

            return true;
        }
    });

    @Override
    protected void onStart() {
        registerReceiver(mReceiver, mIntentFilter);
        super.onStart();
    }

    @Override
    protected void onResume() {
        registerReceiver(mReceiver, mIntentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SmartLocation.with(getApplicationContext()).location().stop();
        super.onStop();
    }

    /*
     *Initialising objects
     * checking for wifi state
     */
    private void initialWork() {


        btnOnOff = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);
        btnSend = findViewById(R.id.sendButton);
        listView = findViewById(R.id.peerRecyclerView);
        read_msg_box = findViewById(R.id.readMsg);
        connectionStatus = findViewById(R.id.connectionStatus);
        writeMsg = findViewById(R.id.writeMsg);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mManager = (WifiP2pManager) getApplicationContext().getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        //mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, wifiManager);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        registerReceiver(mReceiver, mIntentFilter);
        btnSend.setEnabled(false);
        // btnOnOff.setEnabled(false);


    }

    //WifiP2p Peer listener
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                //populating device info to array
                int index = 0;
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);

                if (peerList.getDeviceList().size() == 0) {
                    Toast.makeText(getApplicationContext(), "No Device Found!", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    //WifiP2p connectionInfoListener to handle connection between nodes
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed && info.isGroupOwner) {
                connectionStatus.setText("Host");
                serverClass = new ServerClass();
                serverClass.start();
            } else if (info.groupFormed) {
                connectionStatus.setText("Client");
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }

            btnSend.setEnabled(true);
            btnOnOff.setEnabled(true);

        }
    };


    //Handling access fine location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }
        }
    }


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

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    OnLocationUpdatedListener locationListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            Toast.makeText(getApplicationContext(), "Latitude: " + lat + "\nLongitude:" + lng, Toast.LENGTH_SHORT).show();
            handler.postDelayed(locationRunnable, 8000);
        }
    };

    Runnable locationRunnable = () -> SmartLocation.with(getApplicationContext()).location().start(locationListener);



}