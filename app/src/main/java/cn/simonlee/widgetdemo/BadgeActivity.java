package cn.simonlee.widgetdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import cn.simonlee.widget.badgeview.Badge;
import cn.simonlee.widget.badgeview.BadgeView;
import cn.simonlee.widgetdemo.widget.BadgeButton;
import cn.simonlee.widgetdemo.widget.BadgeImageView;
import cn.simonlee.widgetdemo.widget.BadgeLinearLayout;
import cn.simonlee.widgetdemo.widget.BadgeTextView;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-11
 */

public class BadgeActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, TextWatcher, CompoundButton.OnCheckedChangeListener {

    private BadgeView mBadgeView;
    private BadgeTextView mBadgeTextView;
    private BadgeImageView mBadgeImageView;
    private BadgeButton mBadgeButton;
    private BadgeLinearLayout mBadgeLinearLayout;

    private TextView mTextView_TextSize;
    private TextView mTextView_PaddingLeft, mTextView_PaddingTop, mTextView_PaddingRight, mTextView_PaddingBottom;
    private TextView mTextView_MarginLeft, mTextView_MarginTop, mTextView_MarginRight, mTextView_MarginBottom;

    private RadioButton mRadioButton_GravityLeft, mRadioButton_GravityTop, mRadioButton_GravityRight, mRadioButton_GravityBottom, mRadioButton_GravityCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge);

        mBadgeImageView = findViewById(R.id.imageview_badgeimage);
        mBadgeView = findViewById(R.id.badgeview);
        mBadgeTextView = findViewById(R.id.textview_badgetext);
        mBadgeButton = findViewById(R.id.button_badgebutton);
        mBadgeLinearLayout = findViewById(R.id.linearlayout_badgelayout);

        mTextView_TextSize = findViewById(R.id.tv_textsize);

        mTextView_PaddingLeft = findViewById(R.id.tv_paddingleft);
        mTextView_PaddingTop = findViewById(R.id.tv_paddingtop);
        mTextView_PaddingRight = findViewById(R.id.tv_paddingright);
        mTextView_PaddingBottom = findViewById(R.id.tv_paddingbottom);

        mTextView_MarginLeft = findViewById(R.id.tv_marginleft);
        mTextView_MarginTop = findViewById(R.id.tv_margintop);
        mTextView_MarginRight = findViewById(R.id.tv_marginright);
        mTextView_MarginBottom = findViewById(R.id.tv_marginbottom);

        ((Switch) findViewById(R.id.swicth_bold)).setOnCheckedChangeListener(this);

        ((SeekBar) findViewById(R.id.sb_textsize)).setOnSeekBarChangeListener(this);

        ((SeekBar) findViewById(R.id.sb_paddingleft)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_paddingtop)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_paddingright)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_paddingbottom)).setOnSeekBarChangeListener(this);

        ((SeekBar) findViewById(R.id.sb_marginleft)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_margintop)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_marginright)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_marginbottom)).setOnSeekBarChangeListener(this);

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
        mBadgeImageView.setBadgeText(null);
        mBadgeView.setBadgeText(null);
        mBadgeTextView.setBadgeText(null);
        mBadgeButton.setBadgeText(null);
        mBadgeLinearLayout.setBadgeText(null);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_textsize: {
                mTextView_TextSize.setText(String.valueOf(progress));
                mBadgeImageView.setBadgeTextSize(progress);
                mBadgeView.setBadgeTextSize(progress);
                mBadgeTextView.setBadgeTextSize(progress);
                mBadgeButton.setBadgeTextSize(progress);
                mBadgeLinearLayout.setBadgeTextSize(progress);
                break;
            }
            case R.id.sb_paddingleft: {
                mTextView_PaddingLeft.setText(String.valueOf(progress));
                mBadgeView.setBadgePadding((float) progress, null, null, null);
                mBadgeTextView.setBadgePadding((float) progress, null, null, null);
                mBadgeImageView.setBadgePadding((float) progress, null, null, null);
                mBadgeButton.setBadgePadding((float) progress, null, null, null);
                mBadgeLinearLayout.setBadgePadding((float) progress, null, null, null);
                break;
            }
            case R.id.sb_paddingtop: {
                mTextView_PaddingTop.setText(String.valueOf(progress));
                mBadgeView.setBadgePadding(null, (float) progress, null, null);
                mBadgeTextView.setBadgePadding(null, (float) progress, null, null);
                mBadgeImageView.setBadgePadding(null, (float) progress, null, null);
                mBadgeButton.setBadgePadding(null, (float) progress, null, null);
                mBadgeLinearLayout.setBadgePadding(null, (float) progress, null, null);
                break;
            }
            case R.id.sb_paddingright: {
                mTextView_PaddingRight.setText(String.valueOf(progress));
                mBadgeView.setBadgePadding(null, null, (float) progress, null);
                mBadgeTextView.setBadgePadding(null, null, (float) progress, null);
                mBadgeImageView.setBadgePadding(null, null, (float) progress, null);
                mBadgeButton.setBadgePadding(null, null, (float) progress, null);
                mBadgeLinearLayout.setBadgePadding(null, null, (float) progress, null);
                break;
            }
            case R.id.sb_paddingbottom: {
                mTextView_PaddingBottom.setText(String.valueOf(progress));
                mBadgeView.setBadgePadding(null, null, null, (float) progress);
                mBadgeTextView.setBadgePadding(null, null, null, (float) progress);
                mBadgeImageView.setBadgePadding(null, null, null, (float) progress);
                mBadgeButton.setBadgePadding(null, null, null, (float) progress);
                mBadgeLinearLayout.setBadgePadding(null, null, null, (float) progress);
                break;
            }
            case R.id.sb_marginleft: {
                mTextView_MarginLeft.setText(String.valueOf(progress));
                mBadgeView.setBadgeMargin((float) progress, null, null, null);
                mBadgeTextView.setBadgeMargin((float) progress, null, null, null);
                mBadgeImageView.setBadgeMargin((float) progress, null, null, null);
                mBadgeButton.setBadgeMargin((float) progress, null, null, null);
                mBadgeLinearLayout.setBadgeMargin((float) progress, null, null, null);
                break;
            }
            case R.id.sb_margintop: {
                mTextView_MarginTop.setText(String.valueOf(progress));
                mBadgeView.setBadgeMargin(null, (float) progress, null, null);
                mBadgeTextView.setBadgeMargin(null, (float) progress, null, null);
                mBadgeImageView.setBadgeMargin(null, (float) progress, null, null);
                mBadgeButton.setBadgeMargin(null, (float) progress, null, null);
                mBadgeLinearLayout.setBadgeMargin(null, (float) progress, null, null);
                break;
            }
            case R.id.sb_marginright: {
                mTextView_MarginRight.setText(String.valueOf(progress));
                mBadgeView.setBadgeMargin(null, null, (float) progress, null);
                mBadgeTextView.setBadgeMargin(null, null, (float) progress, null);
                mBadgeImageView.setBadgeMargin(null, null, (float) progress, null);
                mBadgeButton.setBadgeMargin(null, null, (float) progress, null);
                mBadgeLinearLayout.setBadgeMargin(null, null, (float) progress, null);
                break;
            }
            case R.id.sb_marginbottom: {
                mTextView_MarginBottom.setText(String.valueOf(progress));
                mBadgeView.setBadgeMargin(null, null, null, (float) progress);
                mBadgeTextView.setBadgeMargin(null, null, null, (float) progress);
                mBadgeImageView.setBadgeMargin(null, null, null, (float) progress);
                mBadgeButton.setBadgeMargin(null, null, null, (float) progress);
                mBadgeLinearLayout.setBadgeMargin(null, null, null, (float) progress);
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String text = s.toString();
        mBadgeImageView.setBadgeText(text);
        mBadgeView.setBadgeText(text);
        mBadgeTextView.setBadgeText(text);
        mBadgeButton.setBadgeText(text);
        mBadgeLinearLayout.setBadgeText(text);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.swicth_bold: {
                mBadgeImageView.setBadgeBoldText(isChecked);
                mBadgeView.setBadgeBoldText(isChecked);
                mBadgeTextView.setBadgeBoldText(isChecked);
                mBadgeButton.setBadgeBoldText(isChecked);
                mBadgeLinearLayout.setBadgeBoldText(isChecked);
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

        mBadgeImageView.setBadgeGravity(gravity);
        mBadgeView.setBadgeGravity(gravity);
        mBadgeTextView.setBadgeGravity(gravity);
        mBadgeButton.setBadgeGravity(gravity);
        mBadgeLinearLayout.setBadgeGravity(gravity);
    }
}
