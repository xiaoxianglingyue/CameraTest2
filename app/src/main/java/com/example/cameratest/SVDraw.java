package com.example.cameratest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.adasplus.data.AdasConfig;
import com.adasplus.data.FcwInfo;
import com.adasplus.data.LdwInfo;
import com.adasplus.data.RectInt;

public class SVDraw extends SurfaceView implements SurfaceHolder.Callback {

    public static final int MSG_DRAW = 0x01;
    public static final int MSG_SETCHECK = 0x02;

    private int m_stg_cnt = 0;
    private SurfaceHolder sh;
    private int mWidth;
    private int mHeight;
    private int offsetX = 80;
    private int offsetY = 45;
//    private float taupaintX = 1280.f/800.f;
//    private float taupaintY = 720.f/450.f;
//    private int xCenter = 1280/2;

    private float taupaintX;
    private float taupaintY;
    private int xCenter;

    private static Handler mHandler;
    private final Object mLock = new Object();

    private Paint p;
    private Paint p_text;


    private int mBtnLeft;
    private int mBtnTop;
    private int mBtnRight;
    private int mBtnBottom;

    private float mStartX;
    private float mStartY;

    private static volatile Rect mLandRect;
    private static volatile Rect mPortRect;

    public static volatile boolean mIsDraw = true;
    private static volatile float sConfigX;
    private static volatile float sConfigY;

    private boolean isLand = false;
    private boolean isPort = false;

    private float maxLeft;
    private float maxTop;
    private float maxRight;
    private float maxBottom;
    private DrawThread mDrawThread;

    private Paint mBtnPaint;
    private Rect mTextRect;

