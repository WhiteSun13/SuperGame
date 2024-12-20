package com.SuperGame;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.SuperGame.Scenes.StartMenuPanel;
import com.SuperGame.Utils.ResourceLoader;
import com.SuperGame.Utils.SceneManager;

public class GameWindow extends JFrame {

    public static Font customFont;

    public GameWindow() {
        setTitle("Warships Battle");
        setResizable(false);
        setSize(1372, 765);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
        exitItem.addActionListener(e -> {ClientManager.stopClient(); System.exit(0);});
        fileMenu.add(exitItem);
        
        // Инициализация SceneManager с текущим окном
        SceneManager.initialize(this);

        // Начальная сцена
        SceneManager.loadScene(new StartMenuPanel());
        
     // Добавляем обработчик закрытия окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                    ClientManager.stopClient();
                    System.exit(0);
            }
        });
    }

    private void showAboutDialog() {
        String message = """
                         Warships Battle
                         \u0412\u0435\u0440\u0441\u0438\u044f: 0.1
                         \u0410\u0432\u0442\u043e\u0440\u044b: \u0410\u0431\u0438\u0431\u0443\u043b\u043b\u0430\u0435\u0432 \u0421\u0443\u043b\u0435\u0439\u043c\u0430\u043d
                         \u0410\u0431\u043b\u0430\u0435\u0432 \u041c\u0443\u0441\u043b\u0438\u043c
                         \u0412\u0441\u0435 \u043f\u0440\u0430\u0432\u0430 \u0437\u0430\u0449\u0438\u0449\u0435\u043d\u044b.
                         2024 \u00a9""";

        JOptionPane.showMessageDialog(this, message, "О программе", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) throws IOException, FontFormatException {
        // Загружаем шрифт из пакета
        customFont = Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.loadAsURL("/fonts/RussoOne-Regular.ttf").openStream());
        
        GameWindow window = new GameWindow();
        window.setVisible(true);
    }
}