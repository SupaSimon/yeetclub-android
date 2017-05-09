

![alt tag](http://www.elgami.com/images/yeet-club-banner.png)


# Yeet Club - An Exclusive App for Everybody
Yeet Club is an open-source Android application for small, private groups of exclusive friends. It runs on Parse Server and works a lot like Twitter.

# Table of Contents  
* [0.0 Vision](#vision)
* [1.0 Team & Past Contributors](#team)
* [2.0 Installation & Documentation](#installation)
  * [2.1 Trello](#trello)
  * [2.2 Back4App Registration](#back4app)
  * [2.3 Application Configuration](#config)
* [3.0 Database Schema](#schema)
  * [3.1 Classes](#classes)
    * [3.1.0 Installation](#installation)
    * [3.1.1 Role](#role)
    * [3.1.2 Session](#session)
    * [3.1.3 User](#user)
    * [3.1.4 Comment](#comment)
    * [3.1.5 Notification](#notification)
    * [3.1.6 Poll](#poll)
    * [3.1.7 Yeet](#yeet)
    * [3.1.8 Group](#group)
* [4.0 Push Notifications](#push)
* [5.0 Roadmap](#needs)
* [6.0 Third Party Libraries](#libs)
* [7.0 License](#license)
* [8.0 Trademark Information](#trademark)

<hr>

<a name="vision"></a>
## 0.0 Vision

If you are interested in contributing to the main repository here, you could very well see your changes reflected in the Yeet Club app at https://play.google.com/store/apps/details?id=com.yeetclub.android&hl=en. If you love Yeet Club and want to buy me coffee, send some beans this a-way: https://www.paypal.me/hypercycle

Yeet Club is an open-source Android application for small, private groups of exclusive friends. It works a lot like Twitter. My aim in creating Yeet Club was to create a synchronous messaging application that allowed my small group of friends to enjoy the benefits of Twitter without having to rely on their services. Given that 10% of Twitter's 100 million daily active users also enjoy private accounts, I thought it would be interesting to see how a truly private alternative might play out. I wonder, what percentage of Twitter's 10 million private users would be interested in a more customized experience?

Well, we were. So we added funny sounds, and included an RSS feed from a local news outlet to stay up to date for fun. We found that the 140 character limit was awesome for getting our thoughts off our chests without committing to grandiose explanations for how or why we felt what we felt, and so decided to leave that be (kind of like a condensed private journal without the intrusiveness of a "live" messaging application). But there's no reason that you need to use the same constraints (Yeet Club is open-source after all)! There is also a built-in rant feature that lets you send multiple messages in quick succession while notifying your friends that you're about to really speak your mind (lol!). Frankly, given the personalized nature of Yeet Club, I wouldn't be surprised if it found extensive use in counseling or group therapy. It's not that we're crazy, but having our own application definitely provides some peace of mind and let's us add and change things as we please.

You can either use the official Yeet Club application at  https://play.google.com/store/apps/details?id=com.yeetclub.android&hl=en and create a private group with your friends, or you can host the application on your own server using **parse-server:** https://github.com/ParsePlatform/parse-server, but you may find that using a cloud hosting solution such as **Back4App**: http://www.back4app.com/ might be easier to get started (Back4App installation documentation is provided below; private server installation wil not be covered until further notice).

Anyway, happy coding! If you're interested in contributing, please give us a shout either by email at **info@yeet.club** or by submitting a pull request with an explanation. We're eager to create a whole new kind of community with Yeet Club, and so I really do look forward to hearing from you. Cheers.

<hr>

<a name="team"></a>
## 1.0 Team & Past Contributors

* <a href="https://github.com/santafebound/">Martin Erlic</a>
* <a href="https://github.com/supasimon/">Simon Erlic</a>
* <a href="https://github.com/TheFanatr/">Alex Danut</a>

<hr>

<a name="installation"></a>
## 2.0 Installation & Documentation

<a name="trello"></a>
### 2.1 Trello

Trello Board: https://trello.com/b/uYy2BsuH/yeet-club

I'm using this board to organize my thoughts so far. Feel free to peruse it for inspiration. My current personal objectives are listed at the top of the **Priorities** column. If you'd like to contribute in any way, big or small, just get in touch!

<a name="back4app"></a>
### 2.2 Back4App Registration

If you want to host your own version of Yeet Club, that's fine too. In order to host your own version of Yeet Club, you will most likely want to create an account at www.back4app.com. Given that Yeet Club was originally developed with Parse, this means that Back4App is currently one of the few viable free hosting options out-of-box. Back4App is a Cloud BaaS (Back-End as a Service) solution that will host your database initially free of charge (that is, until your query requests reach a specified limit threshold per second). See https://www.back4app.com/pricing for additional pricing information at scale. I've found Back4App to be highly reliable so far, but if you are interested in hosting your own Parse Server elsewhere, there are tons of useful instructions and tutorials online. This article is a good place to start: https://medium.com/@timothy_whiting/setting-up-your-own-parse-server-568ee921333a

<a name="config"></a>
### 2.3 Application Configuration

You will find a class called **GlobalApplication** in the package <b>com.yeetclub.application</b>. This is where we initialize Parse, among other things. From your res/values/strings.xml file, add your own values to R.string.parse_app_id and R.string.parse_client_key with the Application Id and Client Key provided to you by Back4App (https://dashboard.back4app.com/ > Core Settings):

```java
Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId(getString(R.string.parse_app_id))
                .clientKey(getString(R.string.parse_client_key))
                .server("https://parseapi.back4app.com/")

        .build()
        );
```

Configuring your keys in the above code is all you have to do to establish a network connection to your database. Besides downloading the code and creating the appropriate Classes and attributes from the database schema documented below in your own database, this is all you have to do to get Yeet Club working on your own. **Please respect the Yeet Club trademark, however, and do not publish exact replicas to the PlayStore. They will be targeted for destruction! :)**

On the other hand, feel free to rebrand and repackage the app to suit your needs, and as always make sure to provide the included Apache 2.0 license with every copy you create and publish anywhere on the internet.

<hr>

<a name="schema"></a>
## 3.0 Database Schema

The complete database schema is documented below. Without configuring the following classes and attributes in your own Parse dashboard, downloading the source code probably won't be much help!

<a name="classes"></a>
### 3.1 Classes

For each Class, I've excluded the auto-generated default Parse columns, i.e. objectId, createdAt, updatedAt, ACL, etc. due to redundancy. Also, I've provided examples of object data for demonstration purposes only. If you are relatively unfamiliar with software development or have little experience with NoSQL databases in particular, you should be aware that you only need to create the columns with their provided titles, and should not by any means include a sample object like I have below. Doing otherwise could mess up your application; this data will be generated on its own when users start interacting with your application.

* Installation
* Role
* Session
* User
* Comment
* Notification
* Poll
* Yeet
* Group

<hr>

<a name="installation"></a>
#### 3.1.O Installation

This Class will be instantiated by default, but you will need to add a few extra columns to get Push Notifications working with your application. Please add the following columns to your Installation class:

| userId (String) | username (String) | profilePicture (String) |
|-----------------|-------------------|-------------------------|
| mBxXzIUZH3 | lumberg | https://memecrunch.com/image/51621675afa96f32ef000013.jpg?w=400 |

<a name="role"></a>
#### 3.1.1 Role

We do not do anything special with Role but I included it for completion.

<a name="session"></a>
#### 3.1.2 Session

We do not do anything special with Session but I included it for completion.

<a name="user"></a>
#### 3.1.3 User

| username (String) | name (String) | bio (String) | websiteLink (String) | profilePicture (File) | bae (String) | currentGroup (Pointer <_Group>) | myGroups (Array) | isRanting (boolean) |
|--------------------------|-----------------|-------------------|--------------|----------------------|---------------|----------------|-------------------------------------|-----------------|-----------------------|----------------------|-------------------|----------------|---------------|---------------|
| lumberg | Lumberg | Just make sure you go ahead and put that new cover letter on all your TPS reports next time, mmkay? | https://www.youtube.com/watch?v=kVmC0ktznNo | https://memecrunch.com/image/51621675afa96f32ef000013.jpg?w=400 | Scolding Cucks | BDkKvNQ3Co | ["BDkKvNQ3Co","v30ajFHwnA"] | false |

It might be worth explaining a few of these fields:

##### 3.1.3.1 bae

**bae** is an optional field that notifies other users who are visiting your profile who you consider to be bae, i.e. If you're a nice guy, then "being a nice guy" is bae; if you like EDM, then "Steve Aoki" is bae, etc.

##### 3.1.3.2 currentGroup

The **currentGroup** is a pointer to a Group assigned to a user upon registration. Each Group has its own additional "groupId", which is a String that acts as a shared password for the entire group. This idenitifier can easily be shared with users who would like to join your group upon sign up (see Settings). By default, all users are assigned to a default kind of "lobby for noobs" group. This Pointer value can be swapped out for other saved groups from the "myGroups (Array)" column, allowing a User to jump between groups to interact with different members.

##### 3.1.3.3 myGroups

An Array that stores a list of Groups to which the current User belongs. This Array may be accessed in order to switch out the User's current Group.

##### 3.1.3.4 isRanting

**isRanting** is a boolean that checks whether or not the current user is in rant mode. Rant mode allows the user to fire off successive yeets without having to continually press the button that lets you yeet new messages. Entering rant mode also notifies all group members when a user is ranting, and marks their posts in red so that the finished rant appears as a cohesive story. isRanting is by default set to false upon user registration, and is activated in various situations throughout the application where appropriate.

<a name="comment"></a>
#### 3.1.4 Comment

Comments are fairly self explanatory:

| author (Pointer <_User>) | comment (String) | likeCount (Number) | likedBy (Array) |
|--------------------------|-----------------------------------------|-----------------------------|------------------|
| mBxXzIUZH3 | Yeaaaahh, I'm going to need you to come in to the office tomorrow. | 1 | ["mBxXzIUZH3"] |

##### 3.1.4.1 likedBy

**likedBy** is an array that holds the objectIds of users who have liked your comment. This array presently works to stop push notifications from being sent to oneself when liking or replying to one's own comments, but it can be manipulated as seen fit, perhaps to display a list of users who have liked a particular comment.

<a name="notification"></a>
#### 3.1.5 Notification

The Notification class comprises the list of actual notification objects that are retrieved in NotificationsActivity:

| recipientId (String) | notificationText (String) | notificationBody (String) | author (Pointer <_User>) | commentObjectId (String) | read (Boolean) |
|----------------------|---------------------------|---------------------------|--------------------------|--------------------------|----------------|
| NqAq6Edr7D           | reeped to your yeet!      | come on mane!!!!!         | 9sDrqGTZZe               | IFBAB6U53h               | true           |

##### 3.1.5.1 read

**read** determines whether the notification has been seen by a user or not. Refreshing the NotificationsActivity will also set all seen notifications to read. If a notification has been seen a change in color will indicate its change of state. All notifications by default are set to false.

<a name="poll"></a>
#### 3.1.6 Poll

The Poll class has not yet been implemented. Polls will allow users to asks their friends time-limited questions with multiple choice answers. Each Yeet (see below) will have a pollObject (Pointer <_Poll)> column that will allow for retrieval of poll data so that it can be displayed it in the feed.

<a name="yeet"></a>
#### 3.1.7 Yeet

The Yeet class is the bread and butter of Yeet Club. Yeets are short, 140 character messages, or images, or polls that are sent only to the groupId that you signed up with.

| notificationText (String) | author (Pointer <_User>) | groupId (Pointer <_Group>) | likedBy (Array) | likeCount (Number) | replyCount (number) | repliedToBy (Array) | isRant (boolean) | rantId (String) | lastReplyUpdatedAt (Date) | image (File) | pollObject (Pointer <_Poll>) |
|--------------------------|-----------------------------------------|-----------------------------|------------------|------------------|------------------|------------------|------------------|------------------|------------------|------------------|------------------|
| Did you get the latest memo? | mBxXzIUZH3 | 94ca3bde-8735-11e6-ae22-56b6b6499611 | ["NqAq6Edr7D","CC0I8euqTY"] | 2 | 5 | ["NqAq6Edr7D","CC0I8euqTY"] | true | cc90cbf3-3e57-42ad-ab33-849a411bff03 | 2016-09-30T14:55:01.323Z | https://memecrunch.com/image/51621675afa96f32ef000013.jpg?w=400 | NqAq6Edr7D |

##### 3.1.7.1 isRant
**isRant** is a boolean (true or false) value that determines whether a Yeet belongs to a rant.

##### 3.1.7.2 rantId
**rantId** is a unique identifier that assigns a collection of Yeets to a single rant. There may be use for this in the future, but for now I think it is wise to logically group them together.

##### 3.1.7.3 lastReplyUpdatedAt
We make sure to update **lastReplyUpdatedAt** whenever a Yeet is replied to so that it can be bumped back to the top of the feed.

##### 3.1.7.4 pollObject
**pollObject** is a unique identifier that connects a Yeet feed item with a poll. When polls are implemented, the pollObject will be used to query poll data, such as questions, votes, and poll closing time, so that it can be displayed in the feed.

<a name="group"></a>
#### 3.1.8 Group

The Group class stores all information about separate groups created by various users within your application. Group objects are nominally referred to as "Clubs" inside the Yeet Club app. Users may switch between groups by updating their currentGroup (Pointer <_Group>) column, and also store a list of saved groups in their "myGroups (Array)" column.

| name (String) | description (String) | profilePicture (File) | admin (Array) | secretKey (String) | private (Boolean) |
|--------------------------|-----------------------------------------|-----------------------------|------------------|------------------|------------------|------------------|------------------|------------------|------------------|------------------|------------------|
| Yoobies | Welcome. This is where total loses come to learn about Yeet Club. Ask a question! Don't be shy. | https://parsefiles.back4app.com/GDprTC66bNMp3mXWNvWJbZhWwjedoucvp6NlNKVl/236f5b3e2df1f59dbd691794c19b7d5d_total_yoobs.png |  ["NqAq6Edr7D","CC0I8euqTY"] | 74c2ced4-8ae1-4356-b119-b9dc6b5469ee | true |

<hr>

<a name="push"></a>
## 4.0 Push Notifications

Back4App provides a service that leverages Parse Server's cloud code to send GCM notifications to your application. You will have to register for a GCM Sender ID and API Key here: https://developers.google.com/mobile/add?platform=android&cntapi=gcm&cnturl=https:%2F%2Fdevelopers.google.com%2Fcloud-messaging%2Fandroid%2Fclient&cntlbl=Continue%20Adding%20GCM%20Support&%3Fconfigured%3Dtrue, then provide that information to Back4App under Android Push Notification Settings (https://dashboard.back4app.com/) to register your service. This is a useful tutorial to get started: http://docs.back4app.com/docs/android/push-notifications/

The first place to put your GCM Sender ID is inside in the AndroidManifest.xml file located in your **app** directory.

```
<!-- GCM setup -->
        <meta-data
            android:name="com.parse.push.gcm_sender_id"
            android:value="id:YOUR GCM SENDER ID" />
```

Next, set your GCM Sender ID value in the strings.xml file in the path **ParseLoginUI/res/values/strings.xml**, as such: ```<string name="gcm_sender_id" translatable="false">YOUR GCM SENDER ID</string>```

### 4.0.1 The Cloud Code

Simply copy the following code to the "Cloud Code" section of your Back4App dashboard at the following link, in a new file called **main.js**: https://dashboard.back4app.com/apps#/wizard/cloud-code/:

```javascript
// Like
Parse.Cloud.define("pushLike", function (request, response) {
  var pushQuery = new Parse.Query(Parse.Installation);
  pushQuery.equalTo('userId', request.params.userId);

  var username = request.params.username;
  var result = request.params.result;

  Parse.Push.send({
    where: pushQuery,
    data: {
      alert: "Liked by " + username + ": " + result
    },
  }, {
    useMasterKey: true,
    success: function () {
      response.success("Success!");
    },
    error: function (error) {
      response.error("Error! " + error.message);
    }
  });
});

// Reply
Parse.Cloud.define("pushReply", function (request, response) {
  var pushQuery = new Parse.Query(Parse.Installation);
  pushQuery.equalTo('userId', request.params.userId);

  var username = request.params.username;
  var result = request.params.result;

  Parse.Push.send({
    where: pushQuery,
    data: {
      alert: "Reep from " + username + ": " + result
    },
  }, {
    useMasterKey: true,
    success: function () {
      response.success("Success!");
    },
    error: function (error) {
      response.error("Error! " + error.message);
    }
  });
});

// Rant
Parse.Cloud.define("pushRant", function (request, response) {
  var pushQuery = new Parse.Query(Parse.Installation);
  pushQuery.equalTo('groupId', request.params.groupId);
  pushQuery.notEqualTo("userId", request.params.userId);

  var username = request.params.username;

  Parse.Push.send({
    where: pushQuery,
    data: {
      alert: username + " is starting to rant!"
    },
  }, {
    useMasterKey: true,
    success: function () {
      response.success("Success!");
    },
    error: function (error) {
      response.error("Error! " + error.message);
    }
  });
});

// Rant Stop
Parse.Cloud.define("pushRantStop", function (request, response) {
  var pushQuery = new Parse.Query(Parse.Installation);
  pushQuery.equalTo('groupId', request.params.groupId);
  pushQuery.notEqualTo("userId", request.params.userId);

  var username = request.params.username;

  Parse.Push.send({
    where: pushQuery,
    data: {
      alert: username + " has finished ranting!"
    },
  }, {
    useMasterKey: true,
    success: function () {
      response.success("Success!");
    },
    error: function (error) {
      response.error("Error! " + error.message);
    }
  });
});

// Generic function to push to request.params.userId
Parse.Cloud.define("pushFunction", function (request, response) {
  var pushQuery = new Parse.Query(Parse.Installation);
  pushQuery.equalTo('userId', request.params.userId);

  Parse.Push.send({
    where: pushQuery,
    data: {
      alert: "You got notification."
    },
  }, {
    useMasterKey: true,
    success: function () {
      response.success("Success!");
    },
    error: function (error) {
      response.error("Error! " + error.message);
    }
  });
});
```

<hr>

<a name="needs"></a>
## 5.0 Roadmap

There are tons and tons of code optimizations that could be made here, including pulling all Parse queries outside of Activities (separation of general business logic), using RetroFit and ReactiveX to increase the speed and reliability of data transfer in the feed, etc. For now, please refer to the Trello board: https://trello.com/b/uYy2BsuH/yeet-club to keep track of upcoming features. I will try my best to outline a more detailed roadmap as time goes on, including contributors' suggestions, database schema improvements, etc. As always, I encourage anybody who is interested to reach out at info@yeet.club if you have any questions, comments, or concerns. Cheers.

### 5.0.1 The Feed

#### 5.0.1.1 The RecyclerAdapter

Consider contributing to the following classes: **1) FeedFragment** (the feed), **2) FeedAdapter**, 3) and **EndlessRecyclerViewScrollListener**. The feed is one area of every app that can always be optimized to display information faster, especially as the amount of data stored grows larger.

#### 5.0.1.2 Local Datastore, Caching, and Networking

Cacheing data for offline viewing could be another great way to improve the usability of the app. See [Take Your App Offline with Parse Local Datastore](http://blog.parse.com/announcements/take-your-app-offline-with-parse-local-datastore/) for some useful information. Currently, offline data storage and logic for various network states are not as good as it could be.

<hr>

<a name="libs"></a>
## 6.0 Third Party Libraries

The following Third Party Libraries are used in Yeet Club:

- **MaterialDrawer** by mikepenz: https://github.com/mikepenz/MaterialDrawer
- **subsampling-scale-image-view** by davemorrissey: https://github.com/davemorrissey/subsampling-scale-image-view
- **picasso** by square: https://github.com/square/picasso

<hr>

<a name="license"></a>
## 7.0 License

Copyright 2016 Hypercycle, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

<hr>

<a name="trademark"></a>
## 8.0 Trademark Information

Yeet Club™ is a trademark of Hypercycle, Inc. The trademark Yeet Club™ may not be used in any public or commercial products or applications of any kind without the express permission of Hypercycle, Inc. The Yeet Club application may be hosted on a private server and compiled for personal consumption *as is*, but may not be distributed on any public forum, website or by any other means of digital distribution in any way prior to removing and/or replacing the Yeet Club™ trademark. Please keep in mind that this does not prohibit Yeet Club™ code from being used in any public or private, commercial or non-commercial product or application so long as the trademark is removed and/or replaced. For further clarification of the use and distribution of the Yeet Club™ codebase, please see the open-source license agreement provided above.
