package com.example.sabrine.discovertunisia.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.example.sabrine.discovertunisia.firebase.FirebaseEntities;
import com.example.sabrine.discovertunisia.R;
import com.example.sabrine.discovertunisia.SectionsPagerAdapter;
import com.example.sabrine.discovertunisia.SettingsActivity;
import com.example.sabrine.discovertunisia.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private static final String CLASS_NAME = "MainActivity";

    private FirebaseAuth mFirebaseAuth;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    private DatabaseReference mUserDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(mToolbar);

        if (mFirebaseAuth.getCurrentUser() != null) {
            mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FirebaseEntities.Users.ENTITY_NAME).child(mFirebaseAuth.getCurrentUser().getUid());

        }

        mViewPager = (ViewPager) findViewById(R.id.activity_main_tabPager);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.activity_main_tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        super.onOptionsItemSelected(menuItem);

        switch(menuItem.getItemId()){

            case R.id.main_menu_action_logout: {
                Log.i(CLASS_NAME, "User Logout");
                mUserDatabaseRef.child(FirebaseEntities.Users.FIELD_ONLINE).setValue(ServerValue.TIMESTAMP);
                FirebaseAuth.getInstance().signOut();
                backToLogin();
            }
            break;

            case R.id.main_menu_action_settings: {
                Log.i(CLASS_NAME, "User go to settings");
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
            break;

            case R.id.main_menu_action_add_friend: {
                Log.i(CLASS_NAME, "Open AllUsersActivity to find new friends");
                Intent settingsIntent = new Intent(MainActivity.this, AllUsersActivity.class);
                startActivity(settingsIntent);
            }
            break;
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        if (currentUser == null){
            Log.w(CLASS_NAME, "User is not authenticated, back to login");
            backToLogin();
        }else {
            Log.i(CLASS_NAME, "User is now connected");
            mUserDatabaseRef.child(FirebaseEntities.Users.FIELD_ONLINE).setValue(FirebaseEntities.Users.ONLINE_VALUE_AVAILABLE);

        }


    }

    private void backToLogin() {

        Intent startIntent= new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }




    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        if(currentUser != null) {
            Log.i(CLASS_NAME, "User is now offline");
            mUserDatabaseRef.child(FirebaseEntities.Users.FIELD_ONLINE).setValue(ServerValue.TIMESTAMP);

        }

    }


}
