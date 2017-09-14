package com.tqli.anchorbolt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

/**
 * Created by Administrator on 2017/6/8.
 */
public class AnchorBoltView extends View
{
    private Paint				mPaint	= null;
    int width;
    int height;
    int grid;
    int samples;
    int sample_rate;
    double sample_interval;
//    int gain_in_db;

//    WifiTranceiver wifi_send_recv = null;
//    BluetoothDevice remoteBlueDevice = null;
//    BlueTranceiver blue_send_recv = null;
//    BluetoothAdapter mBluetoothAdapter;
//    Context parent_context;

    Global g = new Global();
    public byte samples_index = 1;
    public byte sample_rate_index = 1;
    public byte gain_index = 2;
//    public byte trigger_level_index = 3;

    int draw_offset_x,draw_offset_y,bak_offset_x,bak_offset_y;
    float point_x,point_y;

    boolean scale_set;
    float scale_x,scale_y,bak_scale_x,bak_scale_y;
//    float point_1_x,point_1_y,point_2_x,point_2_y;

//    public boolean  measure_on;
//    boolean trans_data_on;
//    boolean[] check = new boolean[4096];
    short delay = -100;
//    int trans_data_repeat;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

//    private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
//            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
//            | Canvas.CLIP_TO_LAYER_SAVE_FLAG;

    Seg2File    seg2_file = null;

    public AnchorBoltView(Context context, AttributeSet attrs)
    {
        super(context,attrs);
//        parent_context = context;
        draw_offset_x = 0;
        draw_offset_y = 0;
        bak_offset_x = 0;
        bak_offset_y = 0;
        scale_x = 0.5f;
        scale_y = 0.5f;
        bak_scale_x = 1;
        bak_scale_y = 1;
        scale_set = false;

//        measure_on = false;
//        trans_data_on = false;
        mPaint = new Paint();
//        seg2_file = new Seg2File();

//        gain_in_db = g.gain_array[gain_index];
        samples = g.samples_array[samples_index];
        sample_rate = g.sample_rate_array[sample_rate_index];
        sample_interval = 1.0/sample_rate;
//
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

//        new Thread(this).start();
    }

//    public void para_refresh()
//    {
//        gain_in_db = g.gain_array[gain_index];
//        samples = g.samples_array[samples_index];
//        sample_rate = g.sample_rate_array[sample_rate_index];
//        sample_interval = 1.0/sample_rate;
//    }

