package hd.josh.typer.post.list;

import android.content.DialogInterface;                     // Help & About pop-up dialog
import android.content.Intent;
import android.content.res.ColorStateList;                  // Manual theming / icon colors
import android.content.res.Configuration;                   // Manages nav drawer config changes
import android.graphics.drawable.ColorDrawable;             // Manual theming / icon colors
import android.os.Build;                                    // Version checks
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;                  // Help & About pop-up dialog
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;              // Linkify HTML in textview
import android.transition.Fade;                             // Transition animation effect
import android.transition.Transition;                       // Transition animations
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;                         // Help & About pop-up dialog
import android.widget.TextView;                             // Help & About pop-up dialog

import butterknife.Bind;
import butterknife.ButterKnife;
import hd.josh.typer.R;
import hd.josh.typer.helpers.post.Post;
import hd.josh.typer.helpers.post.Posts;
import hd.josh.typer.post.edit.PostEditActivity;

public class PostListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "PostListActivity";
    // Activity request codes
    private static final int NEW_POST_REQUEST = 10;
    private static final int EDIT_POST_REQUEST = 11;
    // Stored checked position and color of nav menu (used on orientation changes)
    private static final String STATE_SELECTED_POSITION = "NAV_POSITION";
    private static final String STATE_SELECTED_COLOR = "SELECTED_COLOR";
    // Bind UI components with Butterknife for boilerplate convenience
    @Bind(R.id.post_list_toolbar) Toolbar mToolbar;
    @Bind(R.id.post_list_coordinator) CoordinatorLayout mCoordinator;
    @Bind(R.id.post_list_appbar) AppBarLayout mAppBar;
    @Bind(R.id.post_list_drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view) NavigationView mNavDrawer;
    @Bind(R.id.header_background) com.example.joshua.material.RatioLayout mNavHeader;
    @Bind(R.id.fab_new_post) FloatingActionButton mNewPostFab;
    // Other UI components
    private ActionBar mSupportActionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private AppBarLayout.Behavior mBehavior;
    private CoordinatorLayout.LayoutParams mCoordinatorParams;
    private PostListFragment mPostListFragment;
    private SearchView mSearchView;
    private MenuItem mSearch;
    // Vars for theming + currently selected filter
    private int mSelectedNavPosition;
    private String mSelectedColor;
    private int mPrimaryColor;
    private int mPrimaryDarkColor;
    private int mAccentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Request window features
        getWindow().requestFeature(android.view.Window.FEATURE_ACTION_BAR_OVERLAY);
        // Super and set layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        ButterKnife.bind(this);
        // Restore nav drawer status and theme on orientation change
        if (savedInstanceState != null) {
            mSelectedNavPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mSelectedColor = savedInstanceState.getString(STATE_SELECTED_COLOR);
        } else {
            mSelectedNavPosition = 0;
            mSelectedColor = "all";
        }
        // Initilization functions
        Posts.getInstance(getApplicationContext(), getResources());
        initToolbar();
        initDrawer();
        initFab();
        initFragment();
        // Set theme after layout inflation
        setThemeColor(mSelectedColor);
        // If 5.0+, set content transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition t = new Fade();
            // Exclude status and nav bars from transition to fix white flash issue
            t.excludeTarget(android.R.id.statusBarBackground, true);
            t.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setEnterTransition(t);
            getWindow().setExitTransition(t);
        }
    }

    private void initToolbar() {
        // Params and Behavior for CoordinatorLayout (FAB, toolbar)
        mCoordinatorParams = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
        mBehavior = (AppBarLayout.Behavior) mCoordinatorParams.getBehavior();
        // Set toolbar as actionbar
        setSupportActionBar(mToolbar);
        mSupportActionBar = getSupportActionBar();
        mSupportActionBar.setTitle(getResources().getString(R.string.app_name_full));
        mSupportActionBar.setDisplayHomeAsUpEnabled(true);
        mSupportActionBar.setHomeButtonEnabled(true);
    }
    private void initDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                showUI();
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                showUI();
            }
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0); // 0 offset disables menu icon spinbars
                showUI();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mNavDrawer.setItemIconTintList(null); // Null fixes coloring issue when manually setting nav drawer icons
        mNavDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                }
        );
    }
    private void initFab() {
        mNewPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewPost();
            }
        });
    }
    private void initFragment() {
        // Fragment holds RecyclerView of posts
        mPostListFragment = (PostListFragment)getFragmentManager().findFragmentById(R.id.post_list_container);
        if (mPostListFragment == null) {
            mPostListFragment = new PostListFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.post_list_container, mPostListFragment)
                    .commit();
        }
    }

    /**
     * Store position and color for nav drawer on orientation change
     * @param outState Bundle to store
     */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mSelectedNavPosition);
        outState.putString(STATE_SELECTED_COLOR, mSelectedColor);
    }

    /**
     * Restore position and color for nav drawer on orientation change
     * @param savedInstanceState Bundle to restore from
     */
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedNavPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION, 0);
        mSelectedColor = savedInstanceState.getString(STATE_SELECTED_COLOR);
        mNavDrawer.getMenu().getItem(mSelectedNavPosition).setChecked(true);
        setThemeColor(mSelectedColor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset theme on app resume
        setThemeColor(mSelectedColor);
        searchBy("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case NEW_POST_REQUEST:
                if (resultCode == RESULT_OK) {
                    // Extract and finish new post if OK
                    Post post = (Post)data.getSerializableExtra(PostEditActivity.POST_EXTRA);
                    finishNewPost(post);
                }
                break;
            case EDIT_POST_REQUEST:
                if (resultCode == RESULT_OK) {
                    // Extract and finish edited post if OK
                    Post post = (Post)data.getSerializableExtra(PostEditActivity.POST_EXTRA);
                    int pos = data.getExtras().getInt(PostEditActivity.POS_EXTRA);
                    finishEditPost(post, pos);
                } else if (resultCode == RESULT_CANCELED) {
                    if (data != null) {
                        // Delete post if cancelled with no return intent data
                        int pos = data.getExtras().getInt(PostEditActivity.POS_EXTRA);
                        finishDeletePost(pos);
                    }
                }
                break;
        }
    }

    /**
     * Send intent to create new post with either default or filtered color
     */
    public void startNewPost() {
        mSearchView.onActionViewCollapsed(); // Collapse searchView
        Intent i = new Intent(PostListActivity.this, PostEditActivity.class);
        i.putExtra(PostEditActivity.POST_EXTRA, new Post(!mSelectedColor.equals("all") ? mSelectedColor : "yellow"));
        i.putExtra(PostEditActivity.ISNEW_EXTRA, true);
        ActivityCompat.startActivityForResult(this, i, NEW_POST_REQUEST, null);
    }

    /**
     * Send intent to edit a post at a given position, and animate transitions
     * @param position Position of selected post
     * @param clickedItem View of selected post
     */
    public void startEditPost(final int position, View clickedItem) {
        mSearchView.onActionViewCollapsed(); // Collapse searchView
        Intent i = new Intent(PostListActivity.this, PostEditActivity.class);
        Post post = Posts.get(position);
        i.putExtra(PostEditActivity.POST_EXTRA, post);
        i.putExtra(PostEditActivity.ISNEW_EXTRA, false);
        i.putExtra(PostEditActivity.POS_EXTRA, position);
        // Set transition animation for selected card view if 5.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                            clickedItem,
                            clickedItem.getTransitionName()
                    );
            ActivityCompat.startActivityForResult(this, i, EDIT_POST_REQUEST, options.toBundle());
        } else { // Otherwise, just do a regular transition
            ActivityCompat.startActivityForResult(this, i, EDIT_POST_REQUEST, null);
        }
    }

    /**
     * Retrieved result post is added to master filtered list
     * @param post Post to be added to master list
     */
    public void finishNewPost(Post post) {
        // Add newly created post to master list
        Posts.add(post);

        String color = post.getColorString();
        if (mSelectedColor.equals("all")) {
            //if posting while all is selected, do nothing special
            filterBy("all", 0);
        } else if (!color.equals(mSelectedColor)) {
            //if posting a different color than selected, update to posted color
            mSelectedColor = color;
            filterBy(mSelectedColor, 0);
        } else {
            //otherwise, you're posting to the same color you just created
            filterBy(mSelectedColor, 0);
        }
        // Scroll to top of list after new post creation
        mPostListFragment.scroll(0);
    }

    /**
     * Retrieved result post replaces previous post at position in master filtered list
     * @param post Updated edited post
     * @param position Position of edited post
     */
    public void finishEditPost(Post post, final int position) {
        // Update newly edited post in master list
        Posts.edit(position, post);

        String color = post.getColorString();
        if (mSelectedColor.equals("all")) {
            //if posting while all is selected, do nothing special
            filterBy("all");
        } else if (!color.equals(mSelectedColor)) {
            //if posting a different color than selected, update to posted color
            mSelectedColor = color;
            filterBy(mSelectedColor, 0);
        } else {
            //otherwise, you're posting to the same color you just created
            filterBy(mSelectedColor);
        }
        // Scroll to newly created post only if at top or bottom to account for visible offsets
        if (position == 0 || position == Posts.size()) {
            mPostListFragment.scroll(position);
        }
    }

    /**
     * Delete post at given position and remove from list
     * @param position Position of post to delete
     */
    public void finishDeletePost(final int position) {
        Post post = Posts.get(position);
        int snackColor = post.getColor("Light");
        // Generate a truncated title string for snackbar pop-up
        String truncTitle, title;
        title = post.getTitle();
        truncTitle = title.substring(0, Math.min(title.length(), 16));
        if (!truncTitle.equals(title)) {
            truncTitle += "...";
        }
        // Make snackbar with undo delete button
        Snackbar.make(findViewById(R.id.post_list_coordinator), "\"" + truncTitle + "\" deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Posts.undoDelete();
                        // Filter to redisplay recovered post, and scroll if at beginning or end of list
                        if (position == 0 || position == Posts.size()) {
                            filterBy(mSelectedColor, position);
                        } else {
                            filterBy(mSelectedColor);
                        }
                    }
                })
                .setActionTextColor(snackColor)
                .show();

        Posts.delete(position);
        // Make sure UI is showing after last post is deleted
        if (Posts.size() <= 1) {
            showUI();
        }
        filterBy(mSelectedColor);
    }

    /**
     * Set checked menu item in the navigation drawer
     * @param color Color to set as checked
     */
    private void setChecked(String color) {
        switch (color) {
            case "red":
                mSelectedColor = "red";
                mSelectedNavPosition = 1;
                break;
            case "orange":
                mSelectedColor = "orange";
                mSelectedNavPosition = 2;
                break;
            case "yellow":
                mSelectedColor = "yellow";
                mSelectedNavPosition = 3;
                break;
            case "green":
                mSelectedColor = "green";
                mSelectedNavPosition = 4;
                break;
            case "blue":
                mSelectedColor = "blue";
                mSelectedNavPosition = 5;
                break;
            case "purple":
                mSelectedColor = "purple";
                mSelectedNavPosition = 6;
                break;
            case "all":
            default:
                mSelectedColor = "all";
                mSelectedNavPosition = 0;
                break;
        }
        mNavDrawer.getMenu().getItem(mSelectedNavPosition).setChecked(true);
    }

    /**
     * Filter master post list by color, or access Help & About (and eventually Settings)
     * @param menuItem Selected menu item
     */
    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
