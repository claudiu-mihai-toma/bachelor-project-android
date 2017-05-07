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
	private static final int SOCKET_TIMEOUT               = 500;
	public static final  int PHONE_PICTURE_SOCKET_TIMEOUT = 3000;

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
				//Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Accepting socket...");
				Socket socket = mServerSocket.accept();
				socket.setSoTimeout(PHONE_PICTURE_SOCKET_TIMEOUT);
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
		mServerSocket.setSoTimeout(SOCKET_TIMEOUT);
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Created.");
	}

	public void open() throws SocketException, UnknownHostException
	{
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Opening...");
		mServerTimer = new Timer();
		mServerTimer.schedule(new SocketAccepter(), 0, SocketAccepter.SOCKET_ACCEPTER_PERIOD);

		mBeaconTimer = new Timer();
		mBeaconTimer.schedule(new BroadcastBeaconTimerTask(Constants.Ports.PICTURE_STREAM_BEACON_PORT), 0,
				BroadcastBeaconTimerTask.BEACON_PERIOD);
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

	public void cancel()
	{
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Cancelling...");
		Utils.stopTimer(mBeaconTimer);
		Utils.stopTimer(mServerTimer);
		mConnectionsManager.closeConnections();
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
		Utils.log(Constants.Classes.PHONE_PICTURE_STREAM, "Cancelled.");
	}
}
