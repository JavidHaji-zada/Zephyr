package javidhaji_zada.Zephyr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class AdActivity extends AppCompatActivity {
    private final String PREFS_NAME = "ads_data";
    private final int widthSize = 600;
    private final int heightsize = 600;

    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private ArrayList<AdModel> ads;
    private int count;
    private ImageView adPhoto;
    private TextView title;
    private TextView text;
    private int adCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_fragment);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Advertisements");
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Advertisements");
        ImageView close = findViewById(R.id.close);
        adPhoto = findViewById(R.id.adsLogo);
        title = findViewById(R.id.adsTitle);
        text = findViewById(R.id.adsText);
        ads = new ArrayList<>();

        count = getData();
        getAds();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void getAds()
    {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adCount = (int) dataSnapshot.getChildrenCount();
                int i = 0;
                for (final DataSnapshot adDetails: dataSnapshot.getChildren())
                {
                    if (i == count % adCount)
                    {
                        mStorageReference.child(adDetails.getKey()).child("gender-male.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).resize(widthSize,heightsize).into(adPhoto);
                                title.setText(adDetails.getKey());
                                text.setText(adDetails.child("Info").getValue(String.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    i++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setData(int count)
    {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE).edit();
        editor.putInt(PREFS_NAME,count);
        editor.apply();
    }
    public int getData() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(PREFS_NAME))
            return sharedPreferences.getInt(PREFS_NAME,0);
        return 0;
    }
}