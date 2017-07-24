package com.tatait.tatamusic.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.http.HttpCallback;
import com.tatait.tatamusic.http.HttpClient;
import com.tatait.tatamusic.model.Info;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.StringUtils;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.binding.Bind;

/**
 * 意见反馈
 */
public class FeekBackActivity extends BaseActivity implements OnClickListener {

    @Bind(R.id.feekback_activity_phone_edi)
    private EditText phone_edi;
    @Bind(R.id.feekback_activity_content_edi)
    private EditText content_edi;
    @Bind(R.id.feekback_activity_submit)
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feek_back);
    }

    @Override
    protected void setListener() {
        submit.setOnClickListener(this);
    }

    /**
     * 各种 case
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //提交反馈
            case R.id.feekback_activity_submit:
                if (!Preferences.isLogin()) {
                    ToastUtils.show(R.string.not_login_feek_back);
                    startActivity(new Intent(FeekBackActivity.this, UserInfoActivity.class));
                } else {
                    final String content = content_edi.getText() == null ? "" : content_edi.getText().toString();
                    final String phone = phone_edi.getText() == null ? "" : phone_edi.getText().toString();
                    if (StringUtils.isEmpty2(content)) {
                        ToastUtils.show(R.string.input_feek_back);
                        return;
                    }
                    AlertDialog.Builder cleanDialog = new AlertDialog.Builder(this);
                    cleanDialog.setMessage(getResources().getString(R.string.confirm_feek_back));
                    cleanDialog.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestSubmitData(content, phone);
                        }
                    });
                    cleanDialog.setNegativeButton(R.string.cancel, null);
                    cleanDialog.show();
                }
                break;
        }
    }

    /**
     * 提交反馈
     */
    private void requestSubmitData(String content, String phone) {
        HttpClient.postFeekBack(content, phone, new HttpCallback<Info>() {
            @Override
            public void onSuccess(Info response) {
                if (response == null) {
                    onFail(null);
                } else if ("1".equals(response.getCode())) {
                    ToastUtils.show(R.string.thank_feek_back);
                    mHandler.sendEmptyMessageDelayed(0x50001, 200);
                } else {
                    ToastUtils.show(response.getInfo());
                    mHandler.sendEmptyMessageDelayed(0x50001, 200);
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

    /**
     * 提交反馈的回调函数，用于异步刷新主界面的UI或者弹出提示
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x50001:
                    finish();
                    break;
            }
        }
    };
}