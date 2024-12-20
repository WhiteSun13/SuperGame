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


public class MultiplayerPanel extends JPanel implements ActionListener {
	private Image backgroundImage;
	
    public MultiplayerPanel() {
        Timer tim = new Timer(50, this);
        tim.start();
        
        backgroundImage = ResourceLoader.loadImageAsURL("/images/Ocean.png");
        
        JLabel serverIP_label = new JLabel("Введите IP адрес сервера:");
    	serverIP_label.setFont(GameWindow.customFont.deriveFont(20f)); // Шрифт и размер текста
    	serverIP_label.setForeground(Color.WHITE); // Цвет текста
    	serverIP_label.setBounds(100, 50, 800, 50); // Координаты и размер
        add(serverIP_label); // Добавление на панель
        
        JTextField serverIP_tf = new JTextField("");
        serverIP_tf.setBounds(100, 100, 800, 50);
        serverIP_tf.setFont(new Font("Arial", Font.BOLD, 20)); // Шрифт и размер текста
        add(serverIP_tf);
        
        JLabel clientName_label = new JLabel("Введите имя:");
        clientName_label.setFont(GameWindow.customFont.deriveFont(20f)); // Шрифт и размер текста
        clientName_label.setForeground(Color.WHITE); // Цвет текста
        clientName_label.setBounds(100, 160, 800, 50); // Координаты и размер
        add(clientName_label); // Добавление на панель
        
        JTextField clientName_tf = new JTextField("");
        clientName_tf.setBounds(100, 210, 800, 50);
        clientName_tf.setFont(new Font("Arial", Font.BOLD, 20)); // Шрифт и размер текста
        add(clientName_tf);
        
        JLabel clientPW_label = new JLabel("Введите пароль:");
        clientPW_label.setFont(GameWindow.customFont.deriveFont(20f)); // Шрифт и размер текста
        clientPW_label.setForeground(Color.WHITE); // Цвет текста
        clientPW_label.setBounds(100, 270, 800, 50); // Координаты и размер
        add(clientPW_label); // Добавление на панель
        
        JTextField clientPW_tf = new JTextField("");
        clientPW_tf.setBounds(100, 320, 800, 50);
        clientPW_tf.setFont(new Font("Arial", Font.BOLD, 20)); // Шрифт и размер текста
        add(clientPW_tf);
        
        JButton p2clientLogin_btn = new AnimatedButton("Войти", 100, 380, 400, 100);
        p2clientLogin_btn.addActionListener(q -> { ClientManager.startClient(serverIP_tf.getText(), clientName_tf.getText(), clientPW_tf.getText(), false);} );
        add(p2clientLogin_btn);
        
        JButton p2clientSingup_btn = new AnimatedButton("Зарегистрироваться", 510, 380, 400, 100);
        p2clientSingup_btn.addActionListener(q -> { ClientManager.startClient(serverIP_tf.getText(), clientName_tf.getText(), clientPW_tf.getText(), true);} );
        add(p2clientSingup_btn);
 
        setLayout(null);
        setFocusable(true);
    }

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
	}

}