    public void onDraw(Canvas canvas)
    {
        int temp;
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        width = getWidth();
        height = getHeight();
        grid = width/10;

        canvas.scale(mScaleFactor, mScaleFactor);

        //canvas.scale(scale_x,scale_y);
        canvas.translate(draw_offset_x,draw_offset_y);
        DrawData(canvas);
        DrawGrid(canvas);
        mPaint.setColor(Color.DKGRAY);
        mPaint.setTextSize(30);
        canvas.translate(-draw_offset_x,-draw_offset_y);

       int n = samples/grid+1;
        for(int i=1;i<n;i+=2)
        {
            String str = null;
            str = String.format("%7.3f",i*sample_interval*1000*grid);
            Rect r = new Rect();
            mPaint.getTextBounds(str,0,str.length(),r);
            temp = 0;
            if(draw_offset_y>30)
                temp = draw_offset_y-30;
            canvas.drawText(str,i*grid-(r.right-r.left)/2+draw_offset_x,30+temp,mPaint);
        }

        n = height/grid;
        for(int i=-n/2;i<n/2+1;i++)
        {
            String str;
            str = String.format("%d",i);
            Rect r = new Rect();
            mPaint.getTextBounds(str,0,str.length(),r);
            temp = 0;
            if(draw_offset_x>r.right-r.left)
                temp = draw_offset_x-r.right+r.left;
            canvas.drawText(str,temp,height/2-i*grid+(r.bottom-r.top)/2+draw_offset_y,mPaint);
        }

        //canvas.scale(1/scale_x,1/scale_y);
//        canvas.saveLayerAlpha(0, 0, 2*width, 2*height, 0x88,LAYER_FLAGS);
//        mPaint.setColor(Color.WHITE);
//        canvas.drawCircle(width/2,height-75,50,mPaint);
//        if(measure_on)
//            mPaint.setColor(Color.RED);
//        else
//            mPaint.setColor(Color.DKGRAY);
//        canvas.drawCircle(width/2,height-75,45,mPaint);
//
//        mPaint.setColor(Color.WHITE);
//        canvas.drawCircle(width/2-150,height-75,50,mPaint);
//        canvas.drawCircle(width/2+150,height-75,50,mPaint);
//        canvas.drawCircle(width-75,height-75,50,mPaint);
//        canvas.drawCircle(75,height-75,50,mPaint);
//        mPaint.setColor(Color.GRAY);
//        canvas.drawCircle(width/2-150,height-75,45,mPaint);
//        canvas.drawCircle(width/2+150,height-75,45,mPaint);
//        canvas.drawCircle(width-75,height-75,45,mPaint);
//        canvas.drawCircle(75,height-75,45,mPaint);
//        mPaint.setColor(Color.BLUE);
//        mPaint.setTextSize(40);
//        String str = new String("<");
//        Rect r = new Rect();
//        mPaint.getTextBounds(str,0,str.length(),r);
//        canvas.drawText(str,width/2-150-(r.right-r.left)/2,height-75+(r.bottom-r.top)/2,mPaint);
//        str = new String(">");
//        canvas.drawText(str,width/2+150-(r.right-r.left)/2,height-75+(r.bottom-r.top)/2,mPaint);
//        str = str.format("%d",seg2_file.cur_trace);
//        mPaint.getTextBounds(str,0,str.length(),r);
//        canvas.drawText(str,width-75-(r.right-r.left)/2,height-75+(r.bottom-r.top)/2,mPaint);
//        str = new String("RT");
//        mPaint.getTextBounds(str,0,str.length(),r);
//        canvas.drawText(str,75-(r.right-r.left)/2,height-75+(r.bottom-r.top)/2,mPaint);
//        canvas.restore();
    }

    public void DrawGrid(Canvas canvas)
    {
        mPaint.setColor(Color.DKGRAY);
        canvas.drawLine(0,0,0,height-1,mPaint);
        canvas.drawLine(0,0,samples-1,0,mPaint);
        canvas.drawLine(samples-1,0,samples-1,height-1,mPaint);
        canvas.drawLine(0,height-1,samples-1,height-1,mPaint);

        int n = samples/grid+1;
        for(int i=1;i<n;i++)
        {
            canvas.drawLine(i*grid,0,i*grid,height-1,mPaint);
        }
        n = height/grid;
        for(int i=0;i<n/2+1;i++)
        {
            canvas.drawLine(0,height/2-i*grid,samples-1,height/2-i*grid,mPaint);
            canvas.drawLine(0,height/2+i*grid,samples-1,height/2+i*grid,mPaint);
        }
     }

    public void DrawData(Canvas canvas)
    {
        int i;
        mPaint.setColor(Color.RED);

        float x1,y1,x2,y2;
        if(seg2_file.trace_nums<=0)
            return;
        if(seg2_file.trace_nums<=seg2_file.cur_trace)
            seg2_file.cur_trace = 0;
        Seg2Data sg2d = seg2_file.DataArray.get(seg2_file.cur_trace);
        samples = sg2d.samples;
        sample_interval = sg2d.sample_interval;
        int[] pdata = sg2d.pdata;

        int max = pdata[0];
        for(i=0;i<samples;i++)
            if(max<pdata[i]) max = pdata[i];

        for(i=0;i<samples-1;i++)
        {
            x1 = i;
            x2 = i+1;
            y1 = height/2*(1-pdata[i]*1.0f/max);
            y2 = height/2*(1-pdata[i+1]*1.0f/max);
            canvas.drawLine(x1,y1,x2,y2,mPaint);
        }
     }

