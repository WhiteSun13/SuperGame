package com.SuperGame;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.SuperGame.Scenes.StartMenuPanel;
import com.SuperGame.Utils.SceneManager;
import com.SuperGame.Utils.SoundManager;


public class GameWindow extends JFrame {

    JPanel panel;

    public GameWindow() {
        setTitle("Warships Battle");
        setResizable(false);
        setSize(1372, 765);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Menu");
        menuBar.add(fileMenu);
        
        JMenuItem settingItem = new JMenuItem("Settings");
        settingItem.addActionListener(e -> SettingsDialog.showSettingsDialog(null));
        fileMenu.add(settingItem);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        fileMenu.add(aboutItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        SoundManager.playMusic("/music/StartMenuMusic.wav", true);
        
        // Инициализация SceneManager с текущим окном
        SceneManager.initialize(this);

        // Начальная сцена
        SceneManager.loadScene(new StartMenuPanel());
    }

    private void showAboutDialog() {
        String message = "Warships Battle\n" +
                         "Версия: 0.1\n" +
                         "Авторы: Абибуллаев Сулейман\nАблаев Муслим\n" +
                         "Все права защищены.\n" +
                         "2024 ©";

        JOptionPane.showMessageDialog(this, message, "О программе", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        GameWindow window = new GameWindow();
        window.setVisible(true);
    }
}

