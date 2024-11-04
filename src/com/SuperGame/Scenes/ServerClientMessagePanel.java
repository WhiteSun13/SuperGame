package com.SuperGame.Scenes;

import com.SuperGame.*;
import com.SuperGame.Objects.Field;
import com.SuperGame.Objects.FieldSet;
import com.SuperGame.Objects.RandomShips;
import com.SuperGame.Objects.Ship;
import com.SuperGame.Utils.ResourceLoader;
import com.SuperGame.Utils.SceneManager;
import com.SuperGame.Utils.SoundManager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


public class ServerClientMessagePanel extends JPanel implements ActionListener {
	private Image backgroundImage;

    public ServerClientMessagePanel(boolean isSuccess) {
        Timer tim = new Timer(50, this);
        tim.start();

        backgroundImage = ResourceLoader.loadImageAsURL("/images/Ocean.png");
        
        if (isSuccess) {
        	successMessage();
        } else {
        	failMassege();
        }
 
        setLayout(null);
        setFocusable(true);
    }

    private void failMassege() {
    	JLabel infoLabel = new JLabel();
	    infoLabel.setForeground(Color.WHITE);
	    infoLabel.setHorizontalTextPosition(JLabel.CENTER);
	    infoLabel.setFont(GameWindow.customFont.deriveFont(48f));
	    
	    if (GameManager.isServer) {
	    	infoLabel.setText("Ошибка сервера");
	        infoLabel.setBounds(500, 300, 500, 100);
	    } else {
	    	infoLabel.setText("Ошибка клиента");
	        infoLabel.setBounds(500, 300, 500, 100);
	    }
	    
	    add(infoLabel);
    	
    	JButton continue_btn = new AnimatedButton("Продолжить", 586, 550, 245, 100);
        
    	continue_btn.addActionListener(e -> {
        	SceneManager.loadScene(new StartMenuPanel());
        });
        
        add(continue_btn);
	}

	private void successMessage() {
		JLabel infoLabel = new JLabel();
	    infoLabel.setForeground(Color.WHITE);
	    infoLabel.setHorizontalTextPosition(JLabel.CENTER);
	    infoLabel.setFont(GameWindow.customFont.deriveFont(48f));
	    
	    if (GameManager.isServer) {
	    	infoLabel.setText("Клиент успешно подключился!");
	        infoLabel.setBounds(300, 300, 1000, 100);
	    } else {
	    	infoLabel.setText("Успешное подключение к серверу!");
	        infoLabel.setBounds(275, 300, 1000, 100);
	    }
	    
	    add(infoLabel);
		
		JButton continue_btn = new AnimatedButton("Продолжить", 586, 550, 245, 100);
        
    	continue_btn.addActionListener(e -> {
        	SceneManager.loadScene(new SetGamePanel());
        });
        
        add(continue_btn);
	}

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
