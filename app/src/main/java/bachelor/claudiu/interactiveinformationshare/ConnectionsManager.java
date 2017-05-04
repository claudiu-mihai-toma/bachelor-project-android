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
		synchronized (mObject)
		{
			mConnections.add(connection);
		}
	}

	public void send(byte[] data)
	{
		synchronized (mObject)
		{
			for (Connection connection : mConnections)
			{
				if (!connection.isBroken())
				{
					try
					{
						//TODO: Run on individual threads!
						connection.getOutputStream().write(data);
					}
					catch (IOException e)
					{
						connection.setBroken();
					}
				}
			}
		}

		cleanConnections();
	}

	private void cleanConnections()
	{
		synchronized (mObject)
		{
			for (Iterator<Connection> iterator = mConnections.iterator(); iterator.hasNext(); )
			{
				Connection connection = iterator.next();

				if (connection.isBroken())
				{
					connection.clear();
					mConnections.remove(connection);
				}
			}
		}
	}

	public void closeConnections()
	{
		synchronized (mObject)
		{
			for (Connection connection : mConnections)
			{
				connection.setBroken();
			}
		}

		cleanConnections();
	}
}
