
package com.yxc.barchart.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.yxc.barchart.R;
import com.yxc.barchart.ui.sleep.SleepActivity;
import com.yxc.barchart.ui.step.StepActivity;
import com.yxc.commonlib.util.TimeUtil;

import org.joda.time.LocalDate;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolBar);
        toolbar.setTitle(TimeUtil.getDateStr(TimeUtil.localDateToTimestamp(LocalDate.now()), "M月dd日"));
        toolbar.setNavigationIcon(R.drawable.ic_navigation_left_black_45dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void clickStep(View view) {
        startActivity(new Intent(this, StepActivity.class));
    }

    public void clickSleep(View view) {
        startActivity(new Intent(this, SleepActivity.class));
    }
}