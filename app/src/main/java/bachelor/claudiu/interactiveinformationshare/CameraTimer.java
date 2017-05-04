package bachelor.claudiu.interactiveinformationshare;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import static bachelor.claudiu.interactiveinformationshare.InteractiveInformationShareActivity.LOGS;

/**
 * Created by claudiu on 04.05.2017.
 */

public class CameraTimer
{
	private static final int CAMERA_PERIOD = 500;

	private Timer mTimer;
	private Camera mCamera;
	private PictureTakenCallback mPictureTakenCallback;

	public CameraTimer(PictureTakenCallback pictureTakenCallback)
	{
		mPictureTakenCallback = pictureTakenCallback;
		mTimer = new Timer();
		mCamera = Utils.getFrontCameraInstance();
		if (mCamera == null)
		{
			throw new RuntimeException("Camera is not available (in use or does not exist)!");
		}

		setCameraParameters();
	}

	public void schedule()
	{
		mTimer.schedule(new CameraTimerTask(mPictureTakenCallback, mCamera), 0, CAMERA_PERIOD);
	}


	private void setCameraParameters()
	{
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

	public void cancel()
	{
		Utils.stopTimer(mTimer);
		Log.d(LOGS, "Releasing camera...");
		mCamera.release();
		Log.d(LOGS, "Camera released.");
		mCamera = null;
	}
}
