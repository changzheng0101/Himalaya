package com.example.hilimaya.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.example.hilimaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.DeviceInfoProviderDefault;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDeviceInfoProvider;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;


public class BaseApplication extends Application {
    private static Handler sHandler = null; //要是androidos包里的handler

    private static Context sContext = null;


    @Override
    public void onCreate() {
        super.onCreate();
        //从给的项目中粘贴过来的  秘钥什么的  都在这里就决定了 集成化喜马拉雅的sdk
        CommonRequest mXimalaya = CommonRequest.getInstanse();
        if (DTransferConstants.isRelease) {
            String mAppSecret = "8646d66d6abe2efd14f2891f9fd1c8af";
            mXimalaya.setAppkey("9f9ef8f10bebeaa83e71e62f935bede8");
            mXimalaya.setPackid("com.app.test.android");
            mXimalaya.init(this, mAppSecret, getDeviceInfoProvider(this));
        } else {
            String mAppSecret = "0a09d7093bff3d4947a5c4da0125972e";
            mXimalaya.setAppkey("f4d8f65918d9878e1702d49a8cdf0183");
            mXimalaya.setPackid("com.ximalaya.qunfeng");
            mXimalaya.init(this, mAppSecret, getDeviceInfoProvider(this));
        }
        //初始化播放器
        XmPlayerManager.getInstance(this).init();


        //初始化打印工具 使得在发布版本时候 不进行打印
        LogUtil.init(this.getPackageName(), false);

        //初始化handler
        sHandler = new Handler();

        //获得context
        sContext = getBaseContext();
    }

    public static Context getContext() {
        return sContext;
    }

    public static Handler getsHandler() {
        return sHandler;
    }

    public IDeviceInfoProvider getDeviceInfoProvider(Context context) {
        return new DeviceInfoProviderDefault(context) {
            @Override
            public String oaid() {
                return "!!!这里要传入真正的oaid oaid 接入请访问 http://www.msa-alliance.cn/col.jsp?id=120";
            }
        };
    }


}
