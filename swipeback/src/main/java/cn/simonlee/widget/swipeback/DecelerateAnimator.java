package cn.simonlee.widget.swipeback;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @createdTime 2018-06-22
 * 1.指定速度，计算出距离和时间
 * 2.指定距离，计算出速度和时间
 * 3.指定速度和距离，计算出时间
 */

public class DecelerateAnimator extends ValueAnimator {

    private final DecelerateEvaluator mDecelerateEvaluator;
    private float mInitialValue;
    private float mFinalValue;
    private float mVelocity;
    private int mDuration;
    private float mDistance;

    public DecelerateAnimator() {
        setInterpolator(new LinearInterpolator());
        mDecelerateEvaluator = new DecelerateEvaluator();
        setEvaluator(mDecelerateEvaluator);

        final float ppi = 2 * 160.0f;//density
        mPhysicalCoeff = SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * ppi
                * 0.84f; // look and feel tuning
    }

    /**
     * 指定速度和距离，开始减速动画
     */
    public void startAnimator(float startValue, float minFinalValue, float maxFinalValue, float velocity) {
        mInitialValue = startValue;
        //TODO 1.计算预期位移
        float futureDistance = getDistanceByVelocity(velocity);
        //TODO 2.进行终点判断
        if (mInitialValue + futureDistance < (maxFinalValue - minFinalValue) * 0.35F) {
            mFinalValue = minFinalValue;
        } else {
            mFinalValue = maxFinalValue;
        }
        //TODO 3.计算移动距离
        mDistance = mFinalValue - mInitialValue;
        //TODO 4.校正移动速度
        float minVelocity = getVelocityByDistance(mDistance);
        mVelocity = velocity * minVelocity > 0 ? (velocity + minVelocity) : minVelocity;
        //TODO 5.计算动画时间
        mDuration = 3 * Math.round(1000 * Math.abs(mDistance / mVelocity));
        Log.e("SLWidget", getClass().getName() + ".startAnimator() mDuration = " + mDuration);
        //TODO 5.启动动画
        setFloatValues(mInitialValue, mFinalValue);
        setDuration(mDuration);
        start();
    }

    /**
     * 根据速度计算位移
     */
    private float getDistanceByVelocity(float velocity) {
        float distance = 0;
        if (velocity != 0) {
            final double l = Math.log(INFLEXION * Math.abs(velocity / 4) / (mFlingFriction * mPhysicalCoeff));
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            distance = (float) (mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l) * Math.signum(velocity));
        }
        return distance;
    }

    /**
     * 根据位移计算速度
     */
    private float getVelocityByDistance(float distance) {
        float velocity = 0;
        if (distance != 0) {
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            double l = Math.log(Math.abs(distance) / (mFlingFriction * mPhysicalCoeff)) * decelMinusOne / DECELERATION_RATE;
            velocity = (float) (Math.exp(l) * (mFlingFriction * mPhysicalCoeff) / INFLEXION * 4 * Math.signum(distance));
        }
        return velocity;
    }

    private float mPhysicalCoeff;
    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
    private final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
    private float mFlingFriction = ViewConfiguration.getScrollFriction();

    private class DecelerateEvaluator implements TypeEvaluator<Float> {

        private final int NB_SAMPLES = 100;
        private final float[] SPLINE_POSITION = new float[NB_SAMPLES + 1];
        private final float[] SPLINE_TIME = new float[NB_SAMPLES + 1];
        private final float START_TENSION = 0.5f;
        private final float END_TENSION = 1.0f;
        private final float P1 = START_TENSION * INFLEXION;
        private final float P2 = 1.0f - END_TENSION * (1.0f - INFLEXION);

        DecelerateEvaluator() {
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

        @Override
        public Float evaluate(float fraction, Float startValue, Float endValue) {
//            Log.e("SLWidget", getClass().getName() + ".evaluate() startValue = " + startValue + " , endValue = " + endValue + " , fraction = " + fraction);

            final int index = (int) (NB_SAMPLES * fraction);//时间占比乘以100
            float distanceCoef = 1.f;
            float velocityCoef = 0.f;
            if (index < NB_SAMPLES) {
                final float t_inf = (float) index / NB_SAMPLES;
                final float t_sup = (float) (index + 1) / NB_SAMPLES;
                final float d_inf = SPLINE_POSITION[index];
                final float d_sup = SPLINE_POSITION[index + 1];
                velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                distanceCoef = d_inf + (fraction - t_inf) * velocityCoef;
            }

            return startValue + distanceCoef * (endValue - startValue);
        }
    }
}
