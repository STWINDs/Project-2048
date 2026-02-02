package com.game2048.model;

import com.game2048.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point; // 仅用于坐标计算

public class GameEngine {
    // --- 核心数据 ---
    // [保留] 变量名 gameField
    public int[][] gameField;
    // [保留] 变量名 score
    public int score = 0;

    // [新增] 用于状态管理的变量
    public boolean isGameStopped = false;
    public boolean isWon = false;

    public GameEngine() {
        createGame();
    }

    // [保留] 核心初始化逻辑
    public void createGame() {
        gameField = new int[Config.SIDE][Config.SIDE];
        score = 0;
        isGameStopped = false;
        isWon = false;
        createNewNumber();
        createNewNumber();
    }

    // [保留] 生成新数字逻辑 (稍微优化了寻找空位的算法，逻辑不变)
    public void createNewNumber() {
        if (getMaxTileValue() == 2048 && !isWon) {
            isWon = true;
        }

        List<Point> emptyCells = new ArrayList<>();
        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 0; j < Config.SIDE; j++) {
                if (gameField[i][j] == 0) {
                    emptyCells.add(new Point(i, j));
                }
            }
        }

        if (emptyCells.isEmpty()) return;

        Point p = emptyCells.get((int) (Math.random() * emptyCells.size()));
        // [保留] 10% 概率生成 4
        gameField[p.x][p.y] = Math.random() < 0.9 ? 2 : 4;
    }

    // --- 移动逻辑 (完全保留原函数名) ---

    public boolean moveLeft() {
        boolean isChanged = false;
        for (int i = 0; i < Config.SIDE; i++) {
            if (compressRow(gameField[i])) isChanged = true;
            if (mergeRow(gameField[i])) isChanged = true;
            if (compressRow(gameField[i])) isChanged = true;
        }
        if (isChanged) {
            createNewNumber();
        }
        return isChanged;
    }

    public boolean moveRight() {
        rotateClockwise();
        rotateClockwise();
        boolean moved = moveLeft();
        rotateClockwise();
        rotateClockwise();
        return moved;
    }

    public boolean moveUp() {
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
        boolean moved = moveLeft();
        rotateClockwise();
        return moved;
    }

    public boolean moveDown() {
        rotateClockwise();
        boolean moved = moveLeft();
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
        return moved;
    }

    // [保留] 辅助算法
    private boolean compressRow(int[] row) {
        int tempIndex = 0;
        boolean changed = false;
        for (int i = 0; i < row.length; i++) {
            if (row[i] != 0) {
                if (i != tempIndex) {
                    row[tempIndex] = row[i];
                    row[i] = 0;
                    changed = true;
                }
                tempIndex++;
            }
        }
        return changed;
    }

    // [保留] 合并算法
    private boolean mergeRow(int[] row) {
        boolean changed = false;
        for (int i = 0; i < row.length - 1; i++) {
            if (row[i] != 0 && row[i] == row[i + 1]) {
                row[i] *= 2;
                score += row[i]; // [保留] 分数计算
                row[i + 1] = 0;
                i++;
                changed = true;
            }
        }
        return changed;
    }

    // [保留] 旋转矩阵
    private void rotateClockwise() {
        int[][] tempField = new int[Config.SIDE][Config.SIDE];
        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 0; j < Config.SIDE; j++) {
                tempField[j][Config.SIDE - 1 - i] = gameField[i][j];
            }
        }
        gameField = tempField;
    }

    // [保留] 获取最大值
    public int getMaxTileValue() {
        int max = 0;
        for (int[] row : gameField) {
            for (int val : row) {
                max = Math.max(max, val);
            }
        }
        return max;
    }

    // [保留] 检查是否可移动
    public boolean canUserMove() {
        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 0; j < Config.SIDE; j++) {
                if (gameField[i][j] == 0) return true;
                if (j < Config.SIDE - 1 && gameField[i][j] == gameField[i][j + 1]) return true;
                if (i < Config.SIDE - 1 && gameField[i][j] == gameField[i + 1][j]) return true;
            }
        }
        return false;
    }
}