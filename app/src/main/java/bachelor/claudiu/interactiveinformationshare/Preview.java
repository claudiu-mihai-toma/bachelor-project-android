package bachelor.claudiu.interactiveinformationshare;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by claudiu on 12.05.2017.
 */

public class Preview implements SurfaceHolder.Callback
{

	private SurfaceView                 mSurfaceView;
	private SurfaceHolder               mHolder;
	private Camera                      mCamera;
	private Camera.Size                 mPreviewSize;
	private BitmapCameraPreviewCallback mPreviewCallback;

	public Preview(SurfaceView surfaceView)
	{
		Utils.log(Constants.Classes.PREVIEW, "Constructing...");

		mSurfaceView = surfaceView;

		if (mSurfaceView == null)
		{
			Utils.log(Constants.Classes.PREVIEW, "SurfaceView is NULL!");
		}

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		Utils.log(Constants.Classes.PREVIEW, "Constructed.");
	}

	public void setCamera(Camera camera)
	{
		Utils.log(Constants.Classes.PREVIEW, "Setting up camera...");
		if (mCamera == camera)
		{
			Utils.log(Constants.Classes.PREVIEW, "Already having this camera.");
			return;
		}

		stopPreviewAndFreeCamera();

		mCamera = camera;

		if (mCamera != null)
		{
			Utils.log(Constants.Classes.PREVIEW, "Non null camera...");

			setCameraParameters();

			mPreviewCallback = new BitmapCameraPreviewCallback(mPreviewSize);

			startPreview();

			Utils.log(Constants.Classes.PREVIEW, "Finished camera set.");
		}
	}

	private void setCameraParameters()
	{
		List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
		mPreviewSize = localSizes.get(localSizes.size() / 4);
		//mPreviewSize = localSizes.get(0);

		Camera.Parameters parameters = mCamera.getParameters();
		Utils.log(Constants.Classes.PREVIEW, "Get preview size " + parameters.getPreviewSize().width + "x" + parameters.getPreviewSize().height);
		Utils.log(Constants.Classes.PREVIEW, "Set preview size " + mPreviewSize.width + "x" + mPreviewSize.height);
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		mCamera.setParameters(parameters);
		mCamera.setDisplayOrientation(90);
		;
	}

	private void startPreview()
	{
		try
		{
			mCamera.setPreviewDisplay(mHolder);
		}
		catch (IOException e)
		{
			Utils.log(Constants.Classes.PREVIEW, "Something went wring when setting up holder");
			Utils.log(Constants.Classes.PREVIEW, e.toString());
			e.printStackTrace();
		}

		mCamera.setPreviewCallback(mPreviewCallback);

		// Important: Call startPreview() to start updating the preview
		// surface. Preview must be started before you can take a picture.
		Utils.log(Constants.Classes.PREVIEW, "Starting preview...");
		mCamera.startPreview();
		Utils.log(Constants.Classes.PREVIEW, "Preview started.");
	}

	/**
	 * When this function returns, mCamera will be null.
	 */
	private void stopPreviewAndFreeCamera()
	{
		if (mCamera != null)
		{
			// Call stopPreview() to stop updating the preview surface.
			mCamera.stopPreview();

			// Important: Call release() to release the camera for use by other
			// applications. Applications should release the camera immediately
			// during onPause() and re-open() it during onResume()).
			mCamera.release();

			mCamera = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Utils.log(Constants.Classes.PREVIEW, "surfaceCreated");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		Utils.log(Constants.Classes.PREVIEW, "surfaceChanged");

		startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Utils.log(Constants.Classes.PREVIEW, "surfaceDestroyed " + mCamera);
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null)
		{
			// Call stopPreview() to stop updating the preview surface.
			mCamera.stopPreview();
		}
	}

	public Bitmap getPreview()
	{
		return cropToFitSurfaceView(mPreviewCallback.getPreview());
	}

	private Bitmap cropToFitSurfaceView(Bitmap bitmap)
	{
		if (bitmap == null)
		{
			return null;
		}
		int heightPreview = bitmap.getHeight();
		int widthPreview = bitmap.getWidth();

		int heightSurface = mSurfaceView.getHeight();
		int widthSurface = mSurfaceView.getWidth();

		int deltaX = (widthPreview * heightSurface - widthSurface * heightPreview) / (heightSurface);
		int deltaY = (widthSurface * heightPreview - widthPreview * heightSurface) / (widthSurface);
		int halfDeltaX = deltaX / 2;
		int halfDeltaY = deltaY / 2;

		if (deltaX > 0)
		{
			bitmap = Bitmap.createBitmap(bitmap, halfDeltaX, 0, widthPreview - deltaX, heightPreview);
		}
		else
		{
			//This check is not redundant. Both deltas might be 0.
			if (deltaY > 0)
			{
				bitmap = Bitmap.createBitmap(bitmap, 0, halfDeltaY, widthPreview, heightPreview - deltaY);
			}
		}

		return bitmap;
	}

	public void clean()
	{
		Utils.log(Constants.Classes.PREVIEW, "Cleaning...");
		mCamera.setPreviewCallback(null);
		mHolder.removeCallback(this);
		stopPreviewAndFreeCamera();
		Utils.log(Constants.Classes.PREVIEW, "Cleaned.");
	}
}
