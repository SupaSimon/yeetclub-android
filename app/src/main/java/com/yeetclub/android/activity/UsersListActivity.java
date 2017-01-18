package com.yeetclub.android.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Log;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yalantis.phoenix.PullToRefreshView;
import com.yeetclub.android.R;
import com.yeetclub.android.adapter.UsersListAdapter;
import com.yeetclub.android.parse.BundleConstants;
import com.yeetclub.android.parse.ParseConstants;
import com.yeetclub.android.utility.NetworkHelper;

import java.util.List;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class UsersListActivity extends AppCompatActivity {

    protected List<ParseUser> mUsers;
    protected PullToRefreshView mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;

    public UsersListActivity() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_users_list);

        setupWindowAnimations();

        showToolbar();

        // Is there a network connection?
        boolean isOnline = NetworkHelper.isOnline(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // If we have listType, i.e. voters, likers, from FeedAdapter or UserProfileAdapter...
            if (bundle.getString(BundleConstants.KEY_LIST_TYPE) != null) {
                // Retrieve the list type...
                String listType = bundle.getString(BundleConstants.KEY_LIST_TYPE);
                Log.w(getClass().toString(), "listType: " + listType);

                // Then retrieve and display all the users who have either liked or voted on a post
                initialise(isOnline, listType, bundle);
            }
        }
    }


    private void showToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void initialise(boolean isOnline, String listType, Bundle bundle) {
        // Define the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve data for the RecyclerView
        retrieveUsers(isOnline, listType, bundle);

        // Set SwipeRefreshLayout logic
        setSwipeRefreshLayout(isOnline, listType, bundle);
    }


    private void setSwipeRefreshLayout(boolean isOnline, String listType, Bundle bundle) {
        mSwipeRefreshLayout = (PullToRefreshView) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isOnline) {
                mSwipeRefreshLayout.setRefreshing(false);
            } else {
                // Retrieve all users in the specified list
                retrieveUsers(true, listType, bundle);
            }
        });
    }


    private void setupWindowAnimations() {
        Fade fade = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            fade = (Fade) TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(fade);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    private void retrieveUsers(boolean isOnline, String listType, Bundle bundle) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        // Depending on if we got here to see a list of likers or voters, retrieve and display the list of users with interactions
        // Query the list of Users with objectIds contained by the votersList or likersList passed as a String Extra
        if (listType.equals(getString(R.string.likers))) {
            TextView toolbarTitle = (TextView) findViewById(R.id.feed_title);
            toolbarTitle.setText(R.string.liked_by);

            String topLevelCommentObjectId = bundle.getString(BundleConstants.KEY_TOP_LEVEL_COMMENT_OBJECT_ID);
            Log.w(getClass().toString(), "topLevelCommentObjectId 2: " + topLevelCommentObjectId);

            ParseQuery<ParseObject> yeetQuery = new ParseQuery<>(ParseConstants.CLASS_YEET);
            yeetQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, topLevelCommentObjectId);
            yeetQuery.findInBackground((topLevelComment, e) -> {
                if (e == null) for (ParseObject topLevelCommentObject : topLevelComment) {
                    List<String> likedBy = topLevelCommentObject.getList(ParseConstants.KEY_LIKED_BY);
                    query.addAscendingOrder(ParseConstants.KEY_USERNAME);
                    query.whereContainedIn(ParseConstants.KEY_OBJECT_ID, likedBy);
                    if (!isOnline) {
                        query.fromLocalDatastore();
                    }
                    query.findInBackground((users, e2) -> {

                        mSwipeRefreshLayout.setRefreshing(false);

                        if (e2 == null) {
                            // We found users!
                            mUsers = users;
                            ParseObject.pinAllInBackground(mUsers);

                            // Attach adapter to RecyclerView
                            UsersListAdapter adapter = new UsersListAdapter(getApplicationContext(), users);
                            adapter.setHasStableIds(true);
                            mRecyclerView.setHasFixedSize(true);
                            adapter.notifyDataSetChanged();
                            mRecyclerView.setAdapter(adapter);
                        }
                    });
                }
            });

        } else if (listType.equals(getString(R.string.voters))) {
            TextView toolbarTitle = (TextView) findViewById(R.id.feed_title);
            toolbarTitle.setText(R.string.voted_on_by);

            String pollObjectId = bundle.getString(BundleConstants.KEY_POLL_OBJECT_ID);
            // Log.w(getClass().toString(), String.valueOf(pollObjectId));

            ParseQuery<ParseObject> pollQuery = new ParseQuery<>(ParseConstants.CLASS_POLL);
            pollQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, pollObjectId);
            pollQuery.findInBackground((poll, e) -> {
                if (e == null) for (ParseObject pollObject : poll) {
                    List<String> votedBy = pollObject.getList(ParseConstants.KEY_POLL_VOTED_BY);
                    // Log.w(getClass().toString(), "Voted by: " + votedBy);
                    query.addAscendingOrder(ParseConstants.KEY_USERNAME);
                    query.whereContainedIn(ParseConstants.KEY_OBJECT_ID, votedBy);
                    if (!isOnline) {
                        query.fromLocalDatastore();
                    }
                    query.findInBackground((users, e2) -> {

                        mSwipeRefreshLayout.setRefreshing(false);

                        if (e2 == null) {
                            // We found users!
                            mUsers = users;
                            ParseObject.pinAllInBackground(mUsers);

                            // Attach adapter to RecyclerView
                            UsersListAdapter adapter = new UsersListAdapter(getApplicationContext(), users);
                            adapter.setHasStableIds(true);
                            mRecyclerView.setHasFixedSize(true);
                            adapter.notifyDataSetChanged();
                            mRecyclerView.setAdapter(adapter);
                        }
                    });
                }
            });
        }
    }
}