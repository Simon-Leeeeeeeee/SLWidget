package cn.simonlee.widgetdemo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import cn.simonlee.widgetdemo.widget.Fake3DView;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private TextView mTextView_TextAngle, mTextView_TextSight;
    private Fake3DView mFake3DView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_badge).setOnClickListener(this);
        findViewById(R.id.btn_scrollpicker).setOnClickListener(this);

        findViewById(R.id.btn_jianshu).setOnClickListener(this);
        findViewById(R.id.btn_juejin).setOnClickListener(this);
        findViewById(R.id.btn_github).setOnClickListener(this);

        mFake3DView = findViewById(R.id.fake3dview);

        mTextView_TextAngle = findViewById(R.id.tv_angle_rotating);
        mTextView_TextSight = findViewById(R.id.tv_sight);

        ((SeekBar) findViewById(R.id.sb_angle_rotating)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_sight)).setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_badge: {
                startActivity(new Intent(this, BadgeActivity.class));
                return;
            }
            case R.id.btn_scrollpicker: {
                startActivity(new Intent(this, DatePickerActivity.class));
                return;
            }
            case R.id.btn_jianshu: {
                Uri uri = Uri.parse("https://www.jianshu.com/u/c35bd597dafb");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return;
            }
            case R.id.btn_juejin: {
                Uri uri = Uri.parse("https://juejin.im/user/5a38846b6fb9a04528469a89");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return;
            }
            case R.id.btn_github: {
                Uri uri = Uri.parse("https://github.com/Simon-Leeeeeeeee/SLWidget");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_angle_rotating: {
                mTextView_TextAngle.setText(String.valueOf(progress) + "度");
                mFake3DView.setRotateAngle(progress);
                break;
            }
            case R.id.sb_sight: {
                mTextView_TextSight.setText(String.valueOf(progress + 1) + "倍");
                mFake3DView.setSightDistance(progress + 1);
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
}
