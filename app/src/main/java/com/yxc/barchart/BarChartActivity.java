
package com.yxc.barchart;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yxc.barchart.tab.OnTabSelectListener;
import com.yxc.barchart.tab.TopTabLayout;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BarChartActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TopTabLayout mTabLayout;

    BarChartAdapter mBarChartAdapter;
    List<BarEntry> mEntries;
    List<BarEntry> mVisibleEntries;
    BarChartItemDecoration mItemDecoration;
    private int displayNumber;
    YAxis mYAxis;
    XAxis mXAxis;

    private String[] mTitles = {"日", "周", "月", "年"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barchart_main);

        mTabLayout = findViewById(R.id.topTabLayout);
        recyclerView = findViewById(R.id.recycler);
        initTableLayout();

        mEntries = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) layoutManager).setOrientation(LinearLayoutManager.HORIZONTAL);

        displayNumber = 25;
        mYAxis = new YAxis();
        mXAxis = new XAxis(this, displayNumber);
        mItemDecoration = new BarChartItemDecoration(this, BarChartItemDecoration.HORIZONTAL_LIST, mYAxis, mXAxis);
//        mItemDecoration.setEnableCharValueDisplay(false);
//        mItemDecoration.setEnableYAxisGridLine(false);
//        mItemDecoration.setEnableYAxisZero(false);
//        mItemDecoration.setBarBorderWidth(10);
//        mItemDecoration.setEnableBarBorder(false);
        mItemDecoration.setEnableLeftYAxisLabel(false);
