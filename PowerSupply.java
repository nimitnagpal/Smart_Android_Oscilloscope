import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "bluetooth2";

    Button start,on5,off5,on9,off9,on12,off12,stop;

    Handler h;
    int strt=0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();


    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "98:D3:31:20:7F:B8";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = (Button) findViewById(R.id.button7);                  // button LED ON
        stop = (Button) findViewById(R.id.button8);    // button LED OFF

        on5 = (Button)findViewById(R.id.button);
        off5 = (Button)findViewById(R.id.button2);
        on9 = (Button)findViewById(R.id.button3);
        off9 = (Button)findViewById(R.id.button4);
        on12 = (Button)findViewById(R.id.button5);
        off12 = (Button)findViewById(R.id.button6);


        start();
        stop();
        on5v();
        off5v();
        on9v();
        off9v();
        on12v();
        off12v();



        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

    }

    public void start(){
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strt=1;

            }
        });
    }

    public void stop(){
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strt=0;
            }
        });
    }
    public void on5v(){
        on5.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(strt==1){
                    mConnectedThread.write(3);
                }
            }
        });
    }
    public void off5v(){
        off5.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(strt==1){
                    mConnectedThread.write(4);
                }
            }
        });
    }

    public void on9v(){
        on9.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(strt==1){
                    mConnectedThread.write(5);
                }
            }
        });
    }
    public void off9v(){
        off9.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(strt==1){
                    mConnectedThread.write(6);
                }
            }
        });
    }
    public void on12v(){
        on12.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(strt==1){
                    mConnectedThread.write(7);
                }
            }
        });
    }
    public void off12v(){
        off12.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(strt==1){
                    mConnectedThread.write(8);
                }
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }



        /* Call this from the main activity to send data to the remote device */
        public void write(int message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            //byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(message);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }


}
