package javidhaji_zada.Zephyr;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    private String ID;
    private ViewPager mViewPager;
    private DatabaseReference mDatabaseReference;
    private TextView about;
    private StorageReference mStorageReference;
    private ArrayList<MatchPartnerUser> details;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        details = new ArrayList<>();
        ID = getIntent().getStringExtra("ID");
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Users").child(ID);
        String userName = getIntent().getStringExtra("Username");
        TextView username = findViewById(R.id.userProfileUsername);
        username.setText(userName);
        about = findViewById(R.id.userProfileAbout);
        mViewPager = findViewById(R.id.userProfileViewPager);
        if (!Tab1Profile.female)
        {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Female").child(ID);
        }
        else
        {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Male").child(ID);
        }
        getPhotos();
    }

    private void setPhotos(MatchPartnerUser user, boolean photoExist)
    {
        details.add(user);
        UserProfileAdapter profileAdapter = new UserProfileAdapter(getApplicationContext(), details, photoExist);
        mViewPager.setAdapter(profileAdapter);
    }

    private void getPhotos()
    {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot userDetails) {
                for (int i = 1; i <= 6;i++)
                {
                    final int finalI = i;
                    mStorageReference.child("photo" + i).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (uri != null)
                            {
                                MatchPartnerUser user = new MatchPartnerUser();
                                user.image = uri;
                                user.ID = ID;
                                user.username = userDetails.child("Username").getValue(String.class);
                                about.setText(userDetails.child("About").getValue(String.class));
                                setPhotos(user,true);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (finalI == 1)
                            {
                                MatchPartnerUser user = new MatchPartnerUser();
                                user.image = null;
                                user.ID = ID;
                                user.username = userDetails.child("Username").getValue(String.class);
                                if (!TextUtils.isEmpty(userDetails.child("About").getValue(String.class)))
                                    about.setText(userDetails.child("About").getValue(String.class));
                                setPhotos(user,false);
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class UserProfileAdapter extends PagerAdapter
    {

        // variables
        private ArrayList<MatchPartnerUser> userDetails;
        private Context mContext;
        private boolean photoExist;

        // constructor
        UserProfileAdapter(Context context, ArrayList<MatchPartnerUser> userDetails, boolean photoExist)
        {
            mContext = context;
            this.userDetails = userDetails;
            this.photoExist = photoExist;
        }
        @Override
        public int getCount() {
            if (photoExist)
                return userDetails.size();
            return 1;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService((Context.LAYOUT_INFLATER_SERVICE));
            View view = layoutInflater.inflate(R.layout.user_profile_photo_swipe,container,false);
            if (photoExist)
            {
                ImageView imageView = view.findViewById(R.id.userProfilePhoto);
                Picasso.get().load(userDetails.get(position).image).fit().into(imageView);
                container.addView(view);
                return view;
            }
            view.findViewById(R.id.userProfilePhoto).setBackgroundDrawable(getResources().getDrawable(R.drawable.default_user));
            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}

