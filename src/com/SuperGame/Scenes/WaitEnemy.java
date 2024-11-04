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


public class WaitEnemy extends JPanel implements ActionListener {
	private Image backgroundImage;
	private JPanel newPanel;

    public WaitEnemy(JPanel newPanel) {
        Timer tim = new Timer(50, this);
        tim.start();
        
        if (GameManager.isServer) {
			Server.send(new MessageWrapper("enemyReady", null));
		} else {
			Client.send(new MessageWrapper("enemyReady", null));
		}

        backgroundImage = ResourceLoader.loadImageAsURL("/images/Ocean.png");
        
        this.newPanel = newPanel;
        
        JLabel infoLabel = new JLabel();
	    infoLabel.setForeground(Color.WHITE);
	    infoLabel.setHorizontalTextPosition(JLabel.CENTER);
	    infoLabel.setFont(GameWindow.customFont.deriveFont(48f));
	    infoLabel.setText("Ожидаем противника");
	    infoLabel.setBounds(300, 300, 750, 100);
	    add(infoLabel);
 
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
	    if (GameManager.isEnemyReady) {
	        GameManager.isEnemyReady = false;
	        ((Timer) e.getSource()).stop(); // Останавливаем таймер
	        SceneManager.loadScene(newPanel);
	    }
	}

}
