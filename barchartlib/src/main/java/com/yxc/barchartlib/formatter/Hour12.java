package com.yxc.barchartlib.formatter;

/**
 * @author yxc
 * @date 2019/4/24
 */
public class Hour12 {
    public int hour;
    public boolean isAnte = true; // 默认上午;

    public String getHour12String(){
        return isAnte?"上午":"下午" + hour + "时";
    }


}
