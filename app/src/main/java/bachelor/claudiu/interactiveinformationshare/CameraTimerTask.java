package bachelor.claudiu.interactiveinformationshare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TimerTask;

/**
 * Created by claudiu on 04.05.2017.
 */

public class CameraTimerTask extends TimerTask
{
	private Object mObject = new Object();

	private PictureTakenCallback mPictureTakenCallback;
	private Camera               mCamera;

	public CameraTimerTask(PictureTakenCallback pictureTakenCallback, Camera camera)
	{
		Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Constructing...");
		mPictureTakenCallback = pictureTakenCallback;
		mCamera = camera;
		Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Constructed.");
	}

	@Override
	public void run()
	{
		LinkedList<Long> timeList = new LinkedList<>();
		long startTime = System.currentTimeMillis();
		long estimatedTime;
		long prevStartTime = startTime;

		timeList.add(startTime);

		Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Before sync...");
		synchronized (mObject)
		{
			Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "After sync.");
			try
			{
				Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Before set preview...");
				SurfaceTexture surfaceTexture = new SurfaceTexture(0);
				if (surfaceTexture == null)
				{
					Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "!!! Surface texture os null !!!");
				}
				else
				{
					Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Created surface texture.");
				}
				mCamera.setPreviewTexture(surfaceTexture);
				Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "After set preview.");
			}
			catch (IOException e)
			{
				Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "!!! Setting preview failed !!!");
			}

			try
			{
				Thread.sleep(50);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			estimatedTime = System.currentTimeMillis() - prevStartTime;
			prevStartTime = System.currentTimeMillis();
			timeList.add(estimatedTime);
			Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Before start preview...");
			try
			{
				mCamera.startPreview();
			}
			catch (Throwable e)
			{
				Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "!!! Start preview failed !!!");
				Utils.log(Constants.Classes.CAMERA_TIMER_TASK, e.getMessage());
				Utils.log(Constants.Classes.CAMERA_TIMER_TASK, e.toString());
				e.printStackTrace();
				throw e;
			}
			Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "After start preview.");

			estimatedTime = System.currentTimeMillis() - prevStartTime;
			prevStartTime = System.currentTimeMillis();
			timeList.add(estimatedTime);

			Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Before takePicture call...");
			try
			{
				mCamera.takePicture(null, null, new Camera.PictureCallback()
				{
					@Override
					public void onPictureTaken(byte[] data, Camera camera)
					{
						Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Start onPictureTaken...");
						synchronized (mObject)
						{
							Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
							//Utils.log(Constants.Classes.CAMERA_TIMER_TASK,"capture width = " + picture.getWidth());
							//Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "capture height = " + picture.getHeight
							// ());

						/*int width = mPhoto.getWidth() / 4;
						int height = mPhoto.getHeight() / 4;
						mPhoto = Bitmap.createScaledBitmap(mPhoto, width, height, false);*/
							picture = Utils.RotateBitmap(picture, -90);
							Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "new capture width = " + picture.getWidth
									());

							Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "new capture height = " + picture.getHeight
									());

							Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Calling pictureTakenCallback...");
							mPictureTakenCallback.pictureTakenCallback(picture);
							Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Called pictureTakenCallback.");

							mObject.notify();
						}
						Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "End onPictureTaken.");
					}
				});

				estimatedTime = System.currentTimeMillis() - prevStartTime;
				prevStartTime = System.currentTimeMillis();
				timeList.add(estimatedTime);
				try
				{
					mObject.wait();
				}
				catch (InterruptedException e)
				{
				}
			}
			catch (RuntimeException e)
			{
				Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "!!! Failed to take picture !!!");
				Utils.log(Constants.Classes.CAMERA_TIMER_TASK, e.getMessage());
				Utils.log(Constants.Classes.CAMERA_TIMER_TASK, e.toString());
			}

			Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "After takePicture call.");
		}

		estimatedTime = System.currentTimeMillis() - prevStartTime;
		timeList.add(estimatedTime);
		timeList.removeFirst();

		estimatedTime = System.currentTimeMillis() - startTime;

		Utils.log(Constants.Classes.CAMERA_TIMER_TASK, timeList.toString());
		Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "overall time = " + estimatedTime + "ms");
		Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "\n");
	}
}
