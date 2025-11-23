package com.lgames.gamebackend.mahjong;

import java.util.ArrayList;
import java.util.List;

/**
 * 麻将动作合法性校验引擎
 */
public class MahjongActionValidator {

    /**
     * 校验玩家动作是否合法
     * @param game 游戏实例
     * @param player 玩家
     * @param action 动作
     * @return 是否合法
     */
    public boolean validateAction(MahjongGame game, MahjongPlayer player, PlayerAction action) {
        if (game.getGameStatus() != MahjongGame.GameStatus.PLAYING) {
            return false;
        }

        if (player != game.getCurrentTurnPlayer()) {
            return false;
        }

        switch (action.getActionType()) {
            case CHUPAI:
                return validateChupai(player, action.getTile());
            case CHI:
                return validateChi(game, player, action.getTile());
            case PENG:
                return validatePeng(game, player, action.getTile());
            case GANG:
                return validateGang(game, player, action.getTile());
            case HU:
                return validateHu(game, player, action.getTile());
            default:
                return false;
        }
    }

    /**
     * 校验出牌动作
     * @param player 玩家
     * @param tile 要出的牌
     * @return 是否合法
     */
    private boolean validateChupai(MahjongPlayer player, MahjongTile tile) {
        return player.getHandTiles().contains(tile);
    }

    /**
     * 校验吃牌动作
     * @param game 游戏实例
     * @param player 玩家
     * @param tile 要吃的牌
     * @return 是否合法
     */
    private boolean validateChi(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        // 吃牌只能吃上家打出的牌
        MahjongPlayer previousPlayer = getPreviousPlayer(game, player);
        if (game.getLastDiscardedTile() != tile || game.getLastDiscardedTile() == null) {
            return false;
        }
        if (game.getLastDiscardedPlayer() != previousPlayer) {
            return false;
        }

        // 吃牌只能用于万子牌
        if (tile.getType() != MahjongTile.TileType.WAN) {
            return false;
        }

        // 检查玩家手牌中是否有可以和该牌组成顺子的牌
        List<MahjongTile> handTiles = player.getHandTiles();
        int value = tile.getValue();

        // 可能的顺子组合：[value-2, value-1, value], [value-1, value, value+1], [value, value+1, value+2]
        boolean hasValidCombination = false;

        // 检查[value-1, value, value+1]
        if (value >= 2 && value <= 8) {
            MahjongTile tile1 = new MahjongTile(0, MahjongTile.TileType.WAN, value - 1);
            MahjongTile tile2 = new MahjongTile(0, MahjongTile.TileType.WAN, value + 1);
            if (handTiles.contains(tile1) && handTiles.contains(tile2)) {
                hasValidCombination = true;
            }
        }

        // 检查[value-2, value-1, value]
        if (value >= 3) {
            MahjongTile tile1 = new MahjongTile(0, MahjongTile.TileType.WAN, value - 2);
            MahjongTile tile2 = new MahjongTile(0, MahjongTile.TileType.WAN, value - 1);
            if (handTiles.contains(tile1) && handTiles.contains(tile2)) {
                hasValidCombination = true;
            }
        }

        // 检查[value, value+1, value+2]
        if (value <= 7) {
            MahjongTile tile1 = new MahjongTile(0, MahjongTile.TileType.WAN, value + 1);
            MahjongTile tile2 = new MahjongTile(0, MahjongTile.TileType.WAN, value + 2);
            if (handTiles.contains(tile1) && handTiles.contains(tile2)) {
                hasValidCombination = true;
            }
        }

        return hasValidCombination;
    }

    /**
     * 校验碰牌动作
     * @param game 游戏实例
     * @param player 玩家
     * @param tile 要碰的牌
     * @return 是否合法
     */
    private boolean validatePeng(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        // 碰牌必须是别人刚打出的牌
        if (game.getLastDiscardedTile() != tile || game.getLastDiscardedTile() == null) {
            return false;
        }

        // 检查玩家手牌中是否有两张相同的牌
        List<MahjongTile> handTiles = player.getHandTiles();
        int count = 0;
        for (MahjongTile handTile : handTiles) {
            if (handTile.equals(tile)) {
                count++;
            }
            if (count >= 2) {
                break;
            }
        }

        return count >= 2;
    }

    /**
     * 校验杠牌动作
     * @param game 游戏实例
     * @param player 玩家
     * @param tile 要杠的牌
     * @return 是否合法
     */
    private boolean validateGang(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        // 检查玩家手牌中是否有三张相同的牌
        List<MahjongTile> handTiles = player.getHandTiles();
        int count = 0;
        for (MahjongTile handTile : handTiles) {
            if (handTile.equals(tile)) {
                count++;
            }
            if (count >= 3) {
                break;
            }
        }

        // 如果是明杠，还需要检查是否是别人刚打出的牌
        if (game.getLastDiscardedTile() == tile) {
            return count >= 3;
        } else {
            // 暗杠需要四张相同的牌
            return count >= 4;
        }
    }

    /**
     * 校验胡牌动作
     * @param game 游戏实例
     * @param player 玩家
     * @param tile 胡的牌（自摸时为null）
     * @return 是否合法
     */
    private boolean validateHu(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        List<MahjongTile> handTiles = new ArrayList<>(player.getHandTiles());

        // 如果是胡别人打出的牌，需要将该牌加入手牌
        if (tile != null) {
            handTiles.add(tile);
        }

        // 胡牌需要14张牌（庄家）或13张牌（闲家）加胡的牌
        if (handTiles.size() != 14) {
            return false;
        }

        // 检查是否符合胡牌牌型
        return isWinningHand(handTiles);
    }

    /**
     * 检查是否为胡牌牌型
     * @param handTiles 手牌
     * @return 是否为胡牌牌型
     */
    private boolean isWinningHand(List<MahjongTile> handTiles) {
        // 简单实现：检查是否为4组顺子/刻子加1对将牌
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
     * 检查剩余的牌是否可以组成顺子或刻子
     * @param tiles 牌列表
     * @return 是否可以组成顺子或刻子
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

    /**
     * 获取上家玩家
     * @param game 游戏实例
     * @param player 当前玩家
     * @return 上家玩家
     */
    private MahjongPlayer getPreviousPlayer(MahjongGame game, MahjongPlayer player) {
        if (game.getPlayer1() == player) {
            return game.getPlayer2();
        } else if (game.getPlayer2() == player) {
            return game.getPlayer1();
        }
        return null;
    }
}