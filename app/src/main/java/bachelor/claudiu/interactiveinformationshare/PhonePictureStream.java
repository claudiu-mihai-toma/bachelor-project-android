package bachelor.claudiu.interactiveinformationshare;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by claudiu on 04.05.2017.
 */

public class PhonePictureStream
{
	public static int IMAGE_STREAM_SERVER_PORT = 55001;
	public static int IMAGE_STREAM_CLIENT_PORT = 55002;
	private static final int SOCKET_TIMEOUT = 500;

	private Timer mBeaconTimer = new Timer();
	private Timer mServerTimer = new Timer();
	private ConnectionsManager mConnectionsManager = new ConnectionsManager();
	private ServerSocket mServerSocket;
	ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();

	private class SocketAccepter extends TimerTask
	{
		private static final int SOCKET_ACCEPTER_PERIOD = 500;

		@Override
		public void run()
		{
			try
			{
				Socket socket = mServerSocket.accept();
				mConnectionsManager.addConnection(new Connection(socket));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public PhonePictureStream() throws IOException
	{
		mServerSocket = new ServerSocket(IMAGE_STREAM_SERVER_PORT);
		mServerSocket.setSoTimeout(SOCKET_TIMEOUT);
	}

	public void open() throws SocketException, UnknownHostException
	{
		mServerTimer.schedule(new SocketAccepter(), 0, SocketAccepter.SOCKET_ACCEPTER_PERIOD);
		mBeaconTimer.schedule(new BroadcastBeaconTimerTask(IMAGE_STREAM_CLIENT_PORT), 0, BroadcastBeaconTimerTask.BEACON_PERIOD);
	}

	public void send(Bitmap picture)
	{
		picture.compress(Bitmap.CompressFormat.JPEG, 100, mByteArrayOutputStream);
		byte[] data = mByteArrayOutputStream.toByteArray();

		mConnectionsManager.send(data);
	}

	public void cancel()
	{
		Utils.stopTimer(mBeaconTimer);
		Utils.stopTimer(mServerTimer);
		mConnectionsManager.closeConnections();
		try
		{
			mServerSocket.close();
		}
		catch (IOException e)
		{
		}
		try
		{
			mByteArrayOutputStream.close();
		}
		catch (IOException e)
		{
		}
	}
}
