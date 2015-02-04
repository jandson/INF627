package ifba.inf627.colorandfacedetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

public class RobotActivity extends Activity implements CvCameraViewListener2, OnTouchListener, RobotListener {

	private static final String 	TAG 				= RobotActivity.class.toString();
	private static final Scalar 	FACE_RECT_COLOR		= new Scalar(0, 255, 0, 255);

	private Mat                    	mRgba;
	private Mat                    	mGray;
	private File					mCascadeFile;
	private CascadeClassifier		mFaceDetector;	
	private float					mRelativeFaceSize   = 0.25f;
	private int						mAbsoluteFaceSize   = 0;

	private ColorBlobDetector    	mColorDetector;
	private Scalar               	CONTOUR_COLOR;
	private Scalar               	mBlobColorHsv;

	private CameraBridgeViewBase	mOpenCvCameraView;

	private Rect[]					mFaces;
	private boolean 				mFaceDetected;

	private List<MatOfPoint>		mColorContours;
	private boolean					mColorDetected;

	private Robot					mRobot;
	
	private boolean              	mIsColorSelected = false;
	private Scalar               	mBlobColorRgba;
	private Mat                  	mSpectrum;
    private Size                 	SPECTRUM_SIZE;
    
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");

				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mFaceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
					if (mFaceDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mFaceDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(RobotActivity.this);
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	public RobotActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_color_and_face_detection);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_color_and_face_detection);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mColorDetector = new ColorBlobDetector();
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);

		mRobot = Robot.getInstance(this);
		mRobot.sendInitialCommand();
		mFaceDetected = false;
		mColorDetected = false;
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if (mIsColorSelected) {
			mColorDetector.process(mRgba);
            List<MatOfPoint> contours = mColorDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }
		
		if (!mFaceDetected || !mColorDetected) {
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();
		}

		if (hasFaces() || hasColor()) {
			//mRobot.stop();
			mRobot.stopRobo();
		}
		
		return mRgba;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		
		case Robot.REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "Bluetooth ativado com SUCESSO!", Toast.LENGTH_LONG).show();
					}
				});
			} else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "FALHA ao ativar Bluetooth!", Toast.LENGTH_LONG).show();
					}
				});
			}
			break;

		default:
			break;
		}
	}
	
	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
	
	public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mColorDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mColorDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

	private boolean hasFaces() {
		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
		}

		MatOfRect faces = new MatOfRect();
		if (mFaceDetector != null)
			mFaceDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

		mFaces = faces.toArray();	
		mFaceDetected = mFaces.length > 0;

		if (mFaceDetected)
			Log.i(RobotActivity.class.toString(), "Face Detectada!");

		return mFaceDetected;
	}

	private boolean hasColor() {
		mColorDetector.process(mRgba);
		mColorContours = mColorDetector.getContours();
		mColorDetected = mColorContours.size() > 0;

		if (mColorDetected)
			Log.i(RobotActivity.class.toString(), "Cor Detectada!");

		return mColorDetected;
	}

	private void markFaces() {
		for (int i = 0; i < mFaces.length; i++)
			Core.rectangle(mRgba, mFaces[i].tl(), mFaces[i].br(), FACE_RECT_COLOR, 3);
	}
	
	private void markColor() {
		Imgproc.drawContours(mRgba, mColorContours, -1, CONTOUR_COLOR);
	}

	private void saveFaces() {
		markFaces();
		Log.i(RobotActivity.class.toString(), "Salvando Imagem de Rosto!");
		
		File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IFBA/INF627/Faces/");
		path.mkdirs();
		
		File[] files = path.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return (name.startsWith("face") && name.endsWith(".png"));
		    }
		});
		
		String imgName = "face";
		if (files == null) {
			imgName = imgName.concat("0");
		} else {
			imgName = imgName.concat(String.valueOf(files.length));
		}
		imgName = imgName.concat(".png");
		File file = new File(path, imgName);		
		String filename = file.toString();
		
		saveImage(mRgba, filename);
	}
	
	private void saveColor() {
		markColor();
		Log.i(RobotActivity.class.toString(), "Salvando Imagem de Cor!");
		
		File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IFBA/INF627/Colors/");
		path.mkdirs();
		
		File[] files = path.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return (name.startsWith("color") && name.endsWith(".png"));
		    }
		});
		
		String imgName = "color";
		if (files == null) {
			imgName = imgName.concat("0");
		} else {
			imgName = imgName.concat(String.valueOf(files.length));
		}
		imgName = imgName.concat(".png");
		File file = new File(path, imgName);		
		String filename = file.toString();
		
		saveImage(mRgba, filename);
	}


	public void saveImage (Mat rgba, String filename) {
		Mat mIntermediateMat = new Mat();
		Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);
		
		Boolean bool = Highgui.imwrite(filename, mIntermediateMat);
		if (bool)
			Log.i(RobotActivity.class.toString(), "Imagem: " + filename + " salva com SUCESSO!");
		else
			Log.i(RobotActivity.class.toString(), "Falha ao salvar imagem: " + filename);
	}

	@Override
	public void robotStopped() {
		Log.i(RobotActivity.class.toString(), "Robô Parou!");
		if (mFaceDetected){
			saveFaces();
			mRobot.walk();
		}
		if (mColorDetected) {
			saveColor();
			mRobot.walk();
		}
	}

	@Override
	public void robotWalking() {
		Log.i(RobotActivity.class.toString(), "Robô Andando!");
		mFaceDetected = false;
		mColorDetected = false;
	}
}
