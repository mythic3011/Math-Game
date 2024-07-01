package com.mythic3011.itp4501_assignment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YourRecordsFragment extends Fragment {

    private ListView listView;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_records, container, false);
        listView = view.findViewById(R.id.listViewYourRecords);
        dbHelper = new DatabaseHelper(getContext());
        loadYourRecords();
        return view;
    }

    private void loadYourRecords() {
        List<GameResult> gameResults = dbHelper.getAllGameResults();
        List<Map<String, String>> data = new ArrayList<>();
        for (GameResult result : gameResults) {
            Map<String, String> item = new HashMap<>();
            item.put("name", result.getName());
            item.put("date", result.getPlayDate() + " " + result.getPlayTime());
            item.put("score", "Correct: " + result.getCorrectCount() + "/10");
            item.put("time", "Time: " + result.getDuration() + "s");
            data.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(getContext(), data,
                R.layout.item_game_result,
                new String[]{"name", "date", "score", "time"},
                new int[]{R.id.tvName, R.id.tvDate, R.id.tvScore, R.id.tvTime});
        listView.setAdapter(adapter);
    }
}
