package com.SuperGame;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.SuperGame.Scenes.StartMenuPanel;
import com.SuperGame.Utils.ResourceLoader;
import com.SuperGame.Utils.SceneManager;


public class GameWindow extends JFrame {

    JPanel panel;
    public static Font customFont;

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
    	// Загружаем шрифт из пакета
	    try {
	        // Убедись, что путь к файлу корректный
	        customFont = Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.loadAsURL("/fonts/RussoOne-Regular.ttf").openStream());
	    } catch (FontFormatException | IOException e) {
	        e.printStackTrace();
	    }

    	
        GameWindow window = new GameWindow();
        window.setVisible(true);
    }
}