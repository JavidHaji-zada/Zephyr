package javidhaji_zada.Zephyr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "rememberMe";

    public  ViewPager mViewPager;
    public static final String adID = "ca-app-pub-7311204396723743~2850568749";
    private TabLayout mTabLayout;
    private AdView adView;
    private boolean doubleBackToExitPressedOnce;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doubleBackToExitPressedOnce = false;
        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.container);
        getPreferencesData();
        // Set up the ViewPager with the sections adapter and setup toolbar.
        setupViewPager(mViewPager);
        MobileAds.initialize(this, adID);
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                adView.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                adView.loadAd(new AdRequest.Builder().build());
            }
        });
        startService();
    }
    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new Tab1Profile(), "");
        adapter.addFragment(new Tab2Matches(), "");
        adapter.addFragment(new Tab3Chats(), "");
        viewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_profile);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_people);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_chat);
        mViewPager.setCurrentItem(1);
        mViewPager.setOffscreenPageLimit(2);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce)
        {
            this.finishAffinity();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(MainActivity.this,R.string.double_press,Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        },2000);
    }

    public void startService()
    {
        Intent intent = new Intent(this,NotificationService.class);
        startService(intent);
    }
    public void getPreferencesData()
    {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if (sp.contains("lang_code")){
            String langCode = sp.getString("lang_code","en");
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.setLocale(new Locale(langCode));
            res.updateConfiguration(conf, dm);
        }
    }
}

