package com.lgames.gamebackend.mahjong;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 麻将番种计算器
 */
public class MahjongFanCalculator {
    private static final int BASE_FAN = 1; // 基础番数

    /**
     * 计算胡牌番数
     * @param game 游戏实例
     * @param player 胡牌玩家
     * @param tile 胡的牌
     * @return 番数
     */
    public int calculateFan(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        List<MahjongTile> handTiles = new ArrayList<>(player.getHandTiles());
        if (tile != null) {
            handTiles.add(tile);
        }

        int fan = 0;

        // 基础番
        fan += BASE_FAN;

        // 检查各种番种
        if (isPingHu(handTiles)) {
            fan += 1;
        }

        if (isPengPengHu(handTiles)) {
            fan += 2;
        }

        if (isQingYiSe(handTiles)) {
            fan += 5;
        }

        if (isQiDui(handTiles)) {
            fan += 4;
        }

        if (isDaDiaoYu(handTiles)) {
            fan += 2;
        }

        if (isGangShangKaiHua(game, player, tile)) {
            fan += 1;
        }

        if (isHaiDiLaoYue(game, tile)) {
            fan += 1;
        }

        if (isMenQing(player)) {
            fan += 1;
        }

        // 风牌刻子番
        fan += calculateFengKeZiFan(handTiles);

        // 箭牌刻子番
        fan += calculateJianKeZiFan(handTiles);

        return fan;
    }

    /**
     * 平胡：4组顺子加1对将牌
     */
    private boolean isPingHu(List<MahjongTile> handTiles) {
        if (handTiles.size() != 14) return false;

        // 检查是否有刻子
        for (int i = 0; i < handTiles.size() - 2; i++) {
            if (handTiles.get(i).equals(handTiles.get(i + 1)) && 
                handTiles.get(i + 1).equals(handTiles.get(i + 2))) {
                return false;
            }
        }

        // 检查是否为4组顺子加1对将牌
        return isWinningHand(handTiles);
    }

    /**
     * 碰碰胡：4组刻子加1对将牌
     */
    private boolean isPengPengHu(List<MahjongTile> handTiles) {
        if (handTiles.size() != 14) return false;

        // 检查是否有顺子
        for (int i = 0; i < handTiles.size() - 2; i++) {
            if (handTiles.get(i).getType() == MahjongTile.TileType.WAN) {
                int value1 = handTiles.get(i).getValue();
                int value2 = handTiles.get(i + 1).getValue();
                int value3 = handTiles.get(i + 2).getValue();
                if (value2 == value1 + 1 && value3 == value2 + 1) {
                    return false;
                }
            }
        }

        // 检查是否为4组刻子加1对将牌
        return isWinningHand(handTiles);
    }

    /**
     * 清一色：所有牌都是同一类型
     */
    private boolean isQingYiSe(List<MahjongTile> handTiles) {
        if (handTiles.isEmpty()) return false;

        MahjongTile.TileType type = handTiles.get(0).getType();
        for (MahjongTile tile : handTiles) {
            if (tile.getType() != type) {
                return false;
            }
        }

        return true;
    }

    /**
     * 七对：7对相同的牌
     */
    private boolean isQiDui(List<MahjongTile> handTiles) {
        if (handTiles.size() != 14) return false;

        Map<MahjongTile, Integer> countMap = new HashMap<>();
        for (MahjongTile tile : handTiles) {
            countMap.put(tile, countMap.getOrDefault(tile, 0) + 1);
        }

        for (int count : countMap.values()) {
            if (count != 2) {
                return false;
            }
        }

        return countMap.size() == 7;
    }

    /**
     * 大吊车：只剩一张牌，单吊将
     */
    private boolean isDaDiaoYu(List<MahjongTile> handTiles) {
        if (handTiles.size() != 2) return false;

        return handTiles.get(0).equals(handTiles.get(1));
    }

    /**
     * 杠上开花：杠牌后补牌胡牌
     */
    private boolean isGangShangKaiHua(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        // 简单实现：检查最后一次动作是否是杠牌
        // 实际需要记录游戏动作历史
        return false;
    }

