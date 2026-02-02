package com.game2048.config;

import java.awt.Color;

public class Config {
    // 保持原有变量名
    public static final int SIDE = 4;

    // UI 相关配置 (新增)
    public static final int TILE_SIZE = 100;
    public static final int MARGIN = 15;
    public static final int HEADER_HEIGHT = 50;
    public static final String FONT_NAME = "Arial";
    public static final int WIDTH = (TILE_SIZE + MARGIN) * SIDE + MARGIN;
    public static final int HEIGHT = WIDTH + HEADER_HEIGHT;

    // 颜色工具方法 (保留你原来的逻辑，提取为静态方法)
    public static Color getColorByValue(int value) {
        switch (value) {
            case 0:    return new Color(0xcdc1b5);
            case 2:    return new Color(0xeee4da);
            case 4:    return new Color(0xede0c8);
            case 8:    return new Color(0xf2b179);
            case 16:   return new Color(0xf59563);
            case 32:   return new Color(0xf67c5f);
            case 64:   return new Color(0xf65e3b);
            case 128:  return new Color(0xedcf72);
            case 256:  return new Color(0xedcc61);
            case 512:  return new Color(0xedc850);
            case 1024: return new Color(0xedc53f);
            case 2048: return new Color(0xedc22e);
            default:   return new Color(0x3c3a32);
        }
    }
}