    // 触笔事件
   public boolean onTouchEvent(MotionEvent event)
    {
        mScaleDetector.onTouchEvent(event);

        float x,y;
//        int cnt = event.getPointerCount();
//        if(cnt==2)
//        {
//            set_scale(event);
//            return true;
//        }

        int act = event.getAction();
        if(act == ACTION_DOWN) {
            point_x = event.getX();
            point_y = event.getY();
        }
        else if(act==ACTION_MOVE)
        {
            x = event.getX();
            y = event.getY();
            draw_offset_x = bak_offset_x+(int) (x-point_x);
            if(draw_offset_x<-samples+100)
                draw_offset_x = -samples+100;
            else if(draw_offset_x>100)
                draw_offset_x = 100;
            draw_offset_y = bak_offset_y+(int) (y-point_y);
            if(draw_offset_y<-height+100)
                draw_offset_y = -height+100;
            else if(draw_offset_y>100)
                draw_offset_y = 100;
        }
        else if(act== ACTION_UP)
        {
            bak_offset_x = draw_offset_x;
            bak_offset_y = draw_offset_y;
//            x = event.getX();
//            y = event.getY();
//           if((x-width/2)*(x-width/2)+(y-height+75)*(y-height+75)<2500)
//            {
//                //measure_on = !measure_on;
//                if(!measure_on) {
//                    byte[] str = {(byte)0xA2,g.READY_SAMPLE,0x00,0x08,samples_index,sample_rate_index,gain_index,trigger_level_index,0x00,0x00,0x00};
//                    int t = g.calcCrc16(str,0,8,0xffff);
//                    str[8] = (byte)t;
//                    str[9] = (byte)(t>>8);
//                    send_cmd(str,10);
//                }
//                else
//                {
//                    byte[] str = {(byte)0xA2,g. POWER_OFF,0x00,0x08,samples_index,sample_rate_index,gain_index,trigger_level_index,0x00,0x00,0x00};
//                    int t = g.calcCrc16(str,0,8,0xffff);
//                    str[8] = (byte)t;
//                    str[9] = (byte)(t>>8);
//                    send_cmd(str,10);
//                }
//            }
//            if((x-width/2-150)*(x-width/2-150)+(y-height+75)*(y-height+75)<2500)
//            {
//                if(seg2_file.trace_nums-1>seg2_file.cur_trace)
//                    seg2_file.cur_trace++;
//            }
//            if((x-width/2+150)*(x-width/2+150)+(y-height+75)*(y-height+75)<2500)
//            {
//                if(seg2_file.cur_trace>0)
//                    seg2_file.cur_trace--;
//            }
//            if((x-width+75)*(x-width+75)+(y-height+75)*(y-height+75)<2500)
//            {
//
//                seg2_file.add_seg2_data(samples,sample_interval,gain_in_db);
//                 seg2_file.cur_trace = seg2_file.DataArray.size()-1;
//            }
//            if((x-75)*(x-75)+(y-height+75)*(y-height+75)<2500)
//            {
//                if(seg2_file.trace_nums==0) {
//                    seg2_file.add_seg2_data(samples, sample_interval, 0);
//                    for (int i = 0; i < seg2_file.max_blocks; i++)
//                        check[i] = false;
//                }
//                trans_data_on = true;
//                trans_data_repeat = 0;
//            }
        }
        return true;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }

