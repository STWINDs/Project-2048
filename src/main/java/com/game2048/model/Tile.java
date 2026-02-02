package com.game2048.model;

import com.game2048.config.Config;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class Tile {
    private int value;
    // 逻辑坐标 (网格中的行列)
    private int row;
    private int col;

    // 动画相关的属性
    private double currentX, currentY; // 当前绘制的像素坐标
    private int targetX, targetY;       // 目标像素坐标
    private double scale = 1.0;         // 缩放比例 (1.0 为正常大小)

    private boolean isNew = false;      // 是否是新生成的方块
    private boolean isMerging = false;  // 是否正在进行合并动画

    // 动画速度配置
    private static final double MOVE_SPEED = 0.2; // 移动插值速度
    private static final double SCALE_SPEED = 0.2; // 缩放速度
    private static final double MAX_SCALE = 1.2;  // 合并时的最大放大比例

    public Tile(int value, int row, int col) {
        this.value = value;
        this.row = row;
        this.col = col;

        // 计算目标像素坐标
        this.targetX = col * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        this.targetY = row * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;

        // 初始像素坐标等于目标坐标
        this.currentX = this.targetX;
        this.currentY = this.targetY;
    }

    // --- Getters & Setters ---
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    // 设置新位置 (用于移动逻辑)
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        this.targetX = col * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        this.targetY = row * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
    }

    // 标记为新生成的方块 (触发从无到有动画)
    public void setNew() {
        this.isNew = true;
        this.scale = 0.1; // 初始大小很小
    }

    // 标记为合并方块 (触发变大动画)
    public void setMerging() {
        this.isMerging = true;
        this.scale = 1.0;
    }

    // 检查动画是否全部完成
    public boolean isAnimationDone() {
        return Math.abs(currentX - targetX) < 1 &&
                Math.abs(currentY - targetY) < 1 &&
                scale == 1.0 && !isNew && !isMerging;
    }

    // --- 核心动画更新逻辑 (每一帧调用) ---
    public void update() {
        // 1. 平滑移动动画 (使用简单的线性插值)
        if (Math.abs(currentX - targetX) > 1) {
            currentX += (targetX - currentX) * MOVE_SPEED;
        } else {
            currentX = targetX; // 防止抖动，直接到位
        }

        if (Math.abs(currentY - targetY) > 1) {
            currentY += (targetY - currentY) * MOVE_SPEED;
        } else {
            currentY = targetY;
        }

        // 2. 新方块出现动画 (从 0.1 变大到 1.0)
        if (isNew) {
            if (scale < 1.0) {
                scale += SCALE_SPEED;
                if (scale >= 1.0) {
                    scale = 1.0;
                    isNew = false; // 动画结束
                }
            }
        }
        // 3. 合并方块动画 (先变大到 MAX_SCALE，再恢复到 1.0)
        else if (isMerging) {
            if (scale < MAX_SCALE) {
                scale += SCALE_SPEED;
            } else {
                // 达到最大后，开始恢复
                scale -= SCALE_SPEED;
                if (scale <= 1.0) {
                    scale = 1.0;
                    isMerging = false; // 动画结束
                }
            }
        }
    }

    // --- 自我绘制方法 ---
    public void draw(Graphics2D g) {
        if (value == 0) return;

        // 【修正点 1】正确保存当前的绘图状态
        // 我们需要一个变量来记住现在的状态是什么
        java.awt.geom.AffineTransform oldTransform = g.getTransform();

        // 1. 移动到方块中心
        double centerX = currentX + Config.TILE_SIZE / 2.0;
        double centerY = currentY + Config.TILE_SIZE / 2.0;
        g.translate(centerX, centerY);

        // 2. 应用缩放
        g.scale(scale, scale);

        // 3. 移回左上角进行绘制 (相对于中心点)
        g.translate(-Config.TILE_SIZE / 2.0, -Config.TILE_SIZE / 2.0);

        // --- 绘制方块背景 ---
        g.setColor(Config.getColorByValue(value));
        g.fillRoundRect(0, 0, Config.TILE_SIZE, Config.TILE_SIZE, 14, 14);

        // --- 绘制数字 ---
        g.setColor(value < 8 ? new Color(0x776e65) : new Color(0xf9f6f2));
        int fontSize = value < 100 ? 36 : (value < 1000 ? 32 : 24);
        Font font = new Font(Config.FONT_NAME, Font.BOLD, fontSize);
        g.setFont(font);

        String s = String.valueOf(value);
        FontMetrics fm = g.getFontMetrics();
        int txtX = (Config.TILE_SIZE - fm.stringWidth(s)) / 2;
        int txtY = ((Config.TILE_SIZE - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(s, txtX, txtY);

        // 【修正点 2】正确恢复之前的绘图状态
        // 把刚才保存的状态设置回去，确保不影响下一个方块的绘制
        g.setTransform(oldTransform);
    }
}