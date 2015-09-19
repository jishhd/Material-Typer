package hd.josh.typer.helpers.post;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hd.josh.typer.R;
import hd.josh.typer.helpers.itemtouch.ItemTouchHelperAdapter;
import hd.josh.typer.helpers.itemtouch.ItemTouchHelperViewHolder;
import hd.josh.typer.helpers.itemtouch.OnStartDragListener;
import hd.josh.typer.post.list.PostListActivity;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

    public static class ItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, ItemTouchHelperViewHolder {

        private final TextView mTitle;
        private final TextView mDate;
        private final TextView mBody;
        private final CardView mCard;
        private final ImageButton mShareBtn;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView)itemView.findViewById(R.id.post_title);
            mDate = (TextView)itemView.findViewById(R.id.post_date);
            mBody = (TextView)itemView.findViewById(R.id.post_body);
            mCard = (CardView)itemView.findViewById(R.id.post_card);
            mShareBtn = (ImageButton)itemView.findViewById(R.id.post_card_btn_share);

            mCard.setOnClickListener(this);
            mShareBtn.setOnClickListener(this);
        }

        public void bind(Post post) {
            mTitle.setText(post.getTitle());
            mDate.setText(post.getDate());
            mDate.setAlpha(0.87f);
            mBody.setText(post.getBody());
            mCard.setCardBackgroundColor(post.getColor("Light"));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ViewCompat.setTransitionName(mCard, "post_card" + getAdapterPosition());
                Log.d(TAG, "Adapter bind: " + ViewCompat.getTransitionName(mCard));
            }
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public void onItemSelected() {
        }
        @Override
        public void onItemClear() {
        }
    }

    private static final String TAG = "PostAdapter";
    private Context mContext;
    private final List<Post> mVisiblePosts;
    static OnItemClickListener mOnItemClickListener;
    private final OnStartDragListener mDragStartListener;
    private ItemViewHolder itemViewHolder;

    public PostAdapter(Context context, List<Post> posts, OnStartDragListener dragStartListener) {
        mContext = context;
        mVisiblePosts = new ArrayList<>(posts);
        mDragStartListener = dragStartListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        itemViewHolder = new ItemViewHolder(view);
        return itemViewHolder;
    }

    @Override
    public void onItemDismiss(int position) {
        ((PostListActivity)mContext).finishDeletePost(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return true;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.bind(mVisiblePosts.get(position));
    }

    @Override
    public int getItemCount() {
        return mVisiblePosts == null ? 0 : mVisiblePosts.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void animateTo(List<Post> posts) {
        applyAndAnimateRemovals(posts);
        applyAndAnimateAdditions(posts);
        applyAndAnimateMovedItems(posts);
    }
    private void applyAndAnimateRemovals(List<Post> newPosts) {
        for (int i = mVisiblePosts.size() - 1; i >= 0; i--) {
            final Post post = mVisiblePosts.get(i);
            if (!newPosts.contains(post)) {
                removeItem(i);
            }
        }
    }
    private void applyAndAnimateAdditions(List<Post> newPosts) {
        for (int i = 0, count = newPosts.size(); i < count; i++) {
            final Post post = newPosts.get(i);
            if (!mVisiblePosts.contains(post)) {
                addItem(i, post);
            }
        }
    }
    private void applyAndAnimateMovedItems(List<Post> newPosts) {
        for (int toPosition = newPosts.size() - 1; toPosition >= 0; toPosition--) {
            final Post post = newPosts.get(toPosition);
            final int fromPosition = mVisiblePosts.indexOf(post);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
    public Post removeItem(int position) {
        final Post post = mVisiblePosts.remove(position);
        notifyItemRemoved(position);
        return post;
    }
    public void addItem(int position, Post post) {
        mVisiblePosts.add(position, post);
        notifyItemInserted(position);
    }
    public void moveItem(int fromPosition, int toPosition) {
        final Post post = mVisiblePosts.remove(fromPosition);
        mVisiblePosts.add(toPosition, post);
        notifyItemMoved(fromPosition, toPosition);
    }
}