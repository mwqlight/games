package com.lgames.gamebackend.mahjong;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 麻将游戏服务
 */
@Service
public class MahjongGameService {
    private Map<String, MahjongGame> games = new HashMap<>();
    private MahjongGameInitializer gameInitializer = new MahjongGameInitializer();
    private MahjongActionValidator actionValidator = new MahjongActionValidator();
    private MahjongFanCalculator fanCalculator = new MahjongFanCalculator();
    private MahjongAIEngine aiEngine = new MahjongAIEngine();

    /**
     * 开始新游戏
     * @param playerName 玩家名称
     * @return 游戏状态
     */
    public MahjongGame startNewGame(String playerName) {
        MahjongGame game = gameInitializer.createNewGame(playerName);
        games.put(game.getGameId(), game);
        return game;
    }

    /**
     * 处理玩家动作
     * @param gameId 游戏ID
     * @param action 玩家动作
     * @return 游戏状态
     */
    public MahjongGame handlePlayerAction(String gameId, PlayerAction action) {
        MahjongGame game = games.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("游戏不存在");
        }

        MahjongPlayer currentPlayer = game.getCurrentTurnPlayer();
        if (currentPlayer == null) {
            throw new IllegalStateException("当前没有玩家回合");
        }

        // 校验动作合法性
        if (!actionValidator.validateAction(game, currentPlayer, action)) {
            throw new IllegalArgumentException("动作不合法");
        }

        // 执行动作
        executeAction(game, currentPlayer, action);

        // 如果是人类玩家，执行后需要触发AI动作
        if (currentPlayer.getType() == MahjongPlayer.PlayerType.HUMAN && 
            game.getGameStatus() == MahjongGame.GameStatus.PLAYING) {
            executeAIAction(game);
        }

