package com.SuperGame.Utils;

import javax.swing.JPanel;

import com.SuperGame.Objects.FieldPlay;
import com.SuperGame.Scenes.InGamePanel;

import javax.swing.JFrame;

public class SceneManager {
    
    private static JFrame window;
    private static JPanel currentPanel;

    // Установка основного окна игры
    public static void initialize(JFrame mainWindow) {
        window = mainWindow;
    }

    // Загрузка новой сцены
    public static void loadScene(JPanel newPanel) {
        if (window != null) {
            // Удаляем старую сцену
            window.getContentPane().removeAll();
            
            // Добавляем новую сцену
            window.add(newPanel);
            currentPanel = newPanel;
            
            // Перерисовываем окно
            window.revalidate();
            window.repaint();
        } else {
            System.out.println("Ошибка: Окно не инициализировано.");
        }
    }
    
    public static FieldPlay[] getPlayerData() {
    	InGamePanel gp = (InGamePanel) currentPanel;
    	return gp.getFields();
    }
}
