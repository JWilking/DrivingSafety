package edu.osu.drivingsafety;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;

public class SensorReaderView extends View {
    public double threshold;
    public boolean simpleDetect = false;

    public boolean bigACCDetected = false;
    public boolean blockedstate = false;
    private String stateText;

    public Bitmap mBitmap;
    public Paint mPaint = new Paint();
    public Canvas mCanvas = new Canvas();
    private Path mPath = new Path();
    private RectF mRect = new RectF();
    public float mLastValues[] = new float[3 * 2];
    public float mOrientationValues[] = new float[3];
    public int mColors[] = new int[3 * 2];
    public float mLastX;
    public float mScale[] = new float[2];
    public float mYOffset;
    private float mMaxX;
    public float mSpeed = 3.0f;// 1.0f;
    private float mWidth;
    private float mHeight;

    private String TAG = ((Object) this).getClass().getSimpleName();

    // initialization
    protected SensorReaderView(Context context) {
        super(context);

        mColors[0] = Color.argb(192, 255, 64, 64);
        mColors[1] = 0xFF00FF00;// green

        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
        mPath.arcTo(mRect, 0, 180);
    }

    @Override
    protected void onSizeChanged(int w, int full_h, int oldw, int oldh) {
        int h = full_h;// -50; // the real height of view
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(0xFFFFFFFF);

        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 1.4f)));
        mScale[1] = -(h * 0.5f * (1.0f / 120));
        mWidth = w;
        mHeight = h;
        if (mWidth < mHeight) {
            mMaxX = w;
            Log.i(TAG, "Width is less than Height. mMaxX is " + mMaxX);
            Log.i(TAG, "mWidth is " + w + "mHeight is " + h);
        } else {
            mMaxX = w - 50;
            Log.i(TAG, "Width is lager than Height. mMaxX is " + mMaxX);
            Log.i(TAG, "mWidth is " + mWidth + "mHeight is " + h);
        }
        mLastX = mMaxX;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            if (mBitmap != null) {
                // ///////////////////////////////////////////////////////
                final Paint paint = mPaint;
                final Path path = mPath;
                final float ruler = 0.6f * SensorManager.STANDARD_GRAVITY
                        * mScale[0];
                final int outer = 0xFFC0C0C0; // gray
                final int inner = 0xFF0076A3; // dark blue
                final int bigACC = 0xFF0080FF; // light blue
                final int block = 0xFFFF0000;// red
                final int ready = 0xFF00FF00;// green

                if (mLastX >= mMaxX) { // reload the background pic
                    mLastX = 0;
                    final Canvas cavas = mCanvas;
                    final float yoffset = mYOffset;
                    final float maxx = mMaxX;

                    paint.setColor(0xFF000000);
                    cavas.drawColor(0xFFFFFFFF);
                    cavas.drawLine(0, yoffset, maxx, yoffset, paint);
                    paint.setColor(0xFF808080);
                    cavas.drawLine(0, yoffset + ruler, maxx, yoffset + ruler,
                            paint);
                    cavas.drawLine(0, yoffset - ruler, maxx, yoffset - ruler,
                            paint);
                    paint.setTextSize(20);
                }
                canvas.drawBitmap(mBitmap, 0, 0, null);

                float[] values = mOrientationValues;
                if (mWidth < mHeight) { // draw the three circles
                    float w0 = mWidth * 0.333333f;
                    float w = w0 - 32;
                    float x = w0 * 0.5f;
                    for (int i = 0; i < 3; i++) {
                        canvas.save(Canvas.MATRIX_SAVE_FLAG);
                        canvas.translate(x, w * 0.5f + 22.0f);
                        canvas.save(Canvas.MATRIX_SAVE_FLAG);
                        paint.setColor(outer);
                        canvas.scale(w, w);
                        canvas.drawOval(mRect, paint);
                        canvas.restore();
                        canvas.scale(w - 5, w - 5);
                        paint.setColor(inner);
                        canvas.rotate(-values[i]);
                        canvas.drawPath(path, paint);
                        canvas.restore();
                        x += w0;
                    }
                    paint.setColor(0xFF808080);
                    canvas.drawText("Orientation:", 7, 19, paint);
                    canvas.drawText("Yaw", mWidth * 1 / 6 - 5, mYOffset + ruler
                            - 11, paint);
                    canvas.drawText("Pitch", mWidth * 0.5f - 5, mYOffset
                            + ruler - 11, paint);
                    canvas.drawText("Roll", mWidth * 5 / 6 - 5, mYOffset
                            + ruler - 11, paint);
                    canvas.drawText("1.6G", 7, mYOffset + ruler + 18, paint);
                    canvas.drawText("1G", 7, mYOffset + 18, paint);
                    canvas.drawText("0.4G", 7, mYOffset - ruler + 18, paint);
                    if (blockedstate == false) {
                        paint.setColor(ready);
                        stateText = "Ready";
                    } else {
                        paint.setColor(block);
                        stateText = "Blocked";
                    }
                    canvas.save(Canvas.MATRIX_SAVE_FLAG);
                    canvas.translate(40, 400);
                    canvas.save(Canvas.MATRIX_SAVE_FLAG);
                    canvas.scale(16, 16);
                    canvas.drawOval(mRect, paint);
                    canvas.restore();
                    paint.setTextSize(20);
                    canvas.drawText(stateText, 9, 7, paint);
                    paint.setColor(bigACC);
                    if (bigACCDetected) {
                        canvas.drawText("Big Acceleration", 130, 7, paint);
                    }
                    canvas.restore();
                } else { // the screen has rotated
                    float h0 = mHeight * 0.333333f;
                    float h = h0 - 32;
                    float y = h0 * 0.5f;
                    for (int i = 0; i < 3; i++) {
                        canvas.save(Canvas.MATRIX_SAVE_FLAG);
                        canvas.translate(mWidth - (h * 0.5f + 4.0f), y);
                        canvas.save(Canvas.MATRIX_SAVE_FLAG);
                        paint.setColor(outer);
                        canvas.scale(h, h);
                        canvas.drawOval(mRect, paint);
                        canvas.restore();
                        canvas.scale(h - 5, h - 5);
                        paint.setColor(inner);
                        canvas.rotate(-values[i]);
                        canvas.drawPath(path, paint);
                        canvas.restore();
                        y += h0;
                    }
                    paint.setColor(0xFF808080);
                    canvas.drawText("Orientation:", mWidth - 129, 19, paint);
                    canvas.drawText("Yaw", mWidth - (h * 0.5f + 19.0f),
                            mHeight * 1 / 3, paint);
                    canvas.drawText("Pitch", mWidth - (h * 0.5f + 21.0f),
                            mHeight * 2 / 3, paint);
                    canvas.drawText("Roll", mWidth - (h * 0.5f + 19.0f),
                            mHeight - 3, paint);
                    canvas.drawText("1.6G", 7, mYOffset + ruler + 18, paint);
                    canvas.drawText("1G", 7, mYOffset + 18, paint);
                    canvas.drawText("0.4G", 7, mYOffset - ruler + 18, paint);
                    if (blockedstate == false) {
                        paint.setColor(ready);
                        stateText = "Ready";
                    } else {
                        paint.setColor(block);
                        stateText = "Blocked";
                    }
                    canvas.save(Canvas.MATRIX_SAVE_FLAG);
                    canvas.translate(40, 240);
                    canvas.save(Canvas.MATRIX_SAVE_FLAG);
                    canvas.scale(16, 16);
                    canvas.drawOval(mRect, paint);
                    canvas.restore();
                    paint.setTextSize(20);
                    canvas.drawText(stateText, 9, 7, paint);
                    paint.setColor(bigACC);
                    if (bigACCDetected)
                        canvas.drawText("Big Acceleration", 130, 7, paint);// ///////??//
                    canvas.restore();
                }
                // ////////////////////////////////////////////////////////////////////////
            }
        }
    }

    public void setSimpleDetect(boolean detect) {
        simpleDetect = detect;
    }
}
