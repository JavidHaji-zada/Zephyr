package javidhaji_zada.Zephyr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LauncherActivity extends AppCompatActivity {

    // constants
    private static final String PREFS_NAME = "rememberMe";

    // variables
    private boolean checked;
    private boolean registered;
    private String emailData;
    private String passwordData;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    public static String langCode = "en";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        checked = false;
        registered = false;
        getPreferencesData();
        mAuth = FirebaseAuth.getInstance();

        startActivity(new  Intent(LauncherActivity.this,MainActivity.class));
        if (checked && registered)
        {
            mAuth.signInWithEmailAndPassword(emailData,passwordData).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    final Map<String,Object> map = new HashMap<>();
                    map.put("Last login",System.currentTimeMillis());
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                    final FirebaseUser mUser= mAuth.getCurrentUser();
                    mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            assert mUser != null;
                            if (dataSnapshot.child("Female").hasChild(mUser.getUid()))
                            {
                                mDatabaseReference.child("Female").child(mUser.getUid()).updateChildren(map);
                            }
                            else
                                mDatabaseReference.child("Male").child(mUser.getUid()).updateChildren(map);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
        else
        {
            startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
        }
    }

    public void getPreferencesData()
    {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if (sp.contains("pref_check"))
        {
            checked = sp.getBoolean("pref_check", false);
        }
        if (sp.contains("pref_logged"))
        {
            registered = sp.getBoolean("pref_logged", false);
        }
        if (sp.contains("pref_email"))
        {
            emailData = sp.getString("pref_email", "not found");
        }
        if (sp.contains("pref_pass"))
        {
            passwordData = sp.getString("pref_pass","not found");
        }
        if (sp.contains("lang_code")){
            langCode = sp.getString("lang_code","en");
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.setLocale(new Locale(langCode));
            res.updateConfiguration(conf, dm);
        }
    }
}