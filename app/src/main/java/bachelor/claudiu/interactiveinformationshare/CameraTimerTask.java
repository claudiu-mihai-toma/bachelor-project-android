package bachelor.claudiu.interactiveinformationshare;

/**
 * Created by claudiu on 04.05.2017.
 */

public class CameraTimerTask implements Runnable
{
	private Object mObject = new Object();

	private PictureTakenCallback mPictureTakenCallback;
	private Preview              mPreview;

	public CameraTimerTask(PictureTakenCallback pictureTakenCallback, Preview preview)
	{
		Utils.log(Constants.Classes.CAMERA_TIMER_TASK, "Constructing...");
		mPictureTakenCallback = pictureTakenCallback;
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
