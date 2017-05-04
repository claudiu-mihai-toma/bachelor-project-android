package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class InteractiveInformationShareActivity extends Activity
{
	private Object mObject = new Object();
	private static final boolean DEBUG_USE_THUMBNAIL = false;

	private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	private static final int QR_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int CONTENT_RECEIVER_PORT = 9753;
	private static final int CAMERA_PERIOD = 500;
	private static final int QR_BEACON_PORT = 9751;
	private static final int BITMAP_REDUCTION_FACTOR = 1;
	public static final String LOGS = "interesting-logs-flag ";

	private String mContent = null;
	private String mDesktopAddress = null;
	Timer mTimer = null;

	private ImageView mPhotoImageView = null;
	private Bitmap mPhoto = null;
	private File mImageFile = null;

	private Camera mCamera = null;
	Timer mCameraTimer = null;

	private enum DataType
	{
		STRING,
		IMAGE
	}

	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	private class SendContentAsyncTask extends AsyncTask<DataType, Void, Void>
	{

		@Override
		protected Void doInBackground(DataType... params)
		{

			try
			{
				Log.d(LOGS, "Sending data " + mContent + " to " + mDesktopAddress);

				Log.d(LOGS, "Creating socket...");
				Socket socket = new Socket(mDesktopAddress, CONTENT_RECEIVER_PORT);
				Log.d(LOGS, "Socket created!");

				DataOutputStream os = new DataOutputStream(socket.getOutputStream());

				DataType dataType = params[0];
				switch (dataType)
				{
					case STRING:
						os.writeUTF(mContent);
						break;

					case IMAGE:
						/*int byteCount = mPhoto.getByteCount();
						ByteBuffer byteBuffer = ByteBuffer.allocate(byteCount);
						mPhoto.copyPixelsToBuffer(byteBuffer);
						byte[] byteArray = byteBuffer.array();*/

						int width = mPhoto.getWidth() / BITMAP_REDUCTION_FACTOR;
						int height = mPhoto.getHeight() / BITMAP_REDUCTION_FACTOR;
						Bitmap sendPhoto = Bitmap.createScaledBitmap(mPhoto, width, height, false);
						//sendPhoto = RotateBitmap(sendPhoto, 90);

						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						sendPhoto.compress(Bitmap.CompressFormat.JPEG, 100, stream);
						byte[] byteArray = stream.toByteArray();

						os.writeInt(byteArray.length);
						os.write(byteArray);
						Log.d(LOGS, "Image sent.");
						break;
				}

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
			//finish();
		}
	}

	private class CameraAsyncTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params)
		{

			Log.d(LOGS, "CameraAsyncTask before sync...");
			synchronized (mObject)
			{
				Log.d(LOGS, "CameraAsyncTask after sync.");
				try
				{
					Log.d(LOGS, "CameraAsyncTask before set preview...");
					mCamera.setPreviewTexture(new SurfaceTexture(10));
					Log.d(LOGS, "CameraAsyncTask after set preview.");
				}
				catch (IOException e)
				{
				}
				Log.d(LOGS, "CameraAsyncTask before start preview...");
				mCamera.startPreview();
				Log.d(LOGS, "CameraAsyncTask after start preview.");

				Log.d(LOGS, "CameraAsyncTask before take picture...");
				mCamera.takePicture(null, null, new Camera.PictureCallback()
				{
					@Override
					public void onPictureTaken(byte[] data, Camera camera)
					{
						synchronized (mObject)
						{
							mPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
							Log.d(LOGS, "capture width = " + mPhoto.getWidth());
							Log.d(LOGS, "capture height = " + mPhoto.getHeight());

							int width = mPhoto.getWidth() / 4;
							int height = mPhoto.getHeight() / 4;
							mPhoto = Bitmap.createScaledBitmap(mPhoto, width, height, false);
							mPhoto = RotateBitmap(mPhoto, -90);
							Log.d(LOGS, "new capture width = " + mPhoto.getWidth());
							Log.d(LOGS, "new capture height = " + mPhoto.getHeight());

							mPhotoImageView.setImageBitmap(mPhoto);

							mObject.notify();
						}
					}
				});

				try
				{
					mObject.wait();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				Log.d(LOGS, "CameraAsyncTask after take picture.");
			}
			return null;
		}
	}

	public void startCameraTimer()
	{
		stopCameraTimer();
		mCameraTimer = new Timer();
		TimerTask doAsynchronousTask = new TimerTask()
		{
			@Override
			public void run()
			{
				/*try
				{
					CameraAsyncTask cameraAsyncTask = new CameraAsyncTask();
					cameraAsyncTask.execute();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
				}*/

				Log.d(LOGS, "CameraAsyncTask before sync...");
				synchronized (mObject)
				{
					Log.d(LOGS, "CameraAsyncTask after sync.");
					try
					{
						Log.d(LOGS, "CameraAsyncTask before set preview...");
						mCamera.setPreviewTexture(new SurfaceTexture(10));
						Log.d(LOGS, "CameraAsyncTask after set preview.");
					}
					catch (IOException e)
					{
					}
					Log.d(LOGS, "CameraAsyncTask before start preview...");
					mCamera.startPreview();
					Log.d(LOGS, "CameraAsyncTask after start preview.");

					Log.d(LOGS, "CameraAsyncTask before take picture...");
					mCamera.takePicture(null, null, new Camera.PictureCallback()
					{
						@Override
						public void onPictureTaken(byte[] data, Camera camera)
						{
							synchronized (mObject)
							{
								mPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
								Log.d(LOGS, "capture width = " + mPhoto.getWidth());
								Log.d(LOGS, "capture height = " + mPhoto.getHeight());

								/*int width = mPhoto.getWidth() / 4;
								int height = mPhoto.getHeight() / 4;
								mPhoto = Bitmap.createScaledBitmap(mPhoto, width, height, false);*/
								mPhoto = RotateBitmap(mPhoto, -90);
								Log.d(LOGS, "new capture width = " + mPhoto.getWidth());
								Log.d(LOGS, "new capture height = " + mPhoto.getHeight());

								mPhotoImageView.setImageBitmap(mPhoto);

								mObject.notify();
							}
						}
					});

					try
					{
						mObject.wait();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}

					Log.d(LOGS, "CameraAsyncTask after take picture.");
				}
			}
		};
		mCameraTimer.schedule(doAsynchronousTask, 0, CAMERA_PERIOD);
	}

	private void stopCameraTimer()
	{
		Utils.stopTimer(mCameraTimer);
	}

	/**
	 * A safe way to get an instance of the Camera object.
	 */
	public static Camera getCameraInstance()
	{
		Camera c = null;
		try
		{
			Log.d(LOGS, "Getting camera instance.");
			c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
			Log.d(LOGS, "GOT camera instance.");
		}
		catch (Exception e)
		{
			// Camera is not available (in use or does not exist)
			Log.d(LOGS, e.toString());
		}
		return c; // returns null if camera is unavailable
	}

	private void setCameraParameters()
	{
		if (mCamera == null)
		{
			Log.d(LOGS, "Why is camera null?");
			return;
		}
		Log.d(LOGS, "Found valid camera.");
		try
		{
			mCamera.setPreviewTexture(new SurfaceTexture(10));
		}
		catch (IOException e)
		{
		}
		//mCamera.startPreview();
		Camera.Parameters params = mCamera.getParameters();
		//params.setPreviewSize(640, 480);
		params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		params.setPictureFormat(ImageFormat.JPEG);
		List<Camera.Size> sizes = params.getSupportedPictureSizes();
		Camera.Size smallSize = sizes.get(sizes.size() - 1);
		params.setPictureSize(smallSize.width, smallSize.height);
		mCamera.setParameters(params);
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

		mPhotoImageView = (ImageView) findViewById(R.id.photo_imageview);

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

		mCamera = getCameraInstance();
		if (mCamera == null)
		{
			Log.d(LOGS, "Camera not available!");
			finishWithToast("Fatal error! No camera detected!");
			return;
		}
		setCameraParameters();
		startCameraTimer();
		Log.d(LOGS, "Finished starting app.");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	private void startQRBroadcastBeacon()
	{
		mTimer = new Timer();
		try
		{
			mTimer.schedule(new BroadcastBeaconTimerTask(QR_BEACON_PORT), 0, BroadcastBeaconTimerTask.BEACON_PERIOD);
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
		Utils.stopTimer(mTimer);
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
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

			if (!DEBUG_USE_THUMBNAIL)
			{
				String imageFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath()
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
					//sendContentToDesktop(DataType.STRING);
					sendContentToDesktop(DataType.IMAGE);
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
						Log.d(LOGS, "Camera activity returned. Image in file = [" + mImageFile + "]");
						mPhoto = BitmapFactory.decodeFile(mImageFile.getPath());
					}
					else
					{
						mPhoto = (Bitmap) data.getExtras().get("data");
					}

					mPhotoImageView.setImageBitmap(mPhoto);

					Log.d(LOGS, "Image data: width = " + mPhoto.getWidth() + " height = " + mPhoto.getHeight());
				}
				else
				{
					finishWithToast("Taking photo failed!");
				}
				break;
		}
	}

	private void sendContentToDesktop(DataType dataType)
	{
		SendContentAsyncTask sendContentAsyncTask = new SendContentAsyncTask();
		sendContentAsyncTask.execute(dataType);
	}

	private void finishWithToast(String toastMessage)
	{
		Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	protected void onDestroy()
	{
		Log.d(LOGS, "Destroying...");
		stopCameraTimer();
		stopBroadcastBeacon();
		synchronized (mObject)
		{
			if (mCamera != null)
			{
				Log.d(LOGS, "Releasing...");
				mCamera.release();
				mCamera = null;
			}
		}
		Log.d(LOGS, "Destroyed!");
		super.onDestroy();
		Log.d(LOGS, "Super Destroyed!");
	}
}
