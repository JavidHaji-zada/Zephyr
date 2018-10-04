package javidhaji_zada.Zephyr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Tab2Matches extends Fragment implements RewardedVideoAdListener {

    private final String PREFS_NAME = "ads_data";
    private final int ADD_NEEEDED = 25;
    private FirebaseUser mUser;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private NonSwipeableViewPager mViewPager;
    private ArrayList<MatchPartnerUser> mContents;
    private ViewPagerAdapter mViewPagerAdapter;
    private boolean female = true;
    private RewardedVideoAd mRewardedVideoAd;
    private int numberClicked = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Users");
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        numberClicked = getData();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.tab2_matches,container,false);
        mViewPager = rootView.findViewById(R.id.viewPager);
        mViewPager.setPagingEnabled(false);
        final ImageButton like = rootView.findViewById(R.id.likeButton);
        final ImageButton dislike = rootView.findViewById(R.id.dislikeButton);
        mContents = new ArrayList<>();
        getContents();
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mContents.size() != 0) {
                    final Map<String, Object> map = new HashMap<>();
                    final String ID = mContents.get(0).ID;
                    map.put(ID,ID);
                    removeModel();
                    if (female) {
                        mDatabaseReference.child("Female").child(mUser.getUid()).child("Likes").updateChildren(map);
                        mDatabaseReference.child("Male").child(ID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child("Likes").hasChild(mUser.getUid()))
                                {
                                    Map<String,Object> map1 = new HashMap<>();
                                    map1.put(mUser.getUid(),mUser.getUid());
                                    mDatabaseReference.child("Male").child(ID).child("Matches").updateChildren(map1);
                                    map1 = new HashMap<>();
                                    map1.put(ID,ID);
                                    mDatabaseReference.child("Female").child(mUser.getUid()).child("Matches").updateChildren(map1);
                                    mDatabaseReference.child("Female").child(mUser.getUid()).child("Matches").child("Recent").updateChildren(map1);

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(),R.string.smth_wrong, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        mDatabaseReference.child("Male").child(mUser.getUid()).child("Likes").updateChildren(map);
                        mDatabaseReference.child("Female").child(ID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child("Likes").hasChild(mUser.getUid()))
                                {
                                    Map<String,Object> map1 = new HashMap<>();
                                    map1.put(mUser.getUid(),mUser.getUid());
                                    mDatabaseReference.child("Female").child(ID).child("Matches").updateChildren(map1);
                                    map1 = new HashMap<>();
                                    map1.put(ID,ID);
                                    mDatabaseReference.child("Male").child(mUser.getUid()).child("Matches").updateChildren(map1);
                                    mDatabaseReference.child("Male").child(mUser.getUid()).child("Matches").child("Recent").updateChildren(map1);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(),R.string.smth_wrong, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    numberClicked++;
                    saveData(numberClicked);
                    if (numberClicked % ADD_NEEEDED == 0)
                    {
                        startActivity(new Intent(getContext(),AdActivity.class));
                    }
                }
            }
        });
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mContents.size() != 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(mContents.get(0).ID, mContents.get(0).ID);
                    removeModel();
                    if (female) {
                        mDatabaseReference.child("Female").child(mUser.getUid()).child("Dislikes").updateChildren(map);
                    }
                    else
                        mDatabaseReference.child("Male").child(mUser.getUid()).child("Dislikes").updateChildren(map);
                    numberClicked++;
                    saveData(numberClicked);
                    if (numberClicked == ADD_NEEEDED)
                    {
                        startActivity(new Intent(getContext(),AdActivity.class));
                    }
                }
            }
        });
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
        return rootView;
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-9467200614816858/9738449297",
                new AdRequest.Builder().build());
    }

    private void removeModel() {
        mContents.remove(0);
        mViewPagerAdapter = new ViewPagerAdapter(mContents,getContext());
        mViewPager.setAdapter(mViewPagerAdapter);
    }

    public void getContents()
    {
        mDatabaseReference.child("Female").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot femaleData) {
                if (femaleData.hasChild(mUser.getUid()))
                {
                    mDatabaseReference.child("Male").addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren())
                            {
                                if (!femaleData.child(mUser.getUid()).child("Dislikes").
                                        hasChild(snapshot.getKey()) &&
                                        !femaleData.child(mUser.getUid()).child("Likes").hasChild(snapshot.getKey()))
                                {
                                    final String id = snapshot.getKey();
                                    final String username = snapshot.child("Username").getValue(String.class);
                                    mStorageReference.child(id).child("photo1").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            MatchPartnerUser model = new MatchPartnerUser();
                                            model.image = uri;
                                            model.ID = id;
                                            model.username = username;
                                            addModel(model);
                                            if (femaleData.child(mUser.getUid()).child("Likes").getChildrenCount() % 20 == 0)
                                            {
                                                loadRewardedVideoAd();
                                                mRewardedVideoAd.show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            MatchPartnerUser model = new MatchPartnerUser();
                                            model.image = null;
                                            model.ID = id;
                                            model.username = username;
                                            addModel(model);
                                            if (femaleData.child(mUser.getUid()).child("Likes").getChildrenCount() % 20 == 0)
                                            {
                                                loadRewardedVideoAd();
                                                mRewardedVideoAd.show();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getContext(),R.string.smth_wrong,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    female = false;
                    mDatabaseReference.child("Female").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                mDatabaseReference.child("Male").child(mUser.getUid()).
                                        addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot2) {
                                                if ((!dataSnapshot2.child("Likes").hasChild(dataSnapshot1.getKey())
                                                        && !dataSnapshot2.child("Dislikes").hasChild(dataSnapshot1.getKey()))) {
                                                    final String id = dataSnapshot1.getKey();
                                                    final String username = dataSnapshot1.child("Username").getValue(String.class);
                                                    mStorageReference.child(id).child("photo1").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            MatchPartnerUser model = new MatchPartnerUser();
                                                            model.image = uri;
                                                            model.ID = id;
                                                            model.username = username;
                                                            addModel(model);
                                                            if (dataSnapshot2.child("Likes").getChildrenCount() % 20 == 0)
                                                            {
                                                                loadRewardedVideoAd();
                                                                mRewardedVideoAd.show();
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            MatchPartnerUser model = new MatchPartnerUser();
                                                            model.image = null;
                                                            model.ID = id;
                                                            model.username = username;
                                                            addModel(model);
                                                            if (dataSnapshot2.child("Likes").getChildrenCount() % 20 == 0)
                                                            {
                                                                loadRewardedVideoAd();
                                                                mRewardedVideoAd.show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(getContext(), R.string.smth_wrong,Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getContext(),R.string.smth_wrong,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(),R.string.smth_wrong,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addModel(MatchPartnerUser model) {
        if (!contains(mContents,model))
            mContents.add(model);
        mViewPagerAdapter = new ViewPagerAdapter(mContents, getContext());
        mViewPager.setAdapter(mViewPagerAdapter);
    }
    private boolean contains(ArrayList<MatchPartnerUser> matchPartnerUsers, MatchPartnerUser matchPartnerUser)
    {
        for ( MatchPartnerUser mp: matchPartnerUsers)
        {
            if (mp.ID.equals(matchPartnerUser.ID))
            {
                return true;
            }
        }
        return false;
    }

    private int getData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(PREFS_NAME))
            return sharedPreferences.getInt(PREFS_NAME, 0);
        return 0;
    }
    private void saveData(int no_of_ads)
    {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE).edit();
        editor.putInt(PREFS_NAME,no_of_ads);
        editor.apply();
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