    public SVDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        sh = getHolder();
        sh.addCallback(this);
        sh.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int w, int h) {
        mWidth = w;
        mHeight = h;
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        sh = getHolder();
        sh.addCallback(this);
        sh.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
        Log.e("DODODO", AppApplication.sScreenWidth+"--"+AppApplication.sScreenHeight);
        taupaintX = AppApplication.sScreenWidth / 800.f;
        taupaintY = AppApplication.sScreenHeight / 450.f;
        xCenter = AppApplication.sScreenWidth / 2;
        p = new Paint();
        p_text = new Paint();
        p_text.setAntiAlias(true);
        p_text.setTextSize(35);
        p_text.setStrokeWidth(3);
        p_text.setTextAlign(Paint.Align.CENTER);
        mTextRect = new Rect();
        mBtnPaint = new Paint();
        mBtnPaint.setStyle(Style.FILL);
        mBtnPaint.setColor(Color.GREEN);
        mLandRect = new Rect();
        mPortRect = new Rect();
        mBtnLeft = AppApplication.sScreenWidth / 2 - AppApplication.sScreenHeight / 10;
        mBtnTop = 10;
        mBtnRight = AppApplication.sScreenWidth / 2 + AppApplication.sScreenHeight / 10;
        mBtnBottom = AppApplication.sScreenHeight / 10;

        mDrawThread = new DrawThread();
        mDrawThread.start();

        maxLeft = AppApplication.sScreenWidth / 3;
        maxTop = AppApplication.sScreenHeight / 2 - AppApplication.sScreenHeight / 8;
        maxRight = AppApplication.sScreenWidth - AppApplication.sScreenWidth / 3;
        maxBottom = AppApplication.sScreenHeight / 2 + AppApplication.sScreenHeight / 6;

    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_DRAW);
            mHandler.removeMessages(MSG_SETCHECK);
        }
    }

    class DrawThread extends Thread {
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            synchronized (mLock) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case MSG_DRAW:
                                DrawInfo drawInfo = (DrawInfo) msg.obj;
                                Log.e("TOTOTOTO", "drawInfo:"+drawInfo.getFcwResults().getCarNum());
                                drawResult(drawInfo);
                                break;
                            case MSG_SETCHECK:
                                setCheckpoint();
                                Log.e("TOTOTOTO", "setCheckpoint");
                                break;
                        }
                    }
                };
                mLock.notifyAll();
            }
            Looper.loop();
        }
    }

    private void setCheckpoint() {
        Canvas canvas = sh.lockCanvas();
        if (canvas != null) {
            synchronized (sh) {
                p.setStrokeWidth(2);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                //draw check button.
                canvas.drawRect(mBtnLeft,
                        mBtnTop, mBtnRight, mBtnBottom, mBtnPaint);
                mTextRect.set(mBtnLeft, mBtnTop, mBtnRight, mBtnBottom);
                Paint.FontMetrics fontMetrics = p_text.getFontMetrics();
                float top = fontMetrics.top;
                float bottom = fontMetrics.bottom;
                int baseLineY = (int) (mTextRect.centerY() - top / 2 - bottom / 2);
                p_text.setColor(Color.WHITE);
                p.setColor(Color.GREEN);
                // draw landspace line.
                if (sConfigX == maxLeft || sConfigX == maxRight) {
                    p.setColor(Color.argb(255, 252, 25, 25));
                } else {
                    p.setColor(Color.GREEN);
                }
                canvas.drawLine(sConfigX, 0, sConfigX, AppApplication.sScreenHeight, p);
                mPortRect.set((int) sConfigX - 30, 0, (int) sConfigX + 30, AppApplication.sScreenHeight);
                // draw portait line.
                if (sConfigY == maxTop || sConfigY == maxBottom) {
                    p.setColor(Color.argb(255, 252, 25, 25));
                } else {
                    p.setColor(Color.GREEN);
                }
                canvas.drawLine(0, sConfigY, AppApplication.sScreenWidth, sConfigY, p);
                mLandRect.set(0, (int) sConfigY - 30, AppApplication.sScreenWidth, (int) sConfigY + 30);
                canvas.drawText(getResources().getString(R.string.ok), mTextRect.centerX(), baseLineY, p_text);
            }
            sh.unlockCanvasAndPost(canvas);
        }

    }

    public Handler getHandler() {
        synchronized (mLock) {
            if (mHandler == null) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return mHandler;
        }
    }

    void clearDraw() {
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(Color.BLUE);
        sh.unlockCanvasAndPost(canvas);
    }

    public void drawResult(DrawInfo drawInfo) {
        LdwInfo ldwResults = drawInfo.getLdwResults();
        FcwInfo fcwResults = drawInfo.getFcwResults();
        AdasConfig config = drawInfo.getConfig();
        if (ldwResults == null || fcwResults == null || config == null) {
            return;
        }
        Canvas canvas = sh.lockCanvas();
        if (canvas != null) {
            synchronized (sh) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                p.setAntiAlias(true);
                p.setColor(Color.GREEN);
                p.setStyle(Style.STROKE);
                p_text.setTextSize(25);
                p_text.setStrokeWidth(3);

                // draw car detect region
                /*p.setStrokeWidth(1);
				canvas.drawRect(xCenter-250*taupaintX, 480/4,
						xCenter+250*taupaintX, 480/8*7, p);*/
                p.setStrokeWidth(2);

                canvas.drawRect(mBtnLeft,
                        mBtnTop, mBtnRight, mBtnBottom, mBtnPaint);
                mTextRect.set(mBtnLeft, mBtnTop, mBtnRight, mBtnBottom);
                p.setColor(Color.GREEN);
                Paint.FontMetrics fontMetrics = p_text.getFontMetrics();
                float top = fontMetrics.top;
                float bottom = fontMetrics.bottom;
                int baseLineY = (int) (mTextRect.centerY() - top / 2 - bottom / 2);
                p_text.setColor(Color.WHITE);
                canvas.drawText(getResources().getString(R.string.check), mTextRect.centerX(), baseLineY, p_text);

                // draw calibration line
                config.setY((config.getY() + offsetY) * taupaintY);
                config.setX((config.getX() + offsetX) * taupaintX);
                sConfigX = config.getX();
                sConfigY = config.getY();

                canvas.drawLine(config.getX() - 150, config.getY(), config.getX() + 150, config.getY(), p);
                canvas.drawLine(config.getX(), config.getY() - 8, config.getX(), config.getY() + 8, p);
                canvas.drawLine(config.getX() - 150, config.getY() - 3, config.getX() - 150, config.getY() + 3, p);
                canvas.drawLine(config.getX() + 150, config.getY() - 3, config.getX() + 150, config.getY() + 3, p);

                // draw ldw
                p.setStrokeWidth(3);
                if (ldwResults.getLeft().getIsCredible() == 1 || ldwResults.getRight().getIsCredible() == 1) {
                	Log.e("TOTOTOTO", "Color.YELLOW:");
                    p.setColor(Color.YELLOW);
                    Point[] pointsLeft = ldwResults.getLeft().getPoints();
                    Point[] pointsRight = ldwResults.getRight().getPoints();
                    
                    int y_start = pointsLeft[0].y < pointsRight[0].y ? pointsLeft[0].y : pointsRight[0].y;
                    int y_end = pointsLeft[1].y > pointsRight[1].y ? pointsLeft[1].y : pointsRight[1].y;
                    int x_start = 0, x_end = 0;
                    int y_len = y_end - y_start;
                    int y_start_vehicle = y_start;
                    
                    if(fcwResults.getCarNum() > 0 && fcwResults.getState() > 0)
                    {
                    	y_start_vehicle = fcwResults.getCar()[0].getCarRect().getY()
                    			+ fcwResults.getCar()[0].getCarRect().getH();
                    	if(y_start_vehicle > y_start && y_start_vehicle < y_end - 20)
                    		y_start = y_start_vehicle;
                    }
                    if (ldwResults.getState() > 0) {
                        p.setColor(Color.RED);
                        Log.e("TOTOTOTO", "Color.RED:");
                    }
                    for (int i = y_start; i < y_end; i++) {
                        if (i % 10 > 2)
                            continue;
                        //laneColor.val[3] = 50;//(float)(i-y_start)/(float)y_len;
                        x_start = (int) (pointsLeft[0].x - ((i - pointsLeft[0].y) * (pointsLeft[0].x - pointsLeft[1].x) / (pointsLeft[1].y - pointsLeft[0].y)) + 0.5);
                        x_end = (int) (pointsRight[0].x + ((i - pointsRight[0].y) * (pointsRight[1].x - pointsRight[0].x) / (pointsRight[1].y - pointsRight[0].y)) + 0.5);
                        x_start = x_start > 0 ? x_start : 0;
                        x_end = x_end < 640 ? x_end : 640;
                        x_start = (int) ((x_start + offsetX) * taupaintX);
                        x_end = (int) ((x_end + offsetX) * taupaintX);
                        canvas.drawLine(x_start, (i + offsetY) * taupaintY, x_end, (i + offsetY) * taupaintY, p);
                    }
                    Log.e("TOTOTOTO", "Color.YELLOW:"+x_start+","+x_end+","+y_start+y_end);
                }

                // draw fcw
                int carState = fcwResults.getState();
                for (int i = 0; i < fcwResults.getCarNum(); i++) {
                	Log.e("TOTOTOTO", "Color.BLUE:");
                    p.setColor(Color.BLUE);
                    p.setStrokeWidth(2);
                    p_text.setColor(Color.BLUE);
                    int car_dis = (int) (fcwResults.getCar()[i].getDis());
                    RectInt carRectInt = fcwResults.getCar()[i].getCarRect();
                    carRectInt.setY((int) ((carRectInt.getY() + offsetY) * taupaintY));
                    carRectInt.setX((int) ((carRectInt.getX() + offsetX) * taupaintX));
                    carRectInt.setW((int) ((carRectInt.getW()) * taupaintX));
                    carRectInt.setH((int) ((carRectInt.getH()) * taupaintY));
                    if (carState == 1 && i == 0) {
                        p.setColor(Color.YELLOW);
                        p_text.setColor(Color.YELLOW);
                    } else if (carState == 2 && i == 0) {
                        p.setColor(Color.RED);
                        p_text.setColor(Color.RED);
                    } else if (carState == 3 && i == 0) {
                        p.setColor(Color.RED);
                        p_text.setColor(Color.RED);
                    }
                    canvas.drawRect(carRectInt.getX(), carRectInt.getY(),
                            carRectInt.getX() + carRectInt.getW(), carRectInt.getY() + carRectInt.getH(), p);
                    if (config.getIsCalibCredible() == 1) {
                        String text = car_dis + "m";
                        float textWidth = p_text.measureText(text);
                        if (textWidth <= carRectInt.getW()) {
                            //TODO should use dynamic params.
                            mTextRect.set(carRectInt.getX(), carRectInt.getY() - 10,
                                    carRectInt.getX() + carRectInt.getW(), carRectInt.getY() - 10);
                        } else {
                            int offsetX = (int) (carRectInt.getX() - (textWidth / 2 - carRectInt.getW() / 2));
                            mTextRect.set(offsetX, carRectInt.getY() - 10,
                                    (int) (offsetX + textWidth), carRectInt.getY() - 10);
                        }
                        fontMetrics = p_text.getFontMetrics();
                        top = fontMetrics.top;
                        bottom = fontMetrics.bottom;
                        baseLineY = (int) (mTextRect.centerY() - top / 2 - bottom / 2);
                        canvas.drawText(text, mTextRect.centerX(), baseLineY, p_text);

                    }
//						canvas.drawText(Integer.toString(car_dis) + "m", carRectInt.getX()-1, carRectInt.getY()-1, p_text);}
//
					canvas.drawLine(carRectInt.getX(), carRectInt.getY()+carRectInt.getH()-2,
							carRectInt.getX()+carRectInt.getW(), carRectInt.getY()+carRectInt.getH()-2, p);
					canvas.drawLine(carRectInt.getX(), carRectInt.getY()+carRectInt.getH()-2,
							carRectInt.getX()+carRectInt.getW(), carRectInt.getY()+carRectInt.getH()-2, p);
					canvas.drawLine(carRectInt.getX(), carRectInt.getY()+carRectInt.getH()-3,
							carRectInt.getX()+carRectInt.getW(), carRectInt.getY()+carRectInt.getH()-3, p);
                }

                sh.unlockCanvasAndPost(canvas);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getRawX();
                float y = event.getRawY();
                if (x > mBtnLeft && x < mBtnRight && y > mBtnTop && y < mBtnBottom) {
                    if (!mIsDraw) {
                        sConfigX = sConfigX / taupaintX - offsetX;
                        sConfigY = sConfigY / taupaintY - offsetY;
                        EventBus.getInstance().post(new float[]{sConfigX, sConfigY});
                        mIsDraw = true;
                    } else {
//						EventBus.getInstance().post("stop");
                        mIsDraw = false;
                    }
                } else {
                    mStartX = event.getRawX();
                    mStartY = event.getRawY();
                    if (mLandRect.contains((int) mStartX, (int) mStartY)) {
                        isLand = true;
                    } else if (mPortRect.contains((int) mStartX, (int) mStartY)) {
                        isPort = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsDraw) {
                    if (isLand) {
                        if (event.getRawY() >= maxBottom) {
                            sConfigY = maxBottom;
                        } else if (event.getRawY() <= maxTop) {
                            sConfigY = maxTop;
                        } else {
                            sConfigY = event.getRawY();
                        }
                    } else if (isPort) {
                        if (event.getRawX() >= maxRight) {
                            sConfigX = maxRight;
                        } else if (event.getRawX() <= maxLeft) {
                            sConfigX = maxLeft;
                        } else {
                            sConfigX = event.getRawX();
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                isPort = false;
                isLand = false;
                break;
        }
        return true;
    }

}