package com.omarea.shared.model;

public class AppConfigInfo {
    public String packageName;

    // AppConfig
    public boolean aloneLight = false;
    // 屏蔽通知
    public boolean disNotice = false;
    // 拦截按键
    public boolean disButton = false;
    // 禁止后台（切换到后台立即休眠）
    public boolean disBackgroundRun = false;
    // 启动时开启GPS
    public boolean gpsOn = false;
    // 不使用时自动冻结
    public boolean freeze = false;

    // Xposed
    public int dpi = -1;
    public boolean excludeRecent = false;
    public boolean smoothScroll = false;
}
