package com.SuperGame.Utils;

import javax.swing.JPanel;
import javax.swing.JFrame;

public class SceneManager {
    
    private static JFrame window;

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
            
            // Перерисовываем окно
            window.revalidate();
            window.repaint();
        } else {
            System.out.println("Ошибка: Окно не инициализировано.");
        }
    }
}
