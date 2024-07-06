package com.mythic3011.itp4501_assignment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class YourRecordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private DatabaseHelper dbHelper;
    private GameResultAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_records, container, false);

        applySettings();
        recyclerView = view.findViewById(R.id.recyclerViewYourRecords);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyView = view.findViewById(R.id.emptyView);

        dbHelper = new DatabaseHelper(requireContext());

        setupRecyclerView();
        setupSwipeRefresh();

        loadYourRecords();

        return view;
    }

    private void applySettings() {
        loadSettings();
        SharedPreferences prefs = requireContext().getSharedPreferences("GameSettings", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));

        int language = prefs.getInt("language", 0);
        if (language != 0) {
            Locale.setDefault(Locale.ENGLISH);
            Locale.setDefault(Locale.forLanguageTag("en"));
        }
    }

    private void loadSettings() {
        SharedPreferences prefs = requireContext().getSharedPreferences("GameSettings", MODE_PRIVATE);

        if (prefs.contains("language") && !(prefs.getAll().get("language") instanceof String)) {
            prefs.edit().remove("language").apply();
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GameResultAdapter(new ArrayList<>(), requireContext());
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadYourRecords);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
    }

    void loadYourRecords() {
        List<GameResult> gameResults = dbHelper.getAllGameResults();
        adapter.updateData(gameResults);
        swipeRefreshLayout.setRefreshing(false);

        if (gameResults.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private static class GameResultAdapter extends RecyclerView.Adapter<GameResultAdapter.ViewHolder> {
        private List<GameResult> gameResults;
        private Context context;

        GameResultAdapter(List<GameResult> gameResults, Context context) {
            this.gameResults = gameResults;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GameResult result = gameResults.get(position);
            holder.tvName.setText(result.getName());
            holder.tvDate.setText(String.format(Locale.getDefault(), "%s %s", result.getPlayDate(), result.getPlayTime()));

            holder.tvScore.setText(new StringBuilder()
                    .append(context.getString(R.string.ranking_correct))
                    .append(String.format(Locale.getDefault(), "%d/10", result.getCorrectCount()))
                    .toString());

            holder.tvTime.setText(new StringBuilder()
                    .append(context.getString(R.string.ranking_time))
                    .append(String.format(Locale.getDefault(), "%ds", result.getDuration()))
                    .toString());

            // Long press to delete
            holder.itemView.setOnLongClickListener(v -> {
                Snackbar.make(v, "Long press to delete", Snackbar.LENGTH_SHORT).show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return gameResults.size();
        }

        void updateData(List<GameResult> newData) {
            gameResults = newData;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvScore, tvTime;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvScore = itemView.findViewById(R.id.tvScore);
                tvTime = itemView.findViewById(R.id.tvTime);
            }
        }
    }
}