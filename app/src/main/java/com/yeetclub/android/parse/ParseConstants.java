package com.yeetclub.android.parse;

/**
 * Created by @santafebound on 2015-11-07.
 */
public class ParseConstants {

    // Class names
    public static final String CLASS_NOTIFICATIONS = "Notification";
    public static final String CLASS_YEET = "Yeet";
    public static final String CLASS_COMMENT = "Comment";
    public static final String CLASS_LIKE = "Like";
    public static final String CLASS_POLL = "Poll";
    public static final String CLASS_GROUP = "Group";

    // Generic
    public static final String KEY_OBJECT_ID = "objectId";

    // Time-related
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_LAST_REPLY_UPDATED_AT = "lastReplyUpdatedAt";

    // User-related
    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_AUTHOR_FULL_NAME = "name";
    public static final String KEY_SENDER_AUTHOR_POINTER = "author";
    public static final String KEY_PROFILE_PICTURE = "profilePicture";
    public static final String KEY_CURRENT_GROUP = "currentGroup";
    public static final String KEY_USER_BIO = "bio";
    public static final String KEY_VERIFIED = "verified";

    // Group-related
    public static final String KEY_GROUP_NAME = "name";
    public static final String KEY_GROUP_DESCRIPTION = "description";
    public static final String KEY_MY_GROUPS = "myGroups";
    public static final String KEY_GROUP_ADMIN_LIST = "admin";
    public static final String KEY_GROUP_SECRET_KEY = "secretKey";
    public static final String KEY_GROUP_PRIVATE = "private";

    // Yeet-related
    public static final String KEY_REPLY_COUNT = "replyCount";
    public static final String KEY_LIKED_BY = "likedBy";
    public static final String KEY_COMMENT_OBJECT_ID = "commentObjectId";
    public static final String KEY_RANT_ID = "rantId";
    public static final String KEY_LIKE_COUNT = "likeCount";

    // Poll-related
    public static final String KEY_POLL_OBJECT = "pollObject";
    public static final String KEY_POLL_OPTION1 = "option1";
    public static final String KEY_POLL_OPTION2 = "option2";
    public static final String KEY_POLL_OPTION3 = "option3";
    public static final String KEY_POLL_OPTION4 = "option4";
    public static final String KEY_POLL_VALUE1 = "value1";
    public static final String KEY_POLL_VALUE2 = "value2";
    public static final String KEY_POLL_VALUE3 = "value3";
    public static final String KEY_POLL_VALUE4 = "value4";
    public static final String KEY_POLL_VOTED_BY ="votedBy";

    // Notification-related
    public static final String KEY_NOTIFICATION_TYPE = "notificationType";
    public static final String TYPE_LIKE = "typeLike";
    public static final String TYPE_COMMENT = "typeComment";
    public static final String KEY_READ_STATE = "read";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECIPIENT_ID = "recipientId";

    // Misc
    public static final String KEY_NOTIFICATION_TEXT = "notificationText";
    public static final String KEY_COMMENT_TEXT = "comment";
    public static final String KEY_NOTIFICATION_BODY = "notificationBody";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_SENDER_FULL_NAME = "senderFullName";
    public static final String KEY_SENDER_POST_POINTER = "post";
    public static final String KEY_SENDER_PARSE_OBJECT_ID = "senderParseObjectId";
    public static final String KEY_SENDER_PROFILE_PICTURE = "senderProfilePicture";

}
