package com.lgames.gamebackend.mahjong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 麻将游戏初始化服务
 */
public class MahjongGameInitializer {
    private static final Random random = new Random();

    /**
     * 创建新的麻将游戏
     * @param playerName 玩家名称
     * @return 麻将游戏实例
     */
    public MahjongGame createNewGame(String playerName) {
        // 创建玩家
        MahjongPlayer humanPlayer = new MahjongPlayer("player1", playerName, MahjongPlayer.PlayerType.HUMAN);
        MahjongPlayer aiPlayer = new MahjongPlayer("player2", "电脑", MahjongPlayer.PlayerType.AI);

        // 创建游戏
        MahjongGame game = new MahjongGame(humanPlayer, aiPlayer);

        // 初始化牌墙
        initializeTileWall(game);

        // 洗牌
        shuffleTileWall(game);

        // 决定庄家
        determineBanker(game);

        // 发牌
        dealTiles(game);

        return game;
    }

    /**
     * 初始化牌墙
     * @param game 麻将游戏实例
     */
    private void initializeTileWall(MahjongGame game) {
        List<MahjongTile> tileWall = new ArrayList<>();
        int id = 1;

        // 添加万子牌（一至九万，各4张）
        for (int value = 1; value <= 9; value++) {
            for (int i = 0; i < 4; i++) {
                tileWall.add(new MahjongTile(id++, MahjongTile.TileType.WAN, value));
            }
        }

        // 添加风牌（东、南、西、北，各4张）
        for (int value = MahjongTile.FENG_DONG; value <= MahjongTile.FENG_BEI; value++) {
            for (int i = 0; i < 4; i++) {
                tileWall.add(new MahjongTile(id++, MahjongTile.TileType.FENG, value));
            }
        }

        // 添加箭牌（中、发、白，各4张）
        for (int value = MahjongTile.JIAN_ZHONG; value <= MahjongTile.JIAN_BAI; value++) {
            for (int i = 0; i < 4; i++) {
                tileWall.add(new MahjongTile(id++, MahjongTile.TileType.JIAN, value));
            }
        }

        // 添加花牌（8张）
        for (int value = MahjongTile.HUA_CHUN; value <= MahjongTile.HUA_JU; value++) {
            tileWall.add(new MahjongTile(id++, MahjongTile.TileType.HUA, value));
        }

        game.setTileWall(tileWall);
    }

    /**
     * 洗牌
     * @param game 麻将游戏实例
     */
    private void shuffleTileWall(MahjongGame game) {
        Collections.shuffle(game.getTileWall(), random);
    }

    /**
     * 决定庄家
     * @param game 麻将游戏实例
     */
    private void determineBanker(MahjongGame game) {
        // 简单随机决定庄家
        MahjongPlayer banker = random.nextBoolean() ? game.getPlayer1() : game.getPlayer2();
        banker.setBanker(true);
        game.setCurrentTurnPlayer(banker);
        banker.setCurrentTurn(true);
    }

    /**
     * 发牌
     * @param game 麻将游戏实例
     */
    private void dealTiles(MahjongGame game) {
        MahjongPlayer banker = game.getPlayer1().isBanker() ? game.getPlayer1() : game.getPlayer2();
        MahjongPlayer nonBanker = game.getOtherPlayer(banker);

        // 庄家发14张牌，闲家发13张牌
        for (int i = 0; i < 13; i++) {
            banker.addHandTile(game.drawTile());
            nonBanker.addHandTile(game.drawTile());
        }
        banker.addHandTile(game.drawTile());

        // 对手牌进行排序
        sortHandTiles(banker);
        sortHandTiles(nonBanker);
    }

    /**
     * 对手牌进行排序
     * @param player 玩家
     */
    private void sortHandTiles(MahjongPlayer player) {
        List<MahjongTile> handTiles = player.getHandTiles();
        Collections.sort(handTiles, (tile1, tile2) -> {
            // 先按类型排序
            int typeCompare = tile1.getType().compareTo(tile2.getType());
            if (typeCompare != 0) {
                return typeCompare;
            }
            // 同类型按数值排序
            return Integer.compare(tile1.getValue(), tile2.getValue());
        });
    }
}