package com.clearlee.autosendwechatmsg;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.clearlee.autosendwechatmsg.AutoSendMsgService.SEND_STATUS;
import static com.clearlee.autosendwechatmsg.AutoSendMsgService.SEND_SUCCESS;
import static com.clearlee.autosendwechatmsg.AutoSendMsgService.hasSend;
import static com.clearlee.autosendwechatmsg.AutoSendMsgService.status;
import static com.clearlee.autosendwechatmsg.WechatUtils.CONTENT;
import static com.clearlee.autosendwechatmsg.WechatUtils.NAME;
import static com.clearlee.autosendwechatmsg.WechatUtils.findTextById;
import static com.clearlee.autosendwechatmsg.WechatUtils.foundNames;
import static com.clearlee.autosendwechatmsg.WechatUtils.names;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Created by Clearlee
 * 2017/12/22.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView nameListView;
    private DataAdapter nameAdapter;
    private Button btnListContacts, btnDeleteContacts;
    private EditText contactsList;
    private AccessibilityManager accessibilityManager;
    private CheckBox selectAll;
    private TextView listTitle;

    class DataViewHodler extends RecyclerView.ViewHolder {
        TextView content;
        CheckBox selected;
        public DataViewHodler(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.item_text);
            selected = itemView.findViewById(R.id.selected);
        }
    }
    class DataAdapter extends RecyclerView.Adapter<DataViewHodler> {

        @Override
        public DataViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(MainActivity.this, R.layout.list_item, null);
            DataViewHodler holder = new DataViewHodler(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(DataViewHodler holder, int position) {
            final String name = foundNames.get(position);
            holder.content.setText(name);
            holder.selected.setChecked(names.contains(name));
            holder.selected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(names.contains(name)){
                        names.remove(name);
                    } else {
                        names.add(name);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return foundNames.size();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        for(int i = 0; i < 10; ++i) {
            foundNames.add("item" + i);
        }
        nameListView = (RecyclerView)findViewById(R.id.nameListView);
        nameAdapter = new DataAdapter();
        nameListView.setAdapter(nameAdapter);
        LinearLayoutManager layout = new LinearLayoutManager(MainActivity.this);
        nameListView.setLayoutManager(layout);

        listTitle = (TextView)findViewById(R.id.listTitle);
        selectAll = (CheckBox)findViewById(R.id.selectAll);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(names.isEmpty()) {
                    names = foundNames.stream().collect(Collectors.<String>toSet());
                } else {
                    names.clear();
                }
                nameAdapter.notifyDataSetChanged();
            }
        });
        nameAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listTitle.setText(String.format("联系人 %d/%d", names.size(), foundNames.size()));
                selectAll.setChecked(names.size() == foundNames.size());
            }
        });
        listTitle.setText(String.format("联系人 %d/%d", names.size(), foundNames.size()));

        contactsList = (EditText)findViewById(R.id.contactsList);
        btnListContacts = (Button)findViewById(R.id.btnListContacts);
        btnDeleteContacts = (Button)findViewById(R.id.btnDeleteContacts);

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
//            setValue(name, content);
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
            nameAdapter.notifyDataSetChanged();
        } else if(reason == AutoSendMsgService.Status.StatusDeleting.ordinal()) {

        }
    }

    private void setSendStatusText(int status) {
        if (status == SEND_SUCCESS) {
        } else {
        }
    }

    public void setValue(String name, String content) {
        NAME = name;
        CONTENT = content;
        hasSend = false;
    }

}
