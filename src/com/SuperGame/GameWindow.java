package com.SuperGame;

import javax.swing.*;


public class GameWindow extends JFrame{

	public GameWindow() {
        setTitle("Simple Java Game");
        setSize(1372, 765);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GamePanel panel = new GamePanel();
        add(panel);
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        fileMenu.add(aboutItem);
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
    }
	
	private void showAboutDialog() {
        String message = "World of Warships\n" +
                         "Версия: 0.1\n" +
                         "Авторы: Абибуллаев Сулейман\nАблаев Муслим\n" +
                         "Все права защищены.\n"+
                         "2024 ©";
        
        JOptionPane.showMessageDialog(this, message, "О программе", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        GameWindow window = new GameWindow();
        window.setVisible(true);
    }

}
