package bachelor.claudiu.interactiveinformationshare;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by claudiu on 04.05.2017.
 */

public class ConnectionsManager
{
	private final Object mObject = new Object();

	private List<Connection> mConnections = new LinkedList<>();

	public void addConnection(Connection newConnection)
	{

		Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Adding connection... " + newConnection.getSocket().getInetAddress().getHostAddress());
		synchronized (mObject)
		{
			String newConnectionAddress = newConnection.getSocket().getInetAddress().getHostAddress();
			for (Connection connection : mConnections)
			{
				String connectionAddress = connection.getSocket().getInetAddress().getHostAddress();
				if (newConnectionAddress.equals(connectionAddress))
				{
					Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Connection already present.");
					return;
				}
			}

			mConnections.add(newConnection);
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
			List<Connection> brokenConnections = new LinkedList<>();
			for (Connection connection : mConnections)
			{
				if (connection.isBroken())
				{
					connection.clear();
					brokenConnections.add(connection);
					Utils.log(Constants.Classes.CONNECTIONS_MANAGER, "Removed some connection.");
				}
			}
			mConnections.removeAll(brokenConnections);
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
