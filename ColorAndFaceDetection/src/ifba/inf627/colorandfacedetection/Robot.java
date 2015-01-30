package ifba.inf627.colorandfacedetection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class Robot /*extends Thread*/{

	public static final int 			REQUEST_ENABLE_BT	= 1;
	private static final UUID			MY_UUID				= UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	//private static UUID			MY_UUID				;
	private static Robot 				mInstance;
	
	private static RobotActivity 		mRobotActivity;
	
	private static BluetoothAdapter 	mBtAdapter;
	private static BluetoothSocket		mBtSocket;
	private static OutputStream			mOutStream;
	private static InputStream			mInStream;
	//private static String				mDeviceAddress		= "B0:34:95:41:A6:78"; //Mac Address of Device
	private static String				mDeviceAddress		= "00:14:01:03:54:81"; //Mac Address of Device
	private ProgressDialog mBluetoothConnectProgressDialog;

	private boolean 					mWalking;
	
	private boolean stopWorker;
	
	private Robot(RobotActivity robotActivity) {
		mRobotActivity = robotActivity;
		mWalking = false;
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter == null) {
			mRobotActivity.runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					Toast.makeText(
						mRobotActivity
						, "Dispositivo n�o possui adaptador Bluetooth!"
						, Toast.LENGTH_LONG
					).show();
				}
			});
		}
		
		if (!mBtAdapter.isEnabled()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mRobotActivity.startActivityForResult(intent, REQUEST_ENABLE_BT);
		}
		
		
		
		Set<BluetoothDevice> pairedDevices  = mBtAdapter.getBondedDevices();
		BluetoothDevice device = null;
		for(BluetoothDevice d:pairedDevices){
			if(d.getName().equals("VINICIUS")){
				device = d;
			}
		}	
		
		try {
			
			mBtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);			
			mBtAdapter.cancelDiscovery();
			mBtSocket.connect();   
			mOutStream = mBtSocket.getOutputStream();
			mInStream = mBtSocket.getInputStream();		
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		stopWorker = false;
		
	}
	
	public static synchronized Robot getInstance(final RobotActivity robotActivity) {
		if (mInstance == null) {
			mInstance = new Robot(robotActivity);
		}
		return mInstance;
	}
	
	public void stopRobo() {
		Log.i(Robot.class.toString(), "Parando Rob�!");
		
		if (mBtSocket != null) {
			sendCommand("S");			
		}
		
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		mWalking = false;
		mRobotActivity.robotStopped();
	}
	
	public void walk() {
		Log.i(Robot.class.toString(), "Ordenando que o rob� ande!");

		if (mBtSocket != null) {
			sendCommand("W");
		}
		
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		mWalking = true;
		mRobotActivity.robotWalking();
	}

	public boolean isWalking() {
		return mWalking;
	}
	
	private boolean sendCommand (String command) {
		boolean retorno = false;
		try {			
			command = "\n"+command;
			byte[] buffer = command.getBytes();
			mOutStream.write(buffer);
			mOutStream.flush();	
			byte[] inputData = new byte[3];
			int result = mInStream.read(inputData, 0, mInStream.available());
			String sretorno = new String(inputData);
			System.out.println("Retorno da forma certa:"+sretorno.trim());			
			retorno = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retorno;
	}
	
	private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }	
	
	public void sendInitialCommand(){		
		try {			
			String command = "\nW";
			byte[] buffer = command.getBytes();
			mOutStream.write(buffer);
			mOutStream.flush();	
			byte[] inputData = new byte[3];
			int result = mInStream.read(inputData, 0, mInStream.available());
			String sretorno = new String(inputData);
			System.out.println("Retorno da Inicializacao:"+sretorno.trim());	
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	Handler mHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
	    	byte[] writeBuf = (byte[]) msg.obj;
	    	int begin = (int)msg.arg1;
	    	int end = (int)msg.arg2;
	    	switch(msg.what) {
	    		case 1 :
		    		String writeMessage = new String(writeBuf);
		    		writeMessage = writeMessage.substring(begin, end);
		    	break;
	    	}
    	}
    };
	
	
	
}
