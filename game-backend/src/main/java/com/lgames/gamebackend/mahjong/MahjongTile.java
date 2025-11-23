package com.lgames.gamebackend.mahjong;

/**
 * 麻将牌模型
 */
public class MahjongTile {
    // 牌类型
    public enum TileType {
        WAN, // 万子
        FENG, // 风牌
        JIAN, // 箭牌
        HUA // 花牌
    }

    // 风牌常量
    public static final int FENG_DONG = 1; // 东
    public static final int FENG_NAN = 2; // 南
    public static final int FENG_XI = 3; // 西
    public static final int FENG_BEI = 4; // 北

    // 箭牌常量
    public static final int JIAN_ZHONG = 1; // 中
    public static final int JIAN_FA = 2; // 发
    public static final int JIAN_BAI = 3; // 白

    // 花牌常量
    public static final int HUA_CHUN = 1; // 春
    public static final int HUA_XIA = 2; // 夏
    public static final int HUA_QIU = 3; // 秋
    public static final int HUA_DONG = 4; // 冬
    public static final int HUA_MEI = 5; // 梅
    public static final int HUA_LAN = 6; // 兰
    public static final int HUA_ZHU = 7; // 竹
    public static final int HUA_JU = 8; // 菊

    private int id; // 唯一标识
    private TileType type; // 牌类型
    private int value; // 数值
    private boolean isUsed; // 是否已使用

    public MahjongTile(int id, TileType type, int value) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.isUsed = false;
    }

    // getter和setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public TileType getType() { return type; }
    public void setType(TileType type) { this.type = type; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }

    @Override
    public String toString() {
        switch (type) {
            case WAN:
                return value + "万";
            case FENG:
                switch (value) {
                    case FENG_DONG: return "东";
                    case FENG_NAN: return "南";
                    case FENG_XI: return "西";
                    case FENG_BEI: return "北";
                    default: return "风" + value;
                }
            case JIAN:
                switch (value) {
                    case JIAN_ZHONG: return "中";
                    case JIAN_FA: return "发";
                    case JIAN_BAI: return "白";
                    default: return "箭" + value;
                }
            case HUA:
                switch (value) {
                    case HUA_CHUN: return "春";
                    case HUA_XIA: return "夏";
                    case HUA_QIU: return "秋";
                    case HUA_DONG: return "冬";
                    case HUA_MEI: return "梅";
                    case HUA_LAN: return "兰";
                    case HUA_ZHU: return "竹";
                    case HUA_JU: return "菊";
                    default: return "花" + value;
                }
            default:
                return "未知牌";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MahjongTile tile = (MahjongTile) obj;
        return type == tile.type && value == tile.value;
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + value;
    }
}