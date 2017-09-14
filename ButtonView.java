package com.tqli.anchorbolt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import static android.R.attr.delay;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

/**
 * Created by TYM on 2017/8/8 0008.
 */

public class ButtonView extends View implements Runnable{
    Paint mPaint = new Paint();

    int width;
    int height;

    int gain_in_db;
    int samples;
    double sample_interval;
    int sample_rate;

    WifiTranceiver wifi_send_recv = null;
    BluetoothDevice remoteBlueDevice = null;
    BlueTranceiver blue_send_recv = null;
    BluetoothAdapter mBluetoothAdapter;
    Context parent_context;

    public byte samples_index = 1;
    public byte sample_rate_index = 1;
    public byte gain_index = 2;
    public byte trigger_level_index = 3;

    public boolean  measure_on;
    boolean trans_data_on;
    int trans_data_repeat;
    boolean[] check = new boolean[4096];

    Seg2File    seg2_file = null;


//    AnchorBoltView anchorBoltView = new AnchorBoltView(getContext(), null);

    Global g = new Global();

    private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
            | Canvas.CLIP_TO_LAYER_SAVE_FLAG;

    public ButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gain_in_db = g.gain_array[gain_index];
        samples = g.samples_array[samples_index];
        sample_rate = g.sample_rate_array[sample_rate_index];
        sample_interval = 1.0/sample_rate;

