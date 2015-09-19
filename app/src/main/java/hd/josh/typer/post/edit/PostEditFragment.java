package hd.josh.typer.post.edit;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import hd.josh.typer.R;
import hd.josh.typer.helpers.misc.ContractFragment;
import hd.josh.typer.helpers.post.Post;

public class PostEditFragment extends ContractFragment<PostEditFragment.Contract> {

    private Post mPost;
    private EditText mEditTitle;
    private EditText mEditBody;
    private TextView mTextDate;
    private ImageButton mShareBtn;
    private int mPosition = 0;
    private boolean mIsNew;
    private PostEditActivity mEditActivity;

    public PostEditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_post_edit, container, false);
        setRetainInstance(true);

        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mEditActivity = (PostEditActivity)getActivity();


        mPost = mEditActivity.getPost();
        mPosition = mEditActivity.getPosition();
        mIsNew = mEditActivity.isNew();

        mEditTitle = (EditText)v.findViewById(R.id.post_edit_title);
        mTextDate = (TextView)v.findViewById(R.id.post_edit_date);
        mEditBody = (EditText)v.findViewById(R.id.post_edit_body);

        mShareBtn = (ImageButton)v.findViewById(R.id.post_edit_btn_share);
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, getTitle());
                i.putExtra(Intent.EXTRA_TEXT, getBody());
                startActivity(i);
            }
        });

        mEditTitle.setText(mPost.getTitle());
        mTextDate.setText(mPost.getDate());
        mTextDate.setAlpha(0.87f);
        mEditBody.setText(mPost.getBody());

    }

    public Post getPost() {
        return mPost;
    }

    public String getTitle() {
        return mEditTitle.getText().toString();
    }

    public String getBody() {
        return mEditBody.getText().toString();
    }

    public boolean isNew() {
        return mIsNew;
    }

    public int getPosition() {
        return mPosition;
    }

    public EditText getEditBody() {
        return mEditBody;
    }

    public EditText getEditTitle() {
        return mEditTitle;
    }

    public interface Contract {
        void finishedPost(Post post, boolean isNew);
    }

}
