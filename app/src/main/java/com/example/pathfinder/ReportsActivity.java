package com.example.pathfinder;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pathfinder.constant.SQLCommand;
import com.example.pathfinder.util.DBOperator;
import com.example.pathfinder.view.TableView;

import java.util.Locale;

public class ReportsActivity extends Activity {

    // Same order as @array/reports
    private static final String[] QUERIES = {SQLCommand.Q3, SQLCommand.Q7, SQLCommand.Q13, SQLCommand.Q17, SQLCommand.Q2};

    private Spinner spinner;
    private EditText input;
    private HorizontalScrollView results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        spinner = findViewById(R.id.report_spinner);
        input = findViewById(R.id.input);
        results = findViewById(R.id.results);

        // The input box only shows for queries that take a value
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                input.setVisibility(QUERIES[position].contains("?") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        findViewById(R.id.show_btn).setOnClickListener(v -> showResult());
    }

    private void showResult() {
        String sql = QUERIES[spinner.getSelectedItemPosition()];
        String[] args = sql.contains("?") ? new String[]{input.getText().toString().trim().toUpperCase(Locale.ROOT)} : null;
        Cursor cursor = DBOperator.getInstance(this).execQuery(sql, args);
        Toast.makeText(this, getString(R.string.row_count, cursor.getCount()), Toast.LENGTH_SHORT).show();
        results.removeAllViews();
        results.addView(new TableView(this, cursor));
    }
}
