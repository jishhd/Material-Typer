package hd.josh.typer.post.list;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import hd.josh.typer.R;
import hd.josh.typer.helpers.itemtouch.OnStartDragListener;
import hd.josh.typer.helpers.itemtouch.SimpleItemTouchHelperCallback;
import hd.josh.typer.helpers.post.Post;
import hd.josh.typer.helpers.post.PostAdapter;
import hd.josh.typer.helpers.post.PostRecyclerView;
import hd.josh.typer.helpers.post.Posts;

public class PostListFragment extends Fragment implements OnStartDragListener {

    private static final String TAG = "PostListFragment";

    public PostAdapter mAdapter;
    public PostRecyclerView mRecyclerView;
    public ImageView mEmptyAlert;
    public PostListActivity mPostListActivity;
    private ItemTouchHelper mItemTouchHelper;
    public RecyclerView.LayoutManager mLayoutManager;

    public PostListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_post_list, container, false);
        mEmptyAlert = (ImageView)v.findViewById(R.id.post_list_empty_alert);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPostListActivity = (PostListActivity)getActivity();

        initRecycler(view);
        initAnimator();
    }

    private void initRecycler(View view) {
        mRecyclerView = (PostRecyclerView)view.findViewById(R.id.post_list_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setEmptyView(view.findViewById(R.id.post_list_empty));
        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.white));

        final int cols;
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT: cols = 1; break;
            case Configuration.ORIENTATION_LANDSCAPE: cols = 2; break;
            default: cols = 1; break;
        }
        mLayoutManager = new StaggeredGridLayoutManager(cols, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PostAdapter(getActivity(), Posts.get(), this);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.SetOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                Log.d(TAG, "ID: " +(view.getId() == R.id.post_card_btn_share));
                switch (view.getId()) {
                    case R.id.post_card_btn_share:
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_SUBJECT, Posts.get(position).getTitle());
                        i.putExtra(Intent.EXTRA_TEXT, Posts.get(position).getBody());
                        startActivity(i);
                        break;
                    case R.id.post_card:
                        ViewCompat.setTransitionName(view, "post_card" + position);
                        mPostListActivity.startEditPost(position, view);
                        break;
                }
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void initAnimator() {
        DefaultItemAnimator animator = new DefaultItemAnimator();
        int animDuration = 200;
        animator.setAddDuration(animDuration);
        animator.setRemoveDuration(animDuration);
        animator.setChangeDuration(animDuration);
        animator.setMoveDuration(animDuration);
        mRecyclerView.setItemAnimator(animator);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void scroll(int position) {
        ((StaggeredGridLayoutManager)mLayoutManager).scrollToPositionWithOffset(position, 0);
    }

    public void animateTo(List<Post> posts) {
        mAdapter.animateTo(posts);
    }

    public void smoothScrollToPosition(int position) {
        mRecyclerView.smoothScrollToPosition(position);
    }

    public void scrollToPosition(int position) {
        mRecyclerView.scrollToPosition(position);
    }
}
