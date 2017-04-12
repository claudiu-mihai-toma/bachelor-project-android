package bachelor.claudiu.interactiveinformationshare;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class InteractiveInformationShareActivity extends AppCompatActivity {

    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private static final int REQUEST_CODE = 0;
    private static final int SEND_DATA_BUFFER_LEN = 1024;
    private static final int CONTENT_RECEIVER_PORT = 9753;
    private static final String LOGS = "interesting-logs-flag ";

    private String content = null;
    private String desktopAddress = null;

    private class SendContentAsyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            byte[] sendData;

            try {
                Log.d(LOGS, "Sending data "+content+" to "+desktopAddress);
                Log.d(LOGS, "Creating socket...");
                DatagramSocket datagramSocket = new DatagramSocket();
                Log.d(LOGS, "Socket created!");
                sendData = content.getBytes();
                InetAddress inetAddress = InetAddress.getByName(desktopAddress);
                Log.d(LOGS, "Destination address "+inetAddress.getHostName());
                DatagramPacket datagramPacket = new DatagramPacket(sendData, sendData.length, inetAddress,CONTENT_RECEIVER_PORT);

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

        TextView intentReceivedTextView = (TextView) findViewById(R.id.intent_received_text_view);
        Intent intent = getIntent();
        if (intent != null) {
            String textShared = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (textShared != null) {
                intentReceivedTextView.setText(textShared);
                content = textShared;
                startQRActivity();
            } else {
                intentReceivedTextView.setText("Intent received error! :(");
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startQRActivity()
    {
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
        TextView qrCodeReceivedTextView = (TextView) findViewById(R.id.qr_code_received_text_view);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = data.getStringExtra("SCAN_RESULT");
                String format = data.getStringExtra("SCAN_RESULT_FORMAT");
                //Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG).show();
                qrCodeReceivedTextView.setText("Content:[" + contents + "] Format:[" + format+"]");
                desktopAddress = contents;
                sendContentToDesktop();
            }
            else{
                qrCodeReceivedTextView.setText("QR error! :(");
            }
        }
    }

    private void sendContentToDesktop(){
        SendContentAsyncTask sendContentAsyncTask = new SendContentAsyncTask();
        sendContentAsyncTask.execute();
    }
}
