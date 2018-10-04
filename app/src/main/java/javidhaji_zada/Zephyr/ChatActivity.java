package javidhaji_zada.Zephyr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private static final int NO_OF_VIDEOS = 45;
    private DatabaseReference userReference;
    private DatabaseReference partnerReceiveReference;
    private String partnerID;
    private FirebaseUser mUser;
    private RecyclerView mMessagesRecyclerView;
    private MessageListAdapter mMessageListAdapter;
    private ArrayList<ChatMessage> mChatMessages;
    private EditText messageBox;
    private int no_of_messages;
    private RewardedVideoAd mRewardedVideoAd;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChatActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        // initialize
        mChatMessages = new ArrayList<>();
        no_of_messages = getDefaults(this);
        TextView partnerProfileName = findViewById(R.id.nameOfContact);
        messageBox = findViewById(R.id.message_box);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        final CircleImageView profilePhoto = findViewById(R.id.chatActivityProfilePic);
        ImageView sendButton = findViewById(R.id.sendButton);
        // get partner details
        partnerID = getIntent().getStringExtra("partnerID");

        // set partner details
        final String partnerUsername = getIntent().getStringExtra("partnerUsername");
        partnerProfileName.setText(partnerUsername);
        FirebaseStorage.getInstance().getReference().child("Users").child(partnerID).child("photo1").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(profilePhoto);
            }
        });
        // server
        if (Tab1Profile.female)
        {
            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Female").child(mUser.getUid())
                    .child("Chats").child(partnerID);
            partnerReceiveReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Male").child(partnerID)
                    .child("Chats").child(mUser.getUid());
        }
        else
        {
            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Male").child(mUser.getUid())
                    .child("Chats").child(partnerID);
            partnerReceiveReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Female").child(partnerID)
                    .child("Chats").child(mUser.getUid());
        }

        // photo clicked
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this,UserProfileActivity.class);
                intent.putExtra("ID",partnerID);
                intent.putExtra("Username",partnerUsername);
                startActivity(intent);
            }
        });
        // send button clicked
        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if ( !messageBox.getText().toString().matches(""))
                {
                    sendMessage(messageBox.getText().toString());
                }
            }
        });
        // get messages and display
        getChatMessages();
        userReference.keepSynced(true);
        mMessagesRecyclerView = findViewById(R.id.chatActivityRV);
        mMessageListAdapter = new MessageListAdapter(this, mChatMessages);
        mMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMessagesRecyclerView.setAdapter(mMessageListAdapter);
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
    }

    private void sendMessage(String s) {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss").format(Calendar.getInstance().getTime());
        ChatMessage message = new ChatMessage(mUser.getUid(), s, date);
        mChatMessages.add(message);
        no_of_messages = getDefaults(this);
        userReference.child("Sent").child(String.valueOf(no_of_messages)).setValue(message);
        messageBox.setText("");
        updateUI();
        partnerReceiveReference.child("Received").child(String.valueOf(no_of_messages)).setValue(message);
        partnerReceiveReference.child("Recent").child(String.valueOf(no_of_messages)).setValue(message);
        setDefaults(this);
    }

    private void updateUI() {
        if (mRewardedVideoAd.isLoaded() && mChatMessages.size() % NO_OF_VIDEOS == 0) {
            mRewardedVideoAd.show();
        }
        Collections.sort(mChatMessages);
        mMessagesRecyclerView.smoothScrollToPosition(mMessageListAdapter.getItemCount());
        mMessageListAdapter.notifyDataSetChanged();
    }


    public void setDefaults( Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("Number of messages", no_of_messages + 1 );
        editor.apply();
    }

    public int getDefaults(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("Number of messages",1);
    }

    private void getChatMessages()
    {
        userReference.child("Received").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for ( DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String sender = ds.child("sender").getValue(String.class);
                    String message = ds.child("message").getValue(String.class);
                    String createdAt = ds.child("createdAt").getValue(String.class);
                    ChatMessage chatMessage = new ChatMessage(sender,message,createdAt);
                    if (contains(mChatMessages,chatMessage))
                    {
                        mChatMessages.add(chatMessage);
                    }
                    userReference.child("Recent").setValue(null);
                }
                userReference.child("Sent").addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for ( DataSnapshot ds: dataSnapshot.getChildren())
                        {
                            String sender = ds.child("sender").getValue(String.class);
                            String message = ds.child("message").getValue(String.class);
                            String createdAt = ds.child("createdAt").getValue(String.class);
                            ChatMessage chatMessage = new ChatMessage(sender,message,createdAt);
                            if (contains(mChatMessages,chatMessage))
                                mChatMessages.add(chatMessage);
                        }
                        Collections.sort(mChatMessages);
                        updateUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private boolean contains(ArrayList<ChatMessage> chatMessages, ChatMessage chatMessage)
    {
        for ( ChatMessage ch: chatMessages)
        {
            if ( ch.getSender().equals(chatMessage.getSender()) && ch.getCreatedAt().equals(chatMessage.getCreatedAt())
                    && ch.getMessage().equals(chatMessage.getMessage()))
            {
                return false;
            }
        }
        return true;
    }

    private void loadRewardedVideoAd() {
        String adID = "ca-app-pub-7311204396723743/3602868470";
        mRewardedVideoAd.loadAd(adID,
                new AdRequest.Builder().build());
        mRewardedVideoAd.setRewardedVideoAdListener(this);
    }
    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }

}
