package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

	public static void stopScheduledExecutorService(ScheduledExecutorService scheduledExecutorService)
	{
		scheduledExecutorService.shutdown();
		try
		{
			scheduledExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		}
		catch (InterruptedException e)
		{
			//e.printStackTrace();
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
			Utils.log(Constants.Classes.UTILS, "Getting camera instance.");
			// attempt to get a Camera instance
			c = Camera.open(cameraIndex);
			Utils.log(Constants.Classes.UTILS, "GOT camera instance.");
		}
		catch (Exception e)
		{
			// Camera is not available (in use or does not exist)
			Utils.log(Constants.Classes.UTILS, e.toString());
		}
		return c; // returns null if camera is unavailable
	}

	public static boolean isCallable(Activity activity, Intent intent)
	{
		List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public static void log(String domain, String message)
	{
		String logMessage = "[" + domain + "] -> " + message + "\n";
		Log.d(LOGS, logMessage);
	}

	public static String scanQRImage(Bitmap bMap)
	{
		log(Constants.Classes.UTILS, "Scanning picture for QR...");
		String contents = null;

		int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
		//copy pixel data from the Bitmap into the 'intArray' array
		bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

		LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		MultiFormatReader reader = new MultiFormatReader();
		try
		{
			Result result = reader.decode(bitmap);
			contents = result.getText();
			log(Constants.Classes.UTILS, "Scan finished.");
		}
		catch (Exception e)
		{
			log(Constants.Classes.UTILS, "Error decoding barcode.");
			log(Constants.Classes.UTILS, e.toString());
		}
		return contents;
	}

	public static String convertStreamToString(InputStream is) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = reader.readLine();
		sb.append(line);
		while ((line = reader.readLine()) != null)
		{
			sb.append("\n").append(line);
		}
		reader.close();
		return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws Exception
	{
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		fin.close();
		return ret;
	}

	public static String getRealPathFromURI(Context context, Uri contentUri)
	{
		Cursor cursor = null;
		try
		{
			String[] proj = {MediaStore.Images.Media.DATA};
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			if (cursor == null)
			{
				return contentUri.getPath();
			}
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String realPath = cursor.getString(column_index);
			return realPath != null ? realPath : contentUri.getPath();
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}

	public static String getFileName(String path)
	{
		return path.substring(path.lastIndexOf("/") + 1);
	}
}
