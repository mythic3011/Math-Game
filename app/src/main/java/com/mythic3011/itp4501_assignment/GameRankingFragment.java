package com.mythic3011.itp4501_assignment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.fragment.app.Fragment;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRankingFragment extends Fragment {

    private ListView listView;
    private static final String API_URL = "https://ranking-mobileasignment-wlicpnigvf.cn-hongkong.fcapp.run";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_ranking, container, false);
        listView = view.findViewById(R.id.listViewGameRanking);
        fetchGameRanking();
        return view;
    }

    private void fetchGameRanking() {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                List<Map<String, String>> data = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Map<String, String> item = new HashMap<>();
                    item.put("name", jsonObject.getString("Name"));
                    item.put("score", "Correct: " + jsonObject.getInt("Correct") + ", Time: " + jsonObject.getInt("Time") + "s");
                    data.add(item);
                }

                getActivity().runOnUiThread(() -> {
                    SimpleAdapter adapter = new SimpleAdapter(getContext(), data,
                            android.R.layout.simple_list_item_2,
                            new String[]{"name", "score"},
                            new int[]{android.R.id.text1, android.R.id.text2});
                    listView.setAdapter(adapter);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
