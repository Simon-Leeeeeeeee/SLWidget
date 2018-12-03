package cn.simonlee.widgetdemo.badge;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import cn.simonlee.widget.badgeview.Badge;
import cn.simonlee.widget.badgeview.BadgeView;
import cn.simonlee.widgetdemo.BaseActivity;
import cn.simonlee.widgetdemo.R;

/**
 * 角标页面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-19
 */
public class BadgeActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, TextWatcher, CompoundButton.OnCheckedChangeListener {

    private BadgeView mBadgeView;
    private BadgeImageView mBadgeImageView;

    private TextView mTextView_TextSize;
    private TextView mTextView_OffsetX, mTextView_OffsetY;
    private TextView mTextView_PaddingLeft, mTextView_PaddingTop, mTextView_PaddingRight, mTextView_PaddingBottom;

    private RadioButton mRadioButton_GravityLeft, mRadioButton_GravityTop, mRadioButton_GravityRight, mRadioButton_GravityBottom, mRadioButton_GravityCenter;
    private float mDensityDP, mDensitySP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge);

        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            toolbar.setTitle(R.string.badgeview);
            toolbar.setNavigationOnClickListener(this);
        }

        mDensityDP = getResources().getDisplayMetrics().density;//DP密度
        mDensitySP = getResources().getDisplayMetrics().scaledDensity;//SP密度

        mBadgeView = findViewById(R.id.badge_badgeview);
        mBadgeImageView = findViewById(R.id.badge_avatar);
        mBadgeView.getBadge().setBadgeText("99+");
        mBadgeImageView.getBadge().setBadgeText("99+");

        mTextView_TextSize = findViewById(R.id.tv_textsize);

        mTextView_PaddingLeft = findViewById(R.id.tv_paddingleft);
        mTextView_PaddingTop = findViewById(R.id.tv_paddingtop);
        mTextView_PaddingRight = findViewById(R.id.tv_paddingright);
        mTextView_PaddingBottom = findViewById(R.id.tv_paddingbottom);

        mTextView_OffsetX = findViewById(R.id.tv_offsetx);
        mTextView_OffsetY = findViewById(R.id.tv_offsety);

        ((Switch) findViewById(R.id.swicth_bold)).setOnCheckedChangeListener(this);

        ((SeekBar) findViewById(R.id.sb_textsize)).setOnSeekBarChangeListener(this);

        ((SeekBar) findViewById(R.id.sb_paddingleft)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_paddingtop)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_paddingright)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_paddingbottom)).setOnSeekBarChangeListener(this);

        ((SeekBar) findViewById(R.id.sb_offsetx)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_offsety)).setOnSeekBarChangeListener(this);

        mRadioButton_GravityLeft = findViewById(R.id.radio_gravityleft);
        mRadioButton_GravityTop = findViewById(R.id.radio_gravitytop);
        mRadioButton_GravityRight = findViewById(R.id.radio_gravityright);
        mRadioButton_GravityBottom = findViewById(R.id.radio_gravitybottom);
        mRadioButton_GravityCenter = findViewById(R.id.radio_gravitycenter);
        mRadioButton_GravityLeft.setOnCheckedChangeListener(this);
        mRadioButton_GravityTop.setOnCheckedChangeListener(this);
        mRadioButton_GravityRight.setOnCheckedChangeListener(this);
        mRadioButton_GravityBottom.setOnCheckedChangeListener(this);
        mRadioButton_GravityCenter.setOnCheckedChangeListener(this);

        ((EditText) findViewById(R.id.et_badgetext)).addTextChangedListener(this);

        findViewById(R.id.button_hide).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_toolbar_navigation: {
                onBackPressed();
                break;
            }
            case R.id.button_hide: {
                mBadgeImageView.getBadge().setBadgeText(null);
                mBadgeView.getBadge().setBadgeText(null);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_textsize: {
                progress++;
                mTextView_TextSize.setText(progress + "SP");
                mBadgeImageView.getBadge().setBadgeTextSize(progress * mDensitySP);
                mBadgeView.getBadge().setBadgeTextSize(progress * mDensitySP);
                break;
            }
            case R.id.sb_offsetx: {
                progress -= 20;
                mTextView_OffsetX.setText(progress + "DP");
                mBadgeView.getBadge().setBadgeOffsetX(progress * mDensityDP);
                mBadgeImageView.getBadge().setBadgeOffsetX(progress * mDensityDP);
                break;
            }
            case R.id.sb_offsety: {
                progress -= 20;
                mTextView_OffsetY.setText(progress + "DP");
                mBadgeView.getBadge().setBadgeOffsetY(progress * mDensityDP);
                mBadgeImageView.getBadge().setBadgeOffsetY(progress * mDensityDP);
                break;
            }
            case R.id.sb_paddingleft: {
                mTextView_PaddingLeft.setText(progress + "DP");
                mBadgeView.getBadge().setBadgePaddingLeft(progress * mDensityDP);
                mBadgeImageView.getBadge().setBadgePaddingLeft(progress * mDensityDP);
                break;
            }
            case R.id.sb_paddingtop: {
                mTextView_PaddingTop.setText(progress + "DP");
                mBadgeView.getBadge().setBadgePaddingTop(progress * mDensityDP);
                mBadgeImageView.getBadge().setBadgePaddingTop(progress * mDensityDP);
                break;
            }
            case R.id.sb_paddingright: {
                mTextView_PaddingRight.setText(progress + "DP");
                mBadgeView.getBadge().setBadgePaddingRight(progress * mDensityDP);
                mBadgeImageView.getBadge().setBadgePaddingRight(progress * mDensityDP);
                break;
            }
            case R.id.sb_paddingbottom: {
                mTextView_PaddingBottom.setText(progress + "DP");
                mBadgeView.getBadge().setBadgePaddingBottom(progress * mDensityDP);
                mBadgeImageView.getBadge().setBadgePaddingBottom(progress * mDensityDP);
                break;
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String text = s.toString();
        mBadgeImageView.getBadge().setBadgeText(text);
        mBadgeView.getBadge().setBadgeText(text);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.swicth_bold: {
                mBadgeImageView.getBadge().setBadgeBoldText(isChecked);
                mBadgeView.getBadge().setBadgeBoldText(isChecked);
                return;
            }
            case R.id.radio_gravityleft: {
                if (isChecked) {
                    mRadioButton_GravityRight.setChecked(false);
                }
                setBadgeGravity(isChecked, mRadioButton_GravityTop.isChecked(), !isChecked & mRadioButton_GravityRight.isChecked(), mRadioButton_GravityBottom.isChecked(), mRadioButton_GravityCenter.isChecked());
                break;
            }
            case R.id.radio_gravitytop: {
                if (isChecked) {
                    mRadioButton_GravityBottom.setChecked(false);
                }
                setBadgeGravity(mRadioButton_GravityLeft.isChecked(), isChecked, mRadioButton_GravityRight.isChecked(), !isChecked & mRadioButton_GravityBottom.isChecked(), mRadioButton_GravityCenter.isChecked());
                break;
            }
            case R.id.radio_gravityright: {
                if (isChecked) {
                    mRadioButton_GravityLeft.setChecked(false);
                }
                setBadgeGravity(!isChecked & mRadioButton_GravityLeft.isChecked(), mRadioButton_GravityTop.isChecked(), isChecked, mRadioButton_GravityBottom.isChecked(), mRadioButton_GravityCenter.isChecked());
                break;
            }
            case R.id.radio_gravitybottom: {
                if (isChecked) {
                    mRadioButton_GravityTop.setChecked(false);
                }
                setBadgeGravity(mRadioButton_GravityLeft.isChecked(), !isChecked & mRadioButton_GravityTop.isChecked(), mRadioButton_GravityRight.isChecked(), isChecked, mRadioButton_GravityCenter.isChecked());
                break;
            }
            case R.id.radio_gravitycenter: {
                setBadgeGravity(mRadioButton_GravityLeft.isChecked(), mRadioButton_GravityTop.isChecked(), mRadioButton_GravityRight.isChecked(), mRadioButton_GravityBottom.isChecked(), isChecked);
                break;
            }
        }
    }

    private void setBadgeGravity(boolean left, boolean top, boolean right, boolean bottom, boolean center) {
        int gravity = 0;
        if (left) {
            gravity |= Badge.GRAVITY_LEFT;
        } else if (right) {
            gravity |= Badge.GRAVITY_RIGHT;
        } else if (center) {
            gravity |= Badge.GRAVITY_CENTER;
        }

        if (top) {
            gravity |= Badge.GRAVITY_TOP;
        } else if (bottom) {
            gravity |= Badge.GRAVITY_BOTTOM;
        } else if (center) {
            gravity |= Badge.GRAVITY_CENTER;
        }

        mBadgeImageView.getBadge().setBadgeGravity(gravity);
        mBadgeView.getBadge().setBadgeGravity(gravity);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

}