//            case R.id.nav_settings:
//                Snackbar.make(findViewById(R.id.post_list_coordinator), "Working on it! :)", Snackbar.LENGTH_LONG).show();
//                break;
            case R.id.nav_about:
                showDialog(R.string.about_title, R.string.about_text);
                break;
            case R.id.nav_filter_red:
                filterBy("red", 0);
                break;
            case R.id.nav_filter_orange:
                filterBy("orange", 0);
                break;
            case R.id.nav_filter_yellow:
                filterBy("yellow", 0);
                break;
            case R.id.nav_filter_green:
                filterBy("green", 0);
                break;
            case R.id.nav_filter_blue:
                filterBy("blue", 0);
                break;
            case R.id.nav_filter_purple:
                filterBy("purple", 0);
                break;
            case R.id.nav_filter_all:
            default:
                filterBy("all", 0);
                break;
        }
        mSearchView.onActionViewCollapsed();
        mDrawerLayout.closeDrawers();
    }

    /**
     * Filter the master post list by color, then animate content additions/deletions/moves
     * @param color Color to filter by, or "all" for default
     */
    public void filterBy(String color) {
        setChecked(color);
        setThemeColor(color);
        mPostListFragment.animateTo(Posts.filter(color));
    }

    /**
     * Filter the master post list by color, animate content additions/deletions/moves, and scroll
     * @param color Color to filter by, or "all" for default
     * @param position Position to scroll to
     */
    public void filterBy(String color, int position) {
        filterBy(color);
        mPostListFragment.smoothScrollToPosition(position);
    }

    /**
     * Search post list by query while scrolling to top of list
     * @param query
     */
    public void searchBy(String query) {
        mPostListFragment.animateTo(Posts.filter(query, mSelectedColor));
//        mPostListFragment.scrollToTop(getSupportActionBar().getHeight());
//        mPostListFragment.scrollToPosition(0);
    }

    /**
     * Set activity theme (for correct multitasking overview color in Lollipop)
     * Manually sets colors for instant display (without having to reload entire activity)
     * @param color Color to set theme to
     */
    private void setThemeColor(String color) {
        switch (color) {
            case "red":
                setTheme(R.style.RedTheme);
                break;
            case "orange":
                setTheme(R.style.OrangeTheme);
                break;
            case "yellow":
                setTheme(R.style.YellowTheme);
                break;
            case "green":
                setTheme(R.style.GreenTheme);
                break;
            case "blue":
                setTheme(R.style.BlueTheme);
                break;
            case "purple":
                setTheme(R.style.PurpleTheme);
                break;
            case "all":
            default:
                setTheme(R.style.AppTheme);
                break;
        }
        mPrimaryColor = Posts.parseColor(color + "Primary");
        mPrimaryDarkColor = Posts.parseColor(color + "Dark");
        mAccentColor = Posts.parseColor(color + "Accent");
        styleScreen();
    }

    /**
     * Style screen with selected theme colors, tinting statusbar on 5.0+
     */
    private void styleScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(mPrimaryDarkColor);
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        mNavHeader.setBackgroundColor(mPrimaryColor);
        mNewPostFab.setBackgroundTintList(ColorStateList.valueOf(mAccentColor));
    }

    /**
     * Displays a pop-up dialog window for Help & About section
     * TODO: Replace with legitimate Settings activity
     * @param titleResId Title resource for pop-up
     * @param bodyResId Body text resource for pop-up
     */
    private void showDialog(int titleResId, int bodyResId) {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(titleResId)
                .setMessage(bodyResId)
                .setIcon(R.drawable.ic_info)
                .setNeutralButton(R.string.intro_cards_relaunch, new DialogInterface.OnClickListener() {
                    @Override // Set button to relaunch all instruction cards
                    public void onClick(DialogInterface dialog, int which) {
                        Posts.reAddIntroCards();
                        // Filter by "all" and scroll up so every intro card is displayed
                        mNavDrawer.getMenu().findItem(R.id.nav_filter_all).setChecked(true);
                        mSelectedColor = "all";
                        filterBy(mSelectedColor, 0);
                        mPostListFragment.scroll(0);
                    }
                })
                .setPositiveButton(R.string.dialog_updates, new DialogInterface.OnClickListener() {
                    @Override // Set button to display latest application updates
                    public void onClick(DialogInterface dialog, int which) {
                        showUpdates();
                    }
                })
                .create();
        d.show();
        // Linkify dialog text so HTML is clickable
        ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Display dialog pop-up containing latest update information
     */
    private void showUpdates() {
        final String[] updateVersions = getResources().getStringArray(R.array.update_versions);
        final String[] updateTexts = getResources().getStringArray(R.array.update_text);
        View updateItem;
        TextView updateVersion, updateText;
        // Inflate scrollable textview to display update text
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_scrollable_text, null);
        LinearLayout linearLayout =  (LinearLayout)dialogView.findViewById(R.id.dialog_linearlayout);
        // Insert every update into scrollable view
        for (int i = 0; i < updateVersions.length; i++) {
            updateItem = inflater.inflate(R.layout.dialog_scrollable_text_item, null);
            updateVersion = (TextView) updateItem.findViewById(R.id.dialog_version);
            updateText = (TextView) updateItem.findViewById(R.id.dialog_text);

            updateVersion.setText(updateVersions[i]);
            updateText.setText(updateTexts[i]);
            linearLayout.addView(updateItem);
        }
        // Display update dialog pop-up
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Latest Updates")
                .setIcon(R.drawable.ic_info)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .create();
        d.show();
    }

    /**
     * Convenience method to re-display scrollable toolbar and FAB
     */
    private void showUI() {
        mBehavior.onNestedFling(mCoordinator, mAppBar, null, 0, -1000, true);
        mNewPostFab.show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_list, menu);
        // Initalize search view and query listener
        mSearch = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearch);
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // Search as query is typed
        searchBy(query);
        mPostListFragment.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Unecessary but required, since query is submitted as typed
        return false;
    }
}
