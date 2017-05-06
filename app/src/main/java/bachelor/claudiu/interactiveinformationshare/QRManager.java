package bachelor.claudiu.interactiveinformationshare;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;

/**
 * Created by claudiu on 06.05.2017.
 */

public class QRManager
{
	private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	public static final int QR_REQUEST_CODE = 0;

	private Activity mActivity;
	private Timer mTimer;

	public QRManager(Activity activity)
	{
		mActivity = activity;
	}

	public void startQRActivity() throws SocketException, UnknownHostException
	{
		mTimer = new Timer();
		mTimer.schedule(new BroadcastBeaconTimerTask(Constants.Ports.QR_BEACON_PORT), 0,
				BroadcastBeaconTimerTask.BEACON_PERIOD);

		Intent intent = new Intent(ACTION_SCAN);
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

		if (!Utils.isCallable(mActivity, intent))
		{
			throw new ActivityNotFoundException("QR scanner app not found!");
		}

		mActivity.startActivityForResult(intent, QR_REQUEST_CODE);
	}

	public void clean()
	{
		Utils.stopTimer(mTimer);
	}
}