        return game;
    }

    /**
     * 获取游戏状态
     * @param gameId 游戏ID
     * @return 游戏状态
     */
    public MahjongGame getGameState(String gameId) {
        MahjongGame game = games.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("游戏不存在");
        }
        return game;
    }

    /**
     * 执行玩家动作
     * @param game 游戏实例
     * @param player 玩家
     * @param action 动作
     */
    private void executeAction(MahjongGame game, MahjongPlayer player, PlayerAction action) {
        switch (action.getActionType()) {
            case CHUPAI:
                executeChupai(game, player, action.getTile());
                break;
            case CHI:
                executeChi(game, player, action.getTile());
                break;
            case PENG:
                executePeng(game, player, action.getTile());
                break;
            case GANG:
                executeGang(game, player, action.getTile());
                break;
            case HU:
                executeHu(game, player, action.getTile());
                break;
        }
    }

    /**
     * 执行出牌动作
     * @param game 游戏实例
     * @param player 玩家
     * @param tile 要出的牌
     */
    private void executeChupai(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        game.discardTile(player, tile);
    }

    /**
     * 执行吃牌动作
     * @param game 游戏实例
     * @param player 玩家
     * @param tile 要吃的牌
     */
    private void executeChi(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        // 从手牌中移除组成顺子的牌
        List<MahjongTile> handTiles = player.getHandTiles();
        int value = tile.getValue();

        // 找到需要移除的牌
        MahjongTile tile1 = null, tile2 = null;

        // 检查[value-1, value, value+1]
        if (value >= 2 && value <= 8) {
            tile1 = new MahjongTile(0, MahjongTile.TileType.WAN, value - 1);
            tile2 = new MahjongTile(0, MahjongTile.TileType.WAN, value + 1);
            if (handTiles.contains(tile1) && handTiles.contains(tile2)) {
                handTiles.remove(tile1);
                handTiles.remove(tile2);
            }
        }

        // 检查[value-2, value-1, value]
        if (tile1 == null && value >= 3) {
            tile1 = new MahjongTile(0, MahjongTile.TileType.WAN, value - 2);
            tile2 = new MahjongTile(0, MahjongTile.TileType.WAN, value - 1);
            if (handTiles.contains(tile1) && handTiles.contains(tile2)) {
                handTiles.remove(tile1);
                handTiles.remove(tile2);
            }
        }

        // 检查[value, value+1, value+2]
        if (tile1 == null && value <= 7) {
            tile1 = new MahjongTile(0, MahjongTile.TileType.WAN, value + 1);
            tile2 = new MahjongTile(0, MahjongTile.TileType.WAN, value + 2);
            if (handTiles.contains(tile1) && handTiles.contains(tile2)) {
                handTiles.remove(tile1);
                handTiles.remove(tile2);
            }
        }

        // 将吃的牌加入明牌
        player.addExposedTile(tile);
        if (tile1 != null) player.addExposedTile(tile1);
        if (tile2 != null) player.addExposedTile(tile2);

        // 移除牌池中的最后一张牌
        game.getDiscardPool().remove(game.getDiscardPool().size() - 1);

        // 切换回合
        game.switchTurn();
    }

    /**
     * 执行碰牌动作
     * @param game 游戏实例
     * @param player 玩家
     * @param tile 要碰的牌
     */
    private void executePeng(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        // 从手牌中移除两张相同的牌
        List<MahjongTile> handTiles = player.getHandTiles();
        int count = 0;
        for (int i = handTiles.size() - 1; i >= 0 && count < 2; i--) {
            if (handTiles.get(i).equals(tile)) {
                handTiles.remove(i);
                count++;
            }
        }

        // 将碰的牌加入明牌
        player.addExposedTile(tile);
        player.addExposedTile(tile);
        player.addExposedTile(tile);

        // 移除牌池中的最后一张牌
        game.getDiscardPool().remove(game.getDiscardPool().size() - 1);

        // 切换回合
        game.switchTurn();
    }

    /**
     * 执行杠牌动作
     * @param game 游戏实例
     * @param player 玩家
     * @param tile 要杠的牌
     */
    private void executeGang(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        // 从手牌中移除三张相同的牌
        List<MahjongTile> handTiles = player.getHandTiles();
        int count = 0;
        for (int i = handTiles.size() - 1; i >= 0 && count < 3; i--) {
            if (handTiles.get(i).equals(tile)) {
                handTiles.remove(i);
                count++;
            }
        }

        // 将杠的牌加入明牌
        player.addExposedTile(tile);
        player.addExposedTile(tile);
        player.addExposedTile(tile);
        player.addExposedTile(tile);

        // 如果是明杠，移除牌池中的最后一张牌
        if (game.getLastDiscardedTile() == tile) {
            game.getDiscardPool().remove(game.getDiscardPool().size() - 1);
        }

        // 杠牌后补牌
        MahjongTile drawnTile = game.drawTile();
        if (drawnTile != null) {
            player.addHandTile(drawnTile);
        }

        // 杠牌后仍为当前玩家回合
    }

    /**
     * 执行胡牌动作
     * @param game 游戏实例
     * @param player 玩家
     * @param tile 胡的牌
     */
    private void executeHu(MahjongGame game, MahjongPlayer player, MahjongTile tile) {
        // 计算番数
        int fan = fanCalculator.calculateFan(game, player, tile);

        // 计算得分（简单实现：番数乘以基础分）
        int score = fan * 10;

        // 更新玩家得分
        player.setScore(player.getScore() + score);

        // 设置游戏状态为已胡牌
        game.setGameStatus(MahjongGame.GameStatus.HU);
    }

    /**
     * 执行AI动作
     * @param game 游戏实例
     */
    private void executeAIAction(MahjongGame game) {
        MahjongPlayer aiPlayer = game.getCurrentTurnPlayer();
        if (aiPlayer.getType() != MahjongPlayer.PlayerType.AI) {
            return;
        }

        // AI做出决策
        PlayerAction action = aiEngine.makeDecision(game, aiPlayer);

        // 执行AI动作
        executeAction(game, aiPlayer, action);
    }
}