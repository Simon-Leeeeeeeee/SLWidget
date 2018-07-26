package cn.simonlee.widget.scrollpicker;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.hardware.SensorManager;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

/**
 * 减速动画，默认启用回弹效果。
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-23
 */
@SuppressWarnings("unused")
public class DecelerateAnimator extends ValueAnimator {

    private final float DECELERATION_RATE = 2.358201815f;//Math.log(0.78) / Math.log(0.9)
    private final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)

    /**
     * 动摩擦系数
     */
    private float mFlingFriction = ViewConfiguration.getScrollFriction();

    /**
     * 物理系数
     */
    private final float mPhysicalCoeff;

    /**
     * 弹性系数
     */
    private final float mBounceCoeff;

    /**
     * 是否启用回弹效果
     */
    private final boolean isBouncing;

    /**
     * 估值器
     */
    private final DecelerateEvaluator mDecelerateEvaluator;

    /**
     * 动画起始值
     */
    private float mInitialValue;

    /**
     * 动画终止值
     */
    private float mFinalValue;

    /**
     * 动画总持续时间
     */
    private long mDuration;

    /**
     * 位移距离
     */
    private float mDistance;

    /**
     * 回弹持续时间
     */
    private long mBounceDuration;

    /**
     * 回弹位移距离
     */
    private float mBounceDistance;

    /**
     * 未处理越界情况下的动画时间
     */
    private long mOriginalDuration;

    /**
     * 未处理越界情况下的位移距离
     */
    private float mOriginalDistance;

    /**
     * 摩擦系数，用于计算越界情况下的动画时间和位移
     */
    private float mFrictionCoeff;

    /**
     * 是否越界（只有越界了才可能会发生回弹）
     */
    private boolean isOutside;

    public DecelerateAnimator(Context context) {
        this(context, 10, true);
    }

    public DecelerateAnimator(Context context, float bounceCoeff) {
        this(context, bounceCoeff, true);
    }

    public DecelerateAnimator(Context context, boolean bouncing) {
        this(context, 10, bouncing);
    }

    /**
     * 减速动画
     *
     * @param context     上下文
     * @param bounceCoeff 回弹系数
     * @param bouncing    是否开启回弹效果
     */
    public DecelerateAnimator(Context context, float bounceCoeff, boolean bouncing) {
        this.mBounceCoeff = bounceCoeff;
        this.isBouncing = bouncing;
        this.mDecelerateEvaluator = new DecelerateEvaluator();
        this.mPhysicalCoeff = context.getResources().getDisplayMetrics().density
                * SensorManager.GRAVITY_EARTH * 5291.328f;// = 160.0f * 39.37f * 0.84f
        setInterpolator(new LinearInterpolator());
    }

    /**
     * 指定起止值和初始速度，开始减速动画
     * 终点值一定是极小值或者极大值
     *
     * @param startValue    初始值
     * @param minFinalValue 极小值
     * @param maxFinalValue 极大值
     * @param velocity      初速度
     */
    public void startAnimator(float startValue, float minFinalValue, float maxFinalValue, float velocity) {
        if (minFinalValue >= maxFinalValue) {
            throw new ArithmeticException("maxFinalValue must be larger than minFinalValue!");
        }
        reset();
        mInitialValue = startValue;
        // 1.根据速度计算位移距离
        float distance = getDistanceByVelocity(velocity);
        float finalValue = startValue + distance;
        // 2.确定终点值、位移距离、动画时间
        if (finalValue < minFinalValue || finalValue > maxFinalValue) {//终点值在界外
            //确定终点值
            mFinalValue = finalValue < minFinalValue ? minFinalValue : maxFinalValue;
            //起止值都在界外同侧
            if ((startValue < minFinalValue && finalValue < minFinalValue) || (startValue > maxFinalValue && finalValue > maxFinalValue)) {
                //改变动摩擦系数，减少动画时间
                mFrictionCoeff = mBounceCoeff;
                //直接校正位移距离并计算动画时间
                mDistance = mFinalValue - startValue;
                mDuration = getDurationByDistance(mDistance, mFrictionCoeff);
            } else if (isBouncing) {//起止值跨越边界，且启用回弹效果
                isOutside = true;
                //记录未处理越界情况下的位移距离和动画时间，用于计算回弹第一阶段的位移
                mOriginalDistance = distance;
                mOriginalDuration = getDurationByDistance(distance);
                //获取越界时的速度
                float bounceVelocity = getVelocityByDistance(finalValue - mFinalValue);
                //改变动摩擦系数，减少回弹时间
                mFrictionCoeff = mBounceCoeff;
                //计算越界后的回弹时间
                mBounceDuration = getDurationByVelocity(bounceVelocity, mFrictionCoeff);
                //根据回弹时间计算回弹位移
                mBounceDistance = getDistanceByDuration(mBounceDuration / 2, mFrictionCoeff) * Math.signum(bounceVelocity);
                //总的动画时间 = 原本动画时间 - 界外时间 + 回弹时间
                mDuration = mOriginalDuration - getDurationByDistance(finalValue - mFinalValue) + mBounceDuration;
            } else {//禁用回弹效果，按未越界处理。当越界达到边界值时会提前结束动画
                isOutside = true;
                mDistance = distance;
                //计算动画时间
                mDuration = getDurationByDistance(distance);
            }
        } else {//终点值在界内
            //校正终点值，计算位移距离和动画时间
            mFinalValue = (finalValue * 2 < minFinalValue + maxFinalValue) ? minFinalValue : maxFinalValue;
            mDistance = mFinalValue - startValue;
            mDuration = getDurationByDistance(mDistance);
        }
        startAnimator();
    }

    /**
     * 指定初始速度，开始减速动画。
     * 无边界
     *
     * @param startValue 起始位置
     * @param velocity   初始速度
     * @param modulus    终点值的模，会对滑动距离进行微调，以保证终点位置一定是modulus的整数倍
     */
    public void startAnimator_Velocity(float startValue, float velocity, float modulus) {
        startAnimator_Velocity(startValue, 0, 0, velocity, modulus);
    }

    /**
     * 指定初始速度，开始减速动画。
     * 当极大值大于极小值时有边界
     *
     * @param startValue 起始位置
     * @param minValue   极小值
     * @param maxValue   极大值
     * @param velocity   初始速度
     * @param modulus    终点值的模，会对滑动距离进行微调，以保证终点位置一定是modulus的整数倍
     */
    public void startAnimator_Velocity(float startValue, float minValue, float maxValue, float velocity, float modulus) {
        reset();
        mInitialValue = startValue;
        // 1.计算位移距离
        float distance = getDistanceByVelocity(velocity);
        // 2.校正位移距离
        distance = reviseDistance(distance, startValue, modulus);
        float finalValue = startValue + distance;
        // 3.确定终点值、位移距离、动画时间
        if (maxValue > minValue && (finalValue < minValue || finalValue > maxValue)) {//终点值在界外
            //确定终点值
            mFinalValue = finalValue < minValue ? minValue : maxValue;
            //起止值都在界外同侧
            if ((startValue < minValue && finalValue < minValue) || (startValue > maxValue && finalValue > maxValue)) {
                //改变动摩擦系数，减少动画时间
                mFrictionCoeff = mBounceCoeff;
                //直接校正位移距离并计算动画时间
                mDistance = mFinalValue - startValue;
                mDuration = getDurationByDistance(mDistance, mFrictionCoeff);
            } else if (isBouncing) {//起止值跨越边界，且启用回弹效果
                isOutside = true;
                //记录未处理越界情况下的位移距离和动画时间，用于计算回弹第一阶段的位移
                mOriginalDistance = distance;
                mOriginalDuration = getDurationByDistance(distance);
                //获取越界时的速度
                float bounceVelocity = getVelocityByDistance(finalValue - mFinalValue);
                //改变动摩擦系数，减少回弹时间
                mFrictionCoeff = mBounceCoeff;
                //计算越界后的回弹时间
                mBounceDuration = getDurationByVelocity(bounceVelocity, mFrictionCoeff);
                //根据回弹时间计算回弹位移
                mBounceDistance = getDistanceByDuration(mBounceDuration / 2, mFrictionCoeff) * Math.signum(bounceVelocity);
                //总的动画时间 = 原本动画时间 - 界外时间 + 回弹时间
                mDuration = mOriginalDuration - getDurationByDistance(finalValue - mFinalValue) + mBounceDuration;
            } else {//禁用回弹效果，按未越界处理。当越界达到边界值时会提前结束动画
                isOutside = true;
                mDistance = distance;
                //计算动画时间
                mDuration = getDurationByDistance(distance);
            }
        } else {//终点值在界内
            //确定终点值、位移距离和动画时间
            mFinalValue = finalValue;
            mDistance = distance;
            mDuration = getDurationByDistance(mDistance);
        }
        startAnimator();
    }

    /**
     * 指定位移距离，开始减速动画。
     * 无边界
     *
     * @param startValue 起始位置
     * @param distance   位移距离
     * @param modulus    终点值的模，会对滑动距离进行微调，以保证终点位置一定是modulus的整数倍
     */
    public void startAnimator_Distance(float startValue, float distance, float modulus) {
        startAnimator_Distance(startValue, 0, 0, distance, modulus);
    }

    /**
     * 指定位移距离，开始减速动画。
     * 当极大值大于极小值时有边界
     *
     * @param startValue 起始位置
     * @param minValue   极小值
     * @param maxValue   极大值
     * @param distance   位移距离
     * @param modulus    终点值的模，会对滑动距离进行微调，以保证终点位置一定是modulus的整数倍
     */
    public void startAnimator_Distance(float startValue, float minValue, float maxValue, float distance, float modulus) {
        reset();
        mInitialValue = startValue;
        // 1.先校正位移
        mDistance = reviseDistance(distance, startValue, modulus);
        if (mDistance == 0) {
            return;
        }
        mFinalValue = startValue + mDistance;
        // 2.极值处理
        if (maxValue > minValue && (mFinalValue < minValue || mFinalValue > maxValue)) {
            return;
        }
        // 3.计算时间
        mDuration = getDurationByDistance(mDistance);
        startAnimator();
    }

    private void reset() {
        isOutside = false;
        mFrictionCoeff = 1;
        mBounceDuration = 0;
        mBounceDistance = 0;
        mOriginalDuration = 0;
        mOriginalDistance = 0;
    }

    private void startAnimator() {
        // 1.设置起止值
        setFloatValues(mInitialValue, mFinalValue);
        // 2.设置估值器
        setEvaluator(mDecelerateEvaluator);
        // 3.设置持续时间
        setDuration(mDuration);
        // 4.开始动画
        start();
    }

    /**
     * 校正位移，确保终点值是模的整数倍
     *
     * @param distance   位移距离
     * @param startValue 起始位置
     * @param modulus    终点值的模，会对滑动距离进行微调，以保证终点位置一定是modulus的整数倍
     */
    public float reviseDistance(float distance, float startValue, float modulus) {
        if (modulus != 0) {
            int multiple = (int) ((startValue + distance) / modulus);
            float remainder = (startValue + distance) - multiple * modulus;
            if (remainder != 0) {
                if (remainder * 2 < -modulus) {
                    return distance - remainder - modulus;
                } else if (remainder * 2 < modulus) {
                    return distance - remainder;
                } else {
                    return distance - remainder + modulus;
                }
            }
        }
        return distance;
    }

    /**
     * 根据位移计算初速度
     *
     * @param distance 位移距离
     */
    public float getVelocityByDistance(float distance) {
        return getVelocityByDistance(distance, 1F);
    }

    /**
     * 根据位移计算初速度
     *
     * @param distance      位移距离
     * @param frictionCoeff 摩擦系数
     */
    public float getVelocityByDistance(float distance, float frictionCoeff) {
        float velocity = 0;
        if (distance != 0) {
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            final double l = Math.pow(Math.abs(distance) / (mFlingFriction * frictionCoeff * mPhysicalCoeff), decelMinusOne / DECELERATION_RATE);
            velocity = (float) (l * mFlingFriction * frictionCoeff * mPhysicalCoeff / INFLEXION * 4 * Math.signum(distance));
        }
        return velocity;
    }

    /**
     * 根据初速度计算位移
     *
     * @param velocity 初速度
     */
    public float getDistanceByVelocity(float velocity) {
        return getDistanceByVelocity(velocity, 1F);
    }

    /**
     * 根据初速度计算位移
     *
     * @param velocity      初速度
     * @param frictionCoeff 摩擦系数
     */
    public float getDistanceByVelocity(float velocity, float frictionCoeff) {
        float distance = 0;
        if (velocity != 0) {
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            final double l = Math.pow(INFLEXION * Math.abs(velocity / 4) / (mFlingFriction * frictionCoeff * mPhysicalCoeff), DECELERATION_RATE / decelMinusOne);
            distance = (float) (l * mFlingFriction * frictionCoeff * mPhysicalCoeff * Math.signum(velocity));
        }
        return distance;
    }

    /**
     * 根据时间计算位移距离，无方向性
     *
     * @param duration 动画时间
     */
    public float getDistanceByDuration(long duration) {
        return getDistanceByDuration(duration, 1F);
    }

    /**
     * 根据时间计算位移距离，无方向性
     *
     * @param duration      动画时间
     * @param frictionCoeff 摩擦系数
     */
    public float getDistanceByDuration(long duration, float frictionCoeff) {
        float distance = 0;
        if (duration > 0) {
            final double base = Math.pow(duration / 1000F, DECELERATION_RATE);
            distance = (float) (base * mFlingFriction * frictionCoeff * mPhysicalCoeff);
        }
        return distance;
    }

    /**
     * 根据初速度计算持续时间
     *
     * @param velocity 初速度
     */
    public long getDurationByVelocity(float velocity) {
        return getDurationByVelocity(velocity, 1F);
    }

    /**
     * 根据初速度计算持续时间
     *
     * @param velocity      初速度
     * @param frictionCoeff 摩擦系数
     */
    public long getDurationByVelocity(float velocity, float frictionCoeff) {
        long duration = 0;
        if (velocity != 0) {
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            duration = (long) (1000 * Math.pow(INFLEXION * Math.abs(velocity / 4) / (mFlingFriction * frictionCoeff * mPhysicalCoeff), 1 / decelMinusOne));
        }
        return duration;
    }

    /**
     * 根据位移距离计算持续时间
     *
     * @param distance 位移距离
     */
    public long getDurationByDistance(float distance) {
        return getDurationByDistance(distance, 1F);
    }

    /**
     * 根据位移距离计算持续时间
     *
     * @param distance      位移距离
     * @param frictionCoeff 摩擦系数
     */
    public long getDurationByDistance(float distance, float frictionCoeff) {
        long duration = 0;
        if (distance != 0) {
            final double base = Math.abs(distance) / (mFlingFriction * frictionCoeff * mPhysicalCoeff);
            duration = (long) (1000 * Math.pow(base, 1 / DECELERATION_RATE));
        }
        return duration;
    }

    public void growFlingFriction(float ratio) {
        if (ratio > 0) {
            this.mFlingFriction *= ratio;
        }
    }

    private class DecelerateEvaluator implements TypeEvaluator<Float> {

        @Override
        public Float evaluate(float fraction, Float startValue, Float endValue) {
            if (!isBouncing) {//禁用回弹效果（可能越界，需要提前结束动画）
                float distance = getDistance(fraction, getDuration(), mDistance, mFrictionCoeff);
                if (isOutside && (distance - endValue + startValue) * mDistance > 0) {//越界了
                    if ((fraction > 0 && fraction < 1)) {//动画还将继续，提前结束
                        end();
                    }
                    return endValue;
                }
                return startValue + distance;
            } else if (isOutside) {//回弹效果触发（发生越界）
                float bounceFraction = 1F * mBounceDuration / getDuration();
                if (fraction <= 1F - bounceFraction) {//第一阶段，按原本位移距离和动画时间进行计算
                    //校正进度值
                    fraction = fraction * getDuration() / mOriginalDuration;
                    float distance = getDistance(fraction, mOriginalDuration, mOriginalDistance, 1F);
                    return startValue + distance;
                } else if (fraction <= 1F - bounceFraction / 2F) {//第二阶段，越过边界开始减速
                    //校正进度值
                    fraction = 2F * (fraction + bounceFraction - 1F) / bounceFraction;
                    float distance = getDistance(fraction, mBounceDuration / 2, mBounceDistance, mFrictionCoeff);
                    return endValue + distance;
                } else {//第三阶段，加速回归边界
                    //校正进度值
                    fraction = 2F * (1F - fraction) / bounceFraction;
                    float distance = getDistance(fraction, mBounceDuration / 2, mBounceDistance, mFrictionCoeff);
                    return endValue + distance;
                }
            } else {//回弹效果未触发（未越界）
                float distance = getDistance(fraction, getDuration(), mDistance, mFrictionCoeff);
                return startValue + distance;
            }
        }

        /**
         * 计算位移距离
         *
         * @param fraction      动画进度
         * @param duration      动画时间
         * @param distance      动画总距离
         * @param frictionCoeff 摩擦系数
         */
        private float getDistance(float fraction, long duration, float distance, float frictionCoeff) {
            //获取剩余动画时间
            long surplusDuration = (long) ((1F - fraction) * duration);
            //计算剩余位移距离
            float surplusDistance = getDistanceByDuration(surplusDuration, frictionCoeff) * Math.signum(distance);
            //计算位移距离
            return distance - surplusDistance;
        }

    }
}
