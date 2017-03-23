package com.halohoop.floralhoopview.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.halohoop.floralhoopview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pooholah on 2017/3/22.
 * 注意，此自定义View不建议用于群头像，因为会强持有大量bitmap，占用内存较大。
 */

public class FloralHoopView extends View {

    private static final String TAG = "halohoop";
    private PointF mCentrePoint;
    private float mRadius = 10;
    private float mItemRadius;
    private float mItemSpreadRadius;
    private List<Flower> mFlowers = new ArrayList<>();
    private float mStartDrawAngle = 0;
    private float mEveryDegree;

    private class Flower {
        PointF centrePoint;
        Bitmap bitmap;
        Bitmap cutBitmap;
        /**
         * 是否和前一个有交错，如果为true，则pre需要不为空
         */
        boolean isCrossWithFirstDraw = false;
    }

    private float mMaxRadius = -1;

    public FloralHoopView(Context context) {
        this(context, null);
    }

    public FloralHoopView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public FloralHoopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.FloralHoopView);
        mItemRadius = attributes.getFloat(R.styleable.FloralHoopView_item_radius, 10 / 2);
        //花瓣展开的半径
        mItemSpreadRadius = attributes.getFloat(
                R.styleable.FloralHoopView_item_spread_radius, 10 / 2);
        mStartDrawAngle = attributes.getFloat(
                R.styleable.FloralHoopView_start_draw_angle, 0);
        attributes.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        this.mCentrePoint = new PointF(measuredWidth / 2, measuredHeight / 2);
        mRadius = this.mCentrePoint.x;
        getMax();
        fixRaidusToRightValue();
    }

    private void getMax() {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        mMaxRadius = (measuredWidth >= measuredHeight ? measuredHeight : measuredWidth) / 2;
    }

    private void fixRaidusToRightValue() {
        if (mRadius > mMaxRadius) {
            mRadius = mMaxRadius;
        }
        if (mItemRadius > mRadius) {
            mItemRadius = mRadius / 2;
        }
        if ((mItemSpreadRadius + mItemRadius) > mRadius) {
            mItemSpreadRadius = mRadius - mItemRadius;
        }
    }

    public PointF getmCentrePoint() {
        return mCentrePoint;
    }

    public void setmCentrePoint(PointF mCentrePoint) {
        this.mCentrePoint = mCentrePoint;
        //TODO
        invalidate();
    }

    /**
     * It will create a new bitmap to use.
     * Please take good care of the bitmap you passed in by yourself.
     *
     * @param bitmap
     */
    public void addBitmap(Bitmap bitmap) {
        Flower flower = new Flower();
        flower.bitmap = compressBitmapForNewOne(bitmap);
        mFlowers.add(flower);
        updateFlowers();
        invalidate();
    }

    private void updateFlowers() {
        float size = mFlowers.size();
        mEveryDegree = 360.0f / size;
        for (int i = 0; i < mFlowers.size(); i++) {
            Flower flower = mFlowers.get(i);
            updateFlowerPonitF(flower, i);
        }
    }

    private Bitmap createACutBitmap(Flower firstFlower, Flower lastFlower) {
        float radius = lastFlower.bitmap.getWidth() / 2;
        Bitmap cutCircleBitmap = Bitmap.createBitmap(lastFlower.bitmap.getWidth(),
                lastFlower.bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutCircleBitmap);
        canvas.save();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        float cx = radius + Math.abs(firstFlower.centrePoint.x - lastFlower.centrePoint.x);
        float cy = radius - Math.abs(firstFlower.centrePoint.y - lastFlower.centrePoint.y);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        canvas.drawBitmap(lastFlower.bitmap, 0f, 0f, paint);
        canvas.restore();
        paint = null;
        canvas = null;
        return cutCircleBitmap;
    }

    private void updateFlowerPonitF(Flower flower, int index) {
        //如果只有一张图
        if (mFlowers.size() == 1) {
            if (flower.centrePoint == null) {
                flower.centrePoint = new PointF();
            }
            flower.centrePoint.x = mCentrePoint.x;
            flower.centrePoint.y = mCentrePoint.y;
        }
        float drawAngle = (mStartDrawAngle + index * mEveryDegree) % 360;
        if (flower.centrePoint == null) {
            flower.centrePoint = new PointF();
        }
        //根据象限计算出所在坐标
        if (drawAngle >= 0 && drawAngle < 90) {
            //第1象限
            float sin = (float) Math.sin(drawAngle * Math.PI / 180.0);
            float cos = (float) Math.cos(drawAngle * Math.PI / 180.0);
            float deltaX = sin * mItemSpreadRadius;
            float deltaY = cos * mItemSpreadRadius;
            flower.centrePoint.x = mCentrePoint.x + deltaX;
            flower.centrePoint.y = mCentrePoint.y - deltaY;
        } else if (drawAngle >= 90 && drawAngle < 180) {
            //第4象限
            drawAngle = 180 - drawAngle;
            float sin = (float) Math.sin(drawAngle * Math.PI / 180.0);
            float cos = (float) Math.cos(drawAngle * Math.PI / 180.0);
            float deltaX = sin * mItemSpreadRadius;
            float deltaY = cos * mItemSpreadRadius;
            flower.centrePoint.x = mCentrePoint.x + deltaX;
            flower.centrePoint.y = mCentrePoint.y + deltaY;
        } else if (drawAngle >= 180 && drawAngle < 270) {
            //第3象限
            drawAngle = 270 - drawAngle;
            float sin = (float) Math.sin(drawAngle * Math.PI / 180.0);
            float cos = (float) Math.cos(drawAngle * Math.PI / 180.0);
            float deltaX = cos * mItemSpreadRadius;
            float deltaY = sin * mItemSpreadRadius;
            flower.centrePoint.x = mCentrePoint.x - deltaX;
            flower.centrePoint.y = mCentrePoint.y + deltaY;
        } else {
            //第2象限
            drawAngle = 360 - drawAngle;
            float sin = (float) Math.sin(drawAngle * Math.PI / 180.0);
            float cos = (float) Math.cos(drawAngle * Math.PI / 180.0);
            float deltaY = cos * mItemSpreadRadius;
            float deltaX = sin * mItemSpreadRadius;
            flower.centrePoint.x = mCentrePoint.x - deltaX;
            flower.centrePoint.y = mCentrePoint.y - deltaY;
        }
        //检查和第一张是否交叉
        if (mFlowers.size() > 4 && (index + 1) > (mFlowers.size() / 4 * 3)) {
            Flower firstDrawFlower = mFlowers.get(0);
            Flower currDrawFlower = flower;
            double firstPowX = Math.pow(
                    Math.abs(firstDrawFlower.centrePoint.x - currDrawFlower.centrePoint.x), 2);
            double firstPowY = Math.pow(
                    Math.abs(firstDrawFlower.centrePoint.y - currDrawFlower.centrePoint.y), 2);
            float distance = (float) Math.sqrt(firstPowX + firstPowY);
            if (distance < (mItemRadius * 2.0f)) {
                currDrawFlower.isCrossWithFirstDraw = true;
            }else{
                currDrawFlower.isCrossWithFirstDraw = false;
                if (currDrawFlower.cutBitmap != null) {
                    if (!currDrawFlower.cutBitmap.isRecycled()) {
                        currDrawFlower.cutBitmap.recycle();
                        currDrawFlower.cutBitmap = null;
                    }
                }
            }
            if (currDrawFlower.isCrossWithFirstDraw) {
                //创建一个裁减后的bitmap
                if (currDrawFlower.cutBitmap != null) {
                    if (!currDrawFlower.cutBitmap.isRecycled()) {
                        currDrawFlower.cutBitmap.recycle();
                        currDrawFlower.cutBitmap = null;
                    }
                }
                currDrawFlower.cutBitmap = createACutBitmap(firstDrawFlower, currDrawFlower);
            }
        }else {
            flower.isCrossWithFirstDraw = false;
            if (flower.cutBitmap != null) {
                if (!flower.cutBitmap.isRecycled()) {
                    flower.cutBitmap.recycle();
                    flower.cutBitmap = null;
                }
            }
        }
    }

    private Bitmap compressBitmapForNewOne(Bitmap bitmap) {
        Bitmap shapedBitmap = getShapeBitmap(bitmap);
        return shapedBitmap;
    }

    /**
     * 变成圆形的bitmap
     *
     * @param bitmap
     * @return 圆形的bitmap
     */
    private Bitmap getShapeBitmap(Bitmap bitmap) {
        int halfWidth = bitmap.getWidth() / 2;
        int halfHeight = bitmap.getHeight() / 2;
        Bitmap scaledBitmap;
        float shrinkRatio;
        int width;
        int height;
        float radius = mItemRadius;
        if (halfWidth >= halfHeight) {
            shrinkRatio = halfHeight / mItemRadius;
            float createHalfWidth = halfWidth / shrinkRatio;
            width = (int) createHalfWidth * 2;
            height = (int) mItemRadius * 2;
            scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                    width, height, true);
        } else {
            shrinkRatio = halfWidth / mItemRadius;
            float createHalfHeight = halfWidth / shrinkRatio;
            width = (int) mItemRadius * 2;
            height = (int) createHalfHeight * 2;
            scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                    (int) mItemRadius * 2, (int) createHalfHeight * 2, true);
        }
        Bitmap circleBitmap = Bitmap.createBitmap((int) radius * 2, (int) radius * 2,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circleBitmap);
        canvas.save();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(circleBitmap.getWidth() / 2, circleBitmap.getHeight() / 2, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, width / 2 - radius, height / 2 - radius, paint);
        canvas.restore();
        scaledBitmap.recycle();
        paint = null;
        canvas = null;
        return circleBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mFlowers.size(); i++) {
            Flower flower = mFlowers.get(i);
            drawFlower(flower, canvas, i);
        }
    }

    private void drawFlower(Flower flower, Canvas canvas, int index) {
        float left = flower.centrePoint.x - mItemRadius;
        float top = flower.centrePoint.y - mItemRadius;
        if (flower.isCrossWithFirstDraw && flower.cutBitmap != null
                && !flower.cutBitmap.isRecycled()) {
            canvas.drawBitmap(flower.cutBitmap, left, top, null);
        } else {
            canvas.drawBitmap(flower.bitmap, left, top, null);
        }
    }

    public void clearAll(){
        for (int i = 0; i < mFlowers.size(); i++) {
            Flower flower = mFlowers.get(i);
            if (flower.bitmap != null && !flower.bitmap.isRecycled()) {
                flower.bitmap.recycle();
                flower.bitmap = null;
            }
            if (flower.cutBitmap != null && !flower.cutBitmap.isRecycled()) {
                flower.cutBitmap.recycle();
                flower.cutBitmap = null;
            }
        }
        mFlowers.clear();
        invalidate();
    }
}
