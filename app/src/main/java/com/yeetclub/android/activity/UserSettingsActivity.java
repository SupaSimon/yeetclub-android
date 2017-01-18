package com.yeetclub.android.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yeetclub.android.R;
import com.yeetclub.android.parse.ParseConstants;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class UserSettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.PreferenceScreen);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_settings);

        Preference shareKeyPref = findPreference("shareKey");
        shareKeyPref.setOnPreferenceClickListener(preference -> {

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

                            String shareKey = groupObject.getString("secretKey");

                            // Copy Yecret Key to clipboard to share with friends in case external application does not support intent extras
                            android.content.ClipboardManager clipboardclipboardMarket = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clipText = ClipData.newPlainText("Share Club Key", shareKey);
                            clipboardclipboardMarket.setPrimaryClip(clipText);
                            Toast.makeText(getApplicationContext(), "Club Key copied to clipboard", Toast.LENGTH_SHORT).show();

                            // Generate social media share options
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, shareKey);
                            sendIntent.setType("text/plain");
                            startActivity(sendIntent);

                        }
                    });
                }
            });

            return false;
        });

        Preference privacyPolicyPref = findPreference("privacyPolicy");
        privacyPolicyPref.setOnPreferenceClickListener(preference -> {
            String url = "http://www.yeet.club/#/privacy";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return false;
        });

        Preference termsOfServicePref = findPreference("termsOfService");
        termsOfServicePref.setOnPreferenceClickListener(preference -> {
            String url = "http://www.yeet.club/#/terms";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return false;
        });

        Preference deleteAllPref = findPreference("deleteAll");
        deleteAllPref.setOnPreferenceClickListener(preference -> {

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(UserSettingsActivity.this);
            dialogBuilder.setTitle("Delete");
            dialogBuilder.setMessage("Do you want to delete all Yeets?");
            dialogBuilder.setPositiveButton("Yes", (dialog, which) -> {

                deleteAllComments();
                deleteAllYeets();
                sendMyFirstYeet();

                Toast.makeText(getApplicationContext(), "All Yeets deleted", Toast.LENGTH_SHORT).show();

                finish();

            });
            dialogBuilder.setNegativeButton("No", (dialog, which) -> {
            });
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();

            return false;
        });

        Preference updatePref = findPreference("update");
        updatePref.setOnPreferenceClickListener(preference -> {
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
            return false;
        });

        Preference logOutPref = findPreference("logOut");
        logOutPref.setOnPreferenceClickListener(preference -> {

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(UserSettingsActivity.this);
            dialogBuilder.setTitle("Log Out");
            dialogBuilder.setPositiveButton("Yes", (dialog, which) -> {

                // Log out the user
                ParseUser.logOut();
                Intent logOut = new Intent(UserSettingsActivity.this, DispatchActivity.class);
                startActivity(logOut);

            });
            dialogBuilder.setNegativeButton("No", (dialog, which) -> {
            });
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();

            return false;
        });

        final Preference editProfilePref = findPreference("editProfile");
        editProfilePref.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(UserSettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);

            return false;
        });

        final Preference changePasswordPref = findPreference("changePassword");
        changePasswordPref.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(UserSettingsActivity.this, ChangePasswordActivity.class);
            startActivity(intent);

            return false;
        });

        // Unscrubribe from push notifications
        final Preference notificationsOffPref = findPreference("notificationsOff");
        notificationsOffPref.setOnPreferenceClickListener(preference -> {

            ParsePush.unsubscribeInBackground("", e -> {
                if (e == null) {
                    Log.d("com.parse.push", "successfully unsubscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to unsubscribe for push", e);
                }
            });
            Toast.makeText(getApplicationContext(), "You have successfully disabled push notifications.", Toast.LENGTH_SHORT).show();

            return false;
        });

        // Subscribe to all push notifications
        final Preference notificationsOnPref = findPreference("notificationsOn");
        notificationsOnPref.setOnPreferenceClickListener(preference -> {

            // Subscribe to push notifications
            ParsePush.subscribeInBackground("", e -> {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            });
            Toast.makeText(getApplicationContext(), "You have successfully enabled push notifications.", Toast.LENGTH_SHORT).show();

            return false;
        });

        // Turn off all application sounds
        final Preference soundsOffPref = findPreference("soundsOff");
        soundsOffPref.setOnPreferenceClickListener(preference -> {

            int storedPreference = 0;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("sound", storedPreference); // Turn off "Yeet" and other application sounds
            editor.apply();
            Toast.makeText(getApplicationContext(), "You have successfully disabled all application sounds.", Toast.LENGTH_SHORT).show();

            return false;
        });

        // Turn on all application sounds
        final Preference soundsOnPref = findPreference("soundsOn");
        soundsOnPref.setOnPreferenceClickListener(preference -> {

            int storedPreference = 1;

            // Enable all application sounds
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("sound", storedPreference); // Turn off "Yeet" and other application sounds
            editor.apply();
            Toast.makeText(getApplicationContext(), "You have successfully enabled all application sounds.", Toast.LENGTH_SHORT).show();

            return false;
        });

    }

    private void deleteAllYeets() {
        ParseQuery<ParseObject> yeetQuery = new ParseQuery<>(ParseConstants.CLASS_YEET);
        // Query the Comment class for comments that have a "author" column value equal to the objectId of the current User
        yeetQuery.whereEqualTo((ParseConstants.KEY_SENDER_AUTHOR_POINTER), ParseUser.getCurrentUser());
        yeetQuery.findInBackground((yeets, e) -> {
            if (e == null) {
                // Iterate over all messages
                for (ParseObject delete : yeets) {

                    // Delete messages from LocalDatastore
                    try {
                        delete.unpin();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    // Delete messages from Parse
                    delete.deleteInBackground();

                }

            } else {
                Log.e("Error", e.getMessage());
            }
        });
    }

    private void deleteAllComments() {
        ParseQuery<ParseObject> commentQuery = new ParseQuery<>(ParseConstants.CLASS_COMMENT);
        // Query the Comment class for comments that have a "author" column value equal to the objectId of the current User
        commentQuery.whereEqualTo((ParseConstants.KEY_SENDER_AUTHOR_POINTER), ParseUser.getCurrentUser());
        commentQuery.findInBackground((comments, e) -> {
            if (e == null) {
                // Iterate over all messages
                for (ParseObject delete : comments) {

                    // Delete messages from LocalDatastore
                    try {
                        delete.unpin();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    // Delete messages from Parse
                    delete.deleteInBackground();
                }
            } else {
                Log.e("Error", e.getMessage());
            }
        });
    }

    private void sendMyFirstYeet() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_YEET);

        message.put(ParseConstants.KEY_SENDER_AUTHOR_POINTER, ParseUser.getCurrentUser());

        // Initialize "likedBy" Array column
        String[] likedBy = new String[0];
        message.put(ParseConstants.KEY_LIKED_BY, Arrays.asList(likedBy));

        Date myDate = new Date();
        message.put(ParseConstants.KEY_LAST_REPLY_UPDATED_AT, myDate);

        message.put(ParseConstants.KEY_NOTIFICATION_TEXT, getString(R.string.my_first_yeet));

        String groupId = ParseUser.getCurrentUser().getString(ParseConstants.KEY_GROUP_ID);
        message.put(ParseConstants.KEY_GROUP_ID, groupId);

        if (ParseUser.getCurrentUser().getParseFile(ParseConstants.KEY_PROFILE_PICTURE) != null) {
            message.put(ParseConstants.KEY_SENDER_PROFILE_PICTURE, ParseUser.getCurrentUser().getParseFile(ParseConstants.KEY_PROFILE_PICTURE).getUrl());
        }

        message.saveEventually();
    }

}