package hd.josh.typer.helpers.post;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hd.josh.typer.R;
import hd.josh.typer.helpers.db.PostsDataSource;

public class Posts {
    private static final String TAG = "Posts";
    // Internal data structures containing all posts and the currently filtered list
    private static ArrayList<Post> mPosts;
    private static List<Post> mVisiblePosts;

    private static Context mContext;
    private static PostsDataSource mDataSource;
    private static Post mPreviousDeletion = null;
    private static int mPreviousPosition = 0;
    private static HashMap<String, String> mColorList;
    private static String mSelectedColor;
    private static Resources mResources;

    private static Posts singleton = null;
    public static Posts getInstance(Context context, Resources res) {
        if (singleton == null) {
            singleton = new Posts(context.getApplicationContext(), res);
        }
        return singleton;
    }

    private Posts(Context context, Resources res) {
        mContext = context;
        mResources = res;
        mSelectedColor = "all";
        initColors();
        // Retrieve master post list from database source
        mDataSource = new PostsDataSource(mContext);
        mPosts = mDataSource.getAllPosts();
        mVisiblePosts = new ArrayList<>(mPosts);
    }

    /***********************************************************************************************
     *  POST MANAGEMENT
     */

    /**
     * Add a newly created post to database and master list
     * @param post Post to add
     */
    public static void add(Post post) {
        mDataSource.createPost(post);
        mPosts.add(0, post);
    }

    /**
     * Replace a post at a given position with an edited post
     * @param position Position of post to edit
     * @param post Edited post to replace with
     */
    public static void edit(int position, Post post) {
        mDataSource.editPost(post);
        // Set correct post, as given position is relative to visible posts instead of master list
        mPosts.set(mPosts.indexOf(mVisiblePosts.get(position)), post);
    }

    /**
     * Delete a post a given position
     * @param position Position of post to delete
     */
    public static void delete(int position) {
        // Store previous post and its position before deletion
        mPreviousDeletion = mVisiblePosts.get(position);
        mPreviousPosition = mPosts.indexOf(mVisiblePosts.get(position));
        // Delete post from database and master list
        mDataSource.deletePost(get(position));
        mPosts.remove(mPreviousDeletion);
    }

    /**
     * Undo the most recent post deletion, and re-insert it into the master list and database
     * @return Position of post that was just re-inserted
     */
    public static int undoDelete() {
        // Re-insert post to database and master post list
        mDataSource.createPost(mPreviousDeletion);
        mPosts.add(mPreviousPosition, mPreviousDeletion);
        // Reset data of previously restored post
        int tempPos = mPreviousPosition;
        mPreviousDeletion = null;
        mPreviousPosition = 0;
        return tempPos;
    }

    /**
     * Filter the master list of posts by a given color
     * @param color Color of posts to retrieve
     * @return List of posts with given color
     */
    public static List<Post> filter(String color) {
        // Start by clearing currently visible list
        mSelectedColor = color;
        mVisiblePosts.clear();
        mVisiblePosts = new ArrayList<>();
        // If no filter selected, return full list
        if (mSelectedColor.equals("all")) {
            mVisiblePosts = new ArrayList<>(mPosts);
        } else { // Otherwise iteratively add posts that are of specified color
            Post post;
            for (int i = mPosts.size()-1; i >= 0; i--) {
                post = mPosts.get(i);
                if (post.getColorString().equals(mSelectedColor)) {
                    mVisiblePosts.add(post);
                }
            }
        }
        return mVisiblePosts;
    }

    /**
     * Filter the master list of posts by combined query and color
     * @param query String to compare against posts
     * @param color Color to search in
     * @return List of posts that match the given query and color criteria
     */
    public static List<Post> filter(String query, String color) {
        filter(color);

        for (Post post : mPosts) {
            if (!post.contains(query)) {
                mVisiblePosts.remove(post);
            }
        }

        return mVisiblePosts;
    }

    /***********************************************************************************************
     *  GETTERS AND SETTINGS
     */

