package bachelor.claudiu.interactiveinformationshare;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by claudiu on 04.05.2017.
 */
class SendContentAsyncTask extends AsyncTask<Void, Void, Void>
{
	private ContentSentCallback mContentSentCallback;
	private String              mDesktopAddress;
	private Content              mContent;

	public SendContentAsyncTask(ContentSentCallback contentSentCallback, String desktopAddress, Content content)
	{
		Utils.log(Constants.Classes.SEND_CONTENT_ASYNC_TASK, "Constructing...");
		mContentSentCallback = contentSentCallback;
		mDesktopAddress = desktopAddress;
		mContent = content;
		Utils.log(Constants.Classes.SEND_CONTENT_ASYNC_TASK, "Constructed.");
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
			Utils.log(Constants.Classes.SEND_CONTENT_ASYNC_TASK, "Sending data " + mContent + " to " +
					mDesktopAddress);

			Utils.log(Constants.Classes.SEND_CONTENT_ASYNC_TASK, "Creating socket...");
			Socket socket = new Socket(mDesktopAddress, Constants.Ports.CONTENT_RECEIVER_PORT);
			Utils.log(Constants.Classes.SEND_CONTENT_ASYNC_TASK, "Socket created!");

			DataOutputStream os = new DataOutputStream(socket.getOutputStream());

			os.writeInt(mContent.getType().getId());
			os.writeUTF(mContent.getTitle());
			byte[] data = mContent.getData();
			if (data != null)
			{
				os.writeInt(data.length);
				os.write(data);
			}

			os.close();
			socket.close();

			Utils.log(Constants.Classes.SEND_CONTENT_ASYNC_TASK, "Send succeeded!");
		}
		catch (IOException e)
		{
			Utils.log(Constants.Classes.SEND_CONTENT_ASYNC_TASK, "Sending failed!");
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void aVoid)
	{
		Utils.log(Constants.Classes.SEND_CONTENT_ASYNC_TASK, "Calling content sent callback...");
		mContentSentCallback.contentSentCallback();
		Utils.log(Constants.Classes.SEND_CONTENT_ASYNC_TASK, "Called content sent callback.");
	}
}
