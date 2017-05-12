package bachelor.claudiu.interactiveinformationshare;

import android.hardware.Camera;
import android.view.SurfaceView;

import java.util.Timer;

/**
 * Created by claudiu on 04.05.2017.
 */

public class CameraTimer
{
	private static final int CAMERA_PERIOD = 200;

	private Timer                mTimer;
	private Camera               mCamera;
	private PictureTakenCallback mPictureTakenCallback;
	private SurfaceView          mSurfaceView;
	private Preview              mPreview;

	public CameraTimer(SurfaceView surfaceView, PictureTakenCallback pictureTakenCallback)
	{
		Utils.log(Constants.Classes.CAMERA_TIMER, "Constructing...");

		mSurfaceView = surfaceView;
		mPictureTakenCallback = pictureTakenCallback;

		if (InteractiveInformationShareActivity.USE_BACK_CAMERA)
		{
			mCamera = Utils.getBackCameraInstance();
		}
		else
		{
			mCamera = Utils.getFrontCameraInstance();
		}

		if (mCamera == null)
		{
			throw new RuntimeException("Camera is not available (in use or does not exist)!");
		}

		mPreview = new Preview(mSurfaceView);

		Utils.log(Constants.Classes.CAMERA_TIMER, "Constructed.");
	}

	public void schedule()
	{
		mPreview.setCamera(mCamera);
		Utils.log(Constants.Classes.CAMERA_TIMER, "Scheduling...");
		mTimer = new Timer();
		mTimer.schedule(new CameraTimerTask(mPictureTakenCallback, mPreview), 0, CAMERA_PERIOD);
		Utils.log(Constants.Classes.CAMERA_TIMER, "Scheduled.");
	}

	public void cancel()
	{
		Utils.log(Constants.Classes.CAMERA_TIMER, "Cancelling...");
		Utils.stopTimer(mTimer);
		Utils.log(Constants.Classes.CAMERA_TIMER, "Releasing camera...");
		mCamera.release();
		Utils.log(Constants.Classes.CAMERA_TIMER, "Camera released.");
		mCamera = null;
		Utils.log(Constants.Classes.CAMERA_TIMER, "Cancelled.");
	}
}
