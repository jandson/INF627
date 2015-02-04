package ifba.inf627.colorandfacedetection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class Robot{

	public static final int 			REQUEST_ENABLE_BT	= 1;
	private static final UUID			MY_UUID				= UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	private static Robot 				mInstance;
	
	private static RobotActivity 		mRobotActivity;
	
	private static BluetoothAdapter 	mBtAdapter;
	private static BluetoothSocket		mBtSocket;
	private static OutputStream			mOutStream;
	private static InputStream			mInStream;
	private boolean 					mWalking;
	
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
						, "Dispositivo não possui adaptador Bluetooth!"
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
			if(d.getName().equals("RoboCarro")){
				device = d;
			}
		}	
		
		if (device != null) {
			try {
				mBtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);			
				mBtAdapter.cancelDiscovery();
				if (mBtSocket != null) {
					mBtSocket.connect();   
					mOutStream = mBtSocket.getOutputStream();
					mInStream = mBtSocket.getInputStream();
				} else {
					Toast toast = Toast.makeText(
							mRobotActivity.getApplicationContext()
							, R.string.failure_bluetooth_connection
							, Toast.LENGTH_LONG);
					toast.show();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static synchronized Robot getInstance(final RobotActivity robotActivity) {
		if (mInstance == null) {
			mInstance = new Robot(robotActivity);
		}
		return mInstance;
	}
	
	public void stopRobo() {
		Log.i(Robot.class.toString(), "Parando Robô!");		
		try {
			if (mBtSocket != null) {				
//				if(!sendCommand("S")){
//					Thread.sleep(100);
//					if(!sendCommand("S")){
//						Thread.sleep(100);
//						if(!sendCommand("S")){
//							Log.i(Robot.class.toString(), "Robo nao consegue parar!");
//							return;
//						}
//					}
//				}
				if (sendCommand("S")) {
					Thread.sleep(3000);
				}
			}else{
				Log.i(Robot.class.toString(), "Robo nao consegue parar!");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		mWalking = false;
		mRobotActivity.robotStopped();
	}
	
	public void walk() {
		Log.i(Robot.class.toString(), "Ordenando que o robô ande!");		
		try {
			if (mBtSocket != null) {
//				if(!sendCommand("W")){
//					Thread.sleep(100);
//					if(!sendCommand("W")){
//						Thread.sleep(100);
//						if(!sendCommand("W")){
//							Log.i(Robot.class.toString(), "Robo nao consegue andar!");
//							return;
//						}
//					}
//				}
				
				if (sendCommand("W")) {
					Thread.sleep(3000);
				}
			}else{
				Log.i(Robot.class.toString(), "Robo nao consegue andar!");
			}
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
			byte[] buffer = command.getBytes();
			mOutStream.write(buffer);
			mOutStream.flush();	
			byte[] inputData = new byte[6];
			mInStream.read(inputData, 0, mInStream.available());
			String sretorno = new String(inputData).trim();
			if(sretorno.contains("1")){			
				retorno = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retorno;
	}
	
	public void sendInitialCommand(){
		if ((mOutStream != null) && (mInStream != null)) { 
			try {			
				String command = "\nS";
				byte[] buffer = command.getBytes();
				mOutStream.write(buffer);
				mOutStream.flush();
				
				mOutStream.write(buffer);
				mOutStream.flush();				
					
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	}
	
	@SuppressLint("HandlerLeak") 
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
