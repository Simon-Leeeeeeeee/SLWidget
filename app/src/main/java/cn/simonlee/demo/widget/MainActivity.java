package cn.simonlee.demo.widget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-11
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "PickerView";
    private CheckBox mCheckBox_Year, mCheckBox_Month, mCheckBox_Day, mCheckBox_Hour, mCheckBox_Min;
    private TextView mTextView_Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCheckBox_Year = findViewById(R.id.checkbox_year);
        mCheckBox_Month = findViewById(R.id.checkbox_month);
        mCheckBox_Day = findViewById(R.id.checkbox_day);
        mCheckBox_Hour = findViewById(R.id.checkbox_hour);
        mCheckBox_Min = findViewById(R.id.checkbox_minute);

        mTextView_Date = findViewById(R.id.textview_date);

        findViewById(R.id.button_datepick).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int type = 0;
        if (mCheckBox_Year.isChecked()) {
            type |= 0x10;
        }
        if (mCheckBox_Month.isChecked()) {
            type |= 0x08;
        }
        if (mCheckBox_Day.isChecked()) {
            type |= 0x04;
        }
        if (mCheckBox_Hour.isChecked()) {
            type |= 0x02;
        }
        if (mCheckBox_Min.isChecked()) {
            type |= 0x01;
        }
        Intent intent = new Intent(this, DatePickerActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("defaultDate", mTextView_Date.getText().toString());
        startActivityForResult(intent, 999);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            mTextView_Date.setText(data.getStringExtra("date"));
        }
    }
}
