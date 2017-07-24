package com.tatait.tatamusic.utils.permission;

/**
 * 权限请求，针对6.0+系统
 * Created by Lynn on 2015/11/27.
 */
public interface PermissionResult {
    void onGranted();

    void onDenied();
}