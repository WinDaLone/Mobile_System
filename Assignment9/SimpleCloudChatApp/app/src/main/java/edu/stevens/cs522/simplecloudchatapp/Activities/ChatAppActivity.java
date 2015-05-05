package edu.stevens.cs522.simplecloudchatapp.Activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import edu.stevens.cs522.simplecloudchatapp.AckReceiverWrapper;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IHandleResult;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IRegisterListener;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Entities.Client;
import edu.stevens.cs522.simplecloudchatapp.Entities.Message;
import edu.stevens.cs522.simplecloudchatapp.Entities.Register;
import edu.stevens.cs522.simplecloudchatapp.Entities.Unregister;
import edu.stevens.cs522.simplecloudchatapp.Fragments.ChatroomCreateFragment;
import edu.stevens.cs522.simplecloudchatapp.Fragments.MainChatFragment;
import edu.stevens.cs522.simplecloudchatapp.Fragments.SettingDialogFragment;
import edu.stevens.cs522.simplecloudchatapp.Helpers.ServiceHelper;
import edu.stevens.cs522.simplecloudchatapp.Managers.ChatroomManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.ClientManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.MessageManager;
import edu.stevens.cs522.simplecloudchatapp.R;
import edu.stevens.cs522.simplecloudchatapp.Services.RequestService;


public class ChatAppActivity extends ActionBarActivity implements
        IRegisterListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = ChatAppActivity.class.getCanonicalName();
    public static final String DEFAULT_HOST = SettingDialogFragment.DESTINATION_HOST_DEFAULT;
    public static final int DEFAULT_PORT = SettingDialogFragment.DESTINATION_PORT_DEFAULT;
    public static Client client = null;

    public static final int CHAT_APP_LOADER_ID = 1;

    public static String clientName;
    public static long clientID;
    public static String host;
    public static int port;
    public static UUID registrationID;
    public static double latitude = 0;
    public static double longitude = 0;
    private GoogleApiClient googleApiClient;
    private LocationManager locationManager = null;

    private ServiceHelper serviceHelper = null;
    private AckReceiverWrapper.IReceiver receiver = null;
    private AckReceiverWrapper wrapper = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        setContentView(R.layout.activity_chat_app);
        serviceHelper = new ServiceHelper(this);
        reSetInfo();
        final boolean MultiPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        Chatroom chatroom = null;
        if (getIntent().hasExtra(MainChatActivity.TAG)) {
            chatroom = getIntent().getExtras().getParcelable(MainChatActivity.TAG);
        }
        if (chatroom != null && MultiPane) {
            Fragment fragment = new MainChatFragment();
            Bundle args = new Bundle();
            args.putParcelable(MainChatFragment.TAG, chatroom);
            fragment.setArguments(args);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_chatroom_container, fragment);
            ft.commit();
            getFragmentManager().executePendingTransactions();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            googleApiClient.connect();
        } else {
            //Toast.makeText(this, "Please enable GPS settings", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please enable GPS settings first!").setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialog.cancel();
                }
            });
            builder.create().show();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_app, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (client == null) {
            menu.findItem(R.id.unregister).setVisible(false);
            menu.findItem(R.id.action_settings).setVisible(true);
        } else {
            menu.findItem(R.id.unregister).setVisible(true);
            menu.findItem(R.id.action_settings).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SettingDialogFragment.launch(this, TAG);
        }

        if (id == R.id.clients) {
            Intent intent = new Intent(this, ClientsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.create_chatroom) {
            if (clientID < 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Please register first!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            } else {
                ChatroomCreateFragment.launch(this, TAG);
            }
        }

        if (id == R.id.unregister) {
            UnregisterClient();
        }

        return super.onOptionsItemSelected(item);
    }

    private void UnregisterClient() {
        if (client != null) {
            Unregister unregister = new Unregister(host, port, client);
            receiver = new AckReceiverWrapper.IReceiver() {
                @Override
                public void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == RequestService.RESULT_UNREGISTER_OK){
                        SharedPreferences.Editor editor = getSharedPreferences(SettingDialogFragment.MY_SHARED_PREF, MODE_PRIVATE).edit();
                        editor.putString(SettingDialogFragment.PREF_USERNAME, "");
                        editor.putLong(SettingDialogFragment.PREF_IDENTIFIER, -1);
                        editor.apply();
                        reSetInfo();
                        MessageManager manager = new MessageManager(ChatAppActivity.this, new IEntityCreator<Message>() {
                            @Override
                            public Message create(Cursor cursor) {
                                return new Message(cursor);
                            }
                        }, CHAT_APP_LOADER_ID);
                        manager.DeleteAllAsync();
                        Toast.makeText(ChatAppActivity.this, "Unregister successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatAppActivity.this, "Unregister failed", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            wrapper = new AckReceiverWrapper(new Handler());
            wrapper.setReceiver(receiver);
            serviceHelper.UnregisterUser(unregister, wrapper);
        }
    }

    private void reSetInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingDialogFragment.MY_SHARED_PREF, MODE_PRIVATE);
        long mst = sharedPreferences.getLong(SettingDialogFragment.PREF_REGID_MOST, 0);
        if (mst == 0) {
            registrationID = UUID.randomUUID();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(SettingDialogFragment.PREF_REGID_MOST, registrationID.getMostSignificantBits());
            editor.putLong(SettingDialogFragment.PREF_REGID_LEAST, registrationID.getLeastSignificantBits());
            editor.apply();
        } else {
            registrationID = new UUID(sharedPreferences.getLong(SettingDialogFragment.PREF_REGID_MOST, 0), sharedPreferences.getLong(SettingDialogFragment.PREF_REGID_LEAST, 0));
        }
        Log.i(TAG, "UUID: " + registrationID.toString());
        clientName = sharedPreferences.getString(SettingDialogFragment.PREF_USERNAME, "");
        clientID = sharedPreferences.getLong(SettingDialogFragment.PREF_IDENTIFIER, -1);
        host = sharedPreferences.getString(SettingDialogFragment.PREF_HOST, ChatAppActivity.DEFAULT_HOST);
        port = sharedPreferences.getInt(SettingDialogFragment.PREF_PORT, ChatAppActivity.DEFAULT_PORT);
        if (clientID < 0) {
            client = null;
            Log.i(TAG, "NEED TO REGISTER APP");
            invalidateOptionsMenu();
        } else {
            Log.i(TAG, "APP INSTALLATION VALID");
            client = new Client(clientID, clientName, registrationID, longitude, latitude);
            invalidateOptionsMenu();
        }
    }

    @Override
    public void register(String host, int port, String name) {
        if (name.equals("")) {
            Toast.makeText(ChatAppActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
            return;
        }
        Register register = new Register(host, port, name, registrationID, latitude, longitude);
        receiver = new AckReceiverWrapper.IReceiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RequestService.RESULT_REGISTER_OK) {
                    reSetInfo();
                    Toast.makeText(ChatAppActivity.this, "Register success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatAppActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
        wrapper = new AckReceiverWrapper(new Handler());
        wrapper.setReceiver(receiver);
        serviceHelper.RegisterUser(register, wrapper);
    }

    @Override
    public void createChatroom(String name) {
        Chatroom chatroom = new Chatroom(name);
        ChatroomManager manager = new ChatroomManager(this, new IEntityCreator<Chatroom>() {
            @Override
            public Chatroom create(Cursor cursor) {
                return new Chatroom(cursor);
            }
        }, CHAT_APP_LOADER_ID);
        manager.InsertAsync(chatroom, new IHandleResult() {
            @Override
            public void onHandleResult(Chatroom chatroom) {
                if (chatroom == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatAppActivity.this);
                    builder.setTitle("Chatroom already exists!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                } else {
                    final boolean MultiPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
                    if (!MultiPane) { // Portrait mode
                        Intent intent = new Intent(ChatAppActivity.this, MainChatActivity.class);
                        intent.putExtra(MainChatActivity.TAG, chatroom);
                        startActivity(intent);
                    } else {
                        FragmentTransaction ft;
                        Fragment fragment = new MainChatFragment();
                        Bundle args = new Bundle();
                        args.putParcelable(MainChatFragment.TAG, chatroom);
                        fragment.setArguments(args);
                        ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.main_chatroom_container, fragment);
                        ft.commit();
                        getFragmentManager().executePendingTransactions();
                    }
                }
            }
        });
    }

    @Override
    public void send(Message message, Chatroom chatroom) {
        MessageManager manager = new MessageManager(this, new IEntityCreator<Message>() {
            @Override
            public Message create(Cursor cursor) {
                return new Message(cursor);
            }
        }, CHAT_APP_LOADER_ID);
        manager.persistAsync(message, client, chatroom);
    }

    /* Google API Clients */
    @Override
    public void onConnected(Bundle bundle) {
        /* Location-based Service */
        Log.i(TAG, "Google Api Connected, try to update location");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, MIN_DISTANCE_IN_METERS, this);
        /* End */
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, ConnectionResult.CANCELED);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    /* LocationListener */
    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    private static final long UPDATE_INTERVAL =
            UPDATE_INTERVAL_IN_SECONDS * 1000;
    private static final int MIN_DISTANCE_IN_METERS = 3;
    @Override
    public void onLocationChanged(Location location) {
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(getApplicationContext(), "Initialize location", Toast.LENGTH_SHORT).show();
        }
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.i(TAG, "Location changed, latitude:" + String.valueOf(latitude) +
                " longitude:" + String.valueOf(longitude));
        //Toast.makeText(getApplicationContext(), "Location Updated", Toast.LENGTH_SHORT).show();
        ClientManager manager = new ClientManager(this, new IEntityCreator<Client>() {
            @Override
            public Client create(Cursor cursor) {
                return new Client(cursor);
            }
        }, CHAT_APP_LOADER_ID);
        if (client != null) {
            //Log.i(TAG, "Client registered, update location");
            client.latitude = latitude;
            client.longitude = longitude;
            AddressTask task = new AddressTask();
            try {
                client.address = task.execute(location).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.i(TAG, "Get client address:" + client.address);
            manager.UpdateGPSAsync(client);
        } else {
            Log.i(TAG, "Client has not registered, do nothing");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String str = "";
        switch (status) {
            case 0:
                str = "OUT_OF_SERVICE";
                break;
            case 1:
                str = "TEMPORARILY UNAVAILABLE";
                break;
            case 2:
                str = "AVAILABLE";
                break;
            default:
                str = "";
        }
        Log.i(TAG, "Provider:" + provider + " status changed to: " + str);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "Provider: " + provider + " is enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "Provider: " + provider + " is disabled");
    }

    public class AddressTask extends AsyncTask<Location, Void, String> {
        @Override
        protected String doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(ChatAppActivity.this, Locale.getDefault());
            Location location = params[0];
            if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                try {
                    List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    Address address = addressList.get(0);
                    String addr = "";
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addr += address.getAddressLine(i);
                        addr += "\n";
                    }
                    return addr;
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Can't find address";
                }
            } else {
                return "Can't find address";
            }
        }
    }

}