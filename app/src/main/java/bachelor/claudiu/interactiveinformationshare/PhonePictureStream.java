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
	private static final int SOCKET_TIMEOUT = 500;
	public static final int PHONE_PICTURE_SOCKET_TIMEOUT = 3000;

	private Timer mBeaconTimer;
	private Timer mServerTimer;
	private ConnectionsManager mConnectionsManager = new ConnectionsManager();
	private ServerSocket mServerSocket;
	private ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();

	private class SocketAccepter extends TimerTask
	{
		private static final int SOCKET_ACCEPTER_PERIOD = 500;

		@Override
		public void run()
		{
			try
			{
				Socket socket = mServerSocket.accept();
				socket.setSoTimeout(PHONE_PICTURE_SOCKET_TIMEOUT);
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
		mServerSocket = new ServerSocket(Constants.Ports.PICTURE_STREAM_SERVER_PORT);
		mServerSocket.setSoTimeout(SOCKET_TIMEOUT);
	}

	public void open() throws SocketException, UnknownHostException
	{
		mServerTimer = new Timer();
		mServerTimer.schedule(new SocketAccepter(), 0, SocketAccepter.SOCKET_ACCEPTER_PERIOD);

		mBeaconTimer = new Timer();
		mBeaconTimer.schedule(new BroadcastBeaconTimerTask(Constants.Ports.PICTURE_STREAM_BEACON_PORT), 0,
				BroadcastBeaconTimerTask.BEACON_PERIOD);
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
