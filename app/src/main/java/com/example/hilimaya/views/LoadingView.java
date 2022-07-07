package com.example.hilimaya.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.RotateDrawable;
import android.util.AttributeSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.hilimaya.R;

@SuppressLint("AppCompatCustomView")
public class LoadingView extends ImageView {

    private int mRotateDegree = 0;
    private boolean mRotate = false;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //以上两个那么做是保证这三个有同一个进入的入口
        setImageResource(R.mipmap.loading);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mRotate = true;
        //当绑定到窗口时
        post(new Runnable() {
            @Override
            public void run() {
                mRotateDegree += 45;
                mRotateDegree = mRotateDegree <= 360 ? mRotateDegree : 0;
                invalidate(); //这个很重要 没这个就不转了
                if (mRotate) {
                    postDelayed(this, 100);
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //当和窗口解绑的时候
        mRotate = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * 第一个参数是旋转的角度
         * 第二个参数是x
         * 第三个是y
         */
        canvas.rotate(mRotateDegree, getWidth() / 2, getHeight() / 2);
        super.onDraw(canvas);

    }
}
