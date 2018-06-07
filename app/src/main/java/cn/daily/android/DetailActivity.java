package cn.daily.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        String id = getIntent().getStringExtra("id");
        String time = getIntent().getData().getQueryParameter("time");
        TextView info = findViewById(R.id.textView);
        info.setText("id:" + id + "\n" + "time:" + time);
    }
}
