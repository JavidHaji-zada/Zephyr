package javidhaji_zada.Zephyr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "rememberMe";


    private SeekBar ageBar;
    private SeekBar distanceBar;
    private TextView distance;
    private TextView age;
    private RadioGroup languageGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // initialize
        age = findViewById(R.id.ageRange);
        distanceBar = findViewById(R.id.distanceBar);
        ageBar = findViewById(R.id.ageBar);
        distance = findViewById(R.id.distance);
        languageGroup = findViewById(R.id.languageGroup);
        languageGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.English)
                {
                    Resources res = getResources();
                    DisplayMetrics dm = res.getDisplayMetrics();
                    android.content.res.Configuration conf = res.getConfiguration();
                    conf.setLocale(new Locale("en"));
                    res.updateConfiguration(conf, dm);
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.language_changed),Toast.LENGTH_SHORT).show();
                }
                else if (checkedId == R.id.Russian)
                {
                    Resources res = getResources();
                    DisplayMetrics dm = res.getDisplayMetrics();
                    android.content.res.Configuration conf = res.getConfiguration();
                    conf.setLocale(new Locale("ru"));
                    res.updateConfiguration(conf, dm);
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.language_changed),Toast.LENGTH_SHORT).show();
                }
                else if (checkedId == R.id.Azerbaijani)
                {
                    Resources res = getResources();
                    DisplayMetrics dm = res.getDisplayMetrics();
                    android.content.res.Configuration conf = res.getConfiguration();
                    conf.setLocale(new Locale("az"));
                    res.updateConfiguration(conf, dm);
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.language_changed),Toast.LENGTH_SHORT).show();
                }
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                saveChanges();
                startActivity(i);
            }
        });
        getSaves();

        // distance bar
        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance.setText(progress + "km.");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // age bar
        ageBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress + 18;
                age.setText("18-" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ImageView back = findViewById(R.id.setting_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        saveChanges();
        startActivity(new Intent(SettingsActivity.this,MainActivity.class));
    }

    private void saveChanges() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit();
        editor.putInt("pref_age",ageBar.getProgress());
        editor.putInt("pref_distance",distanceBar.getProgress());
        editor.putInt("pref_language",languageGroup.getCheckedRadioButtonId());
        int lang = languageGroup.getCheckedRadioButtonId();
        if (lang == R.id.English)
            editor.putString("lang_code","en");
        else if (lang == R.id.Russian)
            editor.putString("lang_code","ru");
        else if (lang == R.id.Azerbaijani)
            editor.putString("lang_code","az");
        editor.apply();
    }

    @SuppressLint("SetTextI18n")
    public void getSaves() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if (sp.contains("pref_age"))
        {
            int ageInt = sp.getInt("pref_age", 18) + 18;
            age.setText("18-" + ageInt);
            ageBar.setProgress(ageInt-18);
        }
        if (sp.contains("pref_distance"))
        {
            int distanceInt = sp.getInt("pref_distance",2);
            distance.setText(String.valueOf(distanceInt) + "km.");
            distanceBar.setProgress(distanceInt);
        }
//        if (sp.contains("pref_language"))
//        {
//            int lang = sp.getInt("pref_language",R.id.English);
//            languageGroup.check(lang);
//        }
    }
}
