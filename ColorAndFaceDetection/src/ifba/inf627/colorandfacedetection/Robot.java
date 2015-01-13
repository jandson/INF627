package ifba.inf627.colorandfacedetection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Robot{

	public static final int 			REQUEST_ENABLE_BT	= 1;
	private static final UUID			MY_UUID				= UUID.fromString("00001101-0000-1000-8000-00805F9B34F8");
	
	private static Robot 				mInstance;
	
	private static RobotActivity 		mRobotActivity;
	
	private static BluetoothAdapter 	mBtAdapter;
	private static BluetoothSocket		mBtSocket;
	private static OutputStream			mOutStream;
	private static InputStream			mInStream;
	private static String				mDeviceAddress		= "B0:34:95:41:A6:78"; //Mac Address of Device

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
		
		BluetoothDevice device = mBtAdapter.getRemoteDevice(mDeviceAddress);
		try {
			mBtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			mBtSocket.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized Robot getInstance(final RobotActivity robotActivity) {
		if (mInstance == null) {
			mInstance = new Robot(robotActivity);
		}
		return mInstance;
	}
	
	public void stop() {
		Log.i(Robot.class.toString(), "Parando Robô!");
		
		if (mBtSocket != null) {
			sendCommand("STOP");
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
		Log.i(Robot.class.toString(), "Ordenando que o robô ande!");

		if (mBtSocket != null) {
			sendCommand("WALK");
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
	
	private void sendCommand (String command) {
		try {
			mOutStream = mBtSocket.getOutputStream();
			byte[] buffer = command.getBytes();
			mOutStream.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
