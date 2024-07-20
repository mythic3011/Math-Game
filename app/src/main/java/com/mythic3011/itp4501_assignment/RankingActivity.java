package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Activity for displaying ranking information in a tabbed layout.
 * This activity includes a toolbar with options to refresh rankings, open settings, and display about information.
 * It uses a ViewPager2 to host fragments for game rankings and user records.
 */
public class RankingActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private OnBackPressedCallback backPressedCallback;

    private MediaPlayer music;

    /**
     * Sets up the activity's layout, toolbar, ViewPager for tabs, and back pressed callback on creation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        applySettings();
        setupToolbar();
        setupViewPager();
        setupTabLayout();
        setupBackPressedCallback();
        playRankingMusic();
    }

    private void applySettings() {
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
    }

    private void playRankingMusic() {
        if (isAudioEnabled()) {
            music = MediaPlayer.create(this, R.raw.song_ranking);
            music.setLooping(true);
            music.start();
        }
    }

    /**
     * Checks if vibration feedback is enabled in the preferences.
     * This method retrieves the vibration setting from the shared preferences and returns its value.
     *
     * @return True if vibration is enabled, false otherwise.
     */
    private boolean isVibrationEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("vibration_enabled", true);
    }

    /**
     * Checks if audio feedback is enabled in the preferences.
     * This method retrieves the audio setting from the shared preferences and returns its value.
     *
     * @return True if audio is enabled, false otherwise.
     */
    private boolean isAudioEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("audio_enabled", true);
    }

    /**
     * Checks if notifications are enabled in the preferences.
     * This method retrieves the notification setting from the shared preferences and returns its value.
     *
     * @return True if notifications are enabled, false otherwise.
     */
    private boolean isNotificationEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("notification_enabled", true);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume playing the background music if it has been initialized.
        if (music != null) {
            music.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause the background music to conserve resources when the activity is not in the foreground.
        if (music != null) {
            music.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Release the MediaPlayer resource when the activity is destroyed to prevent memory leaks.
        if (music != null) {
            music.release();
        }
    }

    /**
     * Initializes the back pressed callback to finish the activity.
     * This method ensures that pressing the back button will correctly finish the activity.
     */
    private void setupBackPressedCallback() {
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    /**
     * Sets up the toolbar with a back button and title.
     * This method initializes the toolbar and sets its title to the string resource R.string.ranking.
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.ranking);
        }
    }

    /**
     * Initializes the ViewPager and its adapter for swiping between fragments.
     * This method sets up the ViewPager to use a RankingPagerAdapter for displaying ranking fragments.
     */
    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new RankingPagerAdapter(this));
    }

    /**
     * Sets up the TabLayout with the ViewPager for tabbed navigation.
     * This method uses TabLayoutMediator to link the TabLayout with the ViewPager, setting the tab titles
     * and adjusting the tab indicator and text colors.
     */
    private void setupTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? getString(R.string.game_ranking) : getString(R.string.your_records))
        ).attach();

        int colorPrimary = getColor(R.color.colorPrimary);
        int colorOnSurface = getColor(R.color.colorOnSurface);

        tabLayout.setSelectedTabIndicatorColor(colorPrimary);
        tabLayout.setTabTextColors(colorOnSurface, colorPrimary);
    }

    /**
     * Handles action bar item selections.
     * This method overrides onOptionsItemSelected to handle actions for refreshing rankings, opening settings,
     * showing about dialog, and navigating back.
     *
     * @param item The menu item that was selected.
     * @return boolean Returns true if the action was handled, otherwise returns super.onOptionsItemSelected(item).
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_refresh) {
            refreshRankings();
            return true;
        } else if (itemId == R.id.action_settings) {
            openSettings();
            return true;
        } else if (itemId == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Refreshes the rankings displayed in the ViewPager.
     * This method retrieves the current fragment from the ViewPager and triggers a data refresh
     * depending on the type of the fragment (GameRankingFragment or YourRecordsFragment).
     * It also displays a toast message to inform the user that the rankings are being refreshed.
     */
    private void refreshRankings() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        RankingPagerAdapter adapter = (RankingPagerAdapter) viewPager.getAdapter();
        if (adapter != null) {
            int currentItem = viewPager.getCurrentItem();
            Fragment fragment = adapter.createFragment(currentItem);
            if (fragment instanceof GameRankingFragment) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (fragment.isAdded() && fragment.getView() != null) {
                        ((GameRankingFragment) fragment).fetchGameRanking();
                    }
                });
            } else if (fragment instanceof YourRecordsFragment) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (fragment.isAdded() && fragment.getView() != null) {
                        ((YourRecordsFragment) fragment).loadYourRecords();
                    }
                });
            }
        }
        Toast.makeText(this, R.string.refreshing, Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens the SettingsActivity.
     * This method creates an Intent to start the SettingsActivity and then starts the activity.
     */
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Displays an about dialog.
     * This method creates and shows an AlertDialog with the application's about information.
     */
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about)
                .setMessage(R.string.about_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * Inflates the menu for the toolbar.
     * This method inflates the toolbar menu from XML, adding items to the action bar if it is present.
     *
     * @param menu The options menu in which you place your items.
     * @return boolean Returns true for the menu to be displayed; if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /**
     * Adapter class for managing fragments in ViewPager2.
     * This class extends FragmentStateAdapter and is responsible for returning the correct fragment
     * based on the selected tab position. It supports two fragments: GameRankingFragment and YourRecordsFragment.
     */
    private static class RankingPagerAdapter extends FragmentStateAdapter {
        public RankingPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new GameRankingFragment() : new YourRecordsFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}