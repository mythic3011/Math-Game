package com.mythic3011.itp4501_assignment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class GameRankingFragment extends Fragment {

    private static final String TAG = "GameRankingFragment";
    private static final String API_URL = "https://ranking-mobileasignment-wlicpnigvf.cn-hongkong.fcapp.run";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private RankingAdapter adapter;


    /**
     * Inflates the fragment layout, initializes UI components, and fetches game ranking data.
     *
     * @param inflater           LayoutInflater for inflating the fragment's view
     * @param container          ViewGroup into which the new view will be added
     * @param savedInstanceState Bundle containing previous state, if the fragment is being re-initialized
     * @return The View for the inflated fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_ranking, container, false);
        applySettings();
        recyclerView = view.findViewById(R.id.recyclerViewGameRanking);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyView = view.findViewById(R.id.emptyView);

        setupRecyclerView();
        setupSwipeRefresh();

        fetchGameRanking();

        return view;
    }

    /**
     * Applies user settings such as theme and language.
     */

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

    /**
     * Applies user settings such as theme and language.
     */
    private void loadSettings() {
        SharedPreferences prefs = requireContext().getSharedPreferences("GameSettings", MODE_PRIVATE);

        if (prefs.contains("language") && !(prefs.getAll().get("language") instanceof String)) {
            prefs.edit().remove("language").apply();
        }
    }

    /**
     * Loads settings from SharedPreferences and performs necessary cleanup.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RankingAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Sets up the RecyclerView with a LinearLayoutManager and an adapter.
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchGameRanking);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
    }

    /**
     * Fetches game ranking data from the server in a background thread and updates the UI accordingly.
     */
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

    /**
     * Parses the JSON response from the server into a list of {@link RankingItem} objects.
     *
     * @param jsonString The JSON string response from the server.
     * @return A list of {@link RankingItem} objects parsed from the JSON string.
     * @throws JSONException If there is an error parsing the JSON string.
     */
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

    /**
     * Shows the empty view when there are no items to display in the RecyclerView.
     */
    private void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the empty view when there are items to display in the RecyclerView.
     */
    private void hideEmptyView() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    /**
     * Displays an error message using a Snackbar.
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show();
        });
    }

    /**
     * Represents an item in the game ranking list.
     */
    private static class RankingItem {
        String name;
        int correct;
        int time;

        /**
         * Constructs a new {@link RankingItem} with the specified details.
         *
         * @param name    The name of the player.
         * @param correct The number of correct answers.
         * @param time    The time taken to complete the game.
         */
        RankingItem(String name, int correct, int time) {
            this.name = name;
            this.correct = correct;
            this.time = time;
        }
    }

    /**
     * Adapter for displaying {@link RankingItem} objects in a RecyclerView.
     */
    private static class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {
        private List<RankingItem> items;
        private Context context;

        /**
         * Constructs a new {@link RankingAdapter} with the specified context and items.
         *
         * @param context The context.
         * @param items   The list of {@link RankingItem} objects to display.
         */
        public RankingAdapter(Context context, List<RankingItem> items) {
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            try {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
                return new ViewHolder(view);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RankingItem item = items.get(position);
            holder.nameTextView.setText(item.name);
            holder.scoreTextView.setText(new StringBuilder()
                    .append(context.getString(R.string.ranking_correct))
                    .append(String.format(Locale.getDefault(), "%d", item.correct))
                    .append(", ")
                    .append(context.getString(R.string.ranking_time))
                    .append(String.format(Locale.getDefault(), "%ds", item.time))
                    .toString());
            holder.itemView.setOnLongClickListener(v -> {
                Snackbar.make(v, "Long press to delete", Snackbar.LENGTH_SHORT).show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        /**
         * Updates the data in the adapter and refreshes the RecyclerView.
         *
         * @param newItems The new list of {@link RankingItem} objects to display.
         */
        void updateData(List<RankingItem> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        /**
         * ViewHolder class for the RecyclerView adapter.
         * This class holds the UI components that will display the ranking item's details.
         */
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView; // TextView for displaying the player's name.
            TextView scoreTextView; // TextView for displaying the player's score and time.

            /**
             * Constructor for the ViewHolder.
             *
             * @param itemView The view of the RecyclerView item.
             */
            ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.nameTextView); // Initialize the name TextView.
                scoreTextView = itemView.findViewById(R.id.scoreTextView); // Initialize the score TextView.
            }
        }
    }
}