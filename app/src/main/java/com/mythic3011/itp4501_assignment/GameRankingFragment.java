package com.mythic3011.itp4501_assignment;

import android.os.Bundle;
import android.util.Log;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GameRankingFragment extends Fragment {

    private static final String TAG = "GameRankingFragment";
    private static final String API_URL = "https://ranking-mobileasignment-wlicpnigvf.cn-hongkong.fcapp.run";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private RankingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_ranking, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGameRanking);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyView = view.findViewById(R.id.emptyView);

        setupRecyclerView();
        setupSwipeRefresh();

        fetchGameRanking();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RankingAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchGameRanking);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
    }

    void fetchGameRanking() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(API_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    List<RankingItem> data = parseJsonResponse(response.toString());

                    requireActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        if (data.isEmpty()) {
                            showEmptyView();
                        } else {
                            hideEmptyView();
                            adapter.updateData(data);
                        }
                    });
                } else {
                    Log.e(TAG, "Error fetching data. Response code: " + responseCode);
                    showError("Error fetching data. Please try again later.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception occurred while fetching game ranking", e);
                showError("Error fetching data. Please check your connection and try again.");
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing reader", e);
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private List<RankingItem> parseJsonResponse(String jsonString) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonString);
        List<RankingItem> data = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            RankingItem item = new RankingItem(
                    jsonObject.getString("Name"),
                    jsonObject.getInt("Correct"),
                    jsonObject.getInt("Time")
            );
            data.add(item);
        }
        return data;
    }

    private void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show();
        });
    }

    private static class RankingItem {
        String name;
        int correct;
        int time;

        RankingItem(String name, int correct, int time) {
            this.name = name;
            this.correct = correct;
            this.time = time;
        }
    }

    private class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {
        private List<RankingItem> items = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RankingItem item = items.get(position);
            holder.nameTextView.setText(item.name);
            holder.scoreTextView.setText(String.format("Correct: %d, Time: %ds", item.correct, item.time));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        void updateData(List<RankingItem> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView scoreTextView;

            ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                scoreTextView = itemView.findViewById(R.id.scoreTextView);
            }
        }
    }
}