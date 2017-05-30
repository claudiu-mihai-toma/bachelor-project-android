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
	private final Object mObject = new Object();

	private List<Connection> mConnections = new LinkedList<>();

	public void addConnection(Connection connection)
	{

		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Adding connection...");
		synchronized (mObject)
		{
			mConnections.add(connection);
			Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "number of connections: " + mConnections.size());
		}
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Connection added.");
	}

	public void send(final byte[] data)
	{
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Sending data of length: " + data.length);
		synchronized (mObject)
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
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

						mObject.notify();
					}
				}
			});

			thread.start();

			try
			{
				mObject.wait();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			try
			{
				thread.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Finished sending data of length: " + data.length);

		cleanConnections();
	}


	public List<ConnectionReceiveInfo<Integer>> receiveInt()
	{
		final List<ConnectionReceiveInfo<Integer>> result = new LinkedList<>();
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Receiving data...");
		synchronized (mObject)
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
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
									Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Receiving data from some " + "connection...");

									int data = connection.getInputStream().readInt();
									ConnectionReceiveInfo<Integer> connectionReceiveInfo = new ConnectionReceiveInfo<>(connection, data);
									result.add(connectionReceiveInfo);

									Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Received data from some " + "connection.");
								}
								catch (IOException e)
								{
									connection.setBroken();
								}
							}
						}

						mObject.notify();
					}
				}
			});

			thread.start();

			try
			{
				mObject.wait();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			try
			{
				thread.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Finished receiving data.");

		cleanConnections();

		return result;
	}

	private void cleanConnections()
	{
		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Cleaning connections...");
		synchronized (mObject)
		{
			for (Iterator<Connection> iterator = mConnections.iterator(); iterator.hasNext(); )
			{
				Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "before get next");
				Connection connection = iterator.next();
				Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "after get next");

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
