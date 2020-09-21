package cn.simonlee.widgetdemo.autowraplayout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.simonlee.widget.lib.widget.titlebar.TitleBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cn.simonlee.widget.autowraplayout.AutoWrapGridLayout;
import cn.simonlee.widgetdemo.CommonActivity;
import cn.simonlee.widgetdemo.R;

/**
 * 自动换行页面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-26
 */
public class AutoWrapActivity extends CommonActivity implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private TextView mTextView_ParentWidth;
    private TextView mTextView_GridLineWidth;
    private int fullWidth;
    private View mAutoWrapLayoutGroup;
    private List<AutoWrapGridLayout> mAutoWrapLayoutList = new ArrayList<>();
    private Random mRandom;
    private RadioButton mRadioButton_GravityLeft, mRadioButton_GravityTop, mRadioButton_GravityRight, mRadioButton_GravityBottom, mRadioButton_GravityCenter, mRadioButton_GravityFill;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autowrap);
        initView();
    }

    private void initView() {
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.autowraplayout);

        mAutoWrapLayoutGroup = findViewById(R.id.autowrap_layout_group);
        mAutoWrapLayoutList.add((AutoWrapGridLayout) findViewById(R.id.autowrap_layout_grid1));
        mAutoWrapLayoutList.add((AutoWrapGridLayout) findViewById(R.id.autowrap_layout_grid2));
        mAutoWrapLayoutList.add((AutoWrapGridLayout) findViewById(R.id.autowrap_layout_grid3));
        mAutoWrapLayoutList.add((AutoWrapGridLayout) findViewById(R.id.autowrap_layout_grid4));

        ((Switch) findViewById(R.id.swicth_stickfirst)).setOnCheckedChangeListener(this);

        ((SeekBar) findViewById(R.id.sb_parentwidth)).setOnSeekBarChangeListener(this);
        mTextView_ParentWidth = findViewById(R.id.tv_parentwidth);

        findViewById(R.id.btn_gridlinecolor).setOnClickListener(this);

        ((SeekBar) findViewById(R.id.sb_gridlinewidth)).setOnSeekBarChangeListener(this);
        mTextView_GridLineWidth = findViewById(R.id.tv_gridlinewidth);

        mRadioButton_GravityLeft = findViewById(R.id.radio_gravityleft);
        mRadioButton_GravityTop = findViewById(R.id.radio_gravitytop);
        mRadioButton_GravityRight = findViewById(R.id.radio_gravityright);
        mRadioButton_GravityBottom = findViewById(R.id.radio_gravitybottom);
        mRadioButton_GravityCenter = findViewById(R.id.radio_gravitycenter);
        mRadioButton_GravityFill = findViewById(R.id.radio_gravityfill);
        mRadioButton_GravityLeft.setOnCheckedChangeListener(this);
        mRadioButton_GravityTop.setOnCheckedChangeListener(this);
        mRadioButton_GravityRight.setOnCheckedChangeListener(this);
        mRadioButton_GravityBottom.setOnCheckedChangeListener(this);
        mRadioButton_GravityCenter.setOnCheckedChangeListener(this);
        mRadioButton_GravityFill.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gridlinecolor: {
                if (mRandom == null) {
                    mRandom = new Random();
                }
                int randomColor = Color.argb(255, mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
                for (AutoWrapGridLayout autoWrapGridLayout : mAutoWrapLayoutList) {
                    //随机色
                    autoWrapGridLayout.setGridLineColor(randomColor);
                }
                break;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_parentwidth: {
                if (fullWidth <= 0) {
                    fullWidth = getWindow().getDecorView().getMeasuredWidth();
                }
                if (fullWidth > 0) {
                    mTextView_ParentWidth.setText(String.format(Locale.getDefault(), "%d%%", progress));
                    ViewGroup.LayoutParams lp = mAutoWrapLayoutGroup.getLayoutParams();
                    lp.width = fullWidth * progress / 100;
                    mAutoWrapLayoutGroup.setLayoutParams(lp);
                } else {
                    seekBar.setProgress(seekBar.getMax());
                }
                break;
            }
            case R.id.sb_gridlinewidth: {
                progress++;
                mTextView_GridLineWidth.setText(String.format(Locale.getDefault(), "%dPX", progress));
                for (AutoWrapGridLayout autoWrapGridLayout : mAutoWrapLayoutList) {
                    autoWrapGridLayout.setGridLineWidth(progress);
                }
                break;
            }
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.swicth_stickfirst: {
                for (AutoWrapGridLayout autoWrapGridLayout : mAutoWrapLayoutList) {
                    autoWrapGridLayout.setStickFirst(isChecked);
                }
                break;
            }
            case R.id.radio_gravityleft: {
                if (isChecked) {
                    mRadioButton_GravityRight.setChecked(false);
                }
                setGridCellGravity(isChecked, mRadioButton_GravityTop.isChecked(), !isChecked & mRadioButton_GravityRight.isChecked(), mRadioButton_GravityBottom.isChecked(), mRadioButton_GravityCenter.isChecked(), mRadioButton_GravityFill.isChecked());
                break;
            }
            case R.id.radio_gravitytop: {
                if (isChecked) {
                    mRadioButton_GravityBottom.setChecked(false);
                }
                setGridCellGravity(mRadioButton_GravityLeft.isChecked(), isChecked, mRadioButton_GravityRight.isChecked(), !isChecked & mRadioButton_GravityBottom.isChecked(), mRadioButton_GravityCenter.isChecked(), mRadioButton_GravityFill.isChecked());
                break;
            }
            case R.id.radio_gravityright: {
                if (isChecked) {
                    mRadioButton_GravityLeft.setChecked(false);
                }
                setGridCellGravity(!isChecked & mRadioButton_GravityLeft.isChecked(), mRadioButton_GravityTop.isChecked(), isChecked, mRadioButton_GravityBottom.isChecked(), mRadioButton_GravityCenter.isChecked(), mRadioButton_GravityFill.isChecked());
                break;
            }
            case R.id.radio_gravitybottom: {
                if (isChecked) {
                    mRadioButton_GravityTop.setChecked(false);
                }
                setGridCellGravity(mRadioButton_GravityLeft.isChecked(), !isChecked & mRadioButton_GravityTop.isChecked(), mRadioButton_GravityRight.isChecked(), isChecked, mRadioButton_GravityCenter.isChecked(), mRadioButton_GravityFill.isChecked());
                break;
            }
            case R.id.radio_gravitycenter: {
                setGridCellGravity(mRadioButton_GravityLeft.isChecked(), mRadioButton_GravityTop.isChecked(), mRadioButton_GravityRight.isChecked(), mRadioButton_GravityBottom.isChecked(), isChecked, mRadioButton_GravityFill.isChecked());
                break;
            }
            case R.id.radio_gravityfill: {
                setGridCellGravity(mRadioButton_GravityLeft.isChecked(), mRadioButton_GravityTop.isChecked(), mRadioButton_GravityRight.isChecked(), mRadioButton_GravityBottom.isChecked(), mRadioButton_GravityCenter.isChecked(), isChecked);
                break;
            }
        }
    }

    private void setGridCellGravity(boolean left, boolean top, boolean right, boolean bottom, boolean center, boolean fill) {
        int gravity = 0;
        if (fill) {
            gravity |= AutoWrapGridLayout.GRAVITY_FILL;
        } else {
            if (left) {
                gravity |= AutoWrapGridLayout.GRAVITY_LEFT;
            } else if (right) {
                gravity |= AutoWrapGridLayout.GRAVITY_RIGHT;
            } else if (center) {
                gravity |= AutoWrapGridLayout.GRAVITY_CENTER;
            }

            if (top) {
                gravity |= AutoWrapGridLayout.GRAVITY_TOP;
            } else if (bottom) {
                gravity |= AutoWrapGridLayout.GRAVITY_BOTTOM;
            } else if (center) {
                gravity |= AutoWrapGridLayout.GRAVITY_CENTER;
            }
        }
        for (AutoWrapGridLayout autoWrapGridLayout : mAutoWrapLayoutList) {
            autoWrapGridLayout.setGridCellGravity(gravity);
        }
    }

}
