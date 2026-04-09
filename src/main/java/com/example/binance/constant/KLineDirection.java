package com.example.binance.constant;

public enum KLineDirection {

    /**
     * 阳线：收盘价 > 开盘价
     */
    Bull(1, "阳线"),

    /**
     * 阴线：收盘价 < 开盘价
     */
    Bear(-1, "阴线"),

    /**
     * 平行线：收盘价 = 开盘价
     */
    Doji(0, "罕见十字星");
    ;

    // 中文名称
    private final int dir;
    // 描述说明
    private final String desc;

    KLineDirection(int dir, String desc) {
        this.dir = dir;
        this.desc = desc;
    }

    public int getDir() {
        return dir;
    }

    public String getDesc() {
        return desc;
    }
}
