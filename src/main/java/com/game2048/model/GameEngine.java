package com.game2048.model;

import com.game2048.config.Config;
import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    public List<Tile> tiles = new ArrayList<>();
    public int score = 0;
    public boolean isGameStopped = false;
    public boolean isWon = false;

    private Runnable mergeSoundCallback;

    public GameEngine() {
        createGame();
    }

    public void setMergeSoundCallback(Runnable callback) {
        this.mergeSoundCallback = callback;
    }

    public void createGame() {
        tiles.clear();
        score = 0;
        isGameStopped = false;
        isWon = false;
        createNewNumber();
        createNewNumber();
    }

    public void createNewNumber() {
        if (getMaxTileValue() == 2048 && !isWon) isWon = true;

        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 0; j < Config.SIDE; j++) {
                if (getTileAt(i, j) == null) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (emptyCells.isEmpty()) return;

        int[] p = emptyCells.get((int) (Math.random() * emptyCells.size()));
        int value = Math.random() < 0.9 ? 2 : 4;

        Tile newTile = new Tile(value, p[0], p[1]);
        newTile.setNew();
        tiles.add(newTile);
    }

    private Tile getTileAt(int row, int col) {
        for (Tile t : tiles) {
            if (t.getRow() == row && t.getCol() == col) {
                return t;
            }
        }
        return null;
    }

    public void updateAnimations() {
        for (Tile t : tiles) {
            t.update();
        }
    }

    public boolean areAnimationsDone() {
        for (Tile t : tiles) {
            if (!t.isAnimationDone()) return false;
        }
        return true;
    }

    // --- ⬇️ 全方向移动逻辑 (核心修改) ⬇️ ---

    // 1. 向左移动
    public boolean moveLeft() {
        boolean isChanged = false;
        List<Tile> mergedTiles = new ArrayList<>(); // 防止单次移动中连续合并

        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 1; j < Config.SIDE; j++) { // 从左数第二列开始
                Tile current = getTileAt(i, j);
                if (current == null) continue;

                int targetCol = j;
                // 向左扫描
                for (int k = j - 1; k >= 0; k--) {
                    Tile prev = getTileAt(i, k);
                    if (prev == null) {
                        targetCol = k; // 空位，继续向左看
                    } else if (prev.getValue() == current.getValue() && !mergedTiles.contains(prev)) {
                        // 合并逻辑
                        performMerge(current, prev, mergedTiles);
                        isChanged = true;
                        targetCol = -1; // 标记为已处理
                        break;
                    } else {
                        break; // 遇到障碍且不能合并
                    }
                }

                // 移动逻辑 (如果没有合并)
                if (targetCol != -1 && targetCol != j) {
                    current.setPosition(i, targetCol);
                    isChanged = true;
                }
            }
        }
        if (isChanged) createNewNumber();
        return isChanged;
    }

    // 2. 向右移动
    public boolean moveRight() {
        boolean isChanged = false;
        List<Tile> mergedTiles = new ArrayList<>();

        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = Config.SIDE - 2; j >= 0; j--) { // 从右数第二列开始，向左遍历
                Tile current = getTileAt(i, j);
                if (current == null) continue;

                int targetCol = j;
                // 向右扫描
                for (int k = j + 1; k < Config.SIDE; k++) {
                    Tile next = getTileAt(i, k);
                    if (next == null) {
                        targetCol = k;
                    } else if (next.getValue() == current.getValue() && !mergedTiles.contains(next)) {
                        performMerge(current, next, mergedTiles);
                        isChanged = true;
                        targetCol = -1;
                        break;
                    } else {
                        break;
                    }
                }

                if (targetCol != -1 && targetCol != j) {
                    current.setPosition(i, targetCol);
                    isChanged = true;
                }
            }
        }
        if (isChanged) createNewNumber();
        return isChanged;
    }

    // 3. 向上移动
    public boolean moveUp() {
        boolean isChanged = false;
        List<Tile> mergedTiles = new ArrayList<>();

        for (int j = 0; j < Config.SIDE; j++) { // 遍历列
            for (int i = 1; i < Config.SIDE; i++) { // 从第二行开始向下遍历
                Tile current = getTileAt(i, j);
                if (current == null) continue;

                int targetRow = i;
                // 向上扫描
                for (int k = i - 1; k >= 0; k--) {
                    Tile prev = getTileAt(k, j);
                    if (prev == null) {
                        targetRow = k;
                    } else if (prev.getValue() == current.getValue() && !mergedTiles.contains(prev)) {
                        performMerge(current, prev, mergedTiles);
                        isChanged = true;
                        targetRow = -1;
                        break;
                    } else {
                        break;
                    }
                }

                if (targetRow != -1 && targetRow != i) {
                    current.setPosition(targetRow, j);
                    isChanged = true;
                }
            }
        }
        if (isChanged) createNewNumber();
        return isChanged;
    }

    // 4. 向下移动
    public boolean moveDown() {
        boolean isChanged = false;
        List<Tile> mergedTiles = new ArrayList<>();

        for (int j = 0; j < Config.SIDE; j++) {
            for (int i = Config.SIDE - 2; i >= 0; i--) { // 从倒数第二行开始向上遍历
                Tile current = getTileAt(i, j);
                if (current == null) continue;

                int targetRow = i;
                // 向下扫描
                for (int k = i + 1; k < Config.SIDE; k++) {
                    Tile next = getTileAt(k, j);
                    if (next == null) {
                        targetRow = k;
                    } else if (next.getValue() == current.getValue() && !mergedTiles.contains(next)) {
                        performMerge(current, next, mergedTiles);
                        isChanged = true;
                        targetRow = -1;
                        break;
                    } else {
                        break;
                    }
                }

                if (targetRow != -1 && targetRow != i) {
                    current.setPosition(targetRow, j);
                    isChanged = true;
                }
            }
        }
        if (isChanged) createNewNumber();
        return isChanged;
    }

    // 提取公共的合并逻辑，避免代码重复
    private void performMerge(Tile current, Tile target, List<Tile> mergedTiles) {
        target.setValue(target.getValue() * 2);
        score += target.getValue();
        target.setMerging();
        mergedTiles.add(target);

        // current 移动到 target 的位置，然后消失
        current.setPosition(target.getRow(), target.getCol());
        tiles.remove(current);

        if (mergeSoundCallback != null) mergeSoundCallback.run();
    }

    public int getMaxTileValue() {
        int max = 0;
        for (Tile t : tiles) max = Math.max(max, t.getValue());
        return max;
    }

    public boolean canUserMove() {
        if (tiles.size() < Config.SIDE * Config.SIDE) return true;
        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 0; j < Config.SIDE; j++) {
                Tile t = getTileAt(i, j);
                if (t == null) continue;
                Tile right = getTileAt(i, j + 1);
                Tile down = getTileAt(i + 1, j);
                if (right != null && right.getValue() == t.getValue()) return true;
                if (down != null && down.getValue() == t.getValue()) return true;
            }
        }
        return false;
    }
}