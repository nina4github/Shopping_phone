package em.twitterido.aw;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class Offer {

	User sharedByUser;
	String originalImageUrl;
	String content;
	Drawable imageFile;
	String published;
	int id;
	String[] tags;

	public Offer() {
		super();
	}

	public User getSharedByUser() {
		return sharedByUser;
	}

	public void setSharedByUser(User sharedByUser) {
		this.sharedByUser = sharedByUser;
	}

	public String getOriginalImageUrl() {
		return originalImageUrl;
	}

	public void setOriginalImageUrl(String originalImageUrl) {
		this.originalImageUrl = originalImageUrl;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Drawable getImageFile() {
		return imageFile;
	}

	public void setImageFile(Drawable imageFile) {
		this.imageFile = imageFile;
	}

	public String getPublished() {
		Log.d("foo", "published: "+ this.published);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss'Z'");
		Date date;
		try {
			date = formatter.parse(this.published);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
		SimpleDateFormat newFormat = new SimpleDateFormat("DD/MM-HH:mm");
		return newFormat.format(date);
	}

	public void setPublished(String published) {
		this.published = published;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	
}
