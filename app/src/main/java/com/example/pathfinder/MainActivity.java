package com.example.pathfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Every home button calls this; add an "else if" line here when your screen is ready
    public void open(View button) {
        int id = button.getId();
        if (id == R.id.reports_btn) startActivity(new Intent(this, ReportsActivity.class));
        else Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
    }
}
