package cn.simonlee.widgetdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_badge).setOnClickListener(this);
        findViewById(R.id.btn_scrollpicker).setOnClickListener(this);

        findViewById(R.id.btn_jianshu).setOnClickListener(this);
        findViewById(R.id.btn_juejin).setOnClickListener(this);
        findViewById(R.id.btn_github).setOnClickListener(this);
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

}
