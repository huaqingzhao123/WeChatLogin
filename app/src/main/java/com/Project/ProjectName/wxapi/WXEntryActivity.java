package com.wingjoy.ghostshock.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.unity3d.player.UnityPlayer;
import com.wingjoy.ghostshock.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//import cn.magicwindow.Session;
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    public static class UserInfo {
        private String openid;
        private String nickname;
        private String sex;
        private String headimgurl;
        private String unionid;
        private String access_token;

        public String getOpenId() {
            return openid;
        }

        public String getNickname() {
            return nickname;
        }

        public String getaccess_token() {
            return access_token;
        }

        public String getSex() {
            return sex;
        }

        public String getHeadimgurl() {
            return headimgurl;
        }

        public String getUnionid() {
            return unionid;
        }

        public void setOpenid(String oi) {
            openid = oi;
        }

        public void setNickname(String nn) {
            nickname = nn;
        }

        public void setSex(String s) {
            sex = s;
        }

        public void setaccess_token(String at) {
            access_token = at;
        }

        public void setHeadimgurl(String hu) {
            headimgurl = hu;
        }

        public void setUnionid(String ui) {
            unionid = ui;
        }
    }

    public SendAuth.Resp x;
    public static BaseResp resp = null;
    public static String code;
    //自行更换AppID和APPSECRE
    public static final String APPID = "";
    public static final String APPSECRET = "";
    public static String ACCESS_TOKEN_URL = null;
    public static UserInfo userInfo = new UserInfo();
    public static String ACCESS_TOKEN_URL1 = null;
    public static String json1;
    public String json2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("unity", "Create成功");
        MainActivity.wxApi.handleIntent(getIntent(), this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        MainActivity.wxApi.handleIntent(intent, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onReq(BaseReq req) {
        Log.i("unity", "weChat onReq");
        switch (req.getType()) {
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp baseResp) {
        resp = baseResp;
        Log.i("unity", "weChat onResp");
        if (baseResp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
            switch (baseResp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    UnityPlayer.UnitySendMessage("Canvas", "ShareDone", "Cancel");
                    break;
                case BaseResp.ErrCode.ERR_AUTH_DENIED:
                    UnityPlayer.UnitySendMessage("Canvas", "ShareDone", "Denied");
                    break;
                default:
                    break;
            }
            finish();
        }
        if (baseResp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            if (baseResp != null) {
                SendAuth.Resp resp1 = (SendAuth.Resp) baseResp;
                x = resp1;
                code = x.code;
                Log.i("unity", code);
            }
            switch (baseResp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + APPID + "&secret=" + APPSECRET + "&code=" + code + "&grant_type=authorization_code";
                    try {
                        requestNews(ACCESS_TOKEN_URL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                case BaseResp.ErrCode.ERR_AUTH_DENIED:
                default:
                    break;
            }
            finish();
        }
    }

    public void requestNews(String s) throws Exception {
        final String urlStr = s;
        Log.i("unity", "weChat requestNews");
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader br = null;
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
                    httpconn.setRequestProperty("accept", "*/*");
                    httpconn.setDoInput(true);
                    httpconn.setDoOutput(true);
                    // 设置请求方式
                    httpconn.setRequestMethod("GET");
                    httpconn.setConnectTimeout(5000);
                    httpconn.connect();

                    if (httpconn.getResponseCode() == 200) {
                        br = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
                        json1 = br.readLine();
                        try {
                            JSONObject jsonObject = new JSONObject(json1);
                            userInfo.setaccess_token(jsonObject.optString("access_token"));
                            userInfo.setOpenid(jsonObject.optString("openid"));
                            Log.i("unity", "access_token:" + userInfo.getaccess_token());
                            Log.i("unity", "openid:" + userInfo.getOpenId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        json1 = "请求失败";
                        UnityPlayer.UnitySendMessage("BDFrame", "LoginCallback", "failed");
                    }
                    httpconn.disconnect();

                    url = new URL("https://api.weixin.qq.com/sns/userinfo?access_token=" + userInfo.getaccess_token() + "&openid=" + userInfo.getOpenId());
                    httpconn = (HttpURLConnection) url.openConnection();
                    httpconn.setRequestProperty("accept", "*/*");
                    httpconn.setDoInput(true);
                    httpconn.setDoOutput(true);
                    // 设置请求方式
                    httpconn.setRequestMethod("GET");
                    httpconn.setConnectTimeout(5000);
                    httpconn.connect();

                    if (httpconn.getResponseCode() == 200) {
                        br = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
                        json2 = br.readLine();
                        try {
                            JSONObject jsonObject1 = new JSONObject(json2);
                            userInfo.setHeadimgurl(jsonObject1.optString("headimgurl"));
                            userInfo.setNickname(jsonObject1.optString("nickname"));
                            userInfo.setSex(jsonObject1.optString("sex"));
                            userInfo.setUnionid(jsonObject1.optString("unionid"));
                            Log.i("unity", "headimgurl:" + userInfo.getHeadimgurl());
                            Log.i("unity", "nickname:" + userInfo.getNickname());
                            Log.i("unity", "sex:" + userInfo.getSex());
                            Log.i("unity", "unionid:" + userInfo.getUnionid());

                            UnityPlayer.UnitySendMessage("BDFrame", "LoginCallback", String.format("ok;%s;%s;%s;%s;%s", userInfo.getUnionid(), userInfo.getOpenId(), userInfo.getNickname(), userInfo.getHeadimgurl(), userInfo.getSex()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        json2 = "请求失败";
                        UnityPlayer.UnitySendMessage("BDFrame", "LoginCallback", "failed");
                    }
                    httpconn.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public static String getOpenId() {
        Log.i("unity", "返回的openID:" + userInfo.getOpenId());
        return userInfo.getOpenId();
    }

    public static String getUnionId() {
        Log.i("unity", "返回的Unionid:" + userInfo.getUnionid());
        return userInfo.getUnionid();
    }

    public static String getNickname() {
        Log.i("unity", "返回的Nickname:" + userInfo.getNickname());
        return userInfo.getNickname();
    }

    public static String getHeadimgurl() {
        Log.i("unity", "返回的Headimgurl:" + userInfo.getHeadimgurl());
        return userInfo.getHeadimgurl();
    }

    public static String getSex() {
        Log.i("unity", "返回的Sex:" + userInfo.getSex());
        return userInfo.getSex();
    }
}
