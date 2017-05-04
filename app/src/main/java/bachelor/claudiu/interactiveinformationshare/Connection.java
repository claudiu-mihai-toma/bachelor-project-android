package bachelor.claudiu.interactiveinformationshare;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by claudiu on 04.05.2017.
 */

public class Connection
{
	private boolean mIsBroken;
	private Socket mSocket;
	private DataOutputStream mOutputStream;

	public Connection(Socket socket) throws IOException
	{
		mIsBroken = false;
		mSocket = socket;
		mOutputStream = new DataOutputStream(mSocket.getOutputStream());
	}

	public void clear()
	{
		try
		{
			mSocket.close();
		}
		catch (IOException e)
		{
		}
		try
		{
			mOutputStream.close();
		}
		catch (IOException e)
		{
		}
	}

	public DataOutputStream getOutputStream()
	{
		return mOutputStream;
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
