package com.mythic3011.itp4501_assignment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

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

        recyclerView = view.findViewById(R.id.recyclerViewYourRecords);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyView = view.findViewById(R.id.emptyView);

        dbHelper = new DatabaseHelper(requireContext());

        setupRecyclerView();
        setupSwipeRefresh();

        loadYourRecords();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GameResultAdapter(new ArrayList<>());
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

        GameResultAdapter(List<GameResult> gameResults) {
            this.gameResults = gameResults;
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
            holder.tvDate.setText(String.format("%s %s", result.getPlayDate(), result.getPlayTime()));
            holder.tvScore.setText(String.format("Correct: %d/10", result.getCorrectCount()));
            holder.tvTime.setText(String.format("Time: %ds", result.getDuration()));

            holder.itemView.setOnLongClickListener(v -> {
                // Implement delete functionality here
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

        static class ViewHolder extends RecyclerView.ViewHolder {
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