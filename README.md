# 路由框架
原理:Android原生隐式调用规则。根据AndroidMainifest.xml中intent-filter中的配置进行Activity的查找，匹配成功进入对应的Activity页面，匹配失败使用默认浏览器打开。
## 使用方法

### 添加依赖
1. 主工程build.gradle添加仓库地址:
	
	```
	allprojects {
	    repositories {
	        google()
	        jcenter()
	        maven { url "http://10.100.62.98:8086/nexus/content/groups/public" }
	    }
	}
	```
2. 项目工程build.gradle 添加依赖,最新版本请查看 [最新版本](http://10.100.62.98:8086/nexus/#nexus-search;gav~cn.daily.android~router~~~)

	```
	compile 'cn.daily.android:router:0.0.1.6-SNAPSHOT'
	```
3. 确定应用中唯一的URI。例如:http://www.8531.cn/detail
4. AndroidManifest.xml配置
	
	```
	        <activity android:name=".DetailActivity">
	            <intent-filter>
	                <action android:name="android.intent.action.VIEW" />
	
	                <category android:name="android.intent.category.BROWSABLE" />
	                <category android:name="android.intent.category.DEFAULT" />
	
	                <data
	                    android:host="www.8531.cn"
	                    android:path="/detail"
	                    android:scheme="http" />
	            </intent-filter>
	        </activity>	
	```
        
5. 调用方法 ``Router.with(this).to("http://www.8531.cn/detail");``
6. 页面间参数传递
	* 通过URL中添加参数方法 例如:http://www.8531.cn/detail?id=12244&time=12333  
	 	   Activity获取参数 ``String id = getIntent().getData().getQueryParameter("id");``
	* 通过intent方法
	
		```
			Bundle args=new Bundle();
			args.putString("id","12345");
			args.putLong("time",12345);
			Router.with(this).setExtras(args).to("http://www.8531.cn/detail");
		```
		
		Activity获取参数 ``String id = getIntent().getStringExtra("id");``
		
7. 路由拦截
	重写方法onIntercept 返回true表示拦截
	
	```
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
	```
8. 全局添加参数

```
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
```