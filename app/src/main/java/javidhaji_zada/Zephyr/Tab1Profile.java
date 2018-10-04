package javidhaji_zada.Zephyr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tab1Profile  extends Fragment{

    private FirebaseUser mUser;
    private DatabaseReference mDatabaseReference;
    private String birthday;
    private long age = 5;
    public static boolean female = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.tab1_profile,container,false);
        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        ImageView settings = rootView.findViewById(R.id.settings);
        ImageView edit = rootView.findViewById(R.id.edit);
        final TextView nameSurname = rootView.findViewById(R.id.username);
        final TextView ageView = rootView.findViewById(R.id.profile_age);

        //setup user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Female");
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mUser.getUid())) {
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Male").child(mUser.getUid());
                    female = false;
                }
                else
                {
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Female").child(mUser.getUid());
                    female = true;
                }
                if (female)
                {
                    ImageView gender = rootView.findViewById(R.id.gender);
                    gender.setImageDrawable(getResources().getDrawable(R.drawable.ic_female));
                    ((TextView) rootView.findViewById(R.id.profile_age)).setTextColor(getResources().getColor(R.color.female));
                }

                mDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        birthday = dataSnapshot.child("Birthday").getValue(String.class);
                        try {
                            calculateAge();
                        } catch (ParseException e) {
                            Toast.makeText(getContext(),R.string.smth_wrong, Toast.LENGTH_SHORT).show();
                        }
                        ageView.setText(String.valueOf((int) age));
                        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder().setDisplayName(mUser.getDisplayName().substring(0,mUser.getDisplayName().indexOf(",")) + ", " + age).build();
                        mUser.updateProfile(changeRequest);
                        mDatabaseReference.child("Username").setValue(mUser.getDisplayName());
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
        // photo
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Users").child(mUser.getUid());
        storageReference.child("photo1").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if(uri != null)
                {
                    ImageView profile_photo = rootView.findViewById(R.id.profile_image);
                    Picasso.get().load(uri).fit().into(profile_photo);
                }
            }
        });

        // change language settings
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),SettingsActivity.class));
            }
        });

        // name and surname

        nameSurname.setText(mUser.getDisplayName());
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),EditProfileActivity.class);
                intent.putExtra("Gender",female);
                startActivity(intent);
            }
        });
        return rootView;
    }

    public void calculateAge() throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy/mm/dd");
        Date birthday = simpleDateFormat.parse(this.birthday);
        long ms = System.currentTimeMillis() - birthday.getTime();
        age = (long)(ms / (1000.0*60*60*24*365));
    }
}
