package bachelor.claudiu.interactiveinformationshare;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

/**
 * Created by claudiu on 04.05.2017.
 */

public class CameraTimer
{
	private static final int CAMERA_PERIOD = 1;

	private Timer                mTimer;
	private Camera               mCamera;
	private PictureTakenCallback mPictureTakenCallback;
	private Context              mContext;
	private SurfaceView          mSurfaceView;
	private Preview              mPreview;

	public CameraTimer(Context context, SurfaceView surfaceView, PictureTakenCallback pictureTakenCallback)
	{
		Utils.log(Constants.Classes.CAMERA_TIMER, "Constructing...");

		mContext = context;
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

		//setCameraParameters();

		Utils.log(Constants.Classes.CAMERA_TIMER, "Constructed.");
	}

	public void schedule()
	{
		mPreview.setCamera(mCamera);
		Utils.log(Constants.Classes.CAMERA_TIMER, "Scheduling...");
		mTimer = new Timer();
		mTimer.schedule(new CameraTimerTask(mPictureTakenCallback, mCamera, mPreview), 0, CAMERA_PERIOD);
		Utils.log(Constants.Classes.CAMERA_TIMER, "Scheduled.");
	}

	private void setCameraParameters()
	{
		Utils.log(Constants.Classes.CAMERA_TIMER, "Setting parameters...");
		try
		{
			// TODO: Try setting a preview texture before every picture taken.
			mCamera.setPreviewTexture(new SurfaceTexture(10));
		}
		catch (IOException e)
		{
			Utils.log(Constants.Classes.CAMERA_TIMER, "!!! Failed to set preview texture !!!");
		}
		//mCamera.startPreview();
		Camera.Parameters params = mCamera.getParameters();
		//params.setPreviewSize(640, 480);
		params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		params.setPictureFormat(ImageFormat.JPEG);
		List<Camera.Size> sizes = params.getSupportedPictureSizes();
		Camera.Size smallSize = sizes.get(sizes.size() - 1);
		params.setPictureSize(smallSize.width, smallSize.height);

		if (true)
		{
			params.setPreviewSize(smallSize.width, smallSize.height);

			//set color efects to none
			params.setColorEffect(Camera.Parameters.EFFECT_NONE);

			//set antibanding to none
			if (params.getAntibanding() != null)
			{
				params.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
			}

			// set white ballance
			if (params.getWhiteBalance() != null)
			{
				params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
			}

			//set flash
			if (params.getFlashMode() != null)
			{
				params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			}

			//set zoom
			if (params.isZoomSupported())
			{
				params.setZoom(0);
			}

			//set focus mode
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
		}

		mCamera.setParameters(params);
		Utils.log(Constants.Classes.CAMERA_TIMER, "Parameters set.");
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
