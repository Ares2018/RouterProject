package cn.daily.router;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
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

    /**
     * 添加全局拦截器
     * @param interceptor
     */
    public static void addInterceptor(Interceptor interceptor) {
        if (sInterceptors == null) {
            sInterceptors = new ArrayList<>();
        }
        sInterceptors.add(interceptor);
    }

    /**
     * 根据URL进行页面跳转
     * @param url
     * @return
     */
    public boolean to(String url) {
        return to(url, DEFAULT_REQUEST_CODE);
    }

    /**
     * 根据URL和RequestCode进行跳转
     * @param url
     * @param requestCode
     * @return
     */
    public boolean to(String url, int requestCode) {
        if (TextUtils.isEmpty(url)) {
            if (BuildConfig.DEBUG) {
                throw new NullPointerException("url");
            }
            return false;
        }
        return to(Uri.parse(url), requestCode);
    }

    /**
     * 具有固定的host和Scheme通过Path进行跳转
     * @param path
     * @return
     */
    public boolean toPath(String path) {
        return to(handlePath(path),DEFAULT_REQUEST_CODE);
    }

    /**
     * @param path
     * @param requestCode
     * @return
     */
    public boolean toPath(String path, int requestCode) {
        return to(handlePath(path), requestCode);
    }

    @Nullable
    private Uri handlePath(String path) {
        Uri uri = null;
        if (sInterceptors != null && sInterceptors.size() > 0) {
            for (Interceptor interceptor : sInterceptors) {
                uri = interceptor.buildByPath(mContext, path);
            }
        }
        if (uri == null) {
            uri = Uri.parse(path)
                    .buildUpon()
                    .scheme(mContext.getString(R.string.default_scheme))
                    .authority(mContext.getString(R.string.default_host))
                    .build();
        }
        return uri;
    }

    /**
     * 页面之间传递参数
     * @param bundle
     * @return
     */
    public Router setExtras(Bundle bundle) {
        mBundle = bundle;
        return this;
    }


    /**
     * 设置Action
     * @param action
     * @return
     */
    public Router setAction(String action) {
        mAction = action;
        return this;
    }

    /**
     * 添加Category
     * @param category
     * @return
     */
    public Router addCategory(String category) {
        if (mCategories == null) {
            mCategories = new ArrayList<>();
        }
        mCategories.add(category);
        return this;
    }

    /**
     * 删除Category
     * @param category
     * @return
     */
    public Router removeCategory(String category) {
        if (mCategories != null && mCategories.size() > 0) {
            mCategories.remove(category);
        }
        return this;
    }

    /**
     * 根据uri和requestCode进行页面跳转
     * @param uri
     * @param requestCode
     * @return
     */
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
                uri = interceptor.before(uri);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mIntent.setData(uri.normalizeScheme());
        }else {
            mIntent.setData(uri);
        }

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

    /**
     * 子类定制
     * @param context
     * @param fragment
     * @param intent
     * @param requestCode
     * @return
     */
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
        /**
         * 全局拦截uri,进行全局修改
         * @param uri
         * @return
         */
        Uri before(Uri uri);

        /**
         * 动态修改Router.toPath()中默认的host和Scheme
         * @param context
         * @param path
         * @return
         */
        Uri buildByPath(Context context, String path);
    }
}
