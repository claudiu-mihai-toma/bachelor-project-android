package bachelor.claudiu.interactiveinformationshare;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by claudiu on 23.04.2017.
 */

public class BroadcastBeaconTimerTask implements Runnable
{

	private static final String BEACON_MESSAGE            = "interactive_information_share";
	private static final String DEFAULT_BROADCAST_ADDRESS = "255.255.255.255";
	static final         int    BEACON_PERIOD             = 100;
	private DatagramSocket mDatagramSocket;
	private int            mPort;

	public BroadcastBeaconTimerTask(int port) throws SocketException
	{
		Utils.log(Constants.Classes.BROADCAST_BEACON_TIMER_TASK, "Constructing...");
		mPort = port;
		mDatagramSocket = new DatagramSocket();
		mDatagramSocket.setBroadcast(true);
		Utils.log(Constants.Classes.BROADCAST_BEACON_TIMER_TASK, "Constructed.");
	}

	@Override
	public void run()
	{
		//Log.d(InteractiveInformationShareActivity.LOGS, "Broadcasting...");
		//Utils.log(Constants.Classes.BROADCAST_BEACON_TIMER_TASK, "Broadcasting...");
		try
		{
			try
			{
				DatagramPacket sendPacket = new DatagramPacket(BEACON_MESSAGE.getBytes(), BEACON_MESSAGE.length(), InetAddress.getByName(DEFAULT_BROADCAST_ADDRESS), mPort);
				mDatagramSocket.send(sendPacket);
			}
			catch (Exception e)
			{
			}

			// Broadcast the message over all the network interfaces
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface networkInterface = interfaces.nextElement();

				if (networkInterface.isLoopback() || !networkInterface.isUp())
				{
					continue; // Don't want to broadcast to the loopback interface
				}

				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
				{
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null)
					{
						continue;
					}

					// Send the broadcast package!
					try
					{
						DatagramPacket sendPacket = new DatagramPacket(BEACON_MESSAGE.getBytes(), BEACON_MESSAGE.length(), broadcast, mPort);
						mDatagramSocket.send(sendPacket);

						//Log.d(InteractiveInformationShareActivity.LOGS, "Sending broadcast to: " + broadcast);
						//Utils.log(Constants.Classes.BROADCAST_BEACON_TIMER_TASK, "Sending broadcast to: " +
						// broadcast);
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		catch (IOException e)
		{
		}
	}
}
