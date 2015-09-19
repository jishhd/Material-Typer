package hd.josh.typer.helpers.post;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Post implements Serializable {

    private long mId = -1;
    private String mTitle;
    private String mDate;
    private String mBody;
    private String mDateFormat = "MMMM, d yyyy 'at' K:mm a";
    private String mColor;

    public Post() {
        mTitle = "";
        mBody = "";
        mDate = new SimpleDateFormat(mDateFormat).format(new Date());
        mColor = "red";
    }

    public Post(String color) {
        mTitle = "";
        mBody = "";
        mDate = new SimpleDateFormat(mDateFormat).format(new Date());
        mColor = color;
    }

    public Post(long id, String title, String text, String date, String color) {
        mId = id;
        mTitle = title;
        mBody = text;
        mDate = date;
        mColor = color;
    }

    public boolean contains(String query) {
        String title = mTitle.toLowerCase();
        String body = mBody.toLowerCase();
        String date = mDate.toLowerCase();
        query = query.toLowerCase();
        return (title.contains(query) || body.contains(query) || date.contains(query));
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        this.mBody = body;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setDate(Date date) {
        mDate = new SimpleDateFormat(mDateFormat).format(date).toString();
    }

    public int getColor(String shade) {
        return Posts.parseColor(mColor + shade);
    }

    public String getColorString() {
        return mColor;
    }

    public void setColor(String color) {
        mColor = color;
    }
}
