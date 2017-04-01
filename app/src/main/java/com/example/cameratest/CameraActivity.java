package com.example.cameratest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.dasheng.util.DisplayUtil;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import com.adasplus.data.AdasConfig;
import com.adasplus.data.FcwInfo;
import com.adasplus.data.LdwInfo;
import com.example.cameratest.RawPreview.CallBack;
import com.example.cameratest.R;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

	private static final int ACTION_DEAL_DATA = 0x01;
	private static final int ACTION_CHECK_POINT = 0x02;

	private static final int WRITE_FILE_TIME = 10000;

	private Camera mCamera = null;
	private Parameters mParameters = null;
	private SurfaceView mSurfaceView = null;
	private RawPreview mPreview = null;
	private SVDraw svdraw = null;

	// protected int mFrameWidth = 1280;
	// protected int mFrameHeight = 720;
	//    private int mFrameWidth;
	//    private int mFrameHeight;
	//-----------------  adas ---------------------//
	private AdasInterfaceImp adasInterface;
	private long mLastTime = 0;
	private int mIsAdasInit = 0;
	private int mLdwBeep = 0;
	private int mFcwBeep = 0;
	private long mLdwCnt = 0;
	private long mFcwCnt = 0;
	private int mStgBeep = 0;
	private float mAdasSpeed = -1;
	private float mGlobCnt = 0;
	// broadcast
	private int mLastLdwState = 0;
	private int mLastFcwState = 0;
	private int mLastStgState = 0;


	SoundPool soundPool;
	HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
	LdwInfo ldwResults;
	FcwInfo fcwResults;
	AdasConfig adasconfig;
	//private byte[] mPicData = null;
	//private Timer mTimer;
	private PowerManager.WakeLock mWakeLock;

	static {
		System.loadLibrary("AdasLib");
	}

	@Override
	protected void onDestroy() {
		Logger.getInstance().logI("release start!");
		adasInterface.release();
		Logger.getInstance().logI("release ok!");
		mIsAdasInit = 2;
		/*if (mTimer != null) {
		mTimer.cancel();
		mTimer = null;
		}*/

		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}

		EventBus.getInstance().unregister(this);
		super.onDestroy();

	}

	@Subscribe
	public void onHandleUpdateAdasConfig(float [] array){
		adasInterface.setVpoint(array[0], array[1]);
	}

	float previewRate = -1f;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		EventBus.getInstance().register(this);
		setContentView(R.layout.activity_main);
		mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview_view);
		//mSurfaceView.setZOrderMediaOverlay(true);
		//		mSurfaceView.setZOrderOnTop(true);

		mSurfaceView.getHolder();
		mSurfaceView.getHolder().addCallback(this);
		mSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

		svdraw = (SVDraw) findViewById(R.id.mDraw);

		LayoutParams params = mSurfaceView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		params.width = p.x;
		params.height = p.y;
		previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
		mSurfaceView.setLayoutParams(params);
		//-----------------  adas ---------------------//
		adasInterface = new AdasInterfaceImp(this);

		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		soundMap.put(0, soundPool.load(this, R.raw.warning_lane, 2));
		soundMap.put(1, soundPool.load(this, R.raw.warning_car, 3));
		soundMap.put(2, soundPool.load(this, R.raw.warning_stopgo, 1));
		//-----------------  adas ---------------------//
		Log.e("KOKOKO", "adasInterface.getVerifyResult() == "+adasInterface.getVerifyResult());
		if (adasInterface.getVerifyResult() == 1) {
			mIsAdasInit = adasInterface.init();
			adasInterface.setUserData("123", "13812341234", "123", "123");
		}
		Logger.getInstance().logI("init ok");

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		if (mWakeLock == null) {
			mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "my lock");
			mWakeLock.acquire();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*mTimer = new Timer("save pic");
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (mPicData != null) {
					saveImage(mPicData,mCamera);
				}
			}
		}, 0, WRITE_FILE_TIME);*/
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Logger.getInstance().logI("surfaceCreated!");
		//        mFrameWidth = AppApplication.sScreenWidth;
		//        mFrameHeight = AppApplication.sScreenHeight;
		if (mCamera == null) {
			Logger.getInstance().logI("camera num:" + Camera.getNumberOfCameras());
			mCamera = Camera.open();
		}
		if (mCamera != null) {
			mParameters = mCamera.getParameters();
			mParameters.setPreviewFormat(PixelFormat.YCbCr_420_SP); // Sets the image format for
			// Logger.getInstance().logI("CameraActivity:" + mFrameWidth +"  " + mFrameHeight);
			// mParameters.setPreviewSize(mFrameWidth, mFrameHeight);
			/* Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
            		mParameters.getSupportedPreviewSizes(), 1/previewRate, 800);
            Logger.getInstance().logI("CameraActivity:" + previewSize.width +"  " + previewSize.height);
            mParameters.setPreviewSize(previewSize.width, previewSize.height);*/
			List<String> focusModes = mParameters.getSupportedFocusModes();
			if(focusModes.contains("continuous-video")){
				mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			}
			// mParameters.setPreviewFpsRange(30000, 30000);
			mCamera.setParameters(mParameters);

			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}

			Logger.getInstance().logI("getPreviewSize:" + mCamera.getParameters().getPreviewSize().width+","+mCamera.getParameters().getPreviewSize().height);
			mPreview = new RawPreview(mCamera.getParameters().getPreviewSize().width,mCamera.getParameters().getPreviewSize().height);
			mPreview.setCallBack(callBack);

			if (mPreview != null) {
				Logger.getInstance().logI("addCallbackBuffer");
				mPreview.addCallbackBuffer(mCamera);
				mPreview.setPreviewCallbackWithBuffer(mCamera);
			}

			mCamera.startPreview();


		}


	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Logger.getInstance().logI("surfaceDestroyed");
		if (mCamera != null) {
			mPreview.setPreviewCallbackWithBufferNull(mCamera);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	CallBack callBack = new CallBack() {
		@Override
		public void processData(byte[] data, Camera camera) {
			//Logger.getInstance().logI("CameraActivity:processData回调");
			// test
			//mPicData = data;
			/*if (adasInterface.getVerifyResult() == 1)
			{
				if(System.currentTimeMillis() - mLastTime > 1000)
				{
					mLastTime = System.currentTimeMillis();
					if(mIsAdasInit == 0)
					{
						mIsAdasInit = adasInterface.init();
						adasInterface.setAdasEnable(true);
					}else{
						adasInterface.setAdasEnable(false);
						adasInterface.release();
						mIsAdasInit = 0;
						mGlobCnt++;
						Logger.getInstance().logI("release: " + mGlobCnt);
					}
				}
			}*/
			// TODO Auto-generated method stub
			Log.e("KOKOKO", "adasInterface.getVerifyResult()11 == "+adasInterface.getVerifyResult());
			if (adasInterface.getVerifyResult() == 1 && mIsAdasInit == 0) {
				mIsAdasInit = adasInterface.init();
				adasInterface.setUserData("123", "13812341234", "123", "123");
			} else if (System.currentTimeMillis() - mLastTime > 85 && mIsAdasInit == 1) {
				mLastTime = System.currentTimeMillis();
				// 1. adas process
				Logger.getInstance().logI("CameraActivity:" + camera.getParameters().getPreviewSize().width +"  " + camera.getParameters().getPreviewSize().height);
				adasInterface.process(data, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height, 2);
				ldwResults = adasInterface.getLdwResults();
				fcwResults = adasInterface.getFcwResults();
				adasconfig = adasInterface.getAdasConfig();
				mAdasSpeed = adasInterface.getAdasSpeed();
				// 2. adas warning
				mLdwCnt++;
				mFcwCnt++;
				/*Log.e("MOMOMO", "ldwResults:"+ldwResults.getState()+","+ldwResults.getLeft()+","+ldwResults.getRight());
				Log.e("MOMOMO", "fcwResults:"+fcwResults.getState()+","+fcwResults.getCarNum());
				Log.e("MOMOMO", "adasconfig:"+adasconfig.getX()+","+adasconfig.getY());
				Log.e("MOMOMO", "mAdasSpeed:"+mAdasSpeed);*/
				// ldw: warning interval 2s
				if (ldwResults != null && (ldwResults.getState() == 1 || ldwResults.getState() == 2)) {
					if (mLdwBeep == 0 && mLdwCnt > 20) {
						mLdwCnt = 0;
						mLdwBeep = 1;
						if(mAdasSpeed > 30.f/3.6f)
							soundPool.play(soundMap.get(0), 1, 1, 2, 0, 1);
					}
				} else {
					mLdwBeep = 0;
				}
				//Logger.getInstance().logI("speed: " + mAdasSpeed);
				// fcw: warning interval 2.5s*t*t
				if (fcwResults != null && fcwResults.getState() == 3) {
					double fcw_volum = 1;

					if (mFcwCnt > (25*fcwResults.getCar()[0].getT()*fcwResults.getCar()[0].getT())) {
						mFcwCnt = 0;
						if(mAdasSpeed > 20.f/3.6f)
							soundPool.play(soundMap.get(1), (float) fcw_volum, (float) fcw_volum, 3, 0, 1);
					}
					mFcwBeep = 1;
				} else if (fcwResults != null && (fcwResults.getState() == 0 || fcwResults.getState() == 1 || fcwResults.getState() == 2)) {
					mFcwBeep = 0;
				}

				// stop go
				if (adasInterface.getStopGoResults() == 1) {
					if (mStgBeep == 0) {
						mStgBeep = 1;
						// 这个声音报警需要增加if判断，如果倒车时不进行报警
						soundPool.play(soundMap.get(2), 1, 1, 1, 0, 1);
					}
				} else {
					mStgBeep = 0;
				}

				if(!SVDraw.mIsDraw){
					Message message = svdraw.getHandler().obtainMessage();
					message.what = ACTION_CHECK_POINT;
					svdraw.getHandler().sendMessage(message);
				} else
				{
					DrawInfo drawInfo = new DrawInfo();
					drawInfo.setLdwResults(ldwResults);
					drawInfo.setConfig(adasconfig);
					drawInfo.setFcwResults(fcwResults);
					//                    drawInfo.setSpeed(adasInterface.getAdasSpeed());
					//                    drawInfo.setStgBeep(mStgBeep);
					Message message = svdraw.getHandler().obtainMessage();
					message.what = ACTION_DEAL_DATA;
					message.obj = drawInfo;
					svdraw.getHandler().sendMessage(message);
				}
			}
		}

	};


	/*private void saveImage(byte[] data ,Camera camera) {
		if(camera!=null){
			Log.e("DMDMDM", "saveimage");
			File fileDir;
			//		if(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED){
			//			fileDir = new File(Environment.getExternalStorageDirectory(), "adas");
			//		} else {
			//
			//		}
			fileDir = new File(Environment.getExternalStorageDirectory(), "adas");
			Logger.getInstance().logI("file dir:" + fileDir.getAbsolutePath());
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}

			File outFile = new File(fileDir, System.currentTimeMillis() + ".jpg");
			BufferedOutputStream bos = null;
			ByteArrayOutputStream baos = null;
			try {
				bos = new BufferedOutputStream(new FileOutputStream(outFile));
				YuvImage image = new YuvImage(data, ImageFormat.NV21, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height, null);
				baos = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, baos);
				byte[] jpegData = baos.toByteArray();
				bos.write(jpegData);
				bos.flush();
				baos.flush();
				Logger.getInstance().logI("write file success");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bos != null) {
					try {
						bos.flush();
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (baos != null) {
					try {
						baos.flush();
						baos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}*/
}
