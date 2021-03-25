package com.anonymous.latticeaid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.anonymous.latticeaid.ui.Chat.ChatFragment;
import com.anonymous.latticeaid.ui.Chat.MessageListAdapter;
import com.anonymous.latticeaid.ui.Chat.UserMessage;
import com.anonymous.latticeaid.ui.Connect.ConnectFragment;
import com.anonymous.latticeaid.ui.Connect.ConnectListAdapter;
import com.anonymous.latticeaid.ui.Profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends AppCompatActivity {

    AlertDialog dialog;

    NavController navController;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiManager wifiManager;
    IntentFilter mIntentFilter;
    BroadcastReceiver mReceiver;
    ConnectListAdapter connectListAdapter;
    ConnectFragment connectFragment;
    ChatFragment chatFragment;
    NavHostFragment nav_host_fragment;
    RecyclerView recycler_gchat;

    List<WifiP2pDevice> peers = new ArrayList<>();
    List<String> deviceNameArray = new ArrayList<>();
    List<WifiP2pDevice> deviceArray = new ArrayList<>();

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;


    List<UserMessage> userMessagesList = new ArrayList<>();
    HashMap<String, Location> userLocations = new HashMap<>();
    MessageListAdapter messageListAdapter;
    public static String android_id;


    public static final int TYPE_MESSAGE = 100;
    public static final int TYPE_GPS = 101;
    public static final int TYPE_CLOSE = 102;
    public static final int TYPE_MERGE = 103;
    private static final String fileName = "messages.txt";
    public static final String deviceChatIDs = "MESSAGES_JSON_LIST";


    File file = null;
    FileReader fileReader = null;
    FileWriter fileWriter = null;
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;

    JSONObject deviceChatIDJSONObject = null;
    JSONObject timeStampJSONObject = null;

    JSONArray jsonArray = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        Log.d("KAHLON", "ONCREATE");


        checkPermission();
        initialWork();
        //createJSONFile();
        readFromJSON();
        setDeviceName(Build.MANUFACTURER + " " + android.os.Build.MODEL + "6488253637");


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_connect, R.id.navigation_chat, R.id.navigation_profile)
                .build();


        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        nav_host_fragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert nav_host_fragment != null;
        navController = nav_host_fragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.navigate(R.id.navigation_connect);

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                //it's possible to do more actions on several items, if there is a large amount of items I prefer switch(){case} instead of if()
                if (id == R.id.navigation_connect) {
                    navController.navigate(R.id.navigation_connect);
                }
                if (id == R.id.navigation_chat) {
                    navController.navigate(R.id.navigation_chat);
                }
                if (id == R.id.navigation_profile) {
                    navController.navigate(R.id.navigation_profile);
                }
                //This is for maintaining the behavior of the Navigation view
                NavigationUI.onNavDestinationSelected(item, navController);
                return true;
            }
        });

    }


    @SuppressLint("HardwareIds")
    private void initialWork() {

        //getting Unique Identifier
        android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);


        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        nav_host_fragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert nav_host_fragment != null;
        Fragment currentFragment = nav_host_fragment.getChildFragmentManager().getFragments().get(0);
        if (currentFragment instanceof ConnectFragment) {
            connectFragment = (ConnectFragment) currentFragment;
            connectListAdapter = new ConnectListAdapter(deviceNameArray, deviceArray, getPeerListListener(), this);
        } else if (currentFragment instanceof ChatFragment) {
            chatFragment = (ChatFragment) currentFragment;
        }
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, wifiManager, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        messageListAdapter = new MessageListAdapter(userMessagesList);

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
                        connectListAdapter.notifyDataSetChanged();
                    }
                }

                connectListAdapter.notifyDataSetChanged();

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
    public void onDestroy() {
        super.onDestroy();
        mManager.removeGroup(mChannel, null);
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = nav_host_fragment.getChildFragmentManager().getFragments().get(0);
        if (fragment instanceof ConnectFragment) {
            exitApp();
        } else if (fragment instanceof ChatFragment) {
            navController.navigate(R.id.navigation_connect);
        } else if (fragment instanceof ProfileFragment) {
            navController.navigate(R.id.navigation_connect);
        }
    }

    private void exitApp() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Exit Application?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            try {
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.exit(0);
        });
        builder.setNegativeButton("No", (dialog, which) ->
                dialog.cancel()
        );

        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //WifiP2p connectionInfoListener to handle connection between nodes
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;
            //search_status_tv = root.findViewById(R.id.search_status_tv);

            //Initialising Location Object
            SmartLocation.with(getApplicationContext()).location().oneFix()
                    .start(location -> {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        //Toast.makeText(requireContext(), "Latitude: " + lat + "\nLongitude:" + lng, Toast.LENGTH_SHORT).show();
                        UserMessage userMessage = new UserMessage(MainActivity.android_id, lat, lng, MainActivity.TYPE_GPS);
                        saveToLocationFile(android_id, location);
                        if (sendReceive != null) {
                            sendReceive.write(SerializationUtils.serialize(userMessage));
                        }
                    });
            //String connectionInfo = "Connected to " + device.deviceName;
            if (info.groupFormed && info.isGroupOwner) {
                connectFragment.setSearchStatusText("Host");

                if (serverClass != null) {
                    closeConnection();
                }
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

    public ConnectListAdapter getConnectListAdapter() {
        return connectListAdapter;
    }

    public MessageListAdapter getMessageListAdapter() {
        return messageListAdapter;
    }

    public NavController getNavController() {
        return navController;
    }

    public HashMap<String, Location> getUserLocations() {
        return userLocations;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            if (msg.what == MESSAGE_READ) {
                byte[] readBuff = (byte[]) msg.obj;
                UserMessage userMessage = null;
                try {
                    userMessage = SerializationUtils.deserialize(readBuff);


                    switch (userMessage.getMsgType()) {
                        case TYPE_CLOSE:
                            Toast.makeText(getApplicationContext(), "Connection Closed", Toast.LENGTH_SHORT).show();
                            closeConnection();
                            break;
                        case TYPE_MESSAGE:
                            userMessagesList.add(userMessage);

                            JSONObject chatJSONObject = new JSONObject();

                            try {

                                chatJSONObject.put("message", userMessage.getMessage());
                                chatJSONObject.put("date", userMessage.getDate());
                                chatJSONObject.put("android_id", userMessage.getAndroid_id());

                                //writeToJSONFile(chatJSONObject);
                                long tsLong = System.currentTimeMillis() / 1000;
                                String ts = Long.toString(tsLong);

                                String line = ts + "," + userMessage.getMessage() + "," + userMessage.getDate().toString() + "," + userMessage.getAndroid_id() + "\n";
                                writeToFile(line);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            messageListAdapter.notifyDataSetChanged();
                            runOnUiThread(() -> {
                                if (recycler_gchat == null)
                                    recycler_gchat = findViewById(R.id.recycler_gchat);
                                if (recycler_gchat != null)
                                    recycler_gchat.scrollToPosition(messageListAdapter.getItemCount() - 1);
                            });
                            break;
                        case TYPE_GPS:
                            double lat = userMessage.getLatitude();
                            double lng = userMessage.getLongitude();
                            userLocations.put(userMessage.getAndroid_id(), createLocation(lat, lng));
                            //String locationLine = userMessage.getAndroid_id() + "," + lat + "," + lng + "\n";
                            saveToLocationFile(userMessage.getAndroid_id(), createLocation(lat, lng));
                            Log.d("JOBANN", userLocations.toString());
                            break;
                        case TYPE_MERGE:
                            //Toast.makeText(getApplicationContext(), userMessage.getAndroid_id(), Toast.LENGTH_SHORT).show();
                            //Log.d("JOBANN", userMessage.getAndroid_id());
                            //deviceChatIDs = getId(MainActivity.android_id, userMessage.getAndroid_id());
                            merge(userMessage.getjSONFileObject());
                            try {
                                Log.d("KAHLONN", userMessage.getjSONFileObject());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                } catch (Exception e) {
                    Log.d("KAHLONHANDLE", e.getMessage());
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
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(8888));
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
                // String jSONFileObject = getJSONFileObject();
                readFromJSON();
                try {
                    UserMessage userMessage = new UserMessage(getFileObject(), MainActivity.TYPE_MERGE);
                    sendReceive.write(SerializationUtils.serialize(userMessage));
                    Log.d("KAHLONN", getFileObject());
                } catch (Exception e) {
                    Log.d("JOBANN", e.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("JOBANN", Objects.requireNonNull(e.getMessage()));
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
                socket.connect(new InetSocketAddress(hostAdd, 8888), 7000);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
                // String jSONFileObject = getJSONFileObject();
                readFromJSON();
                try {
                    UserMessage userMessage = new UserMessage(getFileObject(), MainActivity.TYPE_MERGE);
                    sendReceive.write(SerializationUtils.serialize(userMessage));
                    Log.d("KAHLONN", getFileObject());
                } catch (Exception e) {
                    Log.d("JOBANN", e.getMessage());
                }
            } catch (Exception e) {
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
            } catch (Exception e) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }//end of run

        public void write(final byte[] bytes) {

            new Thread(() -> {
                try {
                    outputStream.write(bytes);
                    UserMessage userMessage = SerializationUtils.deserialize(bytes);
                    if (userMessage.getMsgType() == TYPE_MESSAGE) {
                        userMessagesList.add(userMessage);
                        runOnUiThread(() -> messageListAdapter.notifyDataSetChanged());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    closeConnection();
                }
            }).start();

        }

    }

    @SuppressLint("MissingPermission")
    public void closeConnection() {
        mManager.removeGroup(mChannel, null);
        runOnUiThread(() -> navController.navigate(R.id.navigation_connect));
    }

    public SendReceive getSendReceive() {
        return sendReceive;
    }

    public Location createLocation(double lat, double lng) {
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(lat);
        loc.setLongitude(lng);

        return loc;
    }


    void createJSONFile() {

        deviceChatIDJSONObject = new JSONObject();
        timeStampJSONObject = new JSONObject();


    }//end of createJSONFile


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void writeToJSONFile(JSONObject chatJSONObject) {
        file = new File(this.getFilesDir(), fileName);

        List<String> messageList = new ArrayList<>();


        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            messageList.clear();
            while (line != null) {
                messageList.add(line);
                //stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //reading from the file
        JSONObject resultJSONObject = null;
        Set<String> set = new LinkedHashSet<>(messageList);
        messageList.clear();
        messageList.addAll(set);
        try {
            resultJSONObject = new JSONObject(messageList.toString());
        } catch (Exception e) {
            try {
                //if file exists but is empty
                resultJSONObject = new JSONObject("{\"MESSAGES_JSON_LIST\":[{}]}");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }//end of reading file

        //getting Json array from the file
        try {
            assert resultJSONObject != null;
            jsonArray = resultJSONObject.getJSONArray(deviceChatIDs);
        } catch (Exception e) {
            jsonArray = new JSONArray();
        }

        try {
            fileWriter = new FileWriter(file.getAbsolutePath(), false);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long tsLong = System.currentTimeMillis() / 1000;
        String ts = Long.toString(tsLong);

        try {

            timeStampJSONObject.put(ts, chatJSONObject);
            jsonArray.put(timeStampJSONObject);
            Log.d("JOBANN", jsonArray.toString());

            deviceChatIDJSONObject.put(deviceChatIDs, jsonArray);


        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            bufferedWriter.write(deviceChatIDJSONObject.toString());
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Read from JSON file
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void readFromJSON() {
        Log.d("SINGH", "readFromJSON");
        file = new File(this.getFilesDir(), fileName);
        userMessagesList.clear();
        try {

            if (!file.exists()) {
                file.createNewFile();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(0);


            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                String[] userMessageString = line.split(",");
                Log.d("SINGH", line);
                String timestamp = userMessageString[0];
                String message = userMessageString[1];
                String dateString = userMessageString[2];
                String android_id = userMessageString[3];

                Date date;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                            Locale.ENGLISH);
                    date = sdf.parse(dateString);
                } catch (Exception e) {
                    date = new Date();
                    e.printStackTrace();
                }

                UserMessage userMessage = new UserMessage(message, date, android_id, TYPE_MESSAGE);

                userMessagesList.add(userMessage);
            }
            messageListAdapter.notifyDataSetChanged();

            randomAccessFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public String getFileObject() {

        file = new File(this.getFilesDir(), fileName);
        try {

            if (!file.exists()) {
                file.createNewFile();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(0);


            String line;
            StringBuilder resultFileObject = new StringBuilder();
            while ((line = randomAccessFile.readLine()) != null) {
                if (line.equals(""))
                    continue;
                String[] userMessageString = line.split(",");
                String timestamp = userMessageString[0];
                String message = userMessageString[1];
                String dateString = userMessageString[2];
                String android_id = userMessageString[3];

                resultFileObject.append(timestamp).append(",").append(message).append(",").append(dateString).append(",").append(android_id).append("\n");
            }

            randomAccessFile.close();
            Log.d("KAHLON", resultFileObject.toString());
            return resultFileObject.toString();

        } catch (Exception e) {
            Log.d("KAHLONN", e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    //merge file data between two devices
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void merge(String otherFile) {
        Log.d("KAHLON", "Merge");
        Set<String> set = new HashSet<>();

        if (otherFile != null) {
            String[] fileArray = otherFile.split("\n");
            set.addAll(Arrays.asList(fileArray));
        }
        String localFile = getFileObject();
        if (localFile != null) {
            String[] localFileArray = localFile.split("\n");
            set.addAll(Arrays.asList(localFileArray));
        }

        file = new File(this.getFilesDir(), fileName);
        try {

            if (!file.exists()) {
                file.createNewFile();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(0);

            String newLine = "\n";
            for (String str : set) {
                randomAccessFile.write(str.getBytes());
                randomAccessFile.write(newLine.getBytes());
            }
            randomAccessFile.close();
            readFromJSON();
            Log.d("KAHLONSET", set.toString());


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("KAHLONEXCEPTION", e.getMessage());
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void writeToFile(String line) {
        Log.d("KAHLON", line);
        //Reading file last characters
        try {
            file = new File(this.getFilesDir(), fileName);

            if (!file.exists()) {
                file.createNewFile();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(randomAccessFile.length());

            randomAccessFile.write(line.getBytes());
            randomAccessFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveToLocationFile(String android_id, Location location) {

        userLocations.put(android_id, location);

        try {
            File locationFile = new File(this.getFilesDir(), "locations.txt");

            if (!locationFile.exists()) {
                locationFile.createNewFile();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(locationFile, "rw");
            randomAccessFile.seek(0);

            for (String android_id_key : userLocations.keySet()) {
                Location userLocation = userLocations.get(android_id_key);
                String locationLine = android_id_key + "," + userLocation.getLongitude() + "," + userLocation.getLatitude() + "\n";
                randomAccessFile.write(locationLine.getBytes());
            }
            randomAccessFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void getLocationFileObject() {
        File locationFile = new File(this.getFilesDir(), "locations.txt");
        userLocations.clear();
        try {

            if (!locationFile.exists()) {
                locationFile.createNewFile();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(locationFile, "r");
            randomAccessFile.seek(0);


            String line;
            StringBuilder resultFileObject = new StringBuilder();
            while ((line = randomAccessFile.readLine()) != null) {
                if (line.equals(""))
                    continue;
                String[] userMessageString = line.split(",");
                String android_id = userMessageString[0];
                double lat = Double.parseDouble(userMessageString[1]);
                double lng = Double.parseDouble(userMessageString[2]);

                //resultFileObject.append(android_id).append(",").append(lat).append(",").append(lng).append("\n");

                userLocations.put(android_id, createLocation(lng, lat));


            }

            randomAccessFile.close();

        } catch (Exception e) {
            Log.d("KAHLONN", e.getMessage());
            e.printStackTrace();
        }

    }

}