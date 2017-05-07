package bachelor.claudiu.interactiveinformationshare;

/**
 * Created by claudiu on 07.05.2017.
 */

public class ConnectionReceiveInfo<T>
{
	public Connection mConnection;
	public T mData;

	public ConnectionReceiveInfo(Connection connection, T data)
	{
		mConnection = connection;
		mData = data;
	}
}
