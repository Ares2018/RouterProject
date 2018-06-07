package cn.daily.router;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class Router {
    private static final int DEFAULT_REQUEST_CODE = -1;
    private Context mContext;
    private Intent mIntent;
    private int mRequestCode;
    private Fragment mFragment;
    private Bundle mBundle;
    private String mAction;
    private List<String> mCategories;
    private static List<Interceptor> sInterceptors;

    protected Router(Context context) {
        mContext = context;
        mIntent = new Intent();
        mIntent.setAction(Intent.ACTION_VIEW);
    }

    protected Router(Fragment fragment) {
        this(fragment.getContext());
        mFragment = fragment;
    }

    public static Router with(Context context) {
        return new Router(context);
    }

    public static Router with(Fragment fragment) {
        return new Router(fragment);
    }

    public static void addInterceptor(Interceptor interceptor) {
        if (sInterceptors == null) {
            sInterceptors = new ArrayList<>();
        }
        sInterceptors.add(interceptor);
    }

    public boolean to(String url) {
        return to(url, DEFAULT_REQUEST_CODE);
    }

    public boolean to(String url, int requestCode) {
        if (TextUtils.isEmpty(url)) {
            if (BuildConfig.DEBUG) {
                throw new NullPointerException("url");
            }
            return false;
        }
        return to(Uri.parse(url), requestCode);
    }

    public boolean toPath(String path) {
        return to(handlePath(path));
    }

    public boolean toPath(String path, int requestCode) {
        return to(handlePath(path), requestCode);
    }

    @Nullable
    private String handlePath(String path) {
        if (!TextUtils.isEmpty(path) && path.startsWith("/")) {
            path = mContext.getString(R.string.default_scheme)
                    + "://"
                    + mContext.getString(R.string.default_host)
                    + path;
        }
        return path;
    }

    public Router setExtras(Bundle bundle) {
        mBundle = bundle;
        return this;
    }


    public Router setAction(String action) {
        mAction = action;
        return this;
    }

    public Router addCategory(String category) {
        if (mCategories == null) {
            mCategories = new ArrayList<>();
        }
        mCategories.add(category);
        return this;
    }

    public Router removeCategory(String category) {
        if (mCategories != null && mCategories.size() > 0) {
            mCategories.remove(category);
        }
        return this;
    }

    public boolean to(Uri uri, int requestCode) {

        if (uri == null) {
            if (BuildConfig.DEBUG) {
                throw new NullPointerException("uri");
            }
            return false;
        }

        mRequestCode = requestCode;

        if (!TextUtils.isEmpty(mAction)) {
            mIntent.setAction(mAction);
        }

        if (mCategories != null && mCategories.size() > 0) {
            for (int i = 0; i < mCategories.size(); i++) {
                mIntent.addCategory(mCategories.get(i));
            }
        }

        if (mBundle != null) {
            mIntent.putExtras(mBundle);
        }

        if (sInterceptors != null && sInterceptors.size() > 0) {
            for (Interceptor interceptor : sInterceptors) {
                interceptor.before(uri);
            }
        }
        mIntent.setData(uri.normalizeScheme());

        if (onIntercept(mContext, mFragment, mIntent, mRequestCode)) {
            return false;
        }

        Intent intent = queryIntent(mContext, mIntent);
        if (intent != null) {
            startActivity(mContext, mFragment, intent, mRequestCode);
            return true;
        }
        return false;
    }

    protected boolean onIntercept(Context context, Fragment fragment, Intent intent, int requestCode) {
        return false;
    }


    protected Intent queryIntent(Context context, Intent intent) {
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities
                (intent, PackageManager.MATCH_ALL);
        try {
            if (resolveInfoList == null || resolveInfoList.size() == 0) {
                throw new ActivityNotFoundException("Not match any Activity:" + intent.toString());
            } else {
                ActivityInfo activityInfo = resolveInfoList.get(0).activityInfo;
                //优先匹配应用自己的Activity
                for (int i = 0; i < resolveInfoList.size(); i++) {
                    if (resolveInfoList.get(i).activityInfo.packageName.equals(context.getPackageName())) {
                        activityInfo = resolveInfoList.get(i).activityInfo;
                        break;
                    }
                }
                intent.setClassName(activityInfo.packageName, activityInfo.name);
            }
            return intent;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void startActivity(Context context, Fragment fragment, Intent intent, int requestCode) {
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public interface Interceptor {
        void before(Uri uri);
    }
}
