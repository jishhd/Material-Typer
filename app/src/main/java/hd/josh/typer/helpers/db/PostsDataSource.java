package hd.josh.typer.helpers.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import hd.josh.typer.helpers.post.Post;

public class PostsDataSource {
    private DbHelper mDbHelper;
    private String[] allColumns = {
            DbHelper.COLUMN_ID,
            DbHelper.COLUMN_TITLE,
            DbHelper.COLUMN_TEXT,
            DbHelper.COLUMN_DATE,
            DbHelper.COLUMN_COLOR
    };

    public PostsDataSource(Context context) {
        mDbHelper = DbHelper.getInstance(context);
    }

    public ArrayList<Post> getAllPosts() {
        ArrayList<Post> posts = new ArrayList<>();

        Cursor cursor = mDbHelper.getReadableDatabase().query(DbHelper.POSTS_TABLE, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Post post = cursorToPost(cursor);
            posts.add(0, post);
            cursor.moveToNext();
        }
        cursor.close();
        return posts;
    }

    public void reIntro() {
        mDbHelper.initIntroPosts(mDbHelper.getWritableDatabase());
    }

    public void createPost(Post post) {
        ContentValues values = new ContentValues();
        if (post.getId() != -1) {
            values.put(DbHelper.COLUMN_ID, post.getId());
        }
        values.put(DbHelper.COLUMN_TITLE, post.getTitle());
        values.put(DbHelper.COLUMN_TEXT, post.getBody());
        values.put(DbHelper.COLUMN_DATE, post.getDate());
        values.put(DbHelper.COLUMN_COLOR, post.getColorString());

        post.setId(mDbHelper.getWritableDatabase().insert(DbHelper.POSTS_TABLE, null, values));
    }

    public void editPost(Post post) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_TITLE, post.getTitle());
        values.put(DbHelper.COLUMN_TEXT, post.getBody());
        values.put(DbHelper.COLUMN_DATE, post.getDate());
        values.put(DbHelper.COLUMN_COLOR, post.getColorString());

        String[] whereArgs = {String.valueOf(post.getId())};
        mDbHelper.getWritableDatabase().update(mDbHelper.POSTS_TABLE, values, mDbHelper.COLUMN_ID+"=?", whereArgs);
    }

    public void deletePost(Post post) {
        String [] whereArgs = {String.valueOf(post.getId())};
        mDbHelper.getWritableDatabase().delete(mDbHelper.POSTS_TABLE, mDbHelper.COLUMN_ID+"=?", whereArgs);
    }

    private Post cursorToPost(Cursor cursor) {
        //              id                 title                text                 date                 color
        return new Post(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
    }
}
