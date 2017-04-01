package com.example.cameratest;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;

public class RawPreview implements PreviewCallback {

	public int PREVIEW_WIDTH = 1920;
	public int PREVIEW_HEIGHT = 1080;
//	public static final int PREVIEW_WIDTH = AppApplication.sScreenWidth;
//	public static final int PREVIEW_HEIGHT = AppApplication.sScreenHeight;
	private static final String TAG = null;
	private final int MAX_CALLBACK_BUFF = 3;
	private int m_fps_cnt = 0;
	private long m_last_preview_time = 0;

	public byte[][] mCallbackBuffer = null;
	private CallBack callBack;
	
	public RawPreview(int width, int height) {
		PREVIEW_WIDTH = width;
		PREVIEW_HEIGHT = height;
		if (mCallbackBuffer == null) {
			mCallbackBuffer = new byte[MAX_CALLBACK_BUFF][];

			for (int i = 0; i < MAX_CALLBACK_BUFF; i++) {
				mCallbackBuffer[i] = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 3/2];
			}
		}
	}

	public void addCallbackBuffer(Camera camera) {
		if (camera != null) {
			for (int i = 0; i < MAX_CALLBACK_BUFF; i++) {
				camera.addCallbackBuffer(mCallbackBuffer[i]);
			}
		}
	}

	public void setPreviewCallbackWithBuffer(Camera camera) {
		if (camera != null) {
			camera.setPreviewCallbackWithBuffer(this);
		}
	}

	public void setPreviewCallbackWithBufferNull(Camera camera) {
		if (camera != null) {
			for (int i = 0; i < MAX_CALLBACK_BUFF; i++) {
				mCallbackBuffer[i] = null;
				camera.addCallbackBuffer(null);
			}
			camera.setPreviewCallbackWithBuffer(null);
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		/*if((m_fps_cnt++)%10 == 0)
		{
			Log.i(TAG, "time " + (System.currentTimeMillis() - m_last_preview_time));
			m_last_preview_time = System.currentTimeMillis();
		}*/
		if (data != null) {
			if(callBack != null)
				callBack.processData(data, camera);
			camera.addCallbackBuffer(data);
		} else {
			Logger.getInstance().logI("Null preview");
		}
	}
	
	public CallBack getCallBack() {
		return callBack;
	}

	public void setCallBack(CallBack callBack) {
		this.callBack = callBack;
	}

	public interface CallBack{
		void processData(byte[] data, Camera camera);
	}
}
