package com.Project.ProjectName;

import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.wingjoy.ghostshock.wxapi.WXEntryActivity;

public class MainActivity extends  com.unity3d.player.UnityPlayerActivity {

    public static IWXAPI wxApi;
    private static Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        regToWx();

        mVibrator=(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);
    }

    public void regToWx() {
        wxApi = WXAPIFactory.createWXAPI(this, WXEntryActivity.APPID,false);
        boolean suc = wxApi.registerApp(WXEntryActivity.APPID);
        if (suc) {
            Log.i("unity","注册wxapi成功");
        } else {
            Log.i("unity","注册wxapi失败");
        }
    }

    public void loginByWeiXin() {
        Log.i("unity","LoginByWeiXin");
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_aifuns";
        wxApi.sendReq(req);
    }

    /**
     * 震动时长控制
     * @param milliseconds
     */
    public void vibrate(long milliseconds) {
        Log.i("unity", "vibrate");
        mVibrator.vibrate(milliseconds);
    }

    /**
     * 震动类型
     * @param feedbackConstant
     */
    public void performHapticFeedback(int feedbackConstant) {
        getCurrentFocus().performHapticFeedback(feedbackConstant);
    }
}
