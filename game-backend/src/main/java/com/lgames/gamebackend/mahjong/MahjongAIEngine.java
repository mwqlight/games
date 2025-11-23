package com.lgames.gamebackend.mahjong;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 麻将AI决策引擎
 */
public class MahjongAIEngine {
    private static final Random random = new Random();
    private MahjongActionValidator validator = new MahjongActionValidator();

    /**
     * AI做出决策
     * @param game 游戏实例
     * @param player AI玩家
     * @return 玩家动作
     */
    public PlayerAction makeDecision(MahjongGame game, MahjongPlayer player) {
        // 检查是否可以胡牌
        if (validator.validateAction(game, player, new PlayerAction(PlayerAction.ActionType.HU, game.getLastDiscardedTile()))) {
            return new PlayerAction(PlayerAction.ActionType.HU, game.getLastDiscardedTile());
        }

        // 检查是否可以杠牌
        List<MahjongTile> gangCandidates = findGangCandidates(player);
        if (!gangCandidates.isEmpty()) {
            MahjongTile tile = gangCandidates.get(random.nextInt(gangCandidates.size()));
            return new PlayerAction(PlayerAction.ActionType.GANG, tile);
        }

        // 检查是否可以碰牌
        if (game.getLastDiscardedTile() != null && 
            validator.validateAction(game, player, new PlayerAction(PlayerAction.ActionType.PENG, game.getLastDiscardedTile()))) {
            return new PlayerAction(PlayerAction.ActionType.PENG, game.getLastDiscardedTile());
        }

        // 检查是否可以吃牌
        if (game.getLastDiscardedTile() != null && 
            validator.validateAction(game, player, new PlayerAction(PlayerAction.ActionType.CHI, game.getLastDiscardedTile()))) {
            return new PlayerAction(PlayerAction.ActionType.CHI, game.getLastDiscardedTile());
        }

        // 否则，随机出一张牌
        List<MahjongTile> handTiles = player.getHandTiles();
        MahjongTile tileToDiscard = findTileToDiscard(handTiles);
        return new PlayerAction(PlayerAction.ActionType.CHUPAI, tileToDiscard);
    }

    /**
     * 找到可以杠的牌
     * @param player 玩家
     * @return 可以杠的牌列表
     */
    private List<MahjongTile> findGangCandidates(MahjongPlayer player) {
        List<MahjongTile> candidates = new ArrayList<>();
        List<MahjongTile> handTiles = player.getHandTiles();

        // 检查是否有三张相同的牌
        for (int i = 0; i < handTiles.size() - 2; i++) {
            MahjongTile tile1 = handTiles.get(i);
            MahjongTile tile2 = handTiles.get(i + 1);
            MahjongTile tile3 = handTiles.get(i + 2);

            if (tile1.equals(tile2) && tile2.equals(tile3)) {
                candidates.add(tile1);
                i += 2; // 跳过接下来的两张牌
            }
        }

        return candidates;
    }

    /**
     * 找到要出的牌
     * @param handTiles 手牌
     * @return 要出的牌
     */
    private MahjongTile findTileToDiscard(List<MahjongTile> handTiles) {
        // 简单策略：优先打出风牌和箭牌，然后打出手牌中的孤张

        // 先尝试打风牌
        List<MahjongTile> fengTiles = new ArrayList<>();
        for (MahjongTile tile : handTiles) {
            if (tile.getType() == MahjongTile.TileType.FENG) {
                fengTiles.add(tile);
            }
        }
        if (!fengTiles.isEmpty()) {
            return fengTiles.get(random.nextInt(fengTiles.size()));
        }

        // 然后尝试打箭牌
        List<MahjongTile> jianTiles = new ArrayList<>();
        for (MahjongTile tile : handTiles) {
            if (tile.getType() == MahjongTile.TileType.JIAN) {
                jianTiles.add(tile);
            }
        }
        if (!jianTiles.isEmpty()) {
            return jianTiles.get(random.nextInt(jianTiles.size()));
        }

        // 最后随机打一张万子牌
        List<MahjongTile> wanTiles = new ArrayList<>();
        for (MahjongTile tile : handTiles) {
            if (tile.getType() == MahjongTile.TileType.WAN) {
                wanTiles.add(tile);
            }
        }
        if (!wanTiles.isEmpty()) {
            return wanTiles.get(random.nextInt(wanTiles.size()));
        }

        // 理论上不会走到这里
        return handTiles.get(random.nextInt(handTiles.size()));
    }
}