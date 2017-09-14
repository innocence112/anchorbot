package com.tqli.anchorbolt;


import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String[] sd_path = null;
//    private AnchorBoltView mAnchorBoltView = null;
    private ButtonView mAnchorBoltView = null;
    Context context = null;
    static private int openfileDialogId = 0;
    String prj_name = null;
    String file_name = new String();
    int prj_index = 0;
    boolean save_file = false;
    boolean save_file_ok = false;
    boolean open_file_ok = false;

    private BluetoothAdapter _bluetooth	 = BluetoothAdapter.getDefaultAdapter();
    private List<BluetoothDevice> _devices = new ArrayList<>();
    private volatile boolean _discoveryFinished;
    private Handler _handler = new Handler();
    BluetoothDevice remoteDevice = null;

    private Runnable _discoveryWorkder = new Runnable() {
        public void run()
        {
            _bluetooth.startDiscovery();
            for (;;)
            {
                if (_discoveryFinished)
                {
                    break;
                }
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e){}
            }
        }
    };

    private BroadcastReceiver _foundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            _devices.add(device);
            String str = device.getName();
            if(str.equals("Bolt001"))
            {
                unregisterReceiver(this);
                _discoveryFinished = true;
                remoteDevice = device;
                mAnchorBoltView.remoteBlueDevice = remoteDevice;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mAnchorBoltView = new AnchorBoltView(this,null);
        setContentView(R.layout.activity_main);
//        View v = getLayoutInflater().inflate(R.layout.activity_main,null,false);
//        ButtonView mAnchorBoltView = (ButtonView) this.findViewById(R.id.buttons);
//        mAnchorBoltView.mBluetoothAdapter = _bluetooth;
        context = MainActivity.this;
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setContentView(mAnchorBoltView);


        getExtSDCardPath();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int item_id = item.getItemId();
        switch(item_id)
        {
            case R.id.prj_name_set:
                save_file = false;
                set_prj_name();
                break;
            case R.id.measure_set:
                mAnchorBoltView.set_para(this);
                break;
            case R.id.open:
                showDialog(openfileDialogId);
                break;
            case R.id.close:
                mAnchorBoltView.seg2_file.close_file();
                mAnchorBoltView.para_refresh();
                break;
            case R.id.save:
                if(prj_name==null){
                    save_file = true;
                   set_prj_name();
                }
                else {
                    file_name = prj_name + String.format("%03d", prj_index);
                    save_file_ok = mAnchorBoltView.seg2_file.save_file(file_name);
                    open_save_file_msg(save_file_ok,true);
                }
                break;
            case R.id.blue_pair:
                blue_pair();
                break;
            case R.id.exit:
                if(mAnchorBoltView!=null)
                    mAnchorBoltView.socket_disconnect();
                MainActivity.this.finish();
                break;
        }
        return true;
    }

    public void set_prj_name()
    {
        final EditText text = new EditText(this);
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("请输入项目名称：")
                .setView( text )
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        prj_name = ""+ text.getText();
                        if(save_file) {
                            file_name = prj_name + String.format("%03d", prj_index++);
                            save_file_ok = mAnchorBoltView.seg2_file.save_file(file_name);
                            open_save_file_msg(save_file_ok,true);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    public void open_save_file_msg(boolean ok,boolean open_save)
    {

        String str1 = "文件：" + file_name + ".sg2 已保存！";
        String str2 = "文件：" + file_name + ".sg2 未保存！";
        String str3 = "文件打开错误！";
        String str = null;

        if(ok && open_save) {
            str = str1;  prj_index++;
        }
        else if(!ok && open_save)
            str = str2;
        else if(!ok && ! open_save )
            str = str3;
        Dialog dialog1 = new AlertDialog.Builder(this)
                .setTitle(str)
                .setPositiveButton("确定", null)
                .show();
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        if(id==openfileDialogId){
            Map<String, Integer> images = new HashMap<String, Integer>();
            images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
            images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
            images.put("sg2", R.drawable.filedialog_wavfile);
            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
            Dialog dialog = OpenFileDialog.createDialog(id, this, "Open File", new CallbackBundle() {
                        @Override
                        public void callback(Bundle bundle) {
                            String file_path = bundle.getString("path");
                            setTitle(file_path.substring(20));
                            open_file_ok = mAnchorBoltView.seg2_file.open_file(file_path);
                            if(open_file_ok)
                                mAnchorBoltView.refreshDrawableState();
                            else
                                open_save_file_msg(false,false);
                        }
                    },
                    ".sg2;",
                    images);
            return dialog;
        }
        return null;
    }

    public String[] getExtSDCardPath()
    {
        StorageManager storageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
            getVolumePathsMethod.setAccessible(true);
            Object[] params = {};
            Object invoke = getVolumePathsMethod.invoke(storageManager, params);
            sd_path = (String[])invoke;

            return (String[]) invoke;
        }
        catch (NoSuchMethodException e1)
        {
            e1.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void blue_pair()
    {
        if (!_bluetooth.isEnabled()) {
            finish();
            return;
        }
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(_foundReceiver, foundFilter);

        Utils.indeterminate(this, _handler, "Scanning...", _discoveryWorkder, new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog)
            {
                for (; _bluetooth.isDiscovering();)
                {
                    _bluetooth.cancelDiscovery();
                }
                _discoveryFinished = true;
            }
        }, true);
    }
/*
     public void onDestroy()
    {
        super.onDestroy();
        if(mAnchorBoltView!=null)
            mAnchorBoltView.socket_disconnect();
    }
*/

}
