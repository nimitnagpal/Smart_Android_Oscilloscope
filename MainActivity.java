import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "bluetooth";
    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
    private LineGraphSeries<DataPoint> series1;    //ch2
    private int lastX = 0;
    private int lastX1 = 0;                         //ch2
    Button start,stop;
    TextView f1,vtg1,f2,vtg2;
    Handler h;
    int strt=0,i=0,i1=0,a=0,b=0,c=0,d=0,e=0,r=1,count=0,countON = 0,countOFF=0,point=0;
    int count1=0,count1ON=0,count1OFF=0,point1=0;
    DecimalFormat df = new DecimalFormat("#0.00");

    double vtg =0,vtg3=0;
    byte[] read = new byte[2];
    byte[] readBuf = new byte[1];

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
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
        f1= (TextView)findViewById(R.id.textView);
        vtg1= (TextView)findViewById(R.id.textView4);
        f2= (TextView)findViewById(R.id.textView6);
        vtg2= (TextView)findViewById(R.id.textView8);
        start = (Button)findViewById(R.id.button3);
        stop = (Button)findViewById(R.id.button5);
        // we get graph view instance
        GraphView graph = (GraphView) findViewById(R.id.graph);
        GraphView graph1 = (GraphView) findViewById(R.id.graph);
        // data
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);

        series1 = new LineGraphSeries<DataPoint>();     // ch2
        graph1.addSeries(series1);                        //ch2
        start();
        stop();
        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(5);
        viewport.setScrollable(true);

        Viewport viewport1 = graph1.getViewport();
        viewport1.setYAxisBoundsManual(true);
        viewport1.setMinY(0);
        viewport1.setMaxY(5);
        viewport1.setScrollable(true);


        series.setColor(Color.RED);

        series1.setColor(Color.BLUE);    //ch2

       // viewport.setBackgroundColor(20);


        h = new Handler() {
            public void handleMessage(android.os.Message msg) {

                switch (msg.what) {
                    case RECIEVE_MESSAGE:   // if receive massage
                        readBuf = (byte[]) msg.obj;

                        // if receive massage
                        read[i] = readBuf[0];///read[i]
                        if(i==0) {
                            a = read[0] & 0xff;
                            vtg = a * (float) (0.019);
                            //vtg=2;
                            series.appendData(new DataPoint(lastX++, vtg), true, 30);
                            if(a >= 64)
                            {
                                if(countON==0) {

                                    point++;
                                    countON = 1;
                                    countOFF = 0;
                                }
                            }
                            if( a <= 50)
                            {
                                if(countOFF==0) {

                                    countOFF = 1;
                                    countON = 0;
                                }
                            }
                        }

                        if(i==1)
                        {
                        b = read[1] & 0xff;
                        vtg3 = b * (float) (0.019);
                            //vtg3=1;

                            series1.appendData(new DataPoint(lastX, vtg3), true, 30);
                            if(b >= 64)
                            {
                                if(count1ON==0) {

                                    point1++;
                                    count1ON = 1;
                                    count1OFF = 0;
                                }
                            }
                            if( b <= 50)
                            {
                                if(count1OFF==0) {

                                    count1OFF = 1;
                                    count1ON = 0;
                                }
                            }
                         }

                         i++;
                         if(i==2) {
                             i = 0;
                         }

                        break;
                }
            }
        };
/*
        final Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        series.appendData(new DataPoint(lastX++, vtg), true, 1);
                        series1.appendData(new DataPoint(lastX1++, vtg3), true, 1);
                    }
                });

            }
        },0,30);
*/

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TextView txtClicks = (TextView) findViewById(R.id.txtClicks);
                        // task to be done every 1000 milliseconds
                        // iClicks = iClicks + 1;
                        // txtClicks.setText(String.valueOf(iClicks));
                        if(count==2)
                        {
                            vtg1.setText(" " + df.format(vtg));
                            vtg2.setText(" " + df.format(vtg3));
                        }
                        if (count == 20) {
                            count = 0;
                            f1.setText(" " + point);
                            point = 0;
                            f2.setText(" " + point1);
                            point1 = 0;

                        }
                        if(strt==1) {
                            mConnectedThread.write(r);
                            count++;
                        }
                        r++;
                        if(r==3)
                         {
                         r=1;
                         }


                    }
                });

            }
        }, 0, 100);

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();



    }
/*
    @Override
    protected void onResume() {
        super.onResume();
        // we're going to simulate real time with thread that append data to the graph
        new Thread(new Runnable() {

            @Override
            public void run() {
                // we add 100 new entries
                for (int i = 0; i < 100; i++) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        // manage error ...
                    }
                }
            }
        }).start();
    }


    // add random data to graph
    private void addEntry() {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 10d), true, 10);
    }
*/
            public void start(){
                start.setOnClickListener(
                    new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                                            strt=1;
                                             r=1;
                                             i=0;
                                        }
                    }
                 );
            }

    public void stop(){
        stop.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        strt=0;
                        count=0;
                    }
                }
        );
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
        r=1;
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

        public void run() {
            byte[] buffer= new byte[1];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(int message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            // byte[] msgBuffer = message;
            try {
                mmOutStream.write(message);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }



}
