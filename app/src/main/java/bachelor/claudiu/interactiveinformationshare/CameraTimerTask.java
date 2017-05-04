package bachelor.claudiu.interactiveinformationshare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.util.TimerTask;

import static bachelor.claudiu.interactiveinformationshare.InteractiveInformationShareActivity.LOGS;

/**
 * Created by claudiu on 04.05.2017.
 */

public class CameraTimerTask extends TimerTask
{
	private Object mObject = new Object();

	private PictureTakenCallback mPictureTakenCallback;
	private Camera mCamera;

	public CameraTimerTask(PictureTakenCallback pictureTakenCallback, Camera camera)
	{
		mPictureTakenCallback = pictureTakenCallback;
		mCamera = camera;
	}

	@Override
	public void run()
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
						Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
						Log.d(LOGS, "capture width = " + picture.getWidth());
						Log.d(LOGS, "capture height = " + picture.getHeight());

						/*int width = mPhoto.getWidth() / 4;
						int height = mPhoto.getHeight() / 4;
						mPhoto = Bitmap.createScaledBitmap(mPhoto, width, height, false);*/
						picture = Utils.RotateBitmap(picture, -90);
						Log.d(LOGS, "new capture width = " + picture.getWidth());
						Log.d(LOGS, "new capture height = " + picture.getHeight());

						mPictureTakenCallback.pictureTakenCallback(picture);

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
			}

			Log.d(LOGS, "CameraAsyncTask after take picture.");
		}
	}
}
