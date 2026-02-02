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

public class GamePanel extends JPanel {

    private GameEngine engine;
    private Timer animationTimer; // 动画定时器
    private Clip mergeSoundClip;  // 音效片段

    public GamePanel() {
        engine = new GameEngine();

        // 加载音效
        loadSound();
        // 设置引擎的音效回调
        engine.setMergeSoundCallback(this::playMergeSound);

        setPreferredSize(new Dimension(Config.WIDTH, Config.HEIGHT));
        setBackground(new Color(0xbbada0));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 只有在动画全部结束时才响应新的按键输入
                if (!engine.areAnimationsDone()) return;
                handleInput(e);
            }
        });

        // --- 启动动画循环 ---
        // 每 16ms 触发一次 (约 60 FPS)
        animationTimer = new Timer(16, e -> {
            // 1. 更新所有方块的动画状态
            engine.updateAnimations();
            // 2. 重绘界面
            repaint();
        });
        animationTimer.start();
    }

    private void loadSound() {
        try {
            // 假设你有一个 merge.wav 文件在 resources 文件夹下
            URL soundURL = getClass().getResource("/merge.wav");
            if (soundURL == null) {
                System.err.println("未找到音效文件: /merge.wav");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            mergeSoundClip = AudioSystem.getClip();
            mergeSoundClip.open(audioIn);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playMergeSound() {
        if (mergeSoundClip != null) {
            mergeSoundClip.setFramePosition(0); // 重置到开头
            mergeSoundClip.start(); // 播放
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

        // 绘制背景网格 (空的方块槽)
        for (int i = 0; i < Config.SIDE; i++) {
            for (int j = 0; j < Config.SIDE; j++) {
                drawEmptyTile(g2, j, i);
            }
        }

        // --- 核心绘制变化：让每个 Tile 自己绘制自己 ---
        for (Tile t : engine.tiles) {
            t.draw(g2);
        }

        if (engine.isGameStopped) {
            drawGameOver(g2);
        }
    }

    // 绘制空的背景格
    private void drawEmptyTile(Graphics2D g, int x, int y) {
        int xPos = x * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        int yPos = y * (Config.TILE_SIZE + Config.MARGIN) + Config.MARGIN;
        g.setColor(new Color(0xcdc1b5));
        g.fillRoundRect(xPos, yPos, Config.TILE_SIZE, Config.TILE_SIZE, 14, 14);
    }

    // ... drawHeader 和 drawGameOver 方法保持不变 ...
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