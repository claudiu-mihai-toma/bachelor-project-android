package bachelor.claudiu.interactiveinformationshare;

import java.util.Timer;

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
}