    /**
     * Initialize internal map of colors
     */
    public static void initColors() {
        mColorList = new HashMap<>();
        // Accent colors
        mColorList.put("allAccent", "#" + Integer.toHexString(mResources.getColor(R.color.black)));
        mColorList.put("redAccent", "#" + Integer.toHexString(mResources.getColor(R.color.pink_A400)));
        mColorList.put("orangeAccent", "#" + Integer.toHexString(mResources.getColor(R.color.deep_orange_A400)));
        mColorList.put("yellowAccent", "#" + Integer.toHexString(mResources.getColor(R.color.amber_A400)));
        mColorList.put("greenAccent", "#" + Integer.toHexString(mResources.getColor(R.color.green_A400)));
        mColorList.put("blueAccent", "#" + Integer.toHexString(mResources.getColor(R.color.blue_A400)));
        mColorList.put("purpleAccent", "#" + Integer.toHexString(mResources.getColor(R.color.deep_purple_A400)));
        // Card background colors
        mColorList.put("allLight", "#" + Integer.toHexString(mResources.getColor(R.color.black)));
        mColorList.put("redLight", "#" + Integer.toHexString(mResources.getColor(R.color.red_200)));
        mColorList.put("orangeLight", "#" + Integer.toHexString(mResources.getColor(R.color.orange_200)));
        mColorList.put("yellowLight", "#" + Integer.toHexString(mResources.getColor(R.color.amber_200)));
        mColorList.put("greenLight", "#" + Integer.toHexString(mResources.getColor(R.color.light_green_200)));
        mColorList.put("blueLight", "#" + Integer.toHexString(mResources.getColor(R.color.light_blue_200)));
        mColorList.put("purpleLight", "#" + Integer.toHexString(mResources.getColor(R.color.purple_200)));
        // Toolbar primary colors
        mColorList.put("allPrimary", "#" + Integer.toHexString(mResources.getColor(R.color.black)));
        mColorList.put("redPrimary", "#" + Integer.toHexString(mResources.getColor(R.color.red_500)));
        mColorList.put("orangePrimary", "#"+ Integer.toHexString(mResources.getColor(R.color.orange_500)));
        mColorList.put("yellowPrimary", "#"+ Integer.toHexString(mResources.getColor(R.color.amber_600)));
        mColorList.put("greenPrimary", "#"+ Integer.toHexString(mResources.getColor(R.color.light_green_500)));
        mColorList.put("bluePrimary", "#"+ Integer.toHexString(mResources.getColor(R.color.light_blue_500)));
        mColorList.put("purplePrimary", "#"+ Integer.toHexString(mResources.getColor(R.color.purple_500)));
        // Statusbar darkest colors
        mColorList.put("allDark", "#" + Integer.toHexString(mResources.getColor(R.color.black)));
        mColorList.put("redDark", "#" + Integer.toHexString(mResources.getColor(R.color.red_700)));
        mColorList.put("orangeDark", "#"+ Integer.toHexString(mResources.getColor(R.color.orange_700)));
        mColorList.put("yellowDark", "#" + Integer.toHexString(mResources.getColor(R.color.amber_700)));
        mColorList.put("greenDark", "#"+ Integer.toHexString(mResources.getColor(R.color.light_green_700)));
        mColorList.put("blueDark", "#"+ Integer.toHexString(mResources.getColor(R.color.light_blue_700)));
        mColorList.put("purpleDark", "#" + Integer.toHexString(mResources.getColor(R.color.purple_700)));
    }

    /**
     * Return parsed color based on given key match to hashmap
     * @param key Key to get color value of
     * @return Color value as int
     */
    public static int parseColor(String key) {
        return Color.parseColor(mColorList.get(key));
    }

    /**
     * Retrieve list of visible posts
     * @return Visible posts as list
     */
    public static List<Post> get() {
        return mVisiblePosts;
    }

    /**
     * Returns the post found at a given position
     * @param position Position to find post
     * @return Post at given position
     */
    public static Post get(int position) {
        return mVisiblePosts.get(position);
    }

    /**
     * Returns number of visible posts
     * @return Size of visible post list
     */
    public static int size() {
        return mVisiblePosts.size();
    }


    /***********************************************************************************************
     *  MISCELLANEOUS
     */

    /**
     * Re-adds the introduction information cards
     */
    public static void reAddIntroCards() {
        mDataSource.reIntro();
        mPosts = mDataSource.getAllPosts();
    }
}
