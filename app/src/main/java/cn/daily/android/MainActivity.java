package cn.daily.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.daily.router.Router;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle args=new Bundle();
        args.putString("id","12345");
        args.putLong("time",12345);
        Router.with(this).setExtras(args).to("http://www.8531.cn/detail");
    }
}
