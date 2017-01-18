package com.yeetclub.android.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.yeetclub.android.R;
import com.yeetclub.android.adapter.GroupsAdapter;
import com.yeetclub.android.parse.ParseConstants;
import com.yeetclub.android.utility.NetworkHelper;

import java.util.List;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class GroupsActivity extends AppCompatActivity {

    protected List<ParseObject> mYeets;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView recyclerView;

    public GroupsActivity() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_groups);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set typeface for action bar title
        Typeface tf_reg = Typeface.createFromAsset(getAssets(), "fonts/Lato-Regular.ttf");
        TextView feedTitle = (TextView) findViewById(R.id.feed_title);
        feedTitle.setTypeface(tf_reg);

        boolean isOnline = NetworkHelper.isOnline(this);

        /*ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        userQuery.findInBackground((users, e) -> {
            if (e == null) for (ParseObject userObject : users) {
                // Retrieve the objectId of the user's current group
                String currentGroupObjectId = userObject.getParseObject(ParseConstants.KEY_CURRENT_GROUP).getObjectId();
            }
        });*/

        // Set logic for swipe-to-refresh
        setSwipeRefreshLayout(isOnline);

        // Populate list with groups belonging to the current User
        initialise(isOnline);

        // Create current group header information
        createGroupHeader();

        // Floating action button
        showFloatingActionButton();

    }

    private void showFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#169cee")));
        fab.setOnClickListener(view -> {

            Intent intent = new Intent(GroupsActivity.this, YeetActivity.class);
            startActivity(intent);

        });
    }


    private boolean initialise(Boolean isOnline) {

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        retrieveGroups(isOnline);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isOnline) {
                mSwipeRefreshLayout.setRefreshing(false);
            } else {
                createGroupHeader();
                retrieveGroups(true);
            }
        });

        return isOnline;
    }


    private void setSwipeRefreshLayout(boolean isOnline) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isOnline) {
                mSwipeRefreshLayout.setRefreshing(false);
            } else {
                mSwipeRefreshLayout.setRefreshing(false);

                // Set data to adapter
                retrieveGroups(true);

                // Update user profile data
                createGroupHeader();

            }
        });
    }


    private void createGroupHeader() {
        ImageView topLevelGroupProfilePicture = (ImageView) findViewById(R.id.groupProfilePicture);
        TextView topLevelGroupName = (TextView) findViewById(R.id.groupName);
        TextView topLevelGroupDescription = (TextView) findViewById(R.id.groupDescription);

        Typeface tf_reg = Typeface.createFromAsset(getAssets(), "fonts/Lato-Regular.ttf");
        Typeface tf_black = Typeface.createFromAsset(getAssets(), "fonts/Lato-Black.ttf");

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        userQuery.findInBackground((users, e) -> {
            if (e == null) for (ParseObject userObject : users) {
                // Retrieve the objectId of the user's current group
                String currentGroupObjectId = userObject.getParseObject(ParseConstants.KEY_CURRENT_GROUP).getObjectId();

                // Use the group objectId to query the actual groupId of the queried group
                ParseQuery<ParseObject> groupQuery = new ParseQuery<>(ParseConstants.CLASS_GROUP);
                groupQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, currentGroupObjectId);
                groupQuery.findInBackground((groups, e2) -> {
                    if (e2 == null) for (ParseObject groupObject : groups) {

                        // Set Group name
                        if (groupObject.getString(ParseConstants.KEY_GROUP_NAME) != null) {
                            if (groupObject.getString(ParseConstants.KEY_GROUP_NAME).isEmpty()) {
                                topLevelGroupName.setVisibility(View.GONE);
                            }

                            String topLevelGroupNameText = groupObject.getString(ParseConstants.KEY_GROUP_NAME);
                            topLevelGroupName.setText(topLevelGroupNameText);
                            topLevelGroupName.setTypeface(tf_black);
                        } else {
                            topLevelGroupName.setVisibility(View.GONE);
                        }

                        // Set Group description
                        if (groupObject.getString(ParseConstants.KEY_GROUP_DESCRIPTION) != null) {
                            if (groupObject.getString(ParseConstants.KEY_GROUP_DESCRIPTION).isEmpty()) {
                                topLevelGroupDescription.setVisibility(View.GONE);
                            }

                            String topLevelGroupDescriptionText = groupObject.getString(ParseConstants.KEY_GROUP_DESCRIPTION);
                            topLevelGroupDescription.setText(topLevelGroupDescriptionText);
                            topLevelGroupDescription.setTypeface(tf_reg);
                        } else {
                            topLevelGroupDescription.setVisibility(View.GONE);
                        }

                        // Set Group profile picture
                        if (groupObject.getParseFile(ParseConstants.KEY_PROFILE_PICTURE) != null) {

                            Picasso.with(getApplicationContext())
                                    .load(groupObject.getParseFile(ParseConstants.KEY_PROFILE_PICTURE).getUrl())
                                    .placeholder(R.color.placeholderblue)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE).into(((ImageView) findViewById(R.id.groupProfilePicture)));

                            fadeInProfilePicture();

                        } else {
                            topLevelGroupProfilePicture.setImageResource(Integer.parseInt(String.valueOf(R.drawable.ic_profile_pic_add)));
                        }

                    }
                });

            }
        });
    }


    private void fadeInProfilePicture() {
        Animation animFadeIn;
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        ImageView profilePicture = (ImageView) findViewById(R.id.groupProfilePicture);
        profilePicture.setAnimation(animFadeIn);
        profilePicture.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Check if current User is the admin of the currently selected group, then show edit group option
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        userQuery.findInBackground((users, e) -> {
            if (e == null) for (ParseObject userObject : users) {

                String currentGroupObjectId = userObject.getParseObject(ParseConstants.KEY_CURRENT_GROUP).getObjectId();

                ParseQuery<ParseObject> groupQuery = new ParseQuery<>(ParseConstants.CLASS_GROUP);
                groupQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, currentGroupObjectId);
                groupQuery.findInBackground((groups, e2) -> {
                    // Find the single Comment object associated with the current ListAdapter position
                    if (e2 == null) for (ParseObject groupObject : groups) {
                        // Retrieve the admin List for the user's current group
                        List<String> currentUserAdminList = groupObject.getList(ParseConstants.KEY_GROUP_ADMIN_LIST);
                        String currentUserObjectId = ParseUser.getCurrentUser().getObjectId();

                        // If the current User is on that list, show the "Edit Group" menu option
                        if (currentUserAdminList.contains(currentUserObjectId)) {
                            MenuInflater inflater = getMenuInflater();
                            inflater.inflate(R.menu.settings_groups, menu);
                        }
                    }
                });

            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_group:
                Intent intent = new Intent(this, EditGroupActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_create_group:
                Intent intent2 = new Intent(this, CreateGroupActivity.class);
                startActivity(intent2);
                return true;
            case R.id.action_search_groups:
                Intent intent3 = new Intent(this, SearchGroupsActivity.class);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    private void retrieveGroups(Boolean isOnline) {

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        userQuery.findInBackground((users, e) -> {
            if (e == null) for (ParseObject userObject : users) {
                // Query the Group class to retrieve the groups that the current User belongs to
                ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstants.CLASS_GROUP);

                List<String> myGroups = userObject.getList(ParseConstants.KEY_MY_GROUPS);
                Log.w(getClass().toString(), myGroups.toString());

                query.whereContainedIn(ParseConstants.KEY_OBJECT_ID, myGroups);
                query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
                query.findInBackground((yeets, e2) -> {

                    mSwipeRefreshLayout.setRefreshing(false);

                    if (e2 == null) {

                        // We found groups!
                        mYeets = yeets;
                        ParseObject.pinAllInBackground(mYeets);

                        GroupsAdapter adapter = new GroupsAdapter(getApplicationContext(), yeets);
                        adapter.setHasStableIds(true);
                        /*RecyclerViewHeader header = (RecyclerViewHeader) findViewById(R.id.header);
                        header.attachTo(recyclerView);*/
                        recyclerView.setAdapter(adapter);

                        mSwipeRefreshLayout.setOnRefreshListener(() -> {
                            if (!isOnline) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(getApplicationContext(), getString(R.string.cannot_retrieve_messages), Toast.LENGTH_SHORT).show();
                            } else {
                                createGroupHeader();
                                retrieveGroups(true);
                            }
                        });

                    }
                });
            }
        });


    }

}