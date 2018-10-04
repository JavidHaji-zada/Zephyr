package javidhaji_zada.Zephyr;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class NotificationService extends Service {
    private final String PREFS_NAME = "Notification ID";

    private DatabaseReference mDatabaseReference;
    private DatabaseReference matchesReference;
    private DatabaseReference chatsReference;
    private int notificationID;
    private FirebaseUser mUser;
    private static final String channel_id = "APP_ID";
    private static final String channel_name_chat = "Chat_Notification";
    private String[] notifications;
    private String[] names;
    private boolean female;
    private final int daysNeeded = 3;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.keepSynced(true);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        notifications = new String[]{getResources().getString(R.string.notification1),getResources().getString(R.string.notification2)};
        female = getData();
        if (female)
        {
            matchesReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Female").child(mUser.getUid()).child("Matches").child("Recent");
            chatsReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Female").child(mUser.getUid()).child("Chats");
        }
        else
        {
            matchesReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Male").child(mUser.getUid()).child("Matches");
            chatsReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Male").child(mUser.getUid()).child("Chats");
        }
        checkChats();
        checkMatches();
        checkLastLogin();
        return START_STICKY;
    }

    private void checkMatches() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Female").hasChild(mUser.getUid()))
                {
                    mDatabaseReference.child("Female").child(mUser.getUid()).child("Matches").child("Recent").keepSynced(true);
                    if (dataSnapshot.child("Female").child(mUser.getUid()).child("Matches").hasChild("Recent"))
                        sendNotification(getResources().getString(R.string.app_name),getResources().getString(R.string.newMatch));
                }
                else
                {
                    mDatabaseReference.child("Male").child(mUser.getUid()).child("Matches").child("Recent").keepSynced(true);
                    if (dataSnapshot.child("Male").child(mUser.getUid()).child("Matches").hasChild("Recent"))
                        sendNotification(getResources().getString(R.string.app_name),getResources().getString(R.string.newMatch));
                    female = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkLastLogin() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Female").hasChild(mUser.getUid())) {
                    names = new String[]{getResources().getString(R.string.name1_males),
                            getResources().getString(R.string.name2_males),
                            getResources().getString(R.string.name3_males),
                            getResources().getString(R.string.name4_males)};
                    long lastLogin = dataSnapshot.child("Female").child(mUser.getUid()).child("Last login").getValue(long.class);
                    int days = (int) (System.currentTimeMillis() - lastLogin)/(1000*60*60*24);
                    if (days % daysNeeded == 0 && days != 0) {
                        Random random = new Random();
                        int rand = random.nextInt(1);
                        if (rand == 1)
                        {
                            int nameRand = random.nextInt(names.length);
                            sendNotification(getResources().getString(R.string.app_name),
                                    names[nameRand] + " " + notifications[rand]);
                        }
                        else
                            sendNotification(getResources().getString(R.string.app_name),notifications[rand]);
                    }
                }
                else {
                    names = new String[]{getResources().getString(R.string.name1_females),
                            getResources().getString(R.string.name2_females),
                            getResources().getString(R.string.name3_females),
                            getResources().getString(R.string.name4_females),
                            getResources().getString(R.string.name5_females),
                            getResources().getString(R.string.name6_females),
                            getResources().getString(R.string.name7_females),
                            getResources().getString(R.string.name8_females),
                            getResources().getString(R.string.name9_females),
                            getResources().getString(R.string.name10_females),
                            getResources().getString(R.string.name11_females),
                            getResources().getString(R.string.name12_females),
                            getResources().getString(R.string.name13_females),
                            getResources().getString(R.string.name14_females),
                            getResources().getString(R.string.name15_females),
                    };
                    long lastLogin = dataSnapshot.child("Male").child(mUser.getUid()).child("Last login").getValue(long.class);
                    int days = (int) (System.currentTimeMillis() - lastLogin)/(1000*60*60*24);
                    if (days % daysNeeded == 0 && days != 0) {
                        Random random = new Random();
                        int rand = random.nextInt(2);
                        if (rand == 1)
                        {
                            int nameRand = random.nextInt(names.length);
                            sendNotification(getResources().getString(R.string.app_name),
                                    names[nameRand] + " " + notifications[rand] );
                        }
                        sendNotification(getResources().getString(R.string.app_name),notifications[rand]);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void checkChats() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Female").hasChild(mUser.getUid()))
                {
                    mDatabaseReference.child("Female").child(mUser.getUid()).child("Chats").keepSynced(true);
                    for (final DataSnapshot recentMessageSenderID: dataSnapshot.child("Female").child(mUser.getUid()).child("Chats").getChildren())
                    {
                        if (recentMessageSenderID.hasChild("Recent"))
                        {
                            mDatabaseReference.child("Male").child(recentMessageSenderID.getKey()).child("Username")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot senderUsername) {
                                            for (DataSnapshot messages: recentMessageSenderID.child("Recent").getChildren())
                                            {
                                                String username = senderUsername.getValue(String.class);
                                                String message = messages.child("message").getValue(String.class);
                                                sendNotification(username,message,recentMessageSenderID.getKey());
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                        }
                    }
                }
                else
                {
                    mDatabaseReference.child("Male").child(mUser.getUid()).child("Chats").keepSynced(true);
                    for (final DataSnapshot recentMessageSenderID: dataSnapshot.child("Male").child(mUser.getUid()).child("Chats").getChildren())
                    {
                        mDatabaseReference.child("Male").child(mUser.getUid()).child("Chats").keepSynced(true);
                        if (recentMessageSenderID.hasChild("Recent"))
                        {
                            mDatabaseReference.child("Female").child(recentMessageSenderID.getKey()).child("Username")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot senderUsername) {
                                            for (DataSnapshot messages: recentMessageSenderID.child("Recent").getChildren())
                                            {
                                                String username = senderUsername.getValue(String.class);
                                                String message = messages.child("message").getValue(String.class);
                                                sendNotification(username,message,recentMessageSenderID.getKey());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String username, String message, String senderID)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(channel_id , channel_name_chat, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("partnerID", senderID);
            intent.putExtra("partnerUsername", username);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, channel_id)
                    .setContentTitle(username)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher);
            notificationManager.notify(notificationID, notification.build());
            saveData();
        }
        else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setVibrate(new long[]{0, 1000, 500, 1000})
                    .setContentTitle(username)
                    .setContentText(message);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("partnerID", senderID);
            intent.putExtra("partnerUsername", username);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            NotificationManager notify = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notify.notify(notificationID, builder.build());
            saveData();
        }
    }
    private void sendNotification(String title,String text) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(channel_id , channel_name_chat, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            Intent intent = new Intent(this, LauncherActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, channel_id)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher);
            notificationManager.notify(notificationID, notification.build());
            saveData();

        }
        else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setVibrate(new long[]{0, 1000, 500, 1000})
                    .setContentTitle(title)
                    .setContentText(text);

            Intent intent = new Intent(this, LauncherActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            NotificationManager notify = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notify.notify(notificationID, builder.build());
            saveData();
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean getData() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        notificationID = sp.getInt(PREFS_NAME,0);
        SharedPreferences sharedPreferences = getSharedPreferences("Gender",MODE_PRIVATE);
        return sharedPreferences.getBoolean("Female",true);
    }
    public void saveData()
    {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit();
        editor.putInt(PREFS_NAME,notificationID++);
    }
}
