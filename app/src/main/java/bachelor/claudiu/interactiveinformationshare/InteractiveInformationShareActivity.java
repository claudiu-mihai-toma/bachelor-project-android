package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class InteractiveInformationShareActivity extends Activity implements ContentSentCallback, PictureTakenCallback, ContentReceivedCallback
{
	public static final String  LOGS            = "interesting-logs-flag ";
	public static final boolean USE_BACK_CAMERA = true;

	private Content            mContent                = null;
	private String             mDesktopAddress         = null;
	private CameraTimer        mCameraTimer            = null;
	private PhonePictureStream mPhonePictureStream     = null;
	private SurfaceView        mSurfaceView            = null;
	private AtomicBoolean      mProcessingPictureTaken = null;
	private boolean            mSendContent            = true;

	private void finishWithToast(String toastMessage)
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Finishing with message: " + toastMessage);
		//clean();
		Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_interactive_information_share);

		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Welcome to create!");

		mSurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
		handleIntent();
		if (mSendContent)
		{
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "App will send content.");
		}
		else
		{
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "App will receive content.");
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "onResume.");

		mProcessingPictureTaken = new AtomicBoolean(false);
		initialize();

		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Finished starting app.");
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		clean();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "onPause.");
	}

	@Override
	protected void onDestroy()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Destroying...");
		clean();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Destroyed!");
		super.onDestroy();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Super Destroyed!");
	}

	@Override
	public void contentSentCallback()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Content successfully sent.");
		finishWithToast("Content successfully sent.");
	}

	@Override
	public void contentReceivedCallback(Content content)
	{
		if (mContent == null)
		{
			finishWithToast("Error encountered while receiving content.");
			return;
		}

		mContent = content;
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Content successfully received.");
		String title = mContent.getTitle();
		byte[] data = mContent.getData();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Content title: [" + title + "]");

		if (data != null)
		{
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Content length: [" + data.length + "]");
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, title, Constants.Misc.PICTURE_DESCRIPTION);
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Content stored to path: [" + Utils.getRealPathFromURI(this, Uri.parse(path)) + "]");
		}
		else
		{
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Content length: [none]");
			// TODO: Open a browser (if title is a link)?
			copyTextToClipboard(title);
			Toast.makeText(this, title, Toast.LENGTH_LONG).show();
		}
		finishWithToast("Content successfully received.");
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
					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Valid desktop address from QR scan.");
					transferContent();
				}
				else
				{
					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "No valid desktop address from QR scan.");

					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Sending picture taken...");
					mPhonePictureStream.send(picture);
					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Picture taken sent.");

					Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Receiving results...");
					mDesktopAddress = mPhonePictureStream.receive();
					if (mDesktopAddress == null)
					{
						Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "No valid desktop address received.");
						mProcessingPictureTaken.set(false);
					}
					else
					{
						Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Valid desktop address from picture stream.");
						transferContent();
					}
				}
			}
			else
			{
				mProcessingPictureTaken.set(false);
			}
		}
	}

	private void initialize()
	{
		mProcessingPictureTaken.set(false);
		startPhonePictureStream();
		startCameraTimer();
	}

	private void clean()
	{
		mProcessingPictureTaken.set(true);
		stopCameraTimer();
		stopPhonePictureStream();
	}

	private void startCameraTimer()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Starting/Scheduling camera timer...");
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

	private void startPhonePictureStream()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Starting phone picture stream...");
		try
		{
			mPhonePictureStream = new PhonePictureStream();
			mPhonePictureStream.open();
		}
		catch (IOException e)
		{
			finishWithToast("Cannot start phone picture stream!");
		}
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Phone picture stream started.");
	}

	private void stopPhonePictureStream()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Stopping phone picture stream...");
		if (mPhonePictureStream != null)
		{
			mPhonePictureStream.cancel();
			mPhonePictureStream = null;
		}
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Phone picture stream stopped.");
	}

	private void transferContent()
	{
		clean();
		if (mSendContent)
		{
			sendContentToDesktop();
		}
		else
		{
			receiveContentFromDesktop();
		}
	}

	private void sendContentToDesktop()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Sending " + mContent + " to " + mDesktopAddress);
		SendContentAsyncTask sendContentAsyncTask = new SendContentAsyncTask(this, mDesktopAddress, mContent);
		sendContentAsyncTask.execute();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Send content executed.");
	}

	private void receiveContentFromDesktop()
	{
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Receiving content from " + mDesktopAddress);
		ReceiveContentAsyncTask receiveContentAsyncTask = new ReceiveContentAsyncTask(this, mDesktopAddress);
		receiveContentAsyncTask.execute();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Receive content executed.");
	}

	private void handleIntent()
	{
		Intent intent = getIntent();

		if (intent == null || intent.getExtras() == null)
		{
			mSendContent = false;
			//mContent = new Content(Content.ContentType.TEXT, "AWESOME TEST STRING!", null);

			/*finishWithToast("Something went wrong!\nnull intent");
			return;*/
		}
		else
		{
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Intent received [" + intent.getExtras() + "]");

			String action = intent.getAction();
			String type = intent.getType();

			if (Intent.ACTION_SEND.equals(action) && type != null)
			{
				if (type.equals("text/plain"))
				{
					handleSendText(intent); // Handle text being sent
				}
				else
				{
					if (type.startsWith("image/"))
					{
						handleSendImage(intent); // Handle single image being sent
					}
					else
					{
						mSendContent = false;
					}
				}
			}
			else
			{
				mSendContent = false;
			}
		}
	}

	private void handleSendText(Intent intent)
	{
		String stringContent = intent.getStringExtra(Intent.EXTRA_TEXT);

		mContent = new Content(Content.ContentType.TEXT, stringContent, null);
	}

	private void handleSendImage(Intent intent)
	{
		Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Intent URI: {" + uri + "}");

		String path = uri.getPath();
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Image path: {" + path + "}");
		String realPath = Utils.getRealPathFromURI(this, uri);
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Image real path: {" + realPath + "}");

		File file = new File(realPath);
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Image name: {" + file.getName() + "}");

		String fileName = Utils.getFileName(realPath);
		Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "Image real name: {" + fileName + "}");

		byte[] data = null;

		try
		{
			data = Files.toByteArray(file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "toByteArray exception: {" + e.toString() + "}");
		}

		if (data == null)
		{
			// TODO: Not good! We cannot read the image. Handle this!
			Utils.log(Constants.Classes.INTERACTIVE_INFORMATION_SHARE, "DATA IS NULL!!!");
			return;
		}

		mContent = new Content(Content.ContentType.IMAGE, fileName, data);
	}

	private void copyTextToClipboard(String text)
	{
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("received text", text);
		clipboard.setPrimaryClip(clip);
	}
}
