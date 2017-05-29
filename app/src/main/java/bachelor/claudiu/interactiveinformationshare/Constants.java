package bachelor.claudiu.interactiveinformationshare;

/**
 * Created by claudiu on 06.05.2017.
 */

public class Constants
{
	public class Ports
	{
		public static final int PICTURE_STREAM_SERVER_PORT = 55001;
		public static final int PICTURE_STREAM_BEACON_PORT = 55002;
		public static final int CONTENT_RECEIVER_PORT      = 55004;
	}

	public class Classes
	{
		public static final String BROADCAST_BEACON_TIMER_TASK   = "BroadcastBeaconTimerTask";
		public static final String CAMERA_TIMER                  = "CameraTimer";
		public static final String CAMERA_TIMER_TASK             = "CameraTimerTask";
		public static final String CONNECTION                    = "Connection";
		public static final String CONNECTIONS_MANAGER           = "ConnectionsManager";
		public static final String INTERACTIVE_INFORMATION_SHARE = "InteractiveInformationShareActivity";
		public static final String PHONE_PICTURE_STREAM          = "PhonePictureStream";
		public static final String PREVIEW                       = "Preview";
		public static final String SEND_CONTENT_ASYNC_TASK       = "SendContentAsyncTask";
		public static final String UTILS                         = "Utils";
	}

	public class ContentTypeIDs
	{
		public static final int TEXT = 0;
		public static final int IMAGE = 1;
	}

	public class Charsets
	{
		public static final String UTF_8 = "UTF-8";
	}
}
