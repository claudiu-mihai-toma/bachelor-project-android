package bachelor.claudiu.interactiveinformationshare;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.TimerTask;

/**
 * Created by claudiu on 23.04.2017.
 */

public class BroadcastBeaconTimerTask extends TimerTask {

    private static final int BEACON_PORT = 9751;
    private static final String BEACON_MESSAGE = "interactive_information_share";
    private static final String DEFAULT_BROADCAST_ADDRESS = "255.255.255.255";
    private DatagramSocket mDatagramSocket;

    public BroadcastBeaconTimerTask() throws SocketException, UnknownHostException {
        mDatagramSocket = new DatagramSocket();
        mDatagramSocket.setBroadcast(true);
    }

    @Override
    public void run() {
        Log.d(InteractiveInformationShareActivity.LOGS, "Broadcasting...");
        try {
            try {
                DatagramPacket sendPacket = new DatagramPacket(
                        BEACON_MESSAGE.getBytes(),
                        BEACON_MESSAGE.length(),
                        InetAddress.getByName(DEFAULT_BROADCAST_ADDRESS),
                        BEACON_PORT);
                mDatagramSocket.send(sendPacket);
            } catch (Exception e) {
            }

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(
                                BEACON_MESSAGE.getBytes(),
                                BEACON_MESSAGE.length(),
                                broadcast,
                                BEACON_PORT);
                        mDatagramSocket.send(sendPacket);

                        Log.d(InteractiveInformationShareActivity.LOGS, "Sending broadcast to: " + broadcast);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
