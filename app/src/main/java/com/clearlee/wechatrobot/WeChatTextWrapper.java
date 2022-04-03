package com.clearlee.wechatrobot;

/**
 * Created by Clearlee on 2017/12/22 0023.
 * 微信版本6.6.0
 */

public class WeChatTextWrapper {

    public static final String WECAHT_PACKAGENAME = "com.tencent.mm";


    public static class WechatClass{
        //微信首页
        public static final String WECHAT_CLASS_LAUNCHUI = "com.tencent.mm.ui.LauncherUI";
        //微信联系人页面
        public static final String WECHAT_CLASS_CONTACTINFOUI = "com.tencent.mm.plugin.profile.ui.ContactInfoUI";
        //微信聊天页面
        public static final String WECHAT_CLASS_CHATUI = "com.tencent.mm.ui.chatting.ChattingUI";
        //资料设置界面
        public static final String WECHAT_CLASS_PROFILESETTINGUI = "com.tencent.mm.plugin.profile.ui.ProfileSettingUI";
        //删除联系人确认窗口
        public static final String WECHAT_CLASS_DELETE_CONFIRMUI = "com.tencent.mm.ui.widget.a.e";
    }


    public static class WechatId{
        /**
         * 通讯录界面
         */
        public static final String WECHATID_CONTACTUI_LISTVIEW_ID = "com.tencent.mm:id/jc";
        public static final String WECHATID_CONTACTUI_ITEM_ID = "com.tencent.mm:id/bn6";
        public static final String WECHATID_CONTACTUI_NAME_ID = "com.tencent.mm:id/gw6";

        /**
         * 聊天界面
         */
        public static final String WECHATID_CHATUI_CHATINFO_ID = "com.tencent.mm:id/bu5";
        public static final String WECHATID_CHATUI_EDITTEXT_ID = "com.tencent.mm:id/a_z";
        public static final String WECHATID_CHATUI_USERNAME_ID = "com.tencent.mm:id/ha";
        public static final String WECHATID_CHATUI_BACK_ID = "com.tencent.mm:id/h9";
        public static final String WECHATID_CHATUI_SWITCH_ID = "com.tencent.mm:id/a_x";

        /**
         * 用户信息界面
         */
        public static final String WECHATID_CONTACTINFO_MORE_ID = "com.tencent.mm:id/eh";

        /**
         * 资料设置界面
         */
        public static final String WECHAT_PROFILESETTING_SUMMARY_ID = "android:id/summary";
        public static final String WECHAT_DELETE_CONFIRM_CONTENT_ID = "com.tencent.mm:id/gh3";
    }

}