   /*  public void set_scale(MotionEvent event)
    {
        float  x1,y1,x2,y2;
        int act = event.getActionMasked();
        if(act==ACTION_POINTER_DOWN)
        {
            point_1_x = event.getX(0);
            point_1_y = event.getY(0);
            point_2_x = event.getX(1);
            point_2_y = event.getY(1);
            scale_set = true;
        }
        else if(act==ACTION_MOVE && scale_set)
        {
            x1 = event.getX(0);
            y1 = event.getY(0);
            x2 = event.getX(1);
            y2 = event.getY(1);
            scale_x = bak_scale_x*(x1-x2)/(point_1_x-point_2_x);
            scale_y = bak_scale_y*(y1-y2)/(point_1_y-point_2_y);
            if(scale_x<0.1)
                scale_x = 0.1f;
            else if(scale_x>10)
                scale_x = 10;
            if(scale_y<0.1)
                scale_y = 0.1f;
            else if(scale_y>10)
                scale_y = 10;
        }
        else if(act== ACTION_POINTER_UP )
        {
            bak_scale_x = scale_x;
            bak_scale_y = scale_y;
            scale_set = false;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        return false;
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event)
    {
        return true;
    }

   public void socket_connect()
    {
        if(g.wifi_or_blue) {
            wifi_send_recv = new WifiTranceiver();
            if(wifi_send_recv.connect_ok)
                wifi_send_recv.start();
            else
                wifi_send_recv = null;
        }
        else
        {
            if(remoteBlueDevice==null)
            {
                remoteBlueDevice = mBluetoothAdapter.getRemoteDevice("78:A5:04:6A:34:DD");
                if(remoteBlueDevice==null)
                {
                    blue_connect_error();
                    return;
                }
            }
            blue_send_recv = null;
            blue_send_recv = new BlueTranceiver(remoteBlueDevice,parent_context);
            if(blue_send_recv.connect_ok)
                blue_send_recv.start();
            else
                blue_send_recv = null;
        }
    }

    public void socket_disconnect()
    {
        if(g.wifi_or_blue) {
            wifi_send_recv = null;
        }
        else
        {
            if(blue_send_recv!=null) {
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

    public void blue_connect_error()
    {

        Dialog dialog = new AlertDialog.Builder(parent_context)
                .setTitle("蓝牙连接错误！")
               .setPositiveButton("确定", null)
               .show();
    }

    public void set_para(Context context)
    {
        ParaSetup dialog = new ParaSetup(context,"参数设置",new ParaSetup.OnCustomDialogListener() {

            public void back(byte len,byte rate,byte gain, byte trig) {
                samples_index = len;
                sample_rate_index = rate;
                gain_index = gain;
                trigger_level_index = trig;
                gain_in_db = g.gain_array[gain_index];
                samples = g.samples_array[samples_index];
                sample_rate = g.sample_rate_array[sample_rate_index];
                sample_interval = 1.0/sample_rate;
            }
        });
        dialog.setParaInit(samples_index,sample_rate_index,gain_index,trigger_level_index);
        dialog.show();

    }

    public void send_cmd(byte[] str,int len)
    {
        if(g.wifi_or_blue) {
            if (wifi_send_recv == null) {
                socket_connect();
            }
            if (wifi_send_recv != null) {
                wifi_send_recv.set_pack(str, len);
            }
        }
        else
        {
            if (blue_send_recv == null) {
                socket_connect();
            }
            if (blue_send_recv != null) {
                blue_send_recv.set_pack(str, len);
            }
        }
    }

    public void parse_recv_pack(byte[] msg,int len)
    {
        int n = 0;
        if(g.wifi_or_blue) {
            if(wifi_send_recv!=null)
                n = wifi_send_recv.data_recved_len;
        }
        else {
            if(blue_send_recv!=null)
                n = blue_send_recv.data_recved_len;
        }
        byte[] temp = new byte[64];
        for(int j=0;j<n;j++){
            if( (msg[j]==(byte)0x2A)  && (g.isCrc16Good(msg, j, msg[j+3] + 2, 0xffff))) {
                int l = (int)msg[j+3];
                for(int i=0;i<l;i++)
                    temp[i] = msg[j++];
                parse_msg(temp, temp[3]);
                j--;
            }
        }
    }

    public void parse_msg(byte[] msg,byte len)
    {
        switch(msg[1])
        {
            case (byte)0xD8:  //GET_STATE_ACK
                if((msg[4] & 0x02) == 0x02)
                    measure_on = true;
                else
                    measure_on = false;
                if((msg[4] & 0x04)==0x04) {
                    if(seg2_file.trace_nums==0)
                    {
                        seg2_file.add_seg2_data(samples,sample_interval,0);
                    }
                    for(int i=0;i<seg2_file.max_blocks;i++)
                        check[i] = false;
                    trans_data_on = true;
                    trans_data_repeat = 0;
                }
                break;
            case (byte)0xD1://WORK_PARA_SET_ACK:

                break;
            case (byte)0xD7://DATA_TRANS_ACK:
                int data_len = (msg[3]-6)/2;
                byte[] temp = new byte[2];
                temp[0] = msg[4];
                temp[1] = msg[5];
                int index = (int)(g.bytes_2_short(temp));
                int j,i;
                if(msg[2]==0x00)
                {
                    check[index] = true;
                    int n = seg2_file.DataArray.size();
                    seg2_file.trace_nums = (short)n;
                    Seg2Data data = seg2_file.DataArray.get(n-1);
                    for(j=index*24,i=6;i<len;j++)
                    {
                        if(j>=samples) break;
                        temp[0] = msg[i++];
                        temp[1] = msg[i++];
                        data.pdata[j] = (int)(g.bytes_2_short(temp));
                    }
                }
                break;
            case (byte)0xD9://DATA_TRANS_END:
                trans_data_on = true;
                trans_data_repeat = 0;
                refreshDrawableState();
                break;
         }
    }

    public void trans_data()
    {
        int index = 0;
        byte[] str = new byte[68];
        str[0] = (byte)0xA2;
        str[1] = g.LONG_BLOCK_DATA_TRANS_LIST;
        str[2] = 0x00;

        for(int i=0;i<1;i++)
        {
            boolean received = false;
            boolean first = true;
            short[] error_index = new short[4096];
            short[] error_len = new short[4096];
            error_len[index] = 0;
            for(int j=0;j<seg2_file.max_blocks;j++)
            {
                if(check[j]==false)
                {
                    if(first)
                    {
                        error_index[index++] = (short)j;
                        error_len[index] = 0;
                        first = false;
                    }
                    error_len[index-1]++;
                }
                else
                    first = true;
            }
            if(index==0) {
                Seg2Data data = seg2_file.DataArray.get(seg2_file.trace_nums-1);
                data.data_transed = true;
                trans_data_on = false;
                return;
            }

            int  errors = index>12? 12 : index;
            byte len = (byte)(4*errors+6);
            str[3] = len;
            str[4] = (byte)(delay & 0x00ff);
            str[5] = (byte)((delay>>8) & 0x00ff );

            int k = 6;
            for(int j=0;j<errors;j++)
            {
                str[k++] = (byte)(error_index[j] & 0x00ff);
                str[k++] = (byte)((error_index[j]>>8) & 0x00ff);
                str[k++] = (byte)(error_len[j] & 0x00ff);
                str[k++] = (byte)((error_len[j]>>8) & 0x00ff);
            }
            int t = g.calcCrc16(str,0,len,0xffff);
            str[len] = (byte)t;
            str[len+1] = (byte)(t>>8);
            send_cmd(str,len+2);
         }
    }*/

//    public void run()
//    {
//        int wait_cnt = 0;
//        while (!Thread.currentThread().isInterrupted())
//        {
//            if(wifi_send_recv!=null) {
//                if (wifi_send_recv.data_recved) {
//                    byte[] msg = wifi_send_recv.recv_pack.getData();
//                    parse_recv_pack(msg,msg.length);
//                    wifi_send_recv.data_recved = false;
//                    wait_cnt = 0;
//                }
//                else
//                    wait_cnt++;
//                if(trans_data_on)
//                {
//                    if(wait_cnt%10 == 0) {
//                        trans_data();
//                        wait_cnt = 0;
//                        trans_data_repeat++;
//                        if(trans_data_repeat==10)
//                            trans_data_on = false;
//                    }
//                }
//            }
//            else if(blue_send_recv!=null) {
//                if (blue_send_recv.data_recved) {
//                    byte[] msg = new byte[16384];
//                    System.arraycopy(blue_send_recv.bytes_recv,0,msg,0,blue_send_recv.data_recved_len);        ;
//                    parse_recv_pack(msg,msg.length);
//                    blue_send_recv.data_recved = false;
//                    wait_cnt = 0;
//                }
//                else
//                    wait_cnt++;
//                if(trans_data_on)
//                {
//                    if(wait_cnt%100 == 0) {
//                        trans_data();
//                        wait_cnt = 0;
//                        trans_data_on = false;
//                    }
//                }
//            }
//            try
//            {
//                Thread.sleep(1);
//            }
//            catch (InterruptedException e)
//            {
//                Thread.currentThread().interrupt();
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//            // 使用postInvalidate可以直接在线程中更新界面
//            postInvalidate();
//        }
//
//    }
}