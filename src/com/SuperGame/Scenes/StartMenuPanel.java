package com.SuperGame.Scenes;

import com.SuperGame.*;
import com.SuperGame.Utils.ResourceLoader;
import com.SuperGame.Utils.SceneManager;
import com.SuperGame.Utils.SoundManager;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartMenuPanel extends JPanel implements ActionListener {
    private final Image backgroundImage;
    private final Image logoImage;

    // Позиции и скорость для анимации групп
    private int group1X = 0;
    private int group2X = 1372; // Группа 2 за правым краем экрана
    private int targetGroup1X = 0;
    private int targetGroup2X = 1372;
    private final int speed = 50;

    // Позиции для бесконечного фона
    private int x1, y1, x2, y2;
    private int speedX = 2; // Скорость фона по оси X

    private final Timer animationTimer;

    // Кнопки для первой и второй групп
    private final JButton play_btn;
    private final JButton options_btn;
    private final JButton exit_btn;

    private final JButton player1_btn;
    private final JButton player2Server_btn;
    private final JButton backToFirstMenu_btn;

    private boolean drawBox = false;

    public StartMenuPanel() {
        setLayout(null);
        setBackground(Color.BLACK);
        
        SoundManager.playMusic("/music/StartMenuMusic.wav", true);
        
        GameManager.isEnemyReady = false;

        // Загрузка изображений
        backgroundImage = ResourceLoader.loadImageAsURL("/images/camuflagePatern.png");
        logoImage = ResourceLoader.loadImageAsURL("/images/WhiteLogo.png");

        // Начальные позиции фона
        x1 = 0;
        y1 = 0;
        x2 = -backgroundImage.getWidth(null);
        y2 = 0;

        // Группа 1
        play_btn = new AnimatedButton("Играть", 822, 166, 400, 100);
        play_btn.addActionListener(e -> {SoundManager.playSound("/sounds/EnterMenu.wav");switchToGroup2();});
        add(play_btn);

        options_btn = new AnimatedButton("Настройки", 822, 300, 400, 100);
        options_btn.addActionListener(e -> {
        	SettingsDialog.showSettingsDialog(null);
        });
        add(options_btn);

        exit_btn = new AnimatedButton("Выйти", 822, 434, 400, 100);
        exit_btn.addActionListener(e -> System.exit(0));
        add(exit_btn);

        // Группа 2
        player1_btn = new AnimatedButton("1P vs CPU", group2X + 270, 166, 400, 232);
        player1_btn.addActionListener(e -> {
        	GameManager.setTurn(true);
        	GameManager.isServer = true;
        	GameManager.playWithAI = true;
        	SceneManager.loadScene(new SetGamePanel());
        });
        add(player1_btn);

        player2Server_btn = new AnimatedButton("Многопользовательская", group2X + 701, 166, 400, 232);

        backToFirstMenu_btn = new AnimatedButton("Назад", group2X + 270, 420, 400, 100);
        backToFirstMenu_btn.addActionListener(e -> {SoundManager.playSound("/sounds/ExitMenu.wav"); switchToGroup1();});
        add(backToFirstMenu_btn);
        
        player2Server_btn.addActionListener(e -> {
        	SceneManager.loadScene(new MultiplayerPanel());
        });

        add(player2Server_btn);
        // Таймер для анимации и бесконечного движения фона
        animationTimer = new Timer(16, this); // 16 ms для 60 кадров в секунду
        animationTimer.start();
    }

    private void switchToGroup2() {
        // Целевая позиция: группа 1 уходит влево, группа 2 приходит справа
        targetGroup1X = -getWidth();
        targetGroup2X = 0;
    }

    private void switchToGroup1() {
        // Целевая позиция: группа 1 возвращается, группа 2 уходит вправо
        targetGroup1X = 0;
        targetGroup2X = getWidth();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Рисуем бесконечный фон
        g.drawImage(backgroundImage, x1, y1, this);
        g.drawImage(backgroundImage, x2, y2, this);

        // Логотип
        g.setColor(new Color(27, 24, 90, 240));
        g.fillRect(group1X + 36, 0, 650, getHeight());
        g.drawImage(logoImage,group1X + 36, 25, this);

        // Устанавливаем позиции кнопок для группы 1
        play_btn.setLocation(group1X + 822, 166);
        options_btn.setLocation(group1X + 822, 300);
        exit_btn.setLocation(group1X + 822, 434);

        // Устанавливаем позиции кнопок для группы 2
        player1_btn.setLocation(group2X + 270, 166);
        player2Server_btn.setLocation(group2X + 701, 166);
        backToFirstMenu_btn.setLocation(group2X + 270, 420);
        
        if (drawBox) g.fillRect(36, 36, getWidth() - getWidth() / 4, getHeight() - getHeight() / 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Анимация движения группы 1
        if (group1X != targetGroup1X) {
            group1X += (group1X < targetGroup1X) ? speed : -speed;
            if (Math.abs(group1X - targetGroup1X) < speed) {
                group1X = targetGroup1X; // Останавливаем на целевой позиции
            }
        }

        // Анимация движения группы 2
        if (group2X != targetGroup2X) {
            group2X += (group2X < targetGroup2X) ? speed : -speed;
            if (Math.abs(group2X - targetGroup2X) < speed) {
                group2X = targetGroup2X; // Останавливаем на целевой позиции
            }
        }

        // Бесконечное движение фона
        x1 += speedX;
        x2 += speedX;

        // Проверка выхода фона за пределы экрана
        if (x1 >= backgroundImage.getWidth(null)) {
            x1 = x2 - backgroundImage.getWidth(null);
        }
        if (x2 >= backgroundImage.getWidth(null)) {
            x2 = x1 - backgroundImage.getWidth(null);
        }

        // Перерисовка панели
        repaint();
    }
}
