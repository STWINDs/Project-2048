package com.game2048.ui;

import com.game2048.config.Config;
import com.game2048.model.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {

    // [新增] 持有逻辑层的引用
    private GameEngine engine;

    public GamePanel() {
        // 初始化逻辑引擎
        engine = new GameEngine();

        // 窗口设置
        setPreferredSize(new Dimension(Config.WIDTH, Config.HEIGHT));
        setBackground(new Color(0xbbada0));
        setFocusable(true);

        // [新增] 键盘监听器 (替代原来的 onKeyPress)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });
    }

    // 处理输入的逻辑分发
    private void handleInput(KeyEvent e) {
        if (engine.isGameStopped) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                engine.createGame();
                repaint(); // 重绘界面
            }
            return;
        }

        if (!engine.canUserMove()) {
            engine.isGameStopped = true;
            repaint();
            return;
        }

        boolean moved = false;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:  moved = engine.moveLeft(); break;
            case KeyEvent.VK_RIGHT: moved = engine.moveRight(); break;
            case KeyEvent.VK_UP:    moved = engine.moveUp(); break;
            case KeyEvent.VK_DOWN:  moved = engine.moveDown(); break;
        }

        if (moved) {
            repaint(); // 只有数据变了才重绘，性能更优
        }
    }

    // [保留] 类似于原来的 drawScene，但使用了 Graphics2D
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 开启抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawHeader(g2);

        // 偏移坐标系，留出头部空间
        g2.translate(0, Config.HEADER_HEIGHT);

        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 0; j < Config.SIDE; j++) {
                drawTile(g2, engine.gameField[i][j], j, i);
            }
        }

        if (engine.isGameStopped) {
            drawGameOver(g2);
        }
    }

    private void drawTile(Graphics2D g, int value, int x, int y) {
        int xPos = x * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        int yPos = y * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;

        g.setColor(Config.getColorByValue(value));
        g.fillRoundRect(xPos, yPos, Config.TILE_SIZE, Config.TILE_SIZE, 14, 14);

        if (value > 0) {
            g.setColor(value < 8 ? new Color(0x776e65) : new Color(0xf9f6f2));
            int fontSize = value < 100 ? 36 : (value < 1000 ? 32 : 24);
            g.setFont(new Font(Config.FONT_NAME, Font.BOLD, fontSize));

            String s = String.valueOf(value);
            FontMetrics fm = g.getFontMetrics();
            int txtX = xPos + (Config.TILE_SIZE - fm.stringWidth(s)) / 2;
            int txtY = yPos + ((Config.TILE_SIZE - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(s, txtX, txtY);
        }
    }

    private void drawHeader(Graphics2D g) {
        g.setColor(new Color(0xbbada0));
        g.fillRect(0, 0, getWidth(), Config.HEADER_HEIGHT);
        g.setColor(new Color(0xf9f6f2));
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, 18));
        g.drawString("Score: " + engine.score, 20, 35);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 150));
        g.fillRoundRect(0, 0, getWidth(), getHeight() - Config.HEADER_HEIGHT, 0, 0);
        g.setColor(new Color(0x776e65));
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, 48));
        String msg = engine.isWon ? "You Win!" : "Game Over";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
}