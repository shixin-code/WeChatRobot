package com.clearlee.autosendwechatmsg;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.clearlee.autosendwechatmsg.AutoSendMsgService.SEND_STATUS;
import static com.clearlee.autosendwechatmsg.AutoSendMsgService.SEND_SUCCESS;
import static com.clearlee.autosendwechatmsg.AutoSendMsgService.hasSend;
import static com.clearlee.autosendwechatmsg.AutoSendMsgService.status;
import static com.clearlee.autosendwechatmsg.WechatUtils.CONTENT;
import static com.clearlee.autosendwechatmsg.WechatUtils.NAME;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Clearlee
 * 2017/12/22.
 */
public class MainActivity extends AppCompatActivity {

    private Button btnListContacts, btnDeleteContacts;
    private EditText contactsList;
    private TextView start, sendStatus;
    private EditText sendName, sendContent;
    private AccessibilityManager accessibilityManager;
    private String name, content;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        contactsList = (EditText)findViewById(R.id.contactsList);
        btnListContacts = (Button)findViewById(R.id.btnListContacts);
        btnDeleteContacts = (Button)findViewById(R.id.btnDeleteContacts);

        sendName = (EditText) findViewById(R.id.sendName);
        sendContent = (EditText) findViewById(R.id.sendContent);
        sendStatus = (TextView) findViewById(R.id.sendStatus);

        btnListContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status = AutoSendMsgService.Status.StatusListing;
                checkAndListContacts();
            }
        });
        btnDeleteContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = AutoSendMsgService.Status.StatusDeleting;
                checkAndDeleteContacts();
            }
        });
    }

    private void checkAndListContacts() {
        if(checkEnableService()){
            WechatUtils.names.clear();
            WechatUtils.foundNames.clear();
            status = AutoSendMsgService.Status.StatusListing;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    statusHandler.sendEmptyMessage(goWechat(status));
                }
            }).start();
        }
    }

    private void checkAndDeleteContacts() {
        if(!checkEnableService())
            return;
        String content = contactsList.getText().toString();
        WechatUtils.names = new HashSet<String>(Arrays.asList(content.split("\n")));
        if(WechatUtils.names.isEmpty()){
            Toast.makeText(MainActivity.this, "联系人列表不能为空！", Toast.LENGTH_SHORT);
            return;
        }

        status = AutoSendMsgService.Status.StatusDeleting;
        new Thread(new Runnable() {
            @Override
            public void run() {
                statusHandler.sendEmptyMessage(goWechat(status));
            }
        }).start();
    }

    private int goWechat(AutoSendMsgService.Status reason) {
        try {
            setValue(name, content);
            hasSend = false;
            Intent intent = new Intent();
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(WeChatTextWrapper.WECAHT_PACKAGENAME, WeChatTextWrapper.WechatClass.WECHAT_CLASS_LAUNCHUI);
            startActivity(intent);

            while (true) {
                if (status.equals(AutoSendMsgService.Status.StatusNone)) {
                    return reason.ordinal();
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        openService();
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return AutoSendMsgService.Status.StatusNone.ordinal();
        }
    }


    private void openService() {
        try {
            //打开系统设置中辅助功能
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "找到微信自动发送消息，然后开启服务即可", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkEnableService() {
        accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if(!accessibilityManager.isEnabled()){
            openService();
            return false;
        }
        return true;
    }


    Handler statusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            setSendStatusText(msg.what);
            handleResult(msg.what);
        }
    };

    private void handleResult(int reason)
    {
        if(reason == AutoSendMsgService.Status.StatusListing.ordinal()){
            String content = String.join("\n", WechatUtils.foundNames);
            contactsList.setText(content);
        } else if(reason == AutoSendMsgService.Status.StatusDeleting.ordinal()) {

        }
    }

    private void setSendStatusText(int status) {
        if (status == SEND_SUCCESS) {
            sendStatus.setText("微信发送成功");
        } else {
            sendStatus.setText("微信发送失败");
        }
    }

    public void setValue(String name, String content) {
        NAME = name;
        CONTENT = content;
        hasSend = false;
    }

}
