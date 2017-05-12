package bachelor.claudiu.interactiveinformationshare;

import android.hardware.Camera;

import java.util.TimerTask;

/**
 * Created by claudiu on 04.05.2017.
 */

public class CameraTimerTask extends TimerTask
{
	private Object mObject = new Object();

	private PictureTakenCallback mPictureTakenCallback;
	private Camera               mCamera;
	private Preview              mPreview;

	public CameraTimerTask(PictureTakenCallback pictureTakenCallback, Camera camera, Preview preview)
	{
		Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Constructing...");
		mPictureTakenCallback = pictureTakenCallback;
		mCamera = camera;
		mPreview = preview;
		Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Constructed.");
	}

	@Override
	public void run()
	{
		synchronized (mObject)
		{
			mPictureTakenCallback.pictureTakenCallback(mPreview.getPreview());
		}
	}
}
