package bachelor.claudiu.interactiveinformationshare;

/**
 * Created by claudiu on 29.05.2017.
 */

public class Content
{
	public enum ContentType
	{
		TEXT(Constants.ContentTypeIDs.TEXT), IMAGE(Constants.ContentTypeIDs.IMAGE);

		ContentType(int id)
		{
			this.id = id;
		}

		private int id;

		public int getId()
		{
			return id;
		}
	}

	private byte[]      mData;
	private ContentType mType;
	private String      mTitle;

	public Content(ContentType type, String title, byte[] data)
	{
		mType = type;
		mData = data;
		mTitle = title;
	}

	public byte[] getData()
	{
		return mData;
	}

	public void setData(byte[] data)
	{
		mData = data;
	}

	public ContentType getType()
	{
		return mType;
	}

	public void setType(ContentType type)
	{
		mType = type;
	}

	public String getTitle()
	{
		return mTitle;
	}

	public void setTitle(String title)
	{
		mTitle = title;
	}
}
