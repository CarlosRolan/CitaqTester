package com.citaq.citaqfactory;

import android.os.Bundle;


import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.citaq.R;
import com.citaq.util.Command;
import com.citaq.util.LEDControl;
import com.citaq.util.MainBoardUtil;
import com.printer.util.DataQueue;
import com.printer.util.USBConnectUtil;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class PrintActivity extends SerialPortActivity {
    private static final String TAG  ="PrintActivity";

    Context mContext;

    private ToggleButton bt_red;
    private ToggleButton bt_blue;
    private ToggleButton bt_fresh;

    private LEDControl freshThread = null;

    private Button btn_Opencash;
    private Button btn_cutPaper;
    private Button btn_GetprintStatus;
    private Button btn_Printtest;
    private Button btn_Printdemo;
    private Button btn_OpenPicture;
    private Button btn_PrintPicture;

    private Button btn_EnableBuzzer;

    private Button btn_DisableBuzzer;

    private EditText et_cmd;
    private Button btn_cmd;

    private Bitmap mBitmap = null;
    static private int openfileDialogId = 0;

    int mCurrentBt = -1;


    TextView tv_Reception;
    ImageView imageForPrint;

    Spinner spinnerCP,spinnerCS,spinnerResidentCS;
    private ArrayAdapter<?> cpAdapter,csAdapter,residentCsAdapter;
    private int cpIndex,csIndex,residentCsIndex;

    ////////////////
    USBConnectUtil mUSBConnectUtil = null;

    private ArrayAdapter<?> adapter_type, adapter_cmd;

    int printType = 0;

    SendThread mSendThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        mContext = this;

        String cpu = MainBoardUtil.getCpuHardware();

        Log.println(Log.INFO, "CARLOS", cpu);

        String model = MainBoardUtil.getModel();

        Log.println(Log.INFO, "CARLOS", model);

        setContentView(R.layout.activity_print);

        initSerial();

        initView();
        mSendThread = new SendThread();
        mSendThread.start();
    }

    private void initSerial(){
        try {
//			mSerialPort = mApplication.getSerialPort();
            mSerialPort = mApplication.getPrintSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
//			mInputStream = mSerialPort.getInputStream();
//
//			/* Create a receiving thread */
//			mReadThread = new ReadThread();
//			mReadThread.start();
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }

    }

    private void initInputStream(){
        mInputStream = mSerialPort.getInputStream();

        /* Create a receiving thread */

        if(mReadThread == null){
            mReadThread = new ReadThread();
            mReadThread.start();
        }

    }

    private void initView(){

        bt_red = (ToggleButton) findViewById(R.id.tb_red);
        bt_blue = (ToggleButton) findViewById(R.id.tb_blue);
        bt_fresh = (ToggleButton) findViewById(R.id.tb_fresh);


        bt_red.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if(arg1){
                    LEDControl.trunOnRedRight(true);
                }else{
                    LEDControl.trunOnRedRight(false);
                }
            }
        });

        bt_blue.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if(arg1){
                    LEDControl.trunOnBlueRight(true);
                }else{
                    LEDControl.trunOnBlueRight(false);
                }

            }
        });

        bt_fresh.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if(arg1){
                    if(freshThread == null)
                    {
                        freshThread = new LEDControl();
                        freshThread.StartFresh();
                    }
                }else{
                    if(freshThread != null)
                    {
                        freshThread.StopFresh();
                        freshThread = null;
                    }
                }
            }
        });


        btn_Opencash = (Button) findViewById(R.id.btn_opencash);
        btn_Opencash.setOnClickListener(SendPrintListener);

        btn_cutPaper = ((Button) findViewById(R.id.btn_cutPaper));
        btn_cutPaper.setOnClickListener(SendPrintListener);

        btn_GetprintStatus = (Button) findViewById(R.id.btn_getprintstatus);
        btn_GetprintStatus.setOnClickListener(SendPrintListener);

        btn_Printtest = (Button) findViewById(R.id.btn_printtest);
        btn_Printtest.setOnClickListener(SendPrintListener);

        btn_Printdemo = (Button) findViewById(R.id.btn_printdemo);
        btn_Printdemo.setOnClickListener(SendPrintListener);

        btn_OpenPicture = (Button) findViewById(R.id.btn_openPicture);
        btn_OpenPicture.setOnClickListener(SendPrintListener);

        btn_PrintPicture = (Button) findViewById(R.id.btn_printPicture);
        btn_PrintPicture.setOnClickListener(SendPrintListener);

        btn_EnableBuzzer = (Button) findViewById(R.id.btn_enableBuzzer);
        btn_EnableBuzzer.setOnClickListener(SendPrintListener);

        btn_DisableBuzzer = (Button) findViewById(R.id.btn_disableBuzzer);
        btn_DisableBuzzer.setOnClickListener(SendPrintListener);

        et_cmd = (EditText) findViewById(R.id.et_cmd);
        btn_cmd = (Button) findViewById(R.id.btn_cmd);
        btn_cmd.setOnClickListener(SendPrintListener);

        tv_Reception = (TextView) findViewById(R.id.tv_printReception);
        imageForPrint = (ImageView) findViewById(R.id.imageForPrint);

        adapter_type= ArrayAdapter.createFromResource(this, R.array.PD_type, android.R.layout.simple_spinner_item);
        adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banma);
        imageForPrint.setImageBitmap(mBitmap);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        et_cmd.clearFocus();

        Log.v(TAG, "onResume");
    }

    OnClickListener SendPrintListener = new OnClickListener()
    {

        public void onClick(View v)
        {
            Log.println(Log.INFO, "CARLOS", "onclick");
            mCurrentBt = v.getId();

            Log.println(Log.INFO, "CARLOS", String.valueOf(mCurrentBt));
            switch(mCurrentBt){

                case 2131296782://R.id.btn_opencash:
                    printerWrite(Command.openCash);
                    break;
                case 2131296356://R.id.btn_cutPaper:
                    Log.println(Log.INFO,"CARLOS", "CUT PAPPER");
                    printerWrite(Command.cutPaper);
                    break;
                case 2131296361://R.id.btn_getprintstatus:
                    Log.println(Log.INFO, "CARLOS", "getprintstatus");
                    initInputStream();
                    printerWrite(Command.printStatus);
                    break;
                case 2131296365://R.id.btn_printtest:
                    printerWrite(Command.printTest);
                    break;
                case 2131296364://R.id.btn_printdemo:
                    if(getLanguageEnv()){
                        printerWrite(Command.getPrintDemoZH());
                    }else{
                        printerWrite(Command.getPrintDemo());
                    }
                    break;

                case 2131296362://R.id.btn_openPicture:
                    mBitmap = null;

                    //showDialog();
                    break;
                case 2131296363://R.id.btn_printPicture:
                    if(mBitmap != null)
                    {

					/*Thread thread=new Thread(new Runnable()
			        {
			            @Override
			            public void run()
			            {
			            	printerWrite(Command.getPrintPictureCmd(mBitmap));
			            }
			        });
			        thread.start();*/
                        printerWrite(Command.getPrintPictureCmd(mBitmap));

                    }
                    break;

                //////////
                case 1000001://R.id.btn_setCodepage:
                    printerWrite(Command.getCodepage(cpIndex));//扩展字符集 //参数！！！！！！修改  Spinner
                    break;

                case 1000023://R.id.btn_setCharacterSet:
                    printerWrite(Command.getCharacterSet(csIndex));//国际字符集 //参数！！！！！！修改  Spinner
                    break;
                case 1000026://R.id.btn_setResidentCharacterSet:
                    printerWrite(Command.getResidentCharacterSet(residentCsIndex));
                    break;
                case 1000020://R.id.btn_enableChinese:
                    printerWrite(Command.getChineseMode(1));
                    break;
                case 1000021://R.id.btn_disableChinese:
                    printerWrite(Command.getChineseMode(0));
                    break;
                case 1000009://R.id.btn_enableBuzzer:
                    printerWrite(Command.getBuzzer(1));
                    break;
                case 1000005://R.id.btn_disableBuzzer:
                    printerWrite(Command.getBuzzer(0));
                    break;
                case 1000015://R.id.btn_cmd:
                    String cmd = et_cmd.getText().toString();
                    if(cmd.trim().length()>0){
//					cmd= cmd +"\n";
                        byte[] data=Command.transToPrintText(cmd);
                        printerWrite(data);
                    }
                    break;
                case 1000017://R.id.bt_more:
                    Intent intent = new Intent(mContext, PrintMoreActivity.class);
                    intent.putExtra("Print_type",printType);

                    mContext.startActivity(intent);
                    break;
            }
        }

    };
    private boolean printerWrite(byte[] cmd){
        boolean returnValue = true;

        boolean neddSubpackage;
        if(printType == 0){   //serial
            neddSubpackage = false;
        }else{   //usb
            neddSubpackage = true;
        }


        //neddSubpackage if set false ,len is not used
        mSendThread.addData(cmd,neddSubpackage,1024);

		/*byte[] printText = new byte[1];
		printText[0] = 0x0a;
		mSendThread.addData(printText,true,1024);*/

        return returnValue;
    }

    private  boolean serialWrite(byte[] cmd){
        boolean returnValue=true;
        try{

            mOutputStream.write(cmd);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            returnValue=false;

            // more 后返回 java.io.IOException: write failed: EBADF (Bad file number)

            initSerial();

            try{

                mOutputStream.write(cmd);
            }
            catch(Exception e)
            {
                ex.printStackTrace();
                returnValue=false;
            }

        }
        return returnValue;
    }

    private boolean getLanguageEnv() {
        Locale l = Locale.getDefault();
        String language = l.getLanguage();
        if ("zh".equals(language)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        if(mCurrentBt == R.id.btn_getprintstatus){
            runOnUiThread(new Runnable() {
                public void run() {
					/*for(int i = 0; i < size; i++){
							String s = Integer.toHexString((int)buffer[i]);//String.valueOf(((char)buffer[i]));
							tv_Reception.append(s + ' ');
					}*/
                    if(size > 0){
                        String debstr;
                        debstr = "Rec " + size + " bytes(Serial):   ";
                        for (int i = 0; i < size; i++) {
                            String s;
                            if(buffer[i] < 0){
                                s = Integer.toHexString(256 + buffer[i]);//String.valueOf(((char)buffer[i]));
                            }
                            else {
                                s = Integer.toHexString(buffer[i]);//String.valueOf(((char)buffer[i]));
                            }

                            if(s.length() < 2){
                                s = "0x0" + s + ',';
                            }else{
                                s = "0x" + s + ',';
                            }
                            debstr += s;
                        }
                        debstr += "\r\n";
                        System.out.println(debstr);
                        tv_Reception.append(debstr);
                    }
                }

            });
        }

    }


    public class SendThread extends Thread {
        DataQueue list = new DataQueue();
        boolean isrun = true;

        public void addData(byte[] cmd, boolean neddSubpackage, int len){
            if(len > 0 ){
                list.enQueue(cmd, neddSubpackage,len);
            }

        }

        private void stopRun() {
            isrun = false;
        }

        public void run(){
            while(true && isrun){
                if(list.QueueLength()>0){
                    //打印
                    byte[] data = list.deQueue();
                    if( data != null){
                        if(printType == 0) {   //serial
                            serialWrite(data);
                        }
                    }

	    			/*try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
                }
            }

        }
    }

    Dialog dialog = null;
    /*protected void showDialog() {
        if(OpenFileDialog.isDialogCreate &&
                OpenFileDialog.FileSelectView.getCurrentPath().equals(OpenFileDialog.sRoot)){

            dismissDialog();

        }

        if(dialog==null){
            Map<String, Integer> images = new HashMap<String, Integer>();
            // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
            images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// 根目录图标
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//返回上一层的图标
            images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//文件夹图标
            images.put("bmp", R.drawable.filedialog_bmpfile);	//bmp文件图标
            images.put("png", R.drawable.filedialog_pngfile);	//png文件图标
            images.put("jpeg", R.drawable.filedialog_jpegfile);	//jpeg文件图标
            images.put("jpg", R.drawable.filedialog_jpgfile);	//jpg文件图标
            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
            dialog = OpenFileDialog.createDialog(this, "打开文件", new CallbackBundle() {
                        @Override
                        public void callback(Bundle bundle) {
                            String Picturefilepath = bundle.getString("path");
                            mBitmap = BitmapFactory.decodeFile(Picturefilepath);//打开图片文件
                            //显示要处理的图片
                            imageForPrint.setImageBitmap(mBitmap);
                            //setTitle(filepath); // 把文件路径显示在标题上
                            dialog.dismiss();
                        }
                    },
                    ".bmp;.png;.jpg;.jpeg;",
                    images);
            dialog.show();
        }else{
            if(!dialog.isShowing())
                dialog.show();
        }
    }
*/
    protected void dismissDialog() {
        if(dialog !=null && dialog.isShowing()){
            dialog.dismiss();


        }
        dialog = null;

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.v(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        LEDControl.trunOnRedRight(false);
        LEDControl.trunOnBlueRight(false);
        if(freshThread != null)
        {
            freshThread.StopFresh();
            freshThread = null;
        }

        mSendThread.stopRun();
        mSendThread = null;

        Log.v(TAG, "onDestroy");
    }


}
