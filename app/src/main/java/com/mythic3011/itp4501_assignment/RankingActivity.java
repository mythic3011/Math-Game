package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RankingActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private OnBackPressedCallback backPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        setupToolbar();
        setupViewPager();
        setupTabLayout();
    }

    private void setupBackPressedCallback() {
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.ranking);
        }
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new RankingPagerAdapter(this));
    }

    private void setupTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? getString(R.string.game_ranking) : getString(R.string.your_records))
        ).attach();

        // Use theme attributes for colors to support dark mode
        int colorPrimary = getColor(R.color.colorPrimary);
        int colorOnSurface = getColor(R.color.colorOnSurface);

        tabLayout.setSelectedTabIndicatorColor(colorPrimary);
        tabLayout.setTabTextColors(colorOnSurface, colorPrimary);
    }

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

    private void refreshRankings() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        RankingPagerAdapter adapter = (RankingPagerAdapter) viewPager.getAdapter();
        if (adapter != null) {
            int currentItem = viewPager.getCurrentItem();
            Fragment fragment = adapter.createFragment(currentItem);
            if (fragment instanceof GameRankingFragment) {
                ((GameRankingFragment) fragment).fetchGameRanking();
            } else if (fragment instanceof YourRecordsFragment) {
                ((YourRecordsFragment) fragment).loadYourRecords();
            }
        }
        Toast.makeText(this, R.string.refreshing, Toast.LENGTH_SHORT).show();
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about)
                .setMessage(R.string.about_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }



    private static class RankingPagerAdapter extends FragmentStateAdapter {
        public RankingPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

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