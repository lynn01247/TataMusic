package com.tatait.tatamusic.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.http.HttpCallback;
import com.tatait.tatamusic.http.HttpClient;
import com.tatait.tatamusic.model.User;
import com.tatait.tatamusic.utils.AuthCodeView;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.StringUtils;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.binding.Bind;

import java.util.List;

/**
 * UserInfoActivity
 * Created by Lynn on 2015/12/27.
 */
public class UserInfoActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.fl_user_info_part1)
    private FrameLayout fl_user_info_part1;
    @Bind(R.id.iv_user_info_bg)
    private ImageView iv_user_info_bg;
    @Bind(R.id.btn_user_login)
    private Button btn_user_login;
    @Bind(R.id.btn_user_register)
    private Button btn_user_register;
    @Bind(R.id.et_user_info_username)
    private EditText et_user_info_username;
    @Bind(R.id.et_user_info_password)
    private EditText et_user_info_password;
    @Bind(R.id.et_user_info_password2)
    private EditText et_user_info_password2;
    @Bind(R.id.et_user_info_code)
    private EditText et_user_info_code;
    @Bind(R.id.ac_user_info_code)
    private AuthCodeView ac_user_info_code;
    @Bind(R.id.bt_user_info_btn)
    private Button bt_user_info_btn;

    private int part;// 1 登录或注册选择界面  2 注册  3登录

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        if (!checkServiceAlive()) {
            return;
        }

        fl_user_info_part1.setVisibility(View.VISIBLE);
        part = 1;
        iv_user_info_bg.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    @Override
    protected void setListener() {
        btn_user_login.setOnClickListener(this);
        btn_user_register.setOnClickListener(this);
        bt_user_info_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_user_login:
                fl_user_info_part1.setVisibility(View.GONE);
                et_user_info_password2.setVisibility(View.GONE);
                bt_user_info_btn.setText(R.string.login);
                part = 3;
                break;
            case R.id.btn_user_register:
                fl_user_info_part1.setVisibility(View.GONE);
                et_user_info_password2.setVisibility(View.VISIBLE);
                bt_user_info_btn.setText(R.string.register);
                part = 2;
                break;
            case R.id.ac_user_info_code:
                ac_user_info_code.randomText();
                break;
            case R.id.bt_user_info_btn:
                String userName = et_user_info_username.getText().toString().trim();
                String password = et_user_info_password.getText().toString().trim();
                String password2 = et_user_info_password2.getText().toString().trim();
                String code = et_user_info_code.getText().toString().trim();
                if (StringUtils.isEmpty2(userName)) {
                    ToastUtils.show(R.string.input_name);
                    break;
                } else if (StringUtils.isEmpty2(password)) {
                    ToastUtils.show(R.string.input_password);
                    break;
                } else if (StringUtils.isEmpty2(code)) {
                    ToastUtils.show(R.string.input_codes);
                    break;
                }
                if (part == 2) {
                    if (StringUtils.isEmpty2(password2)) {
                        ToastUtils.show(R.string.input_password_again);
                        break;
                    } else if (!password.equals(password2)) {
                        ToastUtils.show(R.string.password_not_mate);
                        break;
                    }
                }
                if (!code.equals(ac_user_info_code.getAuthCode())) {
                    ToastUtils.show(R.string.code_wrong);
                    break;
                } else {
                    if (part == 2) {
                        Register(userName, password);//注册
                    } else {
                        Login(userName, password);//登录
                    }
                    break;
                }
        }
    }

    private void Register(String userName, String passWord) {
        HttpClient.getRegisterUser(userName, passWord, new HttpCallback<User>() {
            @Override
            public void onSuccess(User response) {
                if (response == null) {
                    onFail(null);
                } else if ("1".equals(response.getCode())) {
                    List data = response.getData();
                    if (data != null && data.size() > 0) {
                        User.UserData userData = (User.UserData) data.get(0);
                        String uid = userData.getUid();
                        String userName = userData.getName();
                        String token = userData.getToken();
                        Preferences.saveUid(uid);
                        Preferences.saveUserName(userName);
                        Preferences.saveUserToken(token);
                        Preferences.saveIsLogin(true);
                        ToastUtils.show(R.string.register_success);
                        setResult(0x00011);
                        finish();
                    } else {
                        ToastUtils.show(R.string.user_null);
                    }
                } else {
                    ToastUtils.show(response.getInfo());
                }
            }

            @Override
            public void onFail(Exception e) {
                if (e != null && e.getMessage() != null) {
                    ToastUtils.show(getString(R.string.get_fail_with_res, e.getMessage()));
                } else {
                    ToastUtils.show(R.string.get_fail);
                }
            }
        });
    }

    private void Login(String userName, String passWord) {
        HttpClient.getLoginUser(userName, passWord, new HttpCallback<User>() {
            @Override
            public void onSuccess(User response) {
                if (response == null) {
                    onFail(null);
                } else if ("1".equals(response.getCode())) {
                    List data = response.getData();
                    if (data != null && data.size() > 0) {
                        User.UserData userData = (User.UserData) data.get(0);
                        String uid = userData.getUid();
                        String userName = userData.getName();
                        String token = userData.getToken();
                        Preferences.saveUid(uid);
                        Preferences.saveUserName(userName);
                        Preferences.saveUserToken(token);
                        Preferences.saveIsLogin(true);
                        ToastUtils.show(R.string.login_success);
                        setResult(0x00011);
                        finish();
                    } else {
                        ToastUtils.show(R.string.user_null);
                    }
                } else {
                    ToastUtils.show(response.getInfo());
                }
            }

            @Override
            public void onFail(Exception e) {
                if (e != null && e.getMessage() != null) {
                    ToastUtils.show(getString(R.string.get_fail_with_res, e.getMessage()));
                } else {
                    ToastUtils.show(R.string.get_fail);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (part == 2 || part == 3) {
                fl_user_info_part1.setVisibility(View.VISIBLE);
                part = 1;
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}