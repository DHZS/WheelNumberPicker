package com.sunny.wheelnumberpicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.sunny.wheelnumberpicker.lib.WheelNumberPicker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WheelNumberPicker picker = (WheelNumberPicker) findViewById(R.id.picker);
        picker.setOnNumberChangedListener(new WheelNumberPicker.OnNumberChangedListener() {
            @Override
            public void onClockWise() {

            }

            @Override
            public void onAntiClockWise() {

            }

            @Override
            public void onNumberChanged(float number) {

            }

            @Override
            public void onFinished(float number) {
                Toast.makeText(MainActivity.this, "" + number, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_site == item.getItemId()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DHZS/WheelNumberPicker"));
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
