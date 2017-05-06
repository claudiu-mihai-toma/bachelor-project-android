package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import static bachelor.claudiu.interactiveinformationshare.QRManager.QR_REQUEST_CODE;

public class InteractiveInformationShareActivity extends Activity implements ContentSentCallback, PictureTakenCallback
{
	public static final String LOGS = "interesting-logs-flag ";

	private String mContent = null;
	private String mDesktopAddress = null;

	private ImageView mPictureImageView = null;

	private QRManager mQRManager = null;
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

		mQRManager = new QRManager(this);

		Button qrButton = (Button) this.findViewById(R.id.qr_button);
		qrButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					mQRManager.startQRActivity();
				}
				catch (ActivityNotFoundException | SocketException | UnknownHostException e)
				{
					finishWithToast("Unable to start QR service!");
				}
			}
		});

		startCameraTimer();

		mPictureImageView = (ImageView) findViewById(R.id.picture_imageview);

		try
		{
			mPhonePictureStream = new PhonePictureStream();
			mPhonePictureStream.open();
		}
		catch (IOException e)
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
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case QR_REQUEST_CODE:
				mQRManager.clean();
				if (resultCode == RESULT_OK)
				{
					mDesktopAddress = data.getStringExtra("SCAN_RESULT");
					sendContentToDesktop();
				}
				else
				{
					finishWithToast("QR scanning failed!");
				}
				break;
		}
	}

	@Override
	protected void onDestroy()
	{
		Log.d(LOGS, "Destroying...");
		stopCameraTimer();
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
		mPhonePictureStream.send(picture);
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

	private void sendContentToDesktop()
	{
		SendContentAsyncTask sendContentAsyncTask = new SendContentAsyncTask(this, mDesktopAddress, mContent);
		sendContentAsyncTask.execute();
	}
}
