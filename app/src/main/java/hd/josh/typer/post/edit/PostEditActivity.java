package hd.josh.typer.post.edit;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import hd.josh.typer.R;
import hd.josh.typer.helpers.post.Post;
import hd.josh.typer.helpers.post.Posts;


public class PostEditActivity extends AppCompatActivity implements PostEditFragment.Contract {

    private static final String TAG = "PostEditActivity";
    public static final String POST_EXTRA = "POST_EXTRA";
    public static final String ISNEW_EXTRA = "ISNEW_EXTRA";
    public static final String POS_EXTRA = "POS_EXTRA";

    public PostEditFragment mFragment;
    public Toolbar mToolbar;
    public View mToolbarExt;
    public CardView mCard;

    private Post mPost;
    private int mPosition;
    private boolean mIsNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPost = (Post)getIntent().getSerializableExtra(PostEditActivity.POST_EXTRA);
        mPosition = getIntent().getExtras().getInt(PostEditActivity.POS_EXTRA);
        mIsNew = getIntent().getBooleanExtra(PostEditActivity.ISNEW_EXTRA, true);

        setThemeColor(mPost.getColorString(), false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);

        mCard = (CardView)findViewById(R.id.post_card);
        mCard.setCardBackgroundColor(mPost.getColor("Light"));

        mFragment = (PostEditFragment)getFragmentManager().findFragmentById(R.id.post_fragment_container);
        if (mFragment == null) {
            mFragment = new PostEditFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.post_fragment_container, mFragment)
                    .commit();
        }

        initToolbar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition t = new Slide(Gravity.TOP);
            t.excludeTarget(android.R.id.statusBarBackground, true);
            t.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setEnterTransition(t);
            getWindow().setExitTransition(t);

            TransitionSet set = new TransitionSet();
            Transition tCard = new Slide(Gravity.TOP);
            tCard.addTarget(mCard);
            set.addTransition(tCard);

            ViewCompat.setTransitionName(mCard, "post_card" + mPosition);
            Log.d(TAG, "Editing: " + ViewCompat.getTransitionName(mCard));
        }
    }

    private void initToolbar() {
        mToolbar = (Toolbar)findViewById(R.id.post_toolbar);
        mToolbarExt = findViewById(R.id.post_toolbar_extended);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_check);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPost.getColor("Primary")));
        mToolbarExt.setBackgroundColor(mPost.getColor("Primary"));
    }

    @Override
    public void finishedPost(Post post, boolean isNew) {
        Intent i = new Intent();
        i.putExtra(POST_EXTRA, post);
        if (mFragment.isNew()) {
            i.putExtra(POS_EXTRA, 0);
        } else {
            i.putExtra(POS_EXTRA, mFragment.getPosition());
        }
        setResult(RESULT_OK, i);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCard.setTransitionName("");
        }
        supportFinishAfterTransition();
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCard.setTransitionName("");
        }
        supportFinishAfterTransition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_edit_card, menu);

        menu.findItem(R.id.menu_color_red).getIcon()
                .setColorFilter(Posts.parseColor("redPrimary"), PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.menu_color_orange).getIcon()
                .setColorFilter(Posts.parseColor("orangePrimary"), PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.menu_color_yellow).getIcon()
                .setColorFilter(Posts.parseColor("yellowPrimary"), PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.menu_color_green).getIcon()
                .setColorFilter(Posts.parseColor("greenPrimary"), PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.menu_color_blue).getIcon()
                .setColorFilter(Posts.parseColor("bluePrimary"), PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.menu_color_purple).getIcon()
                .setColorFilter(Posts.parseColor("purplePrimary"), PorterDuff.Mode.SRC_ATOP);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Post post = mFragment.getPost();

        switch (id) {
            case android.R.id.home:
                attemptToSave();
                return true;
            case R.id.menu_color_red:
                post.setColor("red");
                setThemeColor("red", true);
                break;
            case R.id.menu_color_orange:
                post.setColor("orange");
                setThemeColor("orange", true);
                break;
            case R.id.menu_color_yellow:
                post.setColor("yellow");
                setThemeColor("yellow", true);
                break;
            case R.id.menu_color_green:
                post.setColor("green");
                setThemeColor("green", true);
                break;
            case R.id.menu_color_blue:
                post.setColor("blue");
                setThemeColor("blue", true);
                break;
            case R.id.menu_color_purple:
                post.setColor("purple");
                setThemeColor("purple", true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void attemptToSave() {
        Post post = mFragment.getPost();

        boolean noTitle = mFragment.getTitle().isEmpty();
        boolean noText = mFragment.getBody().isEmpty();

        if (!noTitle && !noText) {
            post.setTitle(mFragment.getTitle());
            post.setBody(mFragment.getBody());
            finishedPost(mFragment.getPost(), mFragment.isNew());
        } else {
            if (noTitle) {
                Snackbar.make(findViewById(R.id.post_edit_coordinator), "Title is blank!", Snackbar.LENGTH_LONG).show();
                mFragment.getEditTitle().requestFocus();
            } else if (noText) {
                Snackbar.make(findViewById(R.id.post_edit_coordinator), "Body text is blank!", Snackbar.LENGTH_LONG).show();
                mFragment.getEditBody().requestFocus();
            }
        }
    }

    private void setThemeColor(String color, boolean styleScreen) {
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

        if (styleScreen) {styleScreen();}
    }

    private void styleScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(mPost.getColor("Dark"));
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPost.getColor("Primary")));
        mToolbarExt.setBackgroundColor(mPost.getColor("Primary"));
        mCard.setCardBackgroundColor(mPost.getColor("Light"));
    }

    public Post getPost() {
        return mPost;
    }

    public int getPosition() {
        return mPosition;
    }

    public boolean isNew() {
        return mIsNew;
    }

}
