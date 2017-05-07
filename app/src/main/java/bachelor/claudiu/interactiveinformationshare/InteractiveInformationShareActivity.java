package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
	public static final boolean USE_BACK_CAMERA = true;

	private String mContent        = null;
	private String mDesktopAddress = null;

	private ImageView mPictureImageView = null;

	private QRManager          mQRManager          = null;
	private CameraTimer        mCameraTimer        = null;
	private PhonePictureStream mPhonePictureStream = null;


	private void finishWithToast(String toastMessage)
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Finishing with message: " + toastMessage);
		Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Welcome to create!");

		Intent intent = getIntent();
		if (intent == null)
		{
			finishWithToast("Something went wrong!\nnull intent");
			return;
		}

		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Intent received [" + intent.getExtras() + "]");
		String textShared = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (textShared == null)
		{
			finishWithToast("Something went wrong!\nnull shared text");
			return;
		}

		setContentView(R.layout.activity_interactive_information_share);

		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Shared text [" + textShared + "]");
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

		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Finished starting app.");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "onResume.");
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "onPause.");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "onActivityResult.");
		switch (requestCode)
		{
			case QR_REQUEST_CODE:
				mQRManager.clean();
				if (resultCode == RESULT_OK)
				{
					mDesktopAddress = data.getStringExtra("SCAN_RESULT");
					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "QR address is: " + mDesktopAddress);
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
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Destroying...");
		stopCameraTimer();
		mPhonePictureStream.cancel();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Destroyed!");
		super.onDestroy();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Super Destroyed!");
	}

	@Override
	public void contentSentCallback()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Content successfully sent.");
		//finish();
	}

	@Override
	public void pictureTakenCallback(Bitmap picture)
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Picture taken callback.");
		mPictureImageView.setImageBitmap(picture);
		mPhonePictureStream.send(picture);
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Picture taken sent.");
		if (true)
		{
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Receiving results...");
			mDesktopAddress = mPhonePictureStream.receive();
			if (mDesktopAddress == null)
			{
				Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "No valid desktop address received.");
			}
			else
			{
				Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Valid desktop address received.");
				sendContentToDesktop();
			}
		}
	}

	public void startCameraTimer()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Starting/Scheduling camera timer...");
		stopCameraTimer();
		mCameraTimer = new CameraTimer(this);
		mCameraTimer.schedule();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Camera timer started/scheduled.");
	}

	private void stopCameraTimer()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Stopping camera timer...");
		if (mCameraTimer != null)
		{
			mCameraTimer.cancel();
			mCameraTimer = null;
		}
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Camera timer stopped.");
	}

	private void sendContentToDesktop()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Sending " + mContent + " to " + mDesktopAddress);
		SendContentAsyncTask sendContentAsyncTask = new SendContentAsyncTask(this, mDesktopAddress, mContent);
		sendContentAsyncTask.execute();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Send content executed.");
	}
}
