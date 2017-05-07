package bachelor.claudiu.interactiveinformationshare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by claudiu on 04.05.2017.
 */

public class Connection
{
	private boolean          mIsBroken;
	private Socket           mSocket;
	private DataOutputStream mOutputStream;
	private DataInputStream  mInputStream;

	public Connection(Socket socket) throws IOException
	{
		Utils.log(Constants.Classes.CONNECTION, "Creating...");
		mIsBroken = false;
		mSocket = socket;
		mOutputStream = new DataOutputStream(mSocket.getOutputStream());
		mInputStream = new DataInputStream(mSocket.getInputStream());
		Utils.log(Constants.Classes.CONNECTION, "Created.");
	}

	public void clear()
	{
		Utils.log(Constants.Classes.CONNECTION, "Clearing...");
		try
		{
			mSocket.close();
		}
		catch (IOException e)
		{
			Utils.log(Constants.Classes.CONNECTION, "!!! Failed to close socket !!!");
		}
		try
		{
			mOutputStream.close();
		}
		catch (IOException e)
		{
			Utils.log(Constants.Classes.CONNECTION, "!!! Failed to close output stream !!!");
		}
		try
		{
			mInputStream.close();
		}
		catch (IOException e)
		{
			Utils.log(Constants.Classes.CONNECTION, "!!! Failed to close input stream !!!");
		}
		Utils.log(Constants.Classes.CONNECTION, "Cleared.");
	}

	public DataOutputStream getOutputStream()
	{
		return mOutputStream;
	}

	public DataInputStream getInputStream()
	{
		return mInputStream;
	}

	public Socket getSocket()
	{
		return mSocket;
	}

	public void setBroken()
	{
		mIsBroken = true;
	}

	public boolean isBroken()
	{
		return mIsBroken;
	}
}
