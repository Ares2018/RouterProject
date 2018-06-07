package cn.daily.android;

import android.app.Application;
import android.net.Uri;

import cn.daily.router.Router;

public class RouterApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Router.addInterceptor(new Router.Interceptor() {
            @Override
            public Uri before(Uri uri) {
                return uri.buildUpon().appendQueryParameter("session","session"+System.currentTimeMillis()).build();
            }
        });
    }
}
