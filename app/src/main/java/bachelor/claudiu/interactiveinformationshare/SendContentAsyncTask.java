package bachelor.claudiu.interactiveinformationshare;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static bachelor.claudiu.interactiveinformationshare.InteractiveInformationShareActivity.LOGS;

/**
 * Created by claudiu on 04.05.2017.
 */
class SendContentAsyncTask extends AsyncTask<Void, Void, Void>
{
	private ContentSentCallback mContentSentCallback;
	private String mDesktopAddress;
	private String mContent;

	public SendContentAsyncTask(ContentSentCallback contentSentCallback, String desktopAddress, String content)
	{
		mContentSentCallback = contentSentCallback;
		mDesktopAddress = desktopAddress;
		mContent = content;
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
			Log.d(LOGS, "Sending data " + mContent + " to " +
					mDesktopAddress);

			Log.d(LOGS, "Creating socket...");
			Socket socket = new Socket(mDesktopAddress, Constants.Ports.CONTENT_RECEIVER_PORT);
			Log.d(LOGS, "Socket created!");

			DataOutputStream os = new DataOutputStream(socket.getOutputStream());

			os.writeUTF(mContent);

			os.close();
			socket.close();

		}
		catch (IOException e)
		{
			Log.d(LOGS, "Sending failed!");
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void aVoid)
	{
		mContentSentCallback.contentSentCallback();
	}
}
