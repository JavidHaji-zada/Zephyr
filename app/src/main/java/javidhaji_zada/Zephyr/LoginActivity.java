package javidhaji_zada.Zephyr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements ResetPasswordDialog.EditDialogListener {

    // constants
    private static final String PREFS_NAME = "rememberMe";

    // variables
    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private CheckBox mCheckBox;
    private ProgressDialog mDialog;
    private String emailForReset;
    private boolean doubleBackToExitPressedOnce;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mCheckBox = findViewById(R.id.rememberMe);
        mAuth = FirebaseAuth.getInstance();
        doubleBackToExitPressedOnce = false;

        // initializing variables
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        CardView signIn = findViewById(R.id.signInCard);
        TextView register = findViewById(R.id.register);
        mDialog = new ProgressDialog(LoginActivity.this);
        mDialog.setMessage(getResources().getString(R.string.loggingIn));
        TextView forgotPassword = findViewById(R.id.forgotPassword);

        // signing in
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
                signIn(email.getText().toString().trim(),password.getText().toString().trim());
            }
        });

        // go to registration
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegistrationActivity.class));
            }
        });

        // forgot password feature
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        if (doubleBackToExitPressedOnce)
        {
            this.finishAffinity();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(LoginActivity.this,R.string.double_press,Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        },2000);
    }

    /** This method is used to sign in current users to the application
     * @param email : E-mail address of the user.
     * @param password : Password entered by user.
     */
    public void signIn(final String email, final String password)
    {
        if (email.isEmpty()) {
            Toast.makeText(LoginActivity.this, R.string.empty_email, Toast.LENGTH_SHORT).show();
            mDialog.dismiss();
            return;
        }
        if ( password.isEmpty())
        {
            Toast.makeText(LoginActivity.this,R.string.empty_password,Toast.LENGTH_SHORT).show();
            mDialog.dismiss();
            return;
        }
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    if (mCheckBox.isChecked())
                    {
                        SharedPreferences.Editor editor =  getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("pref_email", email);
                        editor.putString("pref_pass", password);
                        editor.putBoolean("pref_check", true);
                        editor.putBoolean("pref_logged",true);
                        editor.apply();

                    }
                    else
                    {
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                    }
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
                                SharedPreferences.Editor editor = getSharedPreferences("Gender",MODE_PRIVATE).edit();
                                editor.putBoolean("Female",true);
                            }
                            else {
                                SharedPreferences.Editor editor = getSharedPreferences("Gender",MODE_PRIVATE).edit();
                                editor.putBoolean("Female",false);
                                mDatabaseReference.child("Male").child(mUser.getUid()).updateChildren(map);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    mDialog.dismiss();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, R.string.unsuccessfull_login,Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }

            }
        });
    }
    public void openDialog()
    {
        ResetPasswordDialog resetPasswordDialog = new ResetPasswordDialog();
        resetPasswordDialog.show(getSupportFragmentManager(), "reset pass dialog");
    }

    @Override
    public void applyTexts(String email) {
        emailForReset = email;
        sendResetPassword();
    }
    public void sendResetPassword()
    {
        if (!emailForReset.equals(""))
            FirebaseAuth.getInstance().sendPasswordResetEmail(emailForReset.trim())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_sent),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }
}
