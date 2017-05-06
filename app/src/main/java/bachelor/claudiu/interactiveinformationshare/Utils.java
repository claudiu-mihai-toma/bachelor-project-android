package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.Log;

import java.util.List;
import java.util.Timer;

import static bachelor.claudiu.interactiveinformationshare.InteractiveInformationShareActivity.LOGS;

/**
 * Created by claudiu on 04.05.2017.
 */

public class Utils
{
	public static void stopTimer(Timer timer)
	{
		if (timer != null)
		{
			timer.cancel();
			timer.purge();
		}
	}

	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}


	public static Camera getFrontCameraInstance()
	{
		return getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
	}

	public static Camera getBackCameraInstance()
	{
		return getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
	}

	/**
	 * A safe way to get an instance of the Camera object.
	 */
	public static Camera getCameraInstance(int cameraIndex)
	{
		Camera c = null;
		try
		{
			Log.d(LOGS, "Getting camera instance.");
			// attempt to get a Camera instance
			c = Camera.open(cameraIndex);
			Log.d(LOGS, "GOT camera instance.");
		}
		catch (Exception e)
		{
			// Camera is not available (in use or does not exist)
			Log.d(LOGS, e.toString());
		}
		return c; // returns null if camera is unavailable
	}

	public static boolean isCallable(Activity activity, Intent intent)
	{
		List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
}
