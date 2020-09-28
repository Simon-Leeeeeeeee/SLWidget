package cn.simonlee.widgetdemo.swiperefreshlayout;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

/**
 * Fragment返回键事件分发
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-08-27
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FragmentDispatcher {

    /**
     * 分发事件：ContentParent适应窗口变化
     *
     * @param fragmentManager Fragment管理器
     */
    public static void dispatchContentParentResized(FragmentManager fragmentManager, int safeLeft, int safeTop, int safeRight, int safeBottom) {
        if (fragmentManager == null) {
            return;
        }
        //获取Fragment列表
        List<Fragment> fragmentList = fragmentManager.getFragments();
        for (int index = fragmentList.size() - 1; index >= 0; index--) {
            Fragment fragment = fragmentList.get(index);
            if (fragment != null && fragment.isAdded()) {
                //分发给Fragment的child处理
                dispatchContentParentResized(fragment.getChildFragmentManager(), safeLeft, safeTop, safeRight, safeBottom);
                if (fragment instanceof OnContentParentResizedInterface) {
                    ((OnContentParentResizedInterface) fragment).onContentParentResized(safeLeft, safeTop, safeRight, safeBottom);
                }
            }
        }
    }

    /**
     * 分发返回键事件给前台Fragment
     *
     * @param fragmentManager Fragment管理器
     * @return true：事件已被处理，false：事件未处理
     */
    public static boolean dispatchBackPressed(FragmentManager fragmentManager) {
        if (fragmentManager == null) {
            return false;
        }
        //获取Fragment列表
        List<Fragment> fragmentList = fragmentManager.getFragments();
        for (int index = fragmentList.size() - 1; index >= 0; index--) {
            Fragment fragment = fragmentList.get(index);
            if (fragment != null && isForegroundFragment(fragment)) {
                //判断Fragment前台显示
                if (dispatchBackPressed(fragment.getChildFragmentManager())) {
                    //判断Fragment的child处理了返回键
                    return true;
                } else if (performBackPressed(fragment)) {
                    //判断Fragment处理了返回键
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断Fragment是否前台
     *
     * @return true：前台，false：后台
     */
    private static boolean isForegroundFragment(@NonNull Fragment fragment) {
        return fragment.isVisible() && fragment.getUserVisibleHint();
    }

    /**
     * Fragment执行返回键事件
     *
     * @return true：事件已被处理，false：事件未处理
     */
    private static boolean performBackPressed(@NonNull Fragment fragment) {
        if (fragment instanceof OnBackPressedInterface) {
            return ((OnBackPressedInterface) fragment).onBackPressed();
        }
        return false;
    }

    /**
     * onBackPressed接口
     */
    public interface OnBackPressedInterface {

        /**
         * 返回键事件处理
         *
         * @return true：已被处理，false：不做处理
         */
        boolean onBackPressed();
    }

    /**
     * onTitleBarHeightResized接口
     */
    public interface OnContentParentResizedInterface {

        /**
         * 标题栏高度变化
         */
        void onContentParentResized(int safeLeft, int safeTop, int safeRight, int safeBottom);
    }

}
