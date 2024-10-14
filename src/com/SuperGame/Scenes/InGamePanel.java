package com.SuperGame.Scenes;

import com.SuperGame.Objects.AI;
import com.SuperGame.Objects.FieldPlay;
import com.SuperGame.Utils.ResourceLoader;
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

import javax.swing.JPanel;
import javax.swing.Timer;


public class InGamePanel extends JPanel implements KeyListener, ActionListener, MouseListener, MouseMotionListener  {
	private Image backgroundImage;
	
	private boolean playWithAI;
	private AI ai;
	
	private FieldPlay enemyField;
	private FieldPlay playerField;
	
	public InGamePanel(boolean playWithAI) {
	    Timer tim = new Timer(16, (ActionListener) this);
	    tim.start();
	    
	    setBackground(new Color(0, 0, 50));
	    backgroundImage = ResourceLoader.loadImageAsURL("/images/Ocean.png");
	    SoundManager.playMusic("/music/Sea.wav", true);
	    
	    enemyField = new FieldPlay(30, 30, false);
	    playerField = new FieldPlay(686, 30, true, 0.5);
	    
	    this.playWithAI = playWithAI;
	    if (playWithAI) {
	    	ai = new AI();
	    }
	    
	    setLayout(null);
	    
	    // Добавление обработчика клавиатуры
	    addKeyListener(this);
	    
	    // Добавление фокуса
	    setFocusable(true);
	    requestFocusInWindow();  // Запросить фокус
	    
	    // Добавление обработчиков мыши
	    addMouseListener(this);
	    addMouseMotionListener(this); // Добавить слушатель движения мыши
	}

	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this);
        
        // Отрисовка поля 
        playerField.draw(g);
        enemyField.draw(g);
    }

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		enemyField.hover(e.getX(), e.getY());
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		enemyField.Hit();
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
