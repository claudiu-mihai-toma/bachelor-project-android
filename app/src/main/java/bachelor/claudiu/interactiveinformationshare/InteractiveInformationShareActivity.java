package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;

public class InteractiveInformationShareActivity extends Activity implements ContentSentCallback, PictureTakenCallback
{
	private Object mObject = new Object();
	private static final boolean DEBUG_USE_THUMBNAIL = false;

	private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	private static final int QR_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int QR_BEACON_PORT = 9751;
	public static final String LOGS = "interesting-logs-flag ";

	private String mContent = null;
	private String mDesktopAddress = null;

	private ImageView mPictureImageView = null;
	private Bitmap mPhoto = null;
	private File mImageFile = null;

	private Timer mQRTimer = null;
	private CameraTimer mCameraTimer = null;
	private PhonePictureStream mPhonePictureStream = null;


	private void finishWithToast(String toastMessage)
	{
		Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Log.d(LOGS, "Welcome to create!");

		Intent intent = getIntent();
		if (intent == null)
		{
			finishWithToast("Something went wrong!\nnull intent");
			return;
		}

		Log.d(LOGS, "Intent received [" + intent.getExtras() + "]");
		String textShared = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (textShared == null)
		{
			finishWithToast("Something went wrong!\nnull shared text");
			return;
		}

		setContentView(R.layout.activity_interactive_information_share);

		Log.d(LOGS, "Shared text [" + textShared + "]");
		mContent = textShared;

		mPictureImageView = (ImageView) findViewById(R.id.photo_imageview);

		Button qrButton = (Button) this.findViewById(R.id.qr_button);
		qrButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startQRBroadcastBeacon();
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

		startCameraTimer();

		try
		{
			mPhonePictureStream = new PhonePictureStream();
		}
		catch (IOException e)
		{
			finishWithToast("Cannot start phone picture stream!");
			return;
		}
		try
		{
			mPhonePictureStream.open();
		}
		catch (SocketException e)
		{
			finishWithToast("Cannot start phone picture stream!");
			return;
		}
		catch (UnknownHostException e)
		{
			finishWithToast("Cannot start phone picture stream!");
			return;
		}

		Log.d(LOGS, "Finished starting app.");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//TextView qrCodeReceivedTextView = (TextView) findViewById(R.id
		// .qr_code_received_text_view);
		switch (requestCode)
		{
			case QR_REQUEST_CODE:
				stopQRBroadcastBeacon();
				if (resultCode == RESULT_OK)
				{
					//get the extras that are returned from the intent
					String content = data.getStringExtra("SCAN_RESULT");
					//String format = data.getStringExtra("SCAN_RESULT_FORMAT");
					mDesktopAddress = content;
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
					if (!DEBUG_USE_THUMBNAIL)
					{
						Log.d(LOGS, "Camera activity returned. Image in file = [" + mImageFile +
								"]");
						mPhoto = BitmapFactory.decodeFile(mImageFile.getPath());
					}
					else
					{
						mPhoto = (Bitmap) data.getExtras().get("data");
					}

					mPictureImageView.setImageBitmap(mPhoto);

					Log.d(LOGS, "Image data: width = " + mPhoto.getWidth() + " height = " + mPhoto
							.getHeight());
				}
				else
				{
					finishWithToast("Taking photo failed!");
				}
				break;
		}
	}

	@Override
	protected void onDestroy()
	{
		Log.d(LOGS, "Destroying...");
		stopCameraTimer();
		stopQRBroadcastBeacon();
		mPhonePictureStream.cancel();
		Log.d(LOGS, "Destroyed!");
		super.onDestroy();
		Log.d(LOGS, "Super Destroyed!");
	}

	@Override
	public void contentSentCallback()
	{
		//finish();
	}

	@Override
	public void pictureTakenCallback(Bitmap picture)
	{
		mPictureImageView.setImageBitmap(picture);
	}

	public void startCameraTimer()
	{
		stopCameraTimer();
		mCameraTimer = new CameraTimer(this);
		mCameraTimer.schedule();
	}

	private void stopCameraTimer()
	{
		if (mCameraTimer != null)
		{
			mCameraTimer.cancel();
			mCameraTimer = null;
		}
	}

	private void startQRBroadcastBeacon()
	{
		mQRTimer = new Timer();
		try
		{
			mQRTimer.schedule(new BroadcastBeaconTimerTask(QR_BEACON_PORT), 0,
					BroadcastBeaconTimerTask.BEACON_PERIOD);
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

	private void stopQRBroadcastBeacon()
	{
		Utils.stopTimer(mQRTimer);
	}

	private void startCameraActivity()
	{
		Log.d(LOGS, "Starting Camera Activity with mContent [" + mContent + "]");
		try
		{
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

			if (!DEBUG_USE_THUMBNAIL)
			{
				String imageFolderPath = Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ "/Android/data/bachelor.claudiu.interactiveinformationshare/files/";
				String imageName = "tmp_image.jpg";
				File folder = new File(imageFolderPath);
				folder.mkdirs();

				mImageFile = new File(imageFolderPath + imageName);
				Uri uriSavedImage = Uri.fromFile(mImageFile);
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

				Log.d(LOGS, "Camera activity with URI = [" + uriSavedImage.toString() + "]");
			}

			startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
		}
		catch (ActivityNotFoundException anfe)
		{
			Toast.makeText(this, "Image capture app not found!", Toast.LENGTH_LONG).show();
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

	private void sendContentToDesktop()
	{
		SendContentAsyncTask sendContentAsyncTask = new SendContentAsyncTask(this, mDesktopAddress, mContent);
		sendContentAsyncTask.execute();
	}
}
