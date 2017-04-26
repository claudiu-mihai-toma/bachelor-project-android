package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;

public class InteractiveInformationShareActivity extends Activity
{

	private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	private static final int QR_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int CONTENT_RECEIVER_PORT = 9753;
	private static final int BEACON_PERIOD = 100;
	public static final String LOGS = "interesting-logs-flag ";

	private String mContent = null;
	private String mDesktopAddress = null;
	Timer mTimer = null;

	private ImageView mPhotoImageView = null;
	private File imageFile = null;

	private class SendContentAsyncTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params)
		{

			try
			{
				Log.d(LOGS, "Sending data " + mContent + " to " + mDesktopAddress);

				Log.d(LOGS, "Creating socket...");
				Socket socket = new Socket(mDesktopAddress, CONTENT_RECEIVER_PORT);
				Log.d(LOGS, "Socket created!");

				DataOutputStream os = new DataOutputStream(socket.getOutputStream());
				os.writeUTF(mContent);

				os.close();
				socket.close();

			}
			catch (SocketException e)
			{
				Log.d(LOGS, "Sending failed! 1");
				e.printStackTrace();
			}
			catch (UnknownHostException e)
			{
				Log.d(LOGS, "Sending failed! 2");
				e.printStackTrace();
			}
			catch (IOException e)
			{
				Log.d(LOGS, "Sending failed! 3");
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid)
		{
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent == null)
		{
			finishWithToast("Something went wrong!\nnull intent");
		}

		Log.d(LOGS, "Intent received [" + intent.getExtras() + "]");
		String textShared = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (textShared == null)
		{
			finishWithToast("Something went wrong!\nnull shared text");
		}

		Log.d(LOGS, "Shared text [" + textShared + "]");
		mContent = textShared;

		setContentView(R.layout.activity_interactive_information_share);
		mPhotoImageView = (ImageView) findViewById(R.id.photo_imageview);

		Button qrButton = (Button) this.findViewById(R.id.qr_button);
		qrButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startBroadcastBeacon();
				startQRActivity();
			}
		});

		Button cameraButton = (Button) this.findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startCameraActivity();
			}
		});
	}

	private void startBroadcastBeacon()
	{
		mTimer = new Timer();
		try
		{
			mTimer.schedule(new BroadcastBeaconTimerTask(), 0, BEACON_PERIOD);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			finishWithToast("Beacon socket exception: " + e.getMessage());
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
			finishWithToast("Beacon unknown host exception: " + e.getMessage());
		}
	}

	private void stopBroadcastBeacon()
	{
		if (mTimer != null)
		{
			mTimer.cancel();
		}
	}

	private void startQRActivity()
	{
		Log.d(LOGS, "Starting QR Activity with mContent [" + mContent + "]");
		try
		{
			//start the scanning activity from the com.google.zxing.client.android.SCAN intent
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, QR_REQUEST_CODE);
		}
		catch (ActivityNotFoundException anfe)
		{
			//on catch, show the download dialog
			Toast.makeText(this, "QR scanner app not found!", Toast.LENGTH_LONG).show();
		}
	}

	private void startCameraActivity()
	{
		Log.d(LOGS, "Starting Camera Activity with mContent [" + mContent + "]");
		try
		{
			String imageFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/Android/data/bachelor.claudiu.interactiveinformationshare/files/";
			String imageName = "tmp_image.jpg";
			File folder = new File(imageFolderPath);
			folder.mkdirs();

			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			imageFile = new File(imageFolderPath + imageName);
			Uri uriSavedImage = Uri.fromFile(imageFile);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

			startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
		}
		catch (ActivityNotFoundException anfe)
		{
			Toast.makeText(this, "Image capture app not found!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//TextView qrCodeReceivedTextView = (TextView) findViewById(R.id.qr_code_received_text_view);
		switch (requestCode)
		{
			case QR_REQUEST_CODE:
				stopBroadcastBeacon();
				if (resultCode == RESULT_OK)
				{
					//get the extras that are returned from the intent
					String contents = data.getStringExtra("SCAN_RESULT");
					String format = data.getStringExtra("SCAN_RESULT_FORMAT");
					mDesktopAddress = contents;
					sendContentToDesktop();
				}
				else
				{
					finishWithToast("QR scanning failed!");
				}
				break;

			case CAMERA_REQUEST_CODE:
				if (resultCode == Activity.RESULT_OK)
				{
					Bitmap photo = BitmapFactory.decodeFile(imageFile.getPath());
					//Bitmap photo = (Bitmap) data.getExtras().get("data");
					mPhotoImageView.setImageBitmap(photo);

					Log.d(LOGS, "Image data: width = " + photo.getWidth() + " height = " + photo.getHeight());
				}
				else
				{
					finishWithToast("Taking photo failed!");
				}
				break;
		}
	}

	private void sendContentToDesktop()
	{
		SendContentAsyncTask sendContentAsyncTask = new SendContentAsyncTask();
		sendContentAsyncTask.execute();
	}

	private void finishWithToast(String toastMessage)
	{
		Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
		finish();
	}
}
