package com.SuperGame.Scenes;

import com.SuperGame.*;
import com.SuperGame.Utils.ResourceLoader;
import com.SuperGame.Utils.SceneManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;


public class MultiplayerMenuPanel extends JPanel implements ActionListener {
	private Image backgroundImage;
	
    public MultiplayerMenuPanel() {
        Timer tim = new Timer(50, this);
        tim.start();
        
        backgroundImage = ResourceLoader.loadImageAsURL("/images/Ocean.png");
        
        JLabel stats_label = new JLabel("<html>" + "Победы: " + ClientManager.getStats()[0] + "<br>Поражения: " + ClientManager.getStats()[1] + "</html>");
        stats_label.setFont(GameWindow.customFont.deriveFont(20f)); // Шрифт и размер текста
        stats_label.setForeground(Color.WHITE); // Цвет текста
        stats_label.setBounds(835, 50, 800, 100); // Координаты и размер
        add(stats_label); // Добавление на панель
        
        JLabel serverList_label = new JLabel("<html>" + ClientManager.listAvailableGames().replace("\n", "<br>") + "</html>");
        serverList_label.setFont(GameWindow.customFont.deriveFont(20f)); // Шрифт и размер текста
        serverList_label.setForeground(Color.WHITE); // Цвет текста
        serverList_label.setBounds(25, 50, 800, 600); // Координаты и размер
        add(serverList_label); // Добавление на панель
        
        JButton newGame_btn = new AnimatedButton("Создать игру", 25, 550, 300, 100);
        newGame_btn.addActionListener(q -> {ClientManager.createGame();} );
        add(newGame_btn);
        
        JButton listUpdate_btn = new AnimatedButton("Обновить",330, 550, 300, 100);
        listUpdate_btn.addActionListener(q -> {
        	serverList_label.setText("<html>" + ClientManager.listAvailableGames().replace("\n", "<br>") + "</html>");
        	stats_label.setText("<html>" + "Победы: " + ClientManager.getStats()[0] + "<br>Поражения: " + ClientManager.getStats()[1] + "</html>");
        });
        add(listUpdate_btn);
        
        JTextField clientName_tf = new JTextField("");
        clientName_tf.setBounds(635, 550, 300, 100);
        clientName_tf.setFont(new Font("Arial", Font.BOLD, 20)); // Шрифт и размер текста
        add(clientName_tf);
        
        JButton clientCon_btn = new AnimatedButton("Подключиться",940, 550, 300, 100);
        clientCon_btn.addActionListener(q -> {ClientManager.connectToGame(clientName_tf.getText());} );
        add(clientCon_btn);
 
        setLayout(null);
        setFocusable(true);
    }

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this);
    }

	@Override
	public void actionPerformed(ActionEvent e) {}

}
