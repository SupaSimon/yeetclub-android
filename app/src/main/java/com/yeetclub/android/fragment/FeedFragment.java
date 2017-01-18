package com.yeetclub.android.fragment;

/**
 * Created by @santafebound on 2016-09-29.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yalantis.phoenix.PullToRefreshView;
import com.yeetclub.android.R;
import com.yeetclub.android.adapter.FeedAdapter;
import com.yeetclub.android.parse.ParseConstants;
import com.yeetclub.android.utility.DividerItemDecoration;
import com.yeetclub.android.utility.EndlessRecyclerViewScrollListener;
import com.yeetclub.android.utility.NetworkHelper;
import com.yeetclub.android.utility.PreCachingLayoutManager;

import java.util.Date;
import java.util.List;

import static com.yeetclub.android.R.id.recyclerView;

public class FeedFragment extends Fragment {

    private static final String TAG = FeedFragment.class.getSimpleName();
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";

    private FeedAdapter adapter;

    private EndlessRecyclerViewScrollListener scrollListener;

    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected PreCachingLayoutManager mLayoutManager;

    protected List<ParseObject> mYeets;
    protected PullToRefreshView mSwipeRefreshLayout;

    public FeedFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();

        // Is the network online?
        boolean isOnline = NetworkHelper.isOnline(getContext());

        // Retrieve Data from remote server
        retrieveData(isOnline, 0);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set view
        View rootView = inflater.inflate(R.layout.tab_fragment_1, container, false);
        rootView.setTag(TAG);

        // Initialize SwipeRefreshLayout
        mSwipeRefreshLayout = (PullToRefreshView) rootView.findViewById(R.id.swipeRefreshLayout);

        // Set RecyclerView layout
        mRecyclerView = (RecyclerView) rootView.findViewById(recyclerView);

        // For image caching
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new PreCachingLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        setRecyclerViewLayoutManager(LayoutManagerType.LINEAR_LAYOUT_MANAGER);

        // Is the network online?
        boolean isOnline = NetworkHelper.isOnline(getContext());

        // Retrieve Data from remote server
        retrieveData(isOnline, 0);

        return rootView;
    }

    private void retrieveData(boolean isOnline, int skip) {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        userQuery.findInBackground((users, e) -> {
            if (e == null) for (ParseObject userObject : users) {
                // Retrieve the objectId of the user's current group
                String currentGroupObjectId = userObject.getParseObject(ParseConstants.KEY_CURRENT_GROUP).getObjectId();
                // Log.w(TAG, currentGroupObjectId);

                // Use the groupId to query the appropriate Yeets for the user's current group
                ParseQuery<ParseObject> yeetQuery = new ParseQuery<>(ParseConstants.CLASS_YEET);
                yeetQuery.whereContains(ParseConstants.KEY_GROUP_ID, currentGroupObjectId);
                yeetQuery.orderByDescending(ParseConstants.KEY_LAST_REPLY_UPDATED_AT);
                yeetQuery.setSkip(skip);
                yeetQuery.setLimit(20);
                if (!isOnline) {
                    yeetQuery.fromLocalDatastore();
                }
                yeetQuery.findInBackground((yeets, e3) -> {

                    mSwipeRefreshLayout.setRefreshing(false);

                    if (e3 == null) {

                        // We found Yeets!
                        mYeets = yeets;
                        ParseObject.pinAllInBackground(mYeets);
                        /*System.out.println(yeets);*/

                        adapter = new FeedAdapter(getContext(), yeets);
                        adapter.setHasStableIds(true);
                        mRecyclerView.setHasFixedSize(true);
                        adapter.notifyDataSetChanged();
                        mRecyclerView.setAdapter(adapter);

                        // Scroll listener
                        scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) mLayoutManager) {
                            @Override
                            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                                // Triggered only when new data needs to be appended to the list
                                // Add whatever code is needed to append new items to the bottom of the list

                                // Retrieve Data from remote server
                                addData(isOnline, adapter, 20);
                            }
                        };
                        // Adds the scroll listener to RecyclerView
                        mRecyclerView.addOnScrollListener(scrollListener);

                        // Set swipe refresh listener for adding new messages
                        mSwipeRefreshLayout.setOnRefreshListener(() -> {
                            if (!isOnline) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(getContext(), getString(R.string.cannot_retrieve_messages), Toast.LENGTH_SHORT).show();
                            } else {
                                Date onRefreshDate = new Date();
                                        /*System.out.println(onRefreshDate.getTime());*/
                                refreshData(onRefreshDate, adapter);
                            }
                        });
                    }
                });
            }
        });
    }


    private void refreshData(Date date, FeedAdapter adapter) {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        userQuery.findInBackground((users, e) -> {
            if (e == null) for (ParseObject userObject : users) {
                // Retrieve the objectId of the user's current group
                String currentGroupObjectId = userObject.getParseObject(ParseConstants.KEY_CURRENT_GROUP).getObjectId();

                // Use the groupId to query the appropriate Yeets for the user's current group
                ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstants.CLASS_YEET);
                query.whereContains(ParseConstants.KEY_GROUP_ID, currentGroupObjectId);
                query.orderByDescending(ParseConstants.KEY_LAST_REPLY_UPDATED_AT);
                if (date != null)
                    query.whereLessThanOrEqualTo(ParseConstants.KEY_CREATED_AT, date);
                query.setLimit(1000);
                query.findInBackground((yeets, e3) -> {

                    mSwipeRefreshLayout.setRefreshing(false);

                    if (e3 == null) {

                        // We found Yeets!
                        mYeets.removeAll(yeets);
                        mYeets.addAll(0, yeets); // Append new messages to the top
                        adapter.notifyDataSetChanged();
                        ParseObject.pinAllInBackground(mYeets);

                                /*System.out.println(yeets);*/
                        if (mRecyclerView.getAdapter() == null) {
                            adapter.setHasStableIds(true);
                            mRecyclerView.setHasFixedSize(true);
                            adapter.notifyDataSetChanged();
                            mRecyclerView.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }


    private void addData(boolean isOnline, FeedAdapter adapter, int skip) {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        userQuery.findInBackground((users, e) -> {
            if (e == null) for (ParseObject userObject : users) {
                // Retrieve the objectId of the user's current group
                String currentGroupObjectId = userObject.getParseObject(ParseConstants.KEY_CURRENT_GROUP).getObjectId();

                // Use the groupId to query the appropriate Yeets for the user's current group
                ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstants.CLASS_YEET);
                query.whereContains(ParseConstants.KEY_GROUP_ID, currentGroupObjectId);
                query.orderByDescending(ParseConstants.KEY_LAST_REPLY_UPDATED_AT);
                query.setSkip(skip);
                query.setLimit(20);
                if (!isOnline) {
                    query.fromLocalDatastore();
                }
                query.findInBackground((yeets, e3) -> {

                    mSwipeRefreshLayout.setRefreshing(false);

                            if (e3 == null) {

                                // We found messages!
                                if (mYeets.size() > 10) {
                                    mYeets.addAll(skip, yeets); // Append new messages to the bottom
                            adapter.notifyDataSetChanged();
                        }

                        ParseObject.pinAllInBackground(mYeets);

                        mSwipeRefreshLayout.setOnRefreshListener(() -> {
                            if (!isOnline) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(getContext(), getString(R.string.cannot_retrieve_messages), Toast.LENGTH_SHORT).show();
                            } else {
                                Date onRefreshDate = new Date();
                                        /*System.out.println(onRefreshDate.getTime());*/
                                refreshData(onRefreshDate, adapter);
                            }
                        });
                    }
                });
            }
        });
    }


    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new PreCachingLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new PreCachingLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }
}
