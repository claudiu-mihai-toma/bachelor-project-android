package bachelor.claudiu.interactiveinformationshare;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static bachelor.claudiu.interactiveinformationshare.Utils.stopScheduledExecutorService;

/**
 * Created by claudiu on 04.05.2017.
 */

public class PhonePictureStream
{
	public static final  int DATA_THRESHOLD               = 50;

	private ScheduledExecutorService mBeaconTimer;
	// TODO: Make this a thread that cycles until interrupted.
	private ScheduledExecutorService mServerTimer;
	private ConnectionsManager mConnectionsManager = new ConnectionsManager();
	private ServerSocket mServerSocket;
	private ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();

	private class SocketAccepter implements Runnable
	{
		private static final int SOCKET_ACCEPTER_PERIOD = 500;

		@Override
		public void run()
		{
			try
			{
				//Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Accepting socket...");
				Socket socket = mServerSocket.accept();
				socket.setSoTimeout(Constants.Timeouts.PHONE_PICTURE_SOCKET_TIMEOUT);
				mConnectionsManager.addConnection(new Connection(socket));
				Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Socket accepted.");
			}
			catch (IOException e)
			{
				//Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "No socket accepted.");
			}
		}
	}

	public PhonePictureStream() throws IOException
	{
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Creating...");
		mServerSocket = new ServerSocket(Constants.Ports.PICTURE_STREAM_SERVER_PORT);
		mServerSocket.setSoTimeout(Constants.Timeouts.SOCKET_TIMEOUT);
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Created.");
	}

	public void open() throws SocketException
	{
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Opening...");
		mServerTimer = Executors.newScheduledThreadPool(1);
		mServerTimer.scheduleWithFixedDelay(new SocketAccepter(), 0, SocketAccepter.SOCKET_ACCEPTER_PERIOD, TimeUnit.MILLISECONDS);

		mBeaconTimer = Executors.newScheduledThreadPool(1);
		mBeaconTimer.scheduleWithFixedDelay(new BroadcastBeaconTimerTask(Constants.Ports.PICTURE_STREAM_BEACON_PORT), 0, BroadcastBeaconTimerTask.BEACON_PERIOD, TimeUnit.MILLISECONDS);
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Opened");
	}

	public void send(Bitmap picture)
	{
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Sending...");
		picture.compress(Bitmap.CompressFormat.JPEG, 100, mByteArrayOutputStream);
		byte[] data = mByteArrayOutputStream.toByteArray();
		mByteArrayOutputStream.reset();

		mConnectionsManager.send(data);
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Sent.");
	}

	public String receive()
	{
		String result = null;
		int bestData = 0;
		List<ConnectionReceiveInfo<Integer>> connectionReceiveInfos = mConnectionsManager.receiveInt();

		for (ConnectionReceiveInfo<Integer> connectionReceiveInfo : connectionReceiveInfos)
		{
			int data = connectionReceiveInfo.mData;
			if (data > DATA_THRESHOLD && data > bestData)
			{
				bestData = data;
				result = connectionReceiveInfo.mConnection.getSocket().getInetAddress().getHostAddress();
			}
		}

		return result;
	}

	public void cancel()
	{
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Cancelling...");
		stopScheduledExecutorService(mBeaconTimer);
		stopScheduledExecutorService(mServerTimer);
		try
		{
			mServerSocket.close();
		}
		catch (IOException e)
		{
			Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "!!! Failed to close server !!!");
		}
		try
		{
			mByteArrayOutputStream.close();
		}
		catch (IOException e)
		{
			Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "!!! Failed to close output stream !!!");
		}
		mConnectionsManager.closeConnections();
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Cancelled.");
	}
}