    /**
     * 海底捞月：最后一张牌胡牌
     */
    private boolean isHaiDiLaoYue(MahjongGame game, MahjongTile tile) {
        return game.getTileWall().isEmpty() && tile != null;
    }

    /**
     * 门清：没有吃、碰、杠牌
     */
    private boolean isMenQing(MahjongPlayer player) {
        return player.getExposedTiles().isEmpty();
    }

    /**
     * 计算风牌刻子番数
     */
    private int calculateFengKeZiFan(List<MahjongTile> handTiles) {
        int fan = 0;

        // 检查东、南、西、北风的刻子
        for (int value = MahjongTile.FENG_DONG; value <= MahjongTile.FENG_BEI; value++) {
            MahjongTile tile = new MahjongTile(0, MahjongTile.TileType.FENG, value);
            int count = getTileCount(handTiles, tile);
            if (count >= 3) {
                fan += 1;
            }
        }

        return fan;
    }

    /**
     * 计算箭牌刻子番数
     */
    private int calculateJianKeZiFan(List<MahjongTile> handTiles) {
        int fan = 0;

        // 检查中、发、白的刻子
        for (int value = MahjongTile.JIAN_ZHONG; value <= MahjongTile.JIAN_BAI; value++) {
            MahjongTile tile = new MahjongTile(0, MahjongTile.TileType.JIAN, value);
            int count = getTileCount(handTiles, tile);
            if (count >= 3) {
                fan += 1;
            }
        }

        return fan;
    }

    /**
     * 获取牌的数量
     */
    private int getTileCount(List<MahjongTile> tiles, MahjongTile target) {
        int count = 0;
        for (MahjongTile tile : tiles) {
            if (tile.equals(target)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 检查是否为胡牌牌型（复用自MahjongActionValidator）
     */
    private boolean isWinningHand(List<MahjongTile> handTiles) {
        if (handTiles.size() != 14) {
            return false;
        }

        // 先排序手牌
        List<MahjongTile> sortedTiles = new ArrayList<>(handTiles);
        sortedTiles.sort((t1, t2) -> {
            int typeCompare = t1.getType().compareTo(t2.getType());
            if (typeCompare != 0) return typeCompare;
            return Integer.compare(t1.getValue(), t2.getValue());
        });

        // 尝试找到将牌（一对相同的牌）
        for (int i = 0; i < sortedTiles.size() - 1; i++) {
            if (i > 0 && sortedTiles.get(i).equals(sortedTiles.get(i - 1))) {
                continue; // 跳过已经检查过的相同牌
            }

            if (sortedTiles.get(i).equals(sortedTiles.get(i + 1))) {
                // 假设这对是将牌，移除后检查剩余的牌是否可以组成4组顺子或刻子
                List<MahjongTile> remaining = new ArrayList<>(sortedTiles);
                remaining.remove(i + 1);
                remaining.remove(i);

                if (canFormGroups(remaining)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查剩余的牌是否可以组成顺子或刻子（复用自MahjongActionValidator）
     */
    private boolean canFormGroups(List<MahjongTile> tiles) {
        if (tiles.isEmpty()) {
            return true;
        }

        // 尝试组成刻子
        if (tiles.size() >= 3 && tiles.get(0).equals(tiles.get(1)) && tiles.get(1).equals(tiles.get(2))) {
            List<MahjongTile> remaining = new ArrayList<>(tiles);
            remaining.remove(2);
            remaining.remove(1);
            remaining.remove(0);
            if (canFormGroups(remaining)) {
                return true;
            }
        }

        // 尝试组成顺子（仅适用于万子牌）
        if (tiles.size() >= 3 && tiles.get(0).getType() == MahjongTile.TileType.WAN) {
            int value1 = tiles.get(0).getValue();
            int value2 = tiles.get(1).getValue();
            int value3 = tiles.get(2).getValue();

            if (value2 == value1 + 1 && value3 == value2 + 1) {
                List<MahjongTile> remaining = new ArrayList<>(tiles);
                remaining.remove(2);
                remaining.remove(1);
                remaining.remove(0);
                if (canFormGroups(remaining)) {
                    return true;
                }
            }
        }

        return false;
    }
}