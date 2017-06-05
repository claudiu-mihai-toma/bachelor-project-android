package bachelor.claudiu.interactiveinformationshare;

import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by claudiu on 30.05.2017.
 */
class ReceiveContentAsyncTask extends AsyncTask<Void, Void, Void>
{
	private ContentReceivedCallback mContentReceivedCallback;
	private String                  mDesktopAddress;
	private Content                 mContent;

	public ReceiveContentAsyncTask(ContentReceivedCallback contentReceivedCallback, String desktopAddress)
	{
		Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Constructing...");
		mContentReceivedCallback = contentReceivedCallback;
		mDesktopAddress = desktopAddress;
		mContent = null;
		Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Constructed.");
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
			Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Receiving data from " + mDesktopAddress);

			Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Creating socket...");
			Socket socket = new Socket(mDesktopAddress, Constants.Ports.CONTENT_SENDER_PORT);
			socket.setSoTimeout(Constants.Timeouts.SOCKET_TIMEOUT);
			Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Socket created!");

			DataInputStream is = new DataInputStream(socket.getInputStream());

			int contentTypeId = is.readInt();
			String contentTitle = is.readUTF();
			byte[] contentData = null;
			Content.ContentType contentType = null;

			switch (contentTypeId)
			{
				case Constants.ContentTypeIDs.TEXT:
					contentType = Content.ContentType.TEXT;
					break;
				case Constants.ContentTypeIDs.IMAGE:
					contentType = Content.ContentType.IMAGE;
					int contentSize = is.readInt();
					contentData = new byte[contentSize];
					is.readFully(contentData);
					break;
			}

			mContent = new Content(contentType, contentTitle, contentData);

			is.close();
			socket.close();

			Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Receive succeeded!");
			Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Content: " + mContent);
			Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Content type: " + mContent.getType());
			Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Content title: " + mContent.getTitle());
			Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Content data: " + mContent.getData());
		}
		catch (IOException e)
		{
			Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Receive failed!");
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void aVoid)
	{
		Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Calling content received callback...");
		Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Content: " + mContent);
		mContentReceivedCallback.contentReceivedCallback(mContent);
		Utils.log(Constants.Classes.RECEIVE_CONTENT_ASYNC_TASK, "Called content received callback.");
	}
}