//        mItemDecoration.setEnableRightYAxisLabel(false);
        recyclerView.addItemDecoration(mItemDecoration);
        mBarChartAdapter = new BarChartAdapter(this, mEntries, recyclerView, mXAxis);
        recyclerView.setAdapter(mBarChartAdapter);
        recyclerView.setLayoutManager(layoutManager);

        createDayEntries();
        reSizeYAxis();
        setListener();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYAxis.labelSize = 5;
                mYAxis.maxLabel = 30000;
                mItemDecoration.setYAxis(mYAxis);
                recyclerView.invalidate();
            }
        });
    }

    private void reSizeYAxis() {
        recyclerView.scrollToPosition(mEntries.size() - 1);
        int lastVisiblePosition = mEntries.size() - 1;
        int firstVisiblePosition = lastVisiblePosition - displayNumber;
        mVisibleEntries = mEntries.subList(firstVisiblePosition, lastVisiblePosition);
        mYAxis = YAxis.getYAxis(getTheMaxNumber(mVisibleEntries));
        mBarChartAdapter.notifyDataSetChanged();
        mItemDecoration.setYAxis(mYAxis);
        recyclerView.invalidate();
    }

    //滑动监听
    private void setListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //用来标记是否正在向最后一个滑动
            boolean isSlidingToLast = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                // 当不滚动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition
                    int lastVisibleItem = manager.findLastVisibleItemPosition();
                    int firstVisibleItem = manager.findFirstVisibleItemPosition();
                    List<BarEntry> displayEntries = mEntries.subList(firstVisibleItem, lastVisibleItem);
                    float max = getTheMaxNumber(displayEntries);
                    mYAxis = YAxis.getYAxis(max);
                    mItemDecoration.setYAxis(mYAxis);
                    recyclerView.invalidate();
                    int totalItemCount = manager.getItemCount();
                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
                        //加载更多功能的代码
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //dx用来判断横向滑动方向，dy用来判断纵向滑动方向
                if (dx > 0) {
                    //大于0表示正在向右滚动
                    isSlidingToLast = true;
                } else {
                    //小于等于0表示停止或向左滚动
                    isSlidingToLast = false;
                }
            }
        });
    }

    //获取最大值
    private float getTheMaxNumber(List<BarEntry> entries) {
        BarEntry barEntry = entries.get(0);
        float max = barEntry.value;
        for (int i = 0; i < entries.size(); i++) {
            BarEntry entryTemp = entries.get(i);
            max = Math.max(max, entryTemp.value);
        }
        return max;
    }

    private void initTableLayout() {
        mTabLayout.setIndicatorColor(ColorUtil.getResourcesColor(this, R.color.tab_unchecked));
        mTabLayout.setTextUnselectColor(ColorUtil.getResourcesColor(this, R.color.tab_checked));
        mTabLayout.setDividerColor(ColorUtil.getResourcesColor(this, R.color.tab_unchecked));
        mTabLayout.setTabData(mTitles);

        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                if (position == 0) {
                    createDayEntries();
                    reSizeYAxis();
                } else if (position == 1) {
                    createWeekEntries();
                    reSizeYAxis();
                } else if (position == 2) {
                    createMonthEntries();
                    reSizeYAxis();
                } else if (position == 3) {
                    createYearEntries();
                    reSizeYAxis();
                }
            }
            @Override
            public void onTabReselect(int position) {

            }
        });
        mTabLayout.setCurrentTab(0);
    }

    // 创建 月视图的数据
    private void createMonthEntries() {
        mEntries.clear();
        displayNumber = 33;
        mXAxis = new XAxis(this, displayNumber);
        long timestamp = TimeUtil.changZeroOfTheDay(LocalDate.now());
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            if (i > 0) {
                timestamp = timestamp - TimeUtil.TIME_DAY;
            }
            float mult = 10;
            float value = 0;
            if (i > 500) {
                value = (float) (Math.random() * 30000) + mult;
            } else if (i > 400) {
                value = (float) (Math.random() * 3000) + mult;
            } else if (i > 300) {
                value = (float) (Math.random() * 20000) + mult;
            } else if (i > 200) {
                value = (float) (Math.random() * 5000) + mult;
            } else if (i > 100) {
                value = (float) (Math.random() * 300) + mult;
            } else {
                value = (float) (Math.random() * 6000) + mult;
            }
            value = Math.round(value);
            int type = BarEntry.TYPE_THIRD;
            String xAxisLabel = "";
            LocalDate localDate = TimeUtil.timestampToLocalDate(timestamp);
            boolean isFirstDayOfMonth = TimeUtil.isFirstDayOfMonth(localDate);
            if (isFirstDayOfMonth && (i + 1) % 7 == 0) {
                type = BarEntry.TYPE_SPECIAL;
                xAxisLabel = localDate.getDayOfMonth() + "日";
            } else if (isFirstDayOfMonth) {
                type = BarEntry.TYPE_FIRST;
            } else if ((i + 1) % 7 == 0) {
                type = BarEntry.TYPE_SECOND;
                xAxisLabel = localDate.getDayOfMonth() + "日";
            }
            BarEntry barEntry = new BarEntry(value, timestamp, type);
            barEntry.localDate = localDate;
            barEntry.xAxisLabel = xAxisLabel;
            entries.add(barEntry);
        }
        Collections.sort(entries);
        mEntries.addAll(0, entries);
        mBarChartAdapter.setXAxis(mXAxis);
    }


    //创建Week视图的数据
    private void createWeekEntries() {
        mEntries.clear();
        displayNumber = 8;
        mXAxis = new XAxis(this, displayNumber);
        long timestamp = TimeUtil.changZeroOfTheDay(LocalDate.now());
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            if (i > 0) {
                timestamp = timestamp - TimeUtil.TIME_DAY;
            }
            float mult = 10;
            float value = 0;
            if (i > 500) {
                value = (float) (Math.random() * 30000) + mult;
            } else if (i > 400) {
                value = (float) (Math.random() * 3000) + mult;
            } else if (i > 300) {
                value = (float) (Math.random() * 20000) + mult;
            } else if (i > 200) {
                value = (float) (Math.random() * 5000) + mult;
            } else if (i > 100) {
                value = (float) (Math.random() * 300) + mult;
            } else {
                value = (float) (Math.random() * 6000) + mult;
            }
            value = Math.round(value);
            int type = BarEntry.TYPE_SECOND;
            LocalDate localDate = TimeUtil.timestampToLocalDate(timestamp);
            boolean isMonday = TimeUtil.isMonday(localDate);
            if (isMonday) {
                type = BarEntry.TYPE_FIRST;
            }
            String xAxis = TimeUtil.getWeekStr(localDate.getDayOfWeek());
            BarEntry barEntry = new BarEntry(value, timestamp, type);
            barEntry.localDate = localDate;
            barEntry.xAxisLabel = xAxis;
            entries.add(barEntry);
        }
        Collections.sort(entries);
        mEntries.addAll(0, entries);
        mBarChartAdapter.setXAxis(mXAxis);
    }


    //创建 Day视图的数据
    private void createDayEntries() {
        mEntries.clear();
        displayNumber = 25;
        mXAxis = new XAxis(this, displayNumber);
        long timestamp = TimeUtil.changZeroOfTheDay(LocalDate.now().plusDays(1));
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 72; i++) {
            if (i > 0) {
                timestamp = timestamp - TimeUtil.TIME_HOUR;
            }
            float mult = 10;
            float value = 0;
            if (i > 500) {
                value = (float) (Math.random() * 30000) + mult;
            } else if (i > 400) {
                value = (float) (Math.random() * 3000) + mult;
            } else if (i > 300) {
                value = (float) (Math.random() * 20000) + mult;
            } else if (i > 200) {
                value = (float) (Math.random() * 5000) + mult;
            } else if (i > 100) {
                value = (float) (Math.random() * 300) + mult;
            } else {
                value = (float) (Math.random() * 6000) + mult;
            }
            value = Math.round(value);
            int type = BarEntry.TYPE_THIRD;
            boolean isNextDay = TimeUtil.isNextDay(timestamp);
            LocalDate localDate = TimeUtil.timestampToLocalDate(timestamp);
            String xAxisStr = "";

            if (isNextDay && i % 3 == 0) {
                type = BarEntry.TYPE_SPECIAL;
                xAxisStr = TimeUtil.getHourOfTheDay(timestamp);
            } else if (isNextDay) {
                type = BarEntry.TYPE_FIRST;
            } else if (i % 3 == 0) {
                type = BarEntry.TYPE_SECOND;
                xAxisStr = TimeUtil.getHourOfTheDay(timestamp);
            }
            BarEntry barEntry = new BarEntry(value, timestamp, type);
            barEntry.localDate = localDate;
            barEntry.xAxisLabel = xAxisStr;
            entries.add(barEntry);
        }
        Collections.sort(entries);
        mEntries.addAll(0, entries);
        mBarChartAdapter.setXAxis(mXAxis);
    }


    //创建 Day视图的数据
    private void createYearEntries() {
        mEntries.clear();
        displayNumber = 13;
        mXAxis = new XAxis(this, displayNumber);
        //获取下个月1号
        LocalDate localDate = TimeUtil.getFirstDayOfMonth(LocalDate.now().plusMonths(1));
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            if (i > 0) {
                localDate = localDate.minusMonths(1);
            }
            float mult = 10;
            float value = 0;
            if (i > 500) {
                value = (float) (Math.random() * 30000) + mult;
            } else if (i > 400) {
                value = (float) (Math.random() * 3000) + mult;
            } else if (i > 300) {
                value = (float) (Math.random() * 20000) + mult;
            } else if (i > 200) {
                value = (float) (Math.random() * 5000) + mult;
            } else if (i > 100) {
                value = (float) (Math.random() * 300) + mult;
            } else {
                value = (float) (Math.random() * 6000) + mult;
            }
            value = Math.round(value);

            int type = BarEntry.TYPE_SECOND;
            boolean isNextYear = TimeUtil.isAnotherYear(localDate);
            if (isNextYear) {
                type = BarEntry.TYPE_FIRST;
            }
            String xAxis = Integer.toString(localDate.getMonthOfYear());
            long timestamp = TimeUtil.changZeroOfTheDay(localDate);
            BarEntry barEntry = new BarEntry(value, timestamp, type);
            barEntry.localDate = localDate;
            barEntry.xAxisLabel = xAxis;
            entries.add(barEntry);
        }
        Collections.sort(entries);
        mEntries.addAll(0, entries);
        mBarChartAdapter.setXAxis(mXAxis);
    }
}
