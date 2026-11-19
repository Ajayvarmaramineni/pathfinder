package com.example.pathfinder.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.pathfinder.R;

// Shows any query result as a table: column names as a red header, then one row per record
public class TableView extends TableLayout {

    public TableView(Context context, Cursor cursor) {
        super(context);
        setStretchAllColumns(true);
        addRow(cursor.getColumnNames(), true);
        String[] values = new String[cursor.getColumnCount()];
        while (cursor.moveToNext()) {
            for (int i = 0; i < values.length; i++) values[i] = cursor.getString(i);
            addRow(values, false);
        }
        cursor.close();
    }

    private void addRow(String[] values, boolean header) {
        TableRow row = new TableRow(getContext());
        if (header) row.setBackgroundColor(getContext().getColor(R.color.wpi_red));
        for (String value : values) {
            TextView cell = new TextView(getContext());
            cell.setText(value);
            cell.setMaxEms(12); // long text wraps instead of pushing other columns off screen
            cell.setPadding(12, 12, 12, 12);
            if (header) {
                cell.setTextColor(Color.WHITE);
                cell.setTypeface(null, Typeface.BOLD);
            }
            row.addView(cell);
        }
        addView(row);
    }
}
