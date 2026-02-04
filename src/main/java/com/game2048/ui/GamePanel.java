package com.game2048.ui;

import com.game2048.config.Config;
import com.game2048.model.GameEngine;
import com.game2048.model.Tile;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GamePanel extends JPanel {

    private GameEngine engine;
    private Timer animationTimer;
    private Clip mergeSoundClip;

    public GamePanel() {
        engine = new GameEngine();
        loadSound();
        engine.setMergeSoundCallback(this::playMergeSound);

        setPreferredSize(new Dimension(Config.WIDTH, Config.HEIGHT));
        setBackground(new Color(0xbbada0));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 如果正在移动中，不允许新的输入，防止动画错乱
                if (!engine.areAnimationsDone()) return;
                handleInput(e);
            }
        });

        // 60 FPS 动画循环
        animationTimer = new Timer(16, e -> {
            engine.updateAnimations();
            repaint();
        });
        animationTimer.start();
    }

    private void loadSound() {
        try {
            // 请确保 resources 目录下有 merge.wav
            URL soundURL = getClass().getResource("/merge.wav");
            if (soundURL != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
                mergeSoundClip = AudioSystem.getClip();
                mergeSoundClip.open(audioIn);
            }
        } catch (Exception e) {
            System.err.println("音效加载失败，但这不影响游戏运行");
        }
    }

    private void playMergeSound() {
        if (mergeSoundClip != null) {
            mergeSoundClip.setFramePosition(0);
            mergeSoundClip.start();
        }
    }

    private void handleInput(KeyEvent e) {
        if (engine.isGameStopped) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                engine.createGame();
            }
            return;
        }

        if (!engine.canUserMove()) {
            engine.isGameStopped = true;
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:  engine.moveLeft(); break;
            case KeyEvent.VK_RIGHT: engine.moveRight(); break;
            case KeyEvent.VK_UP:    engine.moveUp(); break;
            case KeyEvent.VK_DOWN:  engine.moveDown(); break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawHeader(g2);
        g2.translate(0, Config.HEADER_HEIGHT);

        // 1. 先画底层的空格子
        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 0; j < Config.SIDE; j++) {
                drawEmptyTile(g2, j, i);
            }
        }

        // 2. 复制一份方块列表用于排序，以免影响逻辑层
        java.util.List<Tile> renderList = new ArrayList<>(engine.tiles);

        // 3. 根据 Z-Index 排序：Z 值小的在下面，大的在上面（后画）
        renderList.sort(Comparator.comparingInt(Tile::getZIndex));

        // 4. 绘制所有方块
        for (Tile t : renderList) {
            t.draw(g2);
        }

        if (engine.isGameStopped) {
            drawGameOver(g2);
        }
    }

    private void drawEmptyTile(Graphics2D g, int x, int y) {
        int xPos = x * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        int yPos = y * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        g.setColor(new Color(0xcdc1b5));
        g.fillRoundRect(xPos, yPos, Config.TILE_SIZE, Config.TILE_SIZE, 14, 14);
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

        g.setFont(new Font(Config.FONT_NAME, Font.PLAIN, 18));
        String subMsg = "Press SPACE to Restart";
        g.drawString(subMsg, (getWidth() - g.getFontMetrics().stringWidth(subMsg)) / 2, getHeight() / 2 + 40);
    }
}