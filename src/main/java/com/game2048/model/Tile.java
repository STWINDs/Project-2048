package com.game2048.model;

import com.game2048.config.Config;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Tile {
    private int value;
    private int row;
    private int col;

    // 动画相关
    private double currentX, currentY;
    private int targetX, targetY;
    private double scale = 1.0;

    private boolean isNew = false;
    private boolean isMerging = false;
    private boolean reachedMax = false; // 合并动画是否到达最大值

    // 动画参数
    private static final double MAX_SCALE = 1.2;
    private static final double SCALE_SPEED = 0.1;

    public Tile(int value, int row, int col) {
        this.value = value;
        this.row = row;
        this.col = col;
        this.targetX = col * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        this.targetY = row * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        this.currentX = targetX;
        this.currentY = targetY;
    }

    // --- Getters & Setters ---
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        this.targetX = col * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        this.targetY = row * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
    }

    public void setNew() {
        this.isNew = true;
        this.scale = 0.1;
    }

    public void setMerging() {
        this.isMerging = true;
        this.scale = 1.0;
        this.reachedMax = false;
    }

    public boolean isAnimationDone() {
        return Math.abs(currentX - targetX) < 1.0 &&
                Math.abs(currentY - targetY) < 1.0 &&
                scale == 1.0 && !isNew && !isMerging;
    }

    // --- 核心：图层权重 (Z-Index) ---
    // 数字越大，画在越上面
    public int getZIndex() {
        if (isMerging) return 100; // 合并中最优先
        if (Math.abs(currentX - targetX) > 2 || Math.abs(currentY - targetY) > 2) return 50; // 移动中次之
        if (isNew) return 0; // 新生的在最底下
        return 10; // 普通静止方块
    }

    // --- 核心：物理更新 ---
    public void update() {
        // 1. 移动动画：指数衰减 (Ease-Out)
        // 距离越远飞得越快，越近越慢，产生磁吸感
        double dx = targetX - currentX;
        double dy = targetY - currentY;

        currentX += dx * 0.35;
        currentY += dy * 0.35;

        if (Math.abs(dx) < 1.0) currentX = targetX;
        if (Math.abs(dy) < 1.0) currentY = targetY;

        // 2. 新生动画
        if (isNew) {
            if (scale < 1.0) {
                scale += 0.15;
                if (scale >= 1.0) {
                    scale = 1.0;
                    isNew = false;
                }
            }
        }
        // 3. 合并动画 (变大再弹回)
        else if (isMerging) {
            if (!reachedMax) {
                scale += SCALE_SPEED * 1.5;
                if (scale >= MAX_SCALE) reachedMax = true;
            } else {
                scale -= SCALE_SPEED;
                if (scale <= 1.0) {
                    scale = 1.0;
                    isMerging = false;
                    reachedMax = false;
                }
            }
        }
    }

    // --- 核心：绘制 ---
    public void draw(Graphics2D g) {
        if (value == 0) return;

        // 1. 保存旧的坐标系状态 (修复 bug 的关键)
        AffineTransform oldTransform = g.getTransform();

        // 2. 变换坐标系到方块中心
        double centerX = currentX + Config.TILE_SIZE / 2.0;
        double centerY = currentY + Config.TILE_SIZE / 2.0;
        g.translate(centerX, centerY);
        g.scale(scale, scale);
        g.translate(-Config.TILE_SIZE / 2.0, -Config.TILE_SIZE / 2.0);

        // 3. 绘制内容
        g.setColor(Config.getColorByValue(value));
        g.fillRoundRect(0, 0, Config.TILE_SIZE, Config.TILE_SIZE, 14, 14);

        g.setColor(value < 8 ? new Color(0x776e65) : new Color(0xf9f6f2));
        int fontSize = value < 100 ? 36 : (value < 1000 ? 32 : 24);
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, fontSize));

        String s = String.valueOf(value);
        FontMetrics fm = g.getFontMetrics();
        int txtX = (Config.TILE_SIZE - fm.stringWidth(s)) / 2;
        int txtY = ((Config.TILE_SIZE - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(s, txtX, txtY);

        // 4. 恢复坐标系 (修复 bug 的关键)
        g.setTransform(oldTransform);
    }
}