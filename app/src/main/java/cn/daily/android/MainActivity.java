package cn.daily.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args=new Bundle();
                args.putString("id","12345");
                Nav.with(v.getContext()).setExtras(args).to("http://www.8531.cn/detail?time="+System.currentTimeMillis());
            }
        });
    }
}
