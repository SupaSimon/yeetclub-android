package com.yeetclub.android.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.yeetclub.android.R;
import com.yeetclub.android.parse.ParseConstants;
import com.yeetclub.android.utility.NetworkHelper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class EditGroupActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO = 2;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set typeface for action bar title
        Typeface tf_reg = Typeface.createFromAsset(getAssets(), "fonts/Lato-Regular.ttf");
        TextView feedTitle = (TextView) findViewById(R.id.edit_profile_title);
        feedTitle.setTypeface(tf_reg);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initiate ParseQuery
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        boolean isOnline = NetworkHelper.isOnline(this);

        // Hide or show views associated with network state
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        ll.setVisibility(isOnline ? View.VISIBLE : View.GONE);
        findViewById(R.id.submitProfileChanges).setVisibility(isOnline ? View.VISIBLE : View.GONE);

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        userQuery.findInBackground((users, e) -> {
            if (e == null) for (ParseObject userObject : users) {
                // Retrieve the objectId of the user's current group
                String currentGroupObjectId = userObject.getParseObject(ParseConstants.KEY_CURRENT_GROUP).getObjectId();

                ParseQuery<ParseObject> groupQuery = new ParseQuery<>(ParseConstants.CLASS_GROUP);
                groupQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, currentGroupObjectId);
                groupQuery.findInBackground((groups, e2) -> {
                    // Find the Group object with the current User's Group objectId
                    if (e2 == null) for (ParseObject groupObject : groups) {

                        final EditText nameField = (EditText) findViewById(R.id.name);
                        final EditText bioField = (EditText) findViewById(R.id.bio);
                        final ToggleButton privateToggle = (ToggleButton) findViewById(R.id.privateToggle);

                        checkPrivate(privateToggle, groupObject);

                        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
                        mSwipeRefreshLayout.setOnRefreshListener(() -> {
                            if (!isOnline) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            } else {
                                if (mSwipeRefreshLayout.isRefreshing()) {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }

                                createProfileHeader(groupObject, nameField, bioField);
                            }
                        });

                        setSubmitProfileChangesClickListener(groupObject, nameField, bioField, privateToggle);

                        setProfilePictureClickListener();

                        // Populate the profile information from Parse
                        createProfileHeader(groupObject, nameField, bioField);
                    }
                });
            }
        });
    }


    private void checkPrivate(ToggleButton privateToggle, ParseObject groupObject) {

        Boolean isPrivate = groupObject.getBoolean(ParseConstants.KEY_GROUP_PRIVATE);
        if (!isPrivate) {
            privateToggle.toggle();
        }

    }


    private void setSubmitProfileChangesClickListener(ParseObject groupObject, EditText nameField, EditText bioField, ToggleButton privateToggle) {
        findViewById(R.id.submitProfileChanges).setOnClickListener(v -> {

            // Update user
            groupObject.put("name", nameField.getText().toString());
            groupObject.put("description", bioField.getText().toString());
            if (privateToggle.isChecked()) {
                groupObject.put("private", false);
            } else {
                groupObject.put("private", true);
            }

            // Set profile picture
            ImageView imageView = (ImageView) findViewById(R.id.profile_picture);
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] image = stream.toByteArray();
            ParseFile file = new ParseFile("profilePicture.png", image);
            groupObject.put("profilePicture", file);

            groupObject.saveInBackground(e -> {
                        finish();
                        Toast.makeText(getApplicationContext(), "Club updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, GroupsActivity.class);
                        startActivity(intent);
                    }
            );
        });
    }


    private void createProfileHeader(ParseObject groupObject, EditText nameField, EditText bioField) {

        Typeface tf_reg = Typeface.createFromAsset(getAssets(), "fonts/Lato-Regular.ttf");

        // Set typefaces for text fields
        nameField.setTypeface(tf_reg);
        bioField.setTypeface(tf_reg);

        // Set text
        nameField.setText(groupObject.getString("name"));
        bioField.setText(groupObject.getString("description"));

        if (groupObject.getString("name") != null) {
            String nameFieldText = groupObject.getString("name");
            nameField.setText(nameFieldText);
        }

        if (groupObject.getString("description") != null) {
            String bioFieldText = groupObject.getString("description");
            bioField.setText(bioFieldText);
        }

        if (groupObject.getParseFile("profilePicture") != null) {

            Picasso.with(getApplicationContext())
                    .load(groupObject.getParseFile("profilePicture").getUrl())
                    .placeholder(R.color.placeholderblue)
                    .memoryPolicy(MemoryPolicy.NO_CACHE).into(((ImageView) findViewById(R.id.profile_picture)));

            fadeInProfilePicture();
        }
    }


    private void setProfilePictureClickListener() {

        ImageView profilePicture = (ImageView) findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(view -> {

            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));
            ChangeProfilePicture();

        });
    }


    public void ChangeProfilePicture() {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    Bitmap croppedThumbnail = ThumbnailUtils.extractThumbnail(yourSelectedImage, 144, 144, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                    ImageView selectedProfilePicture = (ImageView) findViewById(R.id.profile_picture);
                    selectedProfilePicture.setImageBitmap(croppedThumbnail);
                }
        }
    }


    // Relaunches UserProfileActivity
    public void RefreshGalleryActivity() {
        Toast.makeText(getApplicationContext(), "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
        finish();
        Intent intent = new Intent(this, EditGroupActivity.class);
        startActivity(intent);
    }


    private void fadeInProfilePicture() {
        Animation animFadeIn;
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        ImageView profilePicture = (ImageView) findViewById(R.id.profile_picture);
        profilePicture.setAnimation(animFadeIn);
        profilePicture.setVisibility(View.VISIBLE);
    }


    // Relaunches the activity
    public void RefreshActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

}
