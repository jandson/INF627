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
import android.util.Log;
import android.widget.Toast;

public class Robot{

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
	private ConnectedThread connectedThread;
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
			if(d.getName().equals("VINICIUS")){
				device = d;
			}
		}
		
		//BluetoothDevice device = mBtAdapter.getRemoteDevice(mDeviceAddress);
		try {
			//MY_UUID = device.getUuids()[0].getUuid();
			mBtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);			
			mBtAdapter.cancelDiscovery();
			mBtSocket.connect();   
			mOutStream = mBtSocket.getOutputStream();
			mInStream = mBtSocket.getInputStream();
			connectedThread = new ConnectedThread();
			
			connectedThread.start();
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
	
	public void stop() {
		Log.i(Robot.class.toString(), "Parando Robô!");
		
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
		Log.i(Robot.class.toString(), "Ordenando que o robô ande!");

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
		try {		
			if(mBtSocket.isConnected()){
				byte[] buffer = command.getBytes();
				mOutStream.write(buffer);
				mOutStream.flush();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
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
	
	private class ConnectedThread extends Thread {    

	    public void run() {
	        byte[] buffer = null;  // buffer store for the stream
	        int bytes; // bytes returned from read()
	        int bytesAvailable;
	        // Keep listening to the InputStream until an exception occurs
	        while (!Thread.currentThread().isInterrupted() && !stopWorker) {
	            try {
	                // Read from the InputStream	            	
	            	bytesAvailable = mInStream.available();
	            	if(bytesAvailable >0){
	            		buffer = new byte[bytesAvailable];
	            	
		                bytes = mInStream.read(buffer);
		                // Send the obtained bytes to the UI activity
		               /* mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
		                        .sendToTarget();*/
		                String retorno = new String(buffer);
		                System.out.println("Retorno:"+retorno);
		                
	            	}
	            	
	            } catch (IOException e) {
	            	stopWorker = true;
	                break;
	            }
	        }
	    }	   
	}
	
}
