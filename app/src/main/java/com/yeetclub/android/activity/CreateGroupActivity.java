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

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.yeetclub.android.R;
import com.yeetclub.android.parse.ParseConstants;
import com.yeetclub.android.utility.NetworkHelper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.parse.ParseUser.getCurrentUser;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class CreateGroupActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO = 2;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

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
        ParseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return;
        }

        boolean isOnline = NetworkHelper.isOnline(this);

        // Hide or show views associated with network state
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        ll.setVisibility(isOnline ? View.VISIBLE : View.GONE);
        findViewById(R.id.submitProfileChanges).setVisibility(isOnline ? View.VISIBLE : View.GONE);

        final EditText nameField = (EditText) findViewById(R.id.name);
        final EditText bioField = (EditText) findViewById(R.id.bio);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isOnline) {
                mSwipeRefreshLayout.setRefreshing(false);
            } else {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                createProfileHeader(nameField, bioField);
            }
        });

        setSubmitProfileChangesClickListener(nameField, bioField);

        setProfilePictureClickListener();

        // Populate the profile information from Parse
        createProfileHeader(nameField, bioField);
    }


    private void setSubmitProfileChangesClickListener(EditText nameField, EditText bioField) {
        findViewById(R.id.submitProfileChanges).setOnClickListener(v -> {

            // Create new group
            ParseObject newGroup = new ParseObject(ParseConstants.CLASS_GROUP);

            // Set standard fields
            newGroup.put("name", nameField.getText().toString());
            newGroup.put("description", bioField.getText().toString());
            newGroup.put("private", true);

            // Set default secret key
            String idOne = UUID.randomUUID().toString();
            newGroup.put("secretKey", idOne);

            // Set initial admin list
            String currentUserObjectId = ParseUser.getCurrentUser().getObjectId();
            String[] currentUserAdminList = {currentUserObjectId};
            newGroup.put(ParseConstants.KEY_GROUP_ADMIN_LIST, Arrays.asList(currentUserAdminList));

            // Set profile picture
            ImageView imageView = (ImageView) findViewById(R.id.profile_picture);
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] image = stream.toByteArray();
            ParseFile file = new ParseFile("profilePicture.png", image);
            newGroup.put("profilePicture", file);

            newGroup.saveInBackground(e -> {
                        Toast.makeText(getApplicationContext(), "Club created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(this, GroupsActivity.class);
                        startActivity(intent);

                        String newGroupObjectId = newGroup.getObjectId();

                        ParseUser currentUser = ParseUser.getCurrentUser();

                        // Update myGroups list
                        List<String> myGroups = currentUser.getList(ParseConstants.KEY_MY_GROUPS);
                        myGroups.add(newGroupObjectId);
                        currentUser.put("myGroups", myGroups);

                    }
            );
        });
    }


    private void createProfileHeader(EditText nameField, EditText bioField) {

        Typeface tf_reg = Typeface.createFromAsset(getAssets(), "fonts/Lato-Regular.ttf");

        // Set typefaces for text fields
        nameField.setTypeface(tf_reg);
        bioField.setTypeface(tf_reg);

        // fadeInProfilePicture();
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
        Intent intent = new Intent(this, CreateGroupActivity.class);
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
