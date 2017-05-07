package bachelor.claudiu.interactiveinformationshare;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by claudiu on 04.05.2017.
 */

public class ConnectionsManager
{
	private Object mObject = new Object();

	private List<Connection> mConnections = new LinkedList<>();

	public void addConnection(Connection connection)
	{

		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Adding connection...");
		synchronized (mObject)
		{
			mConnections.add(connection);
		}
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Connection added.");
	}

	public void send(byte[] data)
	{
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Sending data of length: " + data.length);
		synchronized (mObject)
		{
			for (Connection connection : mConnections)
			{
				if (!connection.isBroken())
				{
					try
					{
						//TODO: Run on individual threads!
						Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Sending data to some connection...");
						connection.getOutputStream().writeInt(data.length);
						connection.getOutputStream().write(data);
						Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Sent data to some connection.");
					}
					catch (IOException e)
					{
						connection.setBroken();
					}
				}
			}
		}
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Finished sending data of length: " + data.length);

		cleanConnections();
	}

	private void cleanConnections()
	{
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Cleaning connections...");
		synchronized (mObject)
		{
			for (Iterator<Connection> iterator = mConnections.iterator(); iterator.hasNext(); )
			{
				Connection connection = iterator.next();

				if (connection.isBroken())
				{
					connection.clear();
					mConnections.remove(connection);
					Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Removed some connection.");
				}
			}
		}
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Finished cleaning connections.");
	}

	public void closeConnections()
	{
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Closing all connections...");
		synchronized (mObject)
		{
			for (Connection connection : mConnections)
			{
				connection.setBroken();
			}
		}

		cleanConnections();
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "All connections closed.");
	}
}
