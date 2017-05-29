package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class InteractiveInformationShareActivity extends Activity implements ContentSentCallback, PictureTakenCallback
{
	public static final String  LOGS            = "interesting-logs-flag ";
	public static final boolean USE_BACK_CAMERA = true;

	private String             mContent                = null;
	private String             mDesktopAddress         = null;
	private CameraTimer        mCameraTimer            = null;
	private PhonePictureStream mPhonePictureStream     = null;
	private SurfaceView        mSurfaceView            = null;
	private AtomicBoolean      mProcessingPictureTaken = null;

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
		setContentView(R.layout.activity_interactive_information_share);

		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Welcome to create!");

		handleIntent();

		if (mContent == null)
		{
			mContent = "AWESOME TEST STRING!";
			/*finishWithToast("Something went wrong!\nnull shared text");
			return;*/
		}

		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Shared text [" + mContent + "]");

		mProcessingPictureTaken = new AtomicBoolean(false);

		mSurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);

		startCameraTimer();

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
		if (mProcessingPictureTaken.compareAndSet(false, true))
		{
			//Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Picture taken callback.");
			if (picture != null)
			{
				mDesktopAddress = Utils.scanQRImage(picture);

				if (mDesktopAddress != null)
				{
					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Valid desktop address from QR scan" +
							".");
					sendContentToDesktop();

				}
				else
				{
					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "No valid desktop address from QR" +
							" " +
							"scan" +
							".");

					mPhonePictureStream.send(picture);
					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Picture taken sent.");
					if (true)
					{
						Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Receiving results...");
						mDesktopAddress = mPhonePictureStream.receive();
						if (mDesktopAddress == null)
						{
							Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "No valid desktop address" +
									" " +
									"received" +
									".");
						}
						else
						{
							Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Valid desktop address " +
									"received" +
									".");
							sendContentToDesktop();
						}
					}
				}
			}

			mProcessingPictureTaken.set(false);
		}
	}

	public void startCameraTimer()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Starting/Scheduling camera timer...");
		stopCameraTimer();
		mCameraTimer = new CameraTimer(mSurfaceView, this);
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

	private void handleIntent()
	{
		Intent intent = getIntent();
		/*String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.equals("text/plain")) {
				handleSendText(intent); // Handle text being sent
			} else if (type.startsWith("image/")) {
				handleSendImage(intent); // Handle single image being sent
			}
		}*/

		if (intent == null)
		{
			mContent = "AWESOME TEST STRING!";
			/*finishWithToast("Something went wrong!\nnull intent");
			return;*/
		}
		else
		{
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Intent received [" + intent.getExtras() + "]");
			mContent = intent.getStringExtra(Intent.EXTRA_TEXT);

			Bundle intentBundle = intent.getExtras();
			if (intentBundle != null)
			{
				for (String key : intentBundle.keySet())
				{
					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Bundle content: {" + key + "}:{" +
							intentBundle.get(key).toString() + "}");
				}
			}

			Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Intent URI: {" + uri + "}");

			try
			{
				mContent = Utils.getStringFromFile(uri.getPath());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
