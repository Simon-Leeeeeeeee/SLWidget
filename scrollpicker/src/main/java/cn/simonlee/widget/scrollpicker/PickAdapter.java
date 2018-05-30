package cn.simonlee.widget.scrollpicker;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-17
 */
public interface PickAdapter {

    /**
     * 返回数据总个数
     */
    int getCount();

    /**
     * 返回一条对应index的数据
     */
    String getItem(int position);

}
