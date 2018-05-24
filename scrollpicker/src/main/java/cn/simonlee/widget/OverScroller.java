package cn.simonlee.widget;

import android.hardware.SensorManager;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-18
 */
public class OverScroller {

    /**
     * 摩擦系数
     */
    private float mFlingFriction = ViewConfiguration.getScrollFriction();

    /**
     * 是否结束
     */
    private boolean mFinished;

    /**
     * 开始时间
     */
    private long mStartTime;

    /**
     * 当前值
     */
    private float mCurValue;

    /**
     * 速度
     */
    private float mVelocity;

    /**
     * 路程
     */
    private float mDistance;

    /**
     * 持续时间
     */
    private int mDuration;

    /**
     * 初始值
     */
    private float mStarValue;

    /**
     * 最终值
     */
    private float mFinalValue;

    public OverScroller(float density) {

        final float ppi = density * 160.0f;
        mPhysicalCoeff = SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * ppi
                * 0.84f; // look and feel tuning
        Log.d("PickerView", getClass().getName() + ".OverScroller() mPhysicalCoeff = " + mPhysicalCoeff);
    }

    public boolean isFinished() {
        return mFinished;
    }

    public float getCurValue() {
        //TODO 进行计算，返回当前值

        final long currentTime = AnimationUtils.currentAnimationTimeMillis() - mStartTime;

        if (currentTime >= mDuration) {
            mFinished = true;
            return mFinalValue;
        }

        final float t = 1F * currentTime / mDuration;//当前已过时间除以总时间，是占比
        final int index = (int) (NB_SAMPLES * t);//时间占比乘以100
        float distanceCoef = 1.f;
        float velocityCoef = 0.f;
        float distance = 0;
        if (index < NB_SAMPLES) {
            final float t_inf = (float) index / NB_SAMPLES;
            final float t_sup = (float) (index + 1) / NB_SAMPLES;
            final float d_inf = SPLINE_POSITION[index];
            final float d_sup = SPLINE_POSITION[index + 1];
            velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
            distanceCoef = d_inf + (t - t_inf) * velocityCoef;
        }

        distance = distanceCoef * mDistance;
//        mCurrVelocity = velocityCoef * mDistance / mDuration * 1000.0f;

        mCurValue = mStarValue + distance;
        return mCurValue;
    }

    public void finish() {
//        mCurrentPosition = mFinal;
        mFinished = true;
    }

    /**
     * 开始滑动
     *
     * @param startValue 起始位置
     * @param minValue   最小值
     * @param maxValue   最大值
     * @param distance   滑动距离
     * @param modulus    终点位置的模，会对滑动距离进行微调，以保证终点位置一定是modulus的整数倍
     */
    public void startScroll_Value(float startValue, float minValue, float maxValue, float distance, float modulus) {
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStarValue = mCurValue = startValue;
        //校正位移
        mDistance = reviseDistance(mStarValue, distance, modulus);
        //无位移，直接返回
        if (mDistance == 0) {
            mFinished = true;
            return;
        }
        mFinalValue = mDistance + mStarValue;
        //过滤掉越界情况
        if (maxValue > minValue && (mFinalValue < minValue || mFinalValue > maxValue)) {
            mFinished = true;
            return;
        }
        //计算速度
        mVelocity = (float) getSplineFlingVelocity(mDistance) * Math.signum(mDistance);
        //计算持续时间
        mDuration = getSplineFlingDuration(mVelocity);
        mFinished = false;
    }

    public void startScroll_Velocity(float startValue, float minValue, float maxValue, float velocity, float modulus) {
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mCurValue = mStarValue = startValue;
        //计算位移
        float distance = 0;
        if (velocity != 0) {
            distance = (float) getSplineFlingDistance(velocity) * Math.signum(velocity);//获取位移
        }
        //校正位移
        mFinalValue = mStarValue + reviseDistance(mStarValue, distance, modulus);
        //处理越界情况
        if (maxValue > minValue && mFinalValue < minValue) {
            mFinalValue = minValue;
        } else if (maxValue > minValue && mFinalValue > maxValue) {
            mFinalValue = maxValue;
        }
        mDistance = mFinalValue - mStarValue;
        //无位移，直接返回
        if (mDistance == 0) {
            mFinished = true;
            return;
        }
        //校正速度
        mVelocity = (float) getSplineFlingVelocity(mDistance) * Math.signum(mDistance);
        //计算持续时间
        mDuration = getSplineFlingDuration(mVelocity);
        mFinished = false;
    }

    /**
     * 根据起始位置和模校正位移
     */
    private float reviseDistance(float startValue, float distance, float modulus) {
        if (modulus != 0) {
            float rem = (startValue + distance) % modulus;
            if (rem != 0) {
                if (rem < -modulus / 2F) {
                    return distance - rem - modulus;
                } else if (rem < modulus / 2F) {
                    return distance - rem;
                } else {
                    return distance - rem + modulus;
                }
            }
        }
        return distance;
    }

    /**
     * 根据路程计算初速度
     */
    private double getSplineFlingVelocity(float distance) {
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        double l = Math.log(Math.abs(distance) / (mFlingFriction * mPhysicalCoeff)) * decelMinusOne / DECELERATION_RATE;
        return Math.exp(l) * (mFlingFriction * mPhysicalCoeff) / INFLEXION * 4;
    }

    /**
     * 根据速度计算总时长
     */
    private int getSplineFlingDuration(float velocity) {
        final double l = getSplineDeceleration(velocity / 4);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
//        1.3582017421722412
        Log.d("PickerView", getClass().getName() + ".OverScroller() decelMinusOne = " + decelMinusOne);

//        e^(log(0.35*x/(0.015*136211.77))/1.3582017421722412)*1000
        return (int) (1000.0 * Math.exp(l / decelMinusOne));
    }

    /**
     * 根据速度计算路程
     */
    private double getSplineFlingDistance(float velocity) {
        final double l = getSplineDeceleration(velocity / 4);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

    private double getSplineDeceleration(float velocity) {
        return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
    }

    private float mPhysicalCoeff;

    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));

    private static final int NB_SAMPLES = 100;
    private static final float[] SPLINE_POSITION = new float[NB_SAMPLES + 1];
    private static final float[] SPLINE_TIME = new float[NB_SAMPLES + 1];
    private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
    private static final float START_TENSION = 0.5f;
    private static final float END_TENSION = 1.0f;
    private static final float P1 = START_TENSION * INFLEXION;
    private static final float P2 = 1.0f - END_TENSION * (1.0f - INFLEXION);

    static {
        float x_min = 0.0f;
        float y_min = 0.0f;
        for (int i = 0; i < NB_SAMPLES; i++) {
            final float alpha = (float) i / NB_SAMPLES;

            float x_max = 1.0f;
            float x, tx, coef;
            while (true) {
                x = x_min + (x_max - x_min) / 2.0f;
                coef = 3.0f * x * (1.0f - x);
                tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x;
                if (Math.abs(tx - alpha) < 1E-5) break;
                if (tx > alpha) x_max = x;
                else x_min = x;
            }
            SPLINE_POSITION[i] = coef * ((1.0f - x) * START_TENSION + x) + x * x * x;

            float y_max = 1.0f;
            float y, dy;
            while (true) {
                y = y_min + (y_max - y_min) / 2.0f;
                coef = 3.0f * y * (1.0f - y);
                dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y;
                if (Math.abs(dy - alpha) < 1E-5) break;
                if (dy > alpha) y_max = y;
                else y_min = y;
            }
            SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y;
        }
        SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES] = 1.0f;
    }

}
