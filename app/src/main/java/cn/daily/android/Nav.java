package cn.daily.android;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import cn.daily.router.Router;

public class Nav extends Router {
    protected Nav(Context context) {
        super(context);
    }

    protected Nav(Fragment fragment) {
        super(fragment);
    }

    @Override
    protected boolean onIntercept(Context context, Fragment fragment, Intent intent, int requestCode) {
        Intent temp = queryIntent(context, intent);
        if (temp != null) {
            startActivity(context, fragment, temp, requestCode);
            return true;
        } else {
            return false;
        }
    }
}
