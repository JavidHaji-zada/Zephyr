package javidhaji_zada.Zephyr;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {


    // constants
    private final int DIALOG_ID = 0;

    // variables
    private EditText email;
    private EditText password;
    private EditText name;
    private EditText surname;
    private EditText birthday;
    private long age = 5;
    private RadioButton maleButton;
    private RadioButton femaleButton;
    private FirebaseAuth mAuth;
    private String gender;
    private ProgressDialog mDialog;
    private String date;
    private int year,month,day;
    private CardView register;
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // initialize
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        name = findViewById(R.id.registrationName);
        surname = findViewById(R.id.registrationSurname);
        register = findViewById(R.id.signUp);
        maleButton = findViewById(R.id.maleButton);
        femaleButton = findViewById(R.id.femaleButton);
        mDialog = new ProgressDialog(RegistrationActivity.this);
        mDialog.setMessage(getResources().getString(R.string.registering));
        birthday = findViewById(R.id.age);
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        date = year + "/" + month + "/" + day;

        // birthday date picker
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        // configure radio buttons
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                femaleButton.setChecked(false);
            }
        });
        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleButton.setChecked(false);
            }
        });
        // check all the fields are filled
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
                if (checkFilled()) {
                    register.setEnabled(false);
                    if(maleButton.isChecked())
                        gender = "Male";
                    else
                        gender = "Female";
                    signUp(email.getText().toString().trim(), password.getText().toString().trim(), name.getText().toString().trim(), surname.getText().toString().trim(), gender);
                }
                else
                    mDialog.dismiss();
            }
        });
        //privacy policy
        TextView privacyPolicy = findViewById(R.id.privacyPolicy);
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                builder.setTitle(getResources().getString(R.string.privacy_policyTitle));
                builder.setMessage(getResources().getString(R.string.privacy_policy));
                builder.show();
            }
        });
    }
    @Override
    public Dialog onCreateDialog(int id)
    {
        if ( id == DIALOG_ID)
            return new DatePickerDialog(RegistrationActivity.this, datePickerListener,year,month,day);
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @SuppressLint("SimpleDateFormat")
        @Override
        public void onDateSet(DatePicker view, int year_x, int month_x, int dayOfMonth_x) {
            year = year_x;
            month = month_x + 1;
            day = dayOfMonth_x;
            date = year + "/" + month + "/" + day;
            birthday.setText(date);
            try {
                calculateAge();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };

    public boolean checkFilled() {
        if ((email.getText().toString().isEmpty() || password.getText().toString().isEmpty() || name.getText().toString().isEmpty() || surname.getText().toString().isEmpty())|| (!maleButton.isChecked() && !femaleButton.isChecked())) {
            Toast.makeText(RegistrationActivity.this, R.string.complete_all_fields, Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return false;
        }
        if (year == Calendar.getInstance().get(Calendar.YEAR) || year <  Calendar.getInstance().get(Calendar.YEAR) - 60 || year >  Calendar.getInstance().get(Calendar.YEAR))
        {
            Toast.makeText(RegistrationActivity.this, R.string.birthday_error,Toast.LENGTH_SHORT).show();
            return false;
        }
        if ( Calendar.getInstance().get(Calendar.YEAR) - year < 18)
        {
            Toast.makeText(RegistrationActivity.this,R.string.age_rest, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public void signUp(final String  email, String password, final String name, final String surname,final String gender)
    {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(gender);
                if( task.isSuccessful())
                {
                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name + " " + surname + ", " + age).build();
                    FirebaseUser user = mAuth.getCurrentUser();
                    user.updateProfile(userProfileChangeRequest);
                    Map<String, String> newPost = new HashMap<>();
                    newPost.put("Name", name);
                    newPost.put("Surname", surname);
                    newPost.put("Username", name + " " + surname + "," + age);
                    newPost.put("Email", email);
                    newPost.put("Birthday", date);
                    firebaseDatabase.child(user.getUid()).setValue(newPost);
                    mDialog.dismiss();
                    register.setEnabled(true);
                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                }
                else
                {
                    mDialog.dismiss();
                    register.setEnabled(true);
                    Toast.makeText(getApplicationContext(),R.string.auth_failed,Toast.LENGTH_SHORT).show();
                    firebaseDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren())
                            {
                                if (ds.child("Email").getValue(String.class).equals(email))
                                {
                                    Toast.makeText(getApplicationContext(), R.string.already_exists,Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }
    public void calculateAge() throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy/mm/dd");
        Date birthday = simpleDateFormat.parse(date);
        long ms = System.currentTimeMillis() - birthday.getTime();
        age = (long)(ms / (1000.0*60*60*24*365));
    }
}
