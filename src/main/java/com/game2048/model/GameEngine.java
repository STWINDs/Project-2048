package com.game2048.model;

import com.game2048.config.Config;
import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    public List<Tile> tiles = new ArrayList<>();
    public int score = 0;
    public boolean isGameStopped = false;
    public boolean isWon = false;

    // 控制是否需要在动画结束后生成新方块
    private boolean needNewTile = false;

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
        needNewTile = false;
        // 初始生成两个
        spawnNewTileImmediately();
        spawnNewTileImmediately();
    }

    // 内部私有方法，直接生成
    private void spawnNewTileImmediately() {
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
            if (t.getRow() == row && t.getCol() == col) return t;
        }
        return null;
    }

    // --- 每一帧调用的逻辑 ---
    public void updateAnimations() {
        boolean allFinished = true;
        for (Tile t : tiles) {
            t.update();
            if (!t.isAnimationDone()) {
                allFinished = false;
            }
        }

        // 只有当所有方块静止，且有生成需求时，才生成新数字
        if (allFinished && needNewTile) {
            spawnNewTileImmediately();
            needNewTile = false;
        }
    }

    public boolean areAnimationsDone() {
        for (Tile t : tiles) {
            if (!t.isAnimationDone()) return false;
        }
        return true;
    }

    // --- 核心移动逻辑 (触发 needNewTile 而不是直接生成) ---

    public boolean moveLeft() {
        boolean isChanged = false;
        List<Tile> mergedTiles = new ArrayList<>();

        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 1; j < Config.SIDE; j++) {
                Tile current = getTileAt(i, j);
                if (current == null) continue;

                int targetCol = j;
                for (int k = j - 1; k >= 0; k--) {
                    Tile prev = getTileAt(i, k);
                    if (prev == null) {
                        targetCol = k;
                    } else if (prev.getValue() == current.getValue() && !mergedTiles.contains(prev)) {
                        performMerge(current, prev, mergedTiles);
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
        if (isChanged) needNewTile = true; // 标记需要新方块
        return isChanged;
    }

    public boolean moveRight() {
        boolean isChanged = false;
        List<Tile> mergedTiles = new ArrayList<>();
        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = Config.SIDE - 2; j >= 0; j--) {
                Tile current = getTileAt(i, j);
                if (current == null) continue;
                int targetCol = j;
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
        if (isChanged) needNewTile = true;
        return isChanged;
    }

    public boolean moveUp() {
        boolean isChanged = false;
        List<Tile> mergedTiles = new ArrayList<>();
        for (int j = 0; j < Config.SIDE; j++) {
            for (int i = 1; i < Config.SIDE; i++) {
                Tile current = getTileAt(i, j);
                if (current == null) continue;
                int targetRow = i;
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
        if (isChanged) needNewTile = true;
        return isChanged;
    }

    public boolean moveDown() {
        boolean isChanged = false;
        List<Tile> mergedTiles = new ArrayList<>();
        for (int j = 0; j < Config.SIDE; j++) {
            for (int i = Config.SIDE - 2; i >= 0; i--) {
                Tile current = getTileAt(i, j);
                if (current == null) continue;
                int targetRow = i;
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
        if (isChanged) needNewTile = true;
        return isChanged;
    }

    private void performMerge(Tile current, Tile target, List<Tile> mergedTiles) {
        target.setValue(target.getValue() * 2);
        score += target.getValue();
        target.setMerging();
        mergedTiles.add(target);

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