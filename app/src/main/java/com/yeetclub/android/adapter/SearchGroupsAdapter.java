package com.yeetclub.android.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.yeetclub.android.R;
import com.yeetclub.android.activity.GroupsActivity;
import com.yeetclub.android.parse.ParseConstants;
import com.yeetclub.android.utility.NetworkHelper;

import java.util.List;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class SearchGroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ParseObject> mGroups;

    public SearchGroupsAdapter(Context context, List<ParseObject> groups) {
        super();

        this.mGroups = groups;
        this.mContext = context;
        SearchGroupsAdapter adapter = this;
    }


    private void retrieveGroupObjectId(ParseObject group, ViewHolder holder, View v) {

        boolean isOnline = NetworkHelper.isOnline(mContext);
        if (!isOnline) {
            Toast.makeText(mContext, R.string.cannot_retrieve_messages, Toast.LENGTH_SHORT).show();
        } else {
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
            // v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getRootView().getContext());
            alertDialog.setMessage("Do you want to add " + holder.groupName.getText().toString() + " to your list of saved clubs?");
            alertDialog.setTitle("Save Club?");
            alertDialog.setIcon(R.drawable.ic_tab_poo);

            alertDialog.setPositiveButton("YES",
                    (arg0, arg1) -> {

                        // Add public group to current User's list of saved groups
                        addGroup(group);

                    });

            alertDialog.setNegativeButton("NO",
                    (arg0, arg1) -> {
                        // Hide AlertDialog
                    });

            alertDialog.show();
        }
    }


    private void addGroup(ParseObject group) {
        String groupObjectId = group.getObjectId();

        ParseUser currentUser = ParseUser.getCurrentUser();

        // Add public group to current User's list of saved groups
        List<String> myGroups = currentUser.getList(ParseConstants.KEY_MY_GROUPS);
        if (!(myGroups.contains(groupObjectId))) {
            myGroups.add(groupObjectId);
            Toast.makeText(mContext, "Club saved successfully", Toast.LENGTH_SHORT).show();
            currentUser.put("myGroups", myGroups);

            // Save the new User
            currentUser.saveInBackground(arg0 -> {
                startGroupsActivity();
            });
        } else {
            Toast.makeText(mContext, "You have already saved this group", Toast.LENGTH_SHORT).show();
        }

    }


    private void startGroupsActivity() {
        Intent intent = new Intent(mContext, GroupsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


    @Override
    public SearchGroupsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.groups_listview_item, parent, false);
        return new SearchGroupsAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh1 = (ViewHolder) holder;
        configureViewHolder1(vh1, position);
    }


    private void configureViewHolder1(ViewHolder holder, int position) {
        // Define a single ParseObject from a list of ParseUser objects, i.e. private List<ParseUser> mUsers;
        final ParseObject group = mGroups.get(position);

        // Define Typeface Lato-Bold
        Typeface tfBold = Typeface.createFromAsset(mContext.getAssets(), "fonts/Lato-Bold.ttf");

        // Retrieve group name
        holder.groupName.setTypeface(tfBold);
        if (group.getString(ParseConstants.KEY_GROUP_NAME) != null) {
            holder.groupName.setText(group.getString(ParseConstants.KEY_GROUP_NAME));
        } else {
            holder.groupName.setText(R.string.anonymous_loses);
        }

        holder.groupName.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
            retrieveGroupObjectId(group, holder, v);
        });

        // Retrieve group description
        if (group.getString(ParseConstants.KEY_GROUP_DESCRIPTION) != null) {
            holder.groupDescription.setText(group.getString(ParseConstants.KEY_GROUP_DESCRIPTION));
        } else {
            holder.groupDescription.setVisibility(View.GONE);
        }

        // Retrieve group profile picture
        downloadProfilePicture(holder, group);

        // Launch user profile from profile picture
        holder.groupProfilePicture.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
            retrieveGroupObjectId(group, holder, v);
        });

        // Launch user profile from itemView
        holder.itemView.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
            retrieveGroupObjectId(group, holder, v);
        });
    }


    private void downloadProfilePicture(ViewHolder holder, ParseObject user) {
        if (user.getParseFile(ParseConstants.KEY_PROFILE_PICTURE) != null) {
            String profilePictureURL = user.getParseFile(ParseConstants.KEY_PROFILE_PICTURE).getUrl();

            // Asynchronously display the profile picture downloaded from Parse
            if (profilePictureURL != null) {

                Picasso.with(mContext)
                        .load(profilePictureURL)
                        .placeholder(R.color.placeholderblue)
                        .fit()
                        .into(holder.groupProfilePicture);

            } else {
                holder.groupProfilePicture.setImageResource(R.drawable.ic_profile_pic_add);
            }
        } else {
            holder.groupProfilePicture.setImageResource(R.drawable.ic_profile_pic_add);
        }
    }


    @Override
    public int getItemCount() {
        if (mGroups == null) {
            return 0;
        } else {
            return mGroups.size();
        }
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }


    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        ImageView groupProfilePicture;
        TextView groupDescription;

        ViewHolder(View itemView) {
            super(itemView);

            groupName = (TextView) itemView.findViewById(R.id.groupName);
            groupProfilePicture = (ImageView) (itemView.findViewById(R.id.groupProfilePicture));
            groupDescription = (TextView) (itemView.findViewById(R.id.groupDescription));

        }
    }

}