        seg2_file = new Seg2File();
        new Thread(this).start();

    }

    public void para_refresh()
    {
        gain_in_db = g.gain_array[gain_index];
        samples = g.samples_array[samples_index];
        sample_rate = g.sample_rate_array[sample_rate_index];
        sample_interval = 1.0/sample_rate;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = getWidth();
        height = getHeight();

        mPaint.setColor(Color.DKGRAY);
        mPaint.setTextSize(30);
        canvas.saveLayerAlpha(0, 0, 2 * width, 2 * height, 0x88, LAYER_FLAGS);
        //canvas.drawColor(Color.GRAY);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(width / 2, height - 75, 50, mPaint);
        if (measure_on)
            mPaint.setColor(Color.RED);
        else
            mPaint.setColor(Color.DKGRAY);
        canvas.drawCircle(width / 2, height - 75, 45, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(width / 2 - 150, height - 75, 50, mPaint);
        canvas.drawCircle(width / 2 + 150, height - 75, 50, mPaint);
        canvas.drawCircle(width - 75, height - 75, 50, mPaint);
        canvas.drawCircle(75, height - 75, 50, mPaint);
        mPaint.setColor(Color.GRAY);
        canvas.drawCircle(width / 2 - 150, height - 75, 45, mPaint);
        canvas.drawCircle(width / 2 + 150, height - 75, 45, mPaint);
        canvas.drawCircle(width - 75, height - 75, 45, mPaint);
        canvas.drawCircle(75, height - 75, 45, mPaint);
        mPaint.setColor(Color.BLUE);
        mPaint.setTextSize(40);
        String str = new String("<");
        Rect r = new Rect();
        mPaint.getTextBounds(str, 0, str.length(), r);
        canvas.drawText(str, width / 2 - 150 - (r.right - r.left) / 2, height - 75 + (r.bottom - r.top) / 2, mPaint);
        str = new String(">");
        canvas.drawText(str, width / 2 + 150 - (r.right - r.left) / 2, height - 75 + (r.bottom - r.top) / 2, mPaint);
        str = str.format("%d", 0);
        mPaint.getTextBounds(str, 0, str.length(), r);
        canvas.drawText(str, width - 75 - (r.right - r.left) / 2, height - 75 + (r.bottom - r.top) / 2, mPaint);
        str = new String("RT");
        mPaint.getTextBounds(str, 0, str.length(), r);
        canvas.drawText(str, 75 - (r.right - r.left) / 2, height - 75 + (r.bottom - r.top) / 2, mPaint);
        canvas.restore();
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x, y;
        int act = event.getAction();
        if (act == ACTION_DOWN) {

        } else if (act == ACTION_MOVE) {

        } else if (act == ACTION_UP) {
            x = event.getX();
            y = event.getY();
            if ((x - width / 2) * (x - width / 2) + (y - height + 75) * (y - height + 75) < 2500) {
                //measure_on = !measure_on;
                if (!measure_on) {
                    byte[] str = {(byte) 0xA2, g.READY_SAMPLE, 0x00, 0x08, samples_index, sample_rate_index
                            , gain_index, trigger_level_index, 0x00, 0x00, 0x00};
                    int t = g.calcCrc16(str, 0, 8, 0xffff);
                    str[8] = (byte) t;
                    str[9] = (byte) (t >> 8);
                    send_cmd(str, 10);
                } else {
                    byte[] str = {(byte) 0xA2, g.POWER_OFF, 0x00, 0x08, samples_index, sample_rate_index
                            , gain_index, trigger_level_index, 0x00, 0x00, 0x00};
                    int t = g.calcCrc16(str, 0, 8, 0xffff);
                    str[8] = (byte) t;
                    str[9] = (byte) (t >> 8);
                    send_cmd(str, 10);
                }
            }
            if ((x - width / 2 - 150) * (x - width / 2 - 150) + (y - height + 75) * (y - height + 75) < 2500) {
                if (seg2_file.trace_nums - 1 > seg2_file.cur_trace)
                    seg2_file.cur_trace++;
            }
            if ((x - width / 2 + 150) * (x - width / 2 + 150) + (y - height + 75) * (y - height + 75) < 2500) {
                if (seg2_file.cur_trace > 0)
                    seg2_file.cur_trace--;
            }
            if ((x - width + 75) * (x - width + 75) + (y - height + 75) * (y - height + 75) < 2500) {

                seg2_file.add_seg2_data(samples, sample_interval, gain_in_db);
                seg2_file.cur_trace = seg2_file.DataArray.size() - 1;
            }
            if ((x - 75) * (x - 75) + (y - height + 75) * (y - height + 75) < 2500) {
                if (seg2_file.trace_nums == 0) {
                    seg2_file.add_seg2_data(samples, sample_interval, 0);
                    for (int i = 0; i < seg2_file.max_blocks; i++)
                        check[i] = false;
                }
                trans_data_on = true;
                trans_data_repeat = 0;
            }

        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return true;
    }


    public void socket_connect() {
        if (g.wifi_or_blue) {
            wifi_send_recv = new WifiTranceiver();
            if (wifi_send_recv.connect_ok)
                wifi_send_recv.start();
            else
                wifi_send_recv = null;
        } else {
            if (remoteBlueDevice == null) {
                remoteBlueDevice = mBluetoothAdapter.getRemoteDevice("78:A5:04:6A:34:DD");
                if (remoteBlueDevice == null) {
                    blue_connect_error();
                    return;
                }
            }
            blue_send_recv = null;
            blue_send_recv = new BlueTranceiver(remoteBlueDevice, parent_context);
            if (blue_send_recv.connect_ok)
                blue_send_recv.start();
            else
                blue_send_recv = null;
        }
    }

    public void socket_disconnect() {
        if (g.wifi_or_blue) {
            wifi_send_recv = null;
        } else {
            if (blue_send_recv != null) {
                if (blue_send_recv.socket.isConnected()) {
                    try {
                        blue_send_recv.socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                blue_send_recv = null;
            }
        }
    }

    public void blue_connect_error() {

        Dialog dialog = new AlertDialog.Builder(parent_context)
                .setTitle("蓝牙连接错误！")
                .setPositiveButton("确定", null)
                .show();
    }

    public void set_para(Context context) {
        ParaSetup dialog = new ParaSetup(context, "参数设置", new ParaSetup.OnCustomDialogListener() {

            public void back(byte len, byte rate, byte gain, byte trig) {
                samples_index = len;
                sample_rate_index = rate;
                gain_index = gain;
                trigger_level_index = trig;
                gain_in_db = g.gain_array[gain_index];
                samples = g.samples_array[samples_index];
                sample_rate = g.sample_rate_array[sample_rate_index];
                sample_interval = 1.0 / sample_rate;
            }
        });
        dialog.setParaInit(samples_index, sample_rate_index, gain_index, trigger_level_index);
        dialog.show();

    }

    public void send_cmd(byte[] str, int len) {
        if (g.wifi_or_blue) {
            if (wifi_send_recv == null) {
                socket_connect();
            }
            if (wifi_send_recv != null) {
                wifi_send_recv.set_pack(str, len);
            }
        } else {
            if (blue_send_recv == null) {
                socket_connect();
            }
            if (blue_send_recv != null) {
                blue_send_recv.set_pack(str, len);
            }
        }
    }

    public void parse_recv_pack(byte[] msg, int len) {
        int n = 0;
        if (g.wifi_or_blue) {
            if (wifi_send_recv != null)
                n = wifi_send_recv.data_recved_len;
        } else {
            if (blue_send_recv != null)
                n = blue_send_recv.data_recved_len;
        }
        byte[] temp = new byte[64];
        for (int j = 0; j < n; j++) {
            if ((msg[j] == (byte) 0x2A) && (g.isCrc16Good(msg, j, msg[j + 3] + 2, 0xffff))) {
                int l = (int) msg[j + 3];
                for (int i = 0; i < l; i++)
                    temp[i] = msg[j++];
                parse_msg(temp, temp[3]);
                j--;
            }
        }
    }

    public void parse_msg(byte[] msg, byte len) {
        switch (msg[1]) {
            case (byte) 0xD8:  //GET_STATE_ACK
                if ((msg[4] & 0x02) == 0x02)
                    measure_on = true;
                else
                    measure_on = false;
                if ((msg[4] & 0x04) == 0x04) {
                    if (seg2_file.trace_nums == 0) {
                        seg2_file.add_seg2_data(samples, sample_interval, 0);
                    }
                    for (int i = 0; i < seg2_file.max_blocks; i++)
                        check[i] = false;
                    trans_data_on = true;
                    trans_data_repeat = 0;
                }
                break;
            case (byte) 0xD1://WORK_PARA_SET_ACK:

                break;
            case (byte) 0xD7://DATA_TRANS_ACK:
                int data_len = (msg[3] - 6) / 2;
                byte[] temp = new byte[2];
                temp[0] = msg[4];
                temp[1] = msg[5];
                int index = (int) (g.bytes_2_short(temp));
                int j, i;
                if (msg[2] == 0x00) {
                    check[index] = true;
                    int n = seg2_file.DataArray.size();
                    seg2_file.trace_nums = (short) n;
                    Seg2Data data = seg2_file.DataArray.get(n - 1);
                    for (j = index * 24, i = 6; i < len; j++) {
                        if (j >= samples) break;
                        temp[0] = msg[i++];
                        temp[1] = msg[i++];
                        data.pdata[j] = (int) (g.bytes_2_short(temp));
                    }
                }
                break;
            case (byte) 0xD9://DATA_TRANS_END:
                trans_data_on = true;
                trans_data_repeat = 0;
                refreshDrawableState();
                break;
        }
    }

    public void trans_data() {
        int index = 0;
        byte[] str = new byte[68];
        str[0] = (byte) 0xA2;
        str[1] = g.LONG_BLOCK_DATA_TRANS_LIST;
        str[2] = 0x00;

        for (int i = 0; i < 1; i++) {
            boolean received = false;
            boolean first = true;
            short[] error_index = new short[4096];
            short[] error_len = new short[4096];
            error_len[index] = 0;
            for (int j = 0; j < seg2_file.max_blocks; j++) {
                if (check[j] == false) {
                    if (first) {
                        error_index[index++] = (short) j;
                        error_len[index] = 0;
                        first = false;
                    }
                    error_len[index - 1]++;
                } else
                    first = true;
            }
            if (index == 0) {
                Seg2Data data = seg2_file.DataArray.get(seg2_file.trace_nums - 1);
                data.data_transed = true;
                trans_data_on = false;
                return;
            }

            int errors = index > 12 ? 12 : index;
            byte len = (byte) (4 * errors + 6);
            str[3] = len;
            str[4] = (byte) (delay & 0x00ff);
            str[5] = (byte) ((delay >> 8) & 0x00ff);

            int k = 6;
            for (int j = 0; j < errors; j++) {
                str[k++] = (byte) (error_index[j] & 0x00ff);
                str[k++] = (byte) ((error_index[j] >> 8) & 0x00ff);
                str[k++] = (byte) (error_len[j] & 0x00ff);
                str[k++] = (byte) ((error_len[j] >> 8) & 0x00ff);
            }
            int t = g.calcCrc16(str, 0, len, 0xffff);
            str[len] = (byte) t;
            str[len + 1] = (byte) (t >> 8);
            send_cmd(str, len + 2);
        }
    }

        public void run()
    {
        int wait_cnt = 0;
        while (!Thread.currentThread().isInterrupted())
        {
            if(wifi_send_recv!=null) {
                if (wifi_send_recv.data_recved) {
                    byte[] msg = wifi_send_recv.recv_pack.getData();
                    parse_recv_pack(msg,msg.length);
                    wifi_send_recv.data_recved = false;
                    wait_cnt = 0;
                }
                else
                    wait_cnt++;
                if(trans_data_on)
                {
                    if(wait_cnt%10 == 0) {
                        trans_data();
                        wait_cnt = 0;
                        trans_data_repeat++;
                        if(trans_data_repeat==10)
                            trans_data_on = false;
                    }
                }
            }
            else if(blue_send_recv!=null) {
                if (blue_send_recv.data_recved) {
                    byte[] msg = new byte[16384];
                    System.arraycopy(blue_send_recv.bytes_recv,0,msg,0,blue_send_recv.data_recved_len);        ;
                    parse_recv_pack(msg,msg.length);
                    blue_send_recv.data_recved = false;
                    wait_cnt = 0;
                }
                else
                    wait_cnt++;
                if(trans_data_on)
                {
                    if(wait_cnt%100 == 0) {
                        trans_data();
                        wait_cnt = 0;
                        trans_data_on = false;
                    }
                }
            }
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            // 使用postInvalidate可以直接在线程中更新界面
            postInvalidate();
        }

    }
}
