package com.example.cameratest;

import android.content.Context;
import android.util.Log;

import com.adasplus.adas.AdasCollisionCallback;
import com.adasplus.adas.AdasInterface;
import com.adasplus.data.AdasConfig;
import com.adasplus.data.FcwInfo;
import com.adasplus.data.LdwInfo;

import java.io.File;

/**
* <p>Title: AdasInterfaceImp</p>
* <p>Description: </p>
* <p>Company: buptmm</p> 
* @author ming
* @date
*/
public class AdasInterfaceImp {
	AdasConfig adas_config;

	AdasInterface adasInterface;
	public AdasInterfaceImp(Context mContext){
		adasInterface = new AdasInterface(mContext);
		adasInterface.setCallback(new AdasCollisionCallback() {
			@Override
			public void collision(int i) {
				if(i == 9){
					Log.i("Debug", "has collision");
				}
			}
		});
		adas_config = new AdasConfig();
		adas_config.setX(640/2);
		adas_config.setY(360/2);
		adas_config.setVehicleHeight(1.2f);
		adas_config.setVehicleWidth(1.6f);
		adas_config.setIsCalibCredible(0);
		AdasConfig config = adasInterface.getAdasConfig();
		if(config != null){
			adas_config = config;
		}
	}
	public int init(){
    	return adasInterface.adasInit();
	}
	public void release(){
		adasInterface.adasRelease();
	}
	public int process(byte[] img, int width, int height, int dim){
		return adasInterface.adasProcessINC(img, width, height, dim);
	}
	public LdwInfo getLdwResults(){
		return adasInterface.getLdwResults();
	}
	public FcwInfo getFcwResults(){
		return adasInterface.getFcwResults();
	}
	public int getStopGoResults(){
		return adasInterface.getStopGoResults();
	}
	
	public void setUserData(String carId, String userNum, String userId, String insureId) {
    	adasInterface.setUserData(carId, userNum, userId, insureId);
	}

	public int getVerifyResult(){
		return adasInterface.getVerifyResult();
	}
	public AdasConfig getAdasConfig(){
		return adasInterface.getAdasConfig();
	}

	public void setLdwEnable(boolean isOpen){
		AdasConfig adasConfig = getAdasConfig();
		if(adasConfig == null){
			adasConfig = adas_config;
		}
		adasConfig.setIsLdwEnable(isOpen ? 1 : 0);
		adasInterface.setAdasConfig(adasConfig);
	}

	public void setFcwEnable(boolean isOpen){
		AdasConfig adasConfig = getAdasConfig();
		if(adasConfig == null){
			adasConfig = adas_config;
		}
		adasConfig.setIsFcwEnable(isOpen ? 1 : 0);
		adasInterface.setAdasConfig(adasConfig);
	}

	public void setStgEnable(boolean isOpen){
		AdasConfig adasConfig = getAdasConfig();
		if(adasConfig == null){
			adasConfig = adas_config;
		}
		adasConfig.setIsStopgoEnable(isOpen ? 1 : 0);
		adasInterface.setAdasConfig(adasConfig);
	}

	public void setWarningSpeed(int ldw, int fcw){
		AdasConfig adasConfig = getAdasConfig();
		if(adasConfig == null){
			adasConfig = adas_config;
		}
		adasConfig.setLdwMinVelocity(ldw);
		adasConfig.setFcwMinVelocity(fcw);
		adasInterface.setAdasConfig(adasConfig);
	}
	
	public void setIsCalibCredible(){
		AdasConfig adasConfig = getAdasConfig();
		if(adasConfig == null){
			adasConfig = adas_config;
		}
		
		adasConfig.setX(640/2);
		adasConfig.setY(360/2);
		adasConfig.setIsCalibCredible(0);
		adasInterface.setAdasConfig(adasConfig);
	}
	public void setVpoint(float x, float y){
		AdasConfig adasConfig = getAdasConfig();
		if(adasConfig == null){
			adasConfig = adas_config;
		}
		if((adasConfig.getX() - x) > 1 || (adasConfig.getX() - x) < -1
				|| (adasConfig.getY() - y) > 1 || (adasConfig.getY() - y) < -1)
		{
			adasConfig.setX(x);
			adasConfig.setY(y);
			adasConfig.setIsCalibCredible(0);
			adasInterface.setAdasConfig(adasConfig);
		}
	}

	public void setAdasEnable(boolean isOpen){
		AdasConfig adasConfig = getAdasConfig();
		if(adasConfig == null){
			adasConfig = adas_config;
		}
		adasConfig.setIsLdwEnable(isOpen ? 1 : 0);
		adasConfig.setIsFcwEnable(isOpen ? 1 : 0);
		adasConfig.setIsStopgoEnable(isOpen ? 1 : 0);
		adasInterface.setAdasConfig(adasConfig);
	}

	public void setFcwSensitivity(int level){
		AdasConfig adasConfig = getAdasConfig();
		if(adasConfig == null){
			adasConfig = adas_config;
		}
		adasConfig.setFcwSensitivity(level);
		adasInterface.setAdasConfig(adasConfig);
	}
	
	public float getAdasSpeed(){
		return adasInterface.getGpsSpeed();
	}

	public boolean isAdasStop(){
		AdasConfig adasConfig = getAdasConfig();
		if(adasConfig == null){
			adasConfig = adas_config;
		}

		return adasConfig.getIsLdwEnable() == 0
				&& adasConfig.getIsFcwEnable() == 0
				&& adasConfig.getIsStopgoEnable() == 0;
	}
}
