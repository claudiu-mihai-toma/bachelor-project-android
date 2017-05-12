package bachelor.claudiu.interactiveinformationshare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;

/**
 * Created by claudiu on 12.05.2017.
 */

public class BitmapCameraPreviewCallback implements Camera.PreviewCallback
{
	private Bitmap      mBitmap;
	private Camera.Size mPreviewSize;

	public BitmapCameraPreviewCallback(Camera.Size previewSize)
	{
		mPreviewSize = previewSize;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{
		Utils.log(Constants.Classes.PREVIEW, "Preview callback with size " + data.length);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mPreviewSize.width, mPreviewSize.height,
				null);
		yuvImage.compressToJpeg(new Rect(0, 0, mPreviewSize.width, mPreviewSize.height), 50, out);
		byte[] imageBytes = out.toByteArray();

		mBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
	}

	public Bitmap getPreview()
	{
		return mBitmap;
	}
}
