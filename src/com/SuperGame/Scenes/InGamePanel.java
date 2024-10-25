package com.SuperGame.Scenes;

import com.SuperGame.AnimatedButton;
import com.SuperGame.GameManager;
import com.SuperGame.Objects.AI;
import com.SuperGame.Objects.FieldPlay;
import com.SuperGame.Utils.ResourceLoader;
import com.SuperGame.Utils.SoundManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
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
	    playerField = new FieldPlay(686, 30, true, 1);
	    GameManager.addGameEventListener(playerField);
	    
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
        
        g.setColor(new Color(0, 0, 0, 127));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
	
	public void endGame(boolean isWin) {
		JLabel resultLabel = new JLabel();
		resultLabel.setForeground(Color.WHITE);
		resultLabel.setHorizontalTextPosition(JLabel.CENTER);
	    
	    // Загружаем шрифт из пакета
	    try {
	        // Убедись, что путь к файлу корректный
	        Font customFont = Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.loadAsURL("/fonts/RussoOne-Regular.ttf").openStream());
	        customFont = customFont.deriveFont(48f); // Устанавливаем размер шрифта
	        
	        resultLabel.setFont(customFont); // Применяем шрифт к метке
	    } catch (FontFormatException | IOException e) {
	        e.printStackTrace();
	    }

	    if (isWin) {
	        resultLabel.setText("Победа!");
	    } else {
	        resultLabel.setText("Поражение!");
	    }
	    
	    resultLabel.setBounds(586, 200, 400, 100); // Устанавливаем позицию и размер
	    add(resultLabel);
		JButton playAgain_btn = new AnimatedButton("Играть снова", 486, 400, 400, 100);
        add(playAgain_btn);
        JButton exitToMainMenu_btn = new AnimatedButton("Выйти в меню", 486, 510, 400, 100);
        add(exitToMainMenu_btn);
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (GameManager.isTurn) enemyField.hover(e.getX(), e.getY());
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (GameManager.isTurn) {
			int[] tilePos = enemyField.getSelctedTilePosition();
			if (tilePos == null) return;
			
			Object[] positionInfo = ai.checkPosition(tilePos);
			if(playWithAI) {
				if ((boolean) positionInfo[0]){
					enemyField.HitOrMiss(tilePos, true);
					if ((boolean) positionInfo[1]) {
						enemyField.addSunkShip((int) positionInfo[2], (int[][]) positionInfo[3], (boolean) positionInfo[4], (int[]) positionInfo[5]);
					}
				} else {
					enemyField.HitOrMiss(tilePos, false);
					GameManager.isTurn = false;
					ai.giveTurn();
				}
			}
			
			System.out.println(enemyField.getSunkShips().size());
			
			if (playerField.getSunkShips().size() == 10) {
				endGame(false);
			} else if (enemyField.getSunkShips().size() == 10) {
				endGame(true);
			}
		}
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
