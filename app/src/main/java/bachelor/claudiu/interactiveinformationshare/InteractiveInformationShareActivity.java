package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;

public class InteractiveInformationShareActivity extends Activity {

    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private static final int REQUEST_CODE = 0;
    private static final int SEND_DATA_BUFFER_LEN = 1024;
    private static final int CONTENT_RECEIVER_PORT = 9753;
    private static final int BEACON_PERIOD = 100;
    public static final String LOGS = "interesting-logs-flag ";

    private String mContent = null;
    private String mDesktopAddress = null;
    Timer mTimer = null;

    private class SendContentAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            byte[] sendData;

            try {
                Log.d(LOGS, "Sending data " + mContent + " to " + mDesktopAddress);
                Log.d(LOGS, "Creating socket...");
                DatagramSocket datagramSocket = new DatagramSocket();
                Log.d(LOGS, "Socket created!");
                sendData = mContent.getBytes();
                InetAddress inetAddress = InetAddress.getByName(mDesktopAddress);
                Log.d(LOGS, "Destination address " + inetAddress.getHostName());
                DatagramPacket datagramPacket = new DatagramPacket(sendData, sendData.length, inetAddress, CONTENT_RECEIVER_PORT);

                datagramSocket.send(datagramPacket);
            } catch (SocketException e) {
                Log.d(LOGS, "Sending failed! 1");
                e.printStackTrace();
            } catch (UnknownHostException e) {
                Log.d(LOGS, "Sending failed! 2");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(LOGS, "Sending failed! 3");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_interactive_information_share);

        //TextView intentReceivedTextView = (TextView) findViewById(R.id.intent_received_text_view);
        Intent intent = getIntent();
        if (intent != null) {
            Log.d(LOGS, "Intent received [" + intent.getExtras() + "]");
            String textShared = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (textShared != null) {
                //intentReceivedTextView.setText(textShared);
                Log.d(LOGS, "Shared text [" + textShared + "]");
                mContent = textShared;
                startBroadcastBeacon();
                startQRActivity();
            } else {
                //intentReceivedTextView.setText("Intent received error! :(");
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            //intentReceivedTextView.setText("Intent received error! :(");
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startBroadcastBeacon() {
        mTimer = new Timer();
        try {
            mTimer.schedule(new BroadcastBeaconTimerTask(), 0, BEACON_PERIOD);
        } catch (SocketException e) {
            e.printStackTrace();
            finishWithToast("Beacon socket exception: " + e.getMessage());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            finishWithToast("Beacon unknown host exception: " + e.getMessage());
        }
    }

    private void stopBroadcastBeacon() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void startQRActivity() {
        Log.d(LOGS, "Starting QR Activity with mContent [" + mContent + "]");
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            Toast.makeText(this, "QR scanner app not found!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //TextView qrCodeReceivedTextView = (TextView) findViewById(R.id.qr_code_received_text_view);
        if (requestCode == REQUEST_CODE) {
            stopBroadcastBeacon();
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = data.getStringExtra("SCAN_RESULT");
                String format = data.getStringExtra("SCAN_RESULT_FORMAT");
                //Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG).show();
                //qrCodeReceivedTextView.setText("Content:[" + contents + "] Format:[" + format+"]");
                mDesktopAddress = contents;
                sendContentToDesktop();
            } else {
                //qrCodeReceivedTextView.setText("QR error! :(");
                finishWithToast("QR scanning failed!");
            }
        }
    }

    private void sendContentToDesktop() {
        SendContentAsyncTask sendContentAsyncTask = new SendContentAsyncTask();
        sendContentAsyncTask.execute();
    }

    private void finishWithToast(String toastMessage) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        finish();
    }
}
