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
	private static final String ACTION_SCAN     = "com.google.zxing.client.android.SCAN";
	public static final  int    QR_REQUEST_CODE = 0;

	private Activity mActivity;
	private Timer    mTimer;

	public QRManager(Activity activity)
	{
		Utils.log(Constants.Classes.QRMANAGER, "Constructing...");
		mActivity = activity;
		Utils.log(Constants.Classes.QRMANAGER, "Constructed.");
	}

	public void startQRActivity() throws SocketException, UnknownHostException
	{
		Utils.log(Constants.Classes.QRMANAGER, "Starting QR activity...");
		mTimer = new Timer();
		mTimer.schedule(new BroadcastBeaconTimerTask(Constants.Ports.QR_BEACON_PORT), 0,
				BroadcastBeaconTimerTask.BEACON_PERIOD);

		Intent intent = new Intent(ACTION_SCAN);
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

		if (!Utils.isCallable(mActivity, intent))
		{
			Utils.log(Constants.Classes.QRMANAGER, "!!! No QR activity found !!!");
			throw new ActivityNotFoundException("QR scanner app not found!");
		}

		mActivity.startActivityForResult(intent, QR_REQUEST_CODE);
		Utils.log(Constants.Classes.QRMANAGER, "QR activity started.");
	}

	public void clean()
	{
		Utils.stopTimer(mTimer);
	}
}
