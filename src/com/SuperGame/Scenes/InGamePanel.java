package com.SuperGame.Scenes;

import com.SuperGame.AnimatedButton;
import com.SuperGame.Client;
import com.SuperGame.GameManager;
import com.SuperGame.GameWindow;
import com.SuperGame.MessageWrapper;
import com.SuperGame.Server;
import com.SuperGame.Objects.AI;
import com.SuperGame.Objects.FieldPlay;
import com.SuperGame.Utils.ResourceLoader;
import com.SuperGame.Utils.SceneManager;
import com.SuperGame.Utils.SoundManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;


public class InGamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener  {
	private Image backgroundImage;
	
	private boolean isGameOver;
	private AI ai;
	
	private FieldPlay enemyField;
	private FieldPlay playerField;
	
	private int[] tilePos = null;
	
	public InGamePanel() {
		GameManager.GamePanel = this;
	    Timer tim = new Timer(16, (ActionListener) this);
	    tim.start();
	    
	    setBackground(new Color(0, 0, 50));
	    backgroundImage = ResourceLoader.loadImageAsURL("/images/Ocean.png");
	    SoundManager.playMusic("/music/Sea.wav", true);
	    
	    enemyField = new FieldPlay(30, 30, false);
	    playerField = new FieldPlay(686, 30, true, 1);
	    GameManager.addGameEventListener(playerField);
	    
	    if (GameManager.isServer) {
	    	GameManager.setTurn(true);
	    } else {
	    	GameManager.setTurn(false);
	    }
	    
	    if (GameManager.playWithAI) {
	    	ai = new AI();
	    }
	    
	    setLayout(null);
	    
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
        
        if (isGameOver) {
        	g.setColor(new Color(0, 0, 0, 127));
        	g.fillRect(0, 0, getWidth(), getHeight());        	
        }
    }
	
	public void endGame(boolean isWin) {
	    if (isGameOver) return; // Проверка, если игра уже завершена
	    
	    GameManager.setTurn(false);
	    isGameOver = true;

	    JLabel resultLabel = new JLabel();
	    resultLabel.setForeground(Color.WHITE);
	    resultLabel.setHorizontalTextPosition(JLabel.CENTER);
	    resultLabel.setFont(GameWindow.customFont.deriveFont(48f)); // Применяем шрифт к метке
	    
	    if (isWin) {
	        resultLabel.setText("Победа!");
	        resultLabel.setBounds(586, 200, 400, 100);
	    } else {
	        resultLabel.setText("Поражение!");
	        resultLabel.setBounds(550, 200, 400, 100);
	    }

	    add(resultLabel);

	    JButton playAgain_btn = new AnimatedButton("Играть снова", 486, 400, 400, 100);
	    playAgain_btn.addActionListener(e -> {
	    	if (GameManager.playWithAI) {
	    		SceneManager.loadScene(new SetGamePanel());	    		
	    	} else {
	    		SceneManager.loadScene(new WaitEnemy(new SetGamePanel()));
	    	}
	    });
	    add(playAgain_btn);

	    JButton exitToMainMenu_btn = new AnimatedButton("Выйти в меню", 486, 510, 400, 100);
	    exitToMainMenu_btn.addActionListener(e -> {
	        GameManager.setTurn(false);
	        SceneManager.loadScene(new StartMenuPanel());
	    });
	    add(exitToMainMenu_btn);
	}

	
	public void setPositionInfo(Object[] positionInfo) {
		if ((boolean) positionInfo[0]){
			enemyField.HitOrMiss(tilePos, true);
			if ((boolean) positionInfo[1]) {
				enemyField.addSunkShip((int) positionInfo[2], (int[][]) positionInfo[3], (boolean) positionInfo[4], (int[]) positionInfo[5]);
			}
		} else {
			enemyField.HitOrMiss(tilePos, false);
			GameManager.isTurn = false;
			if (GameManager.playWithAI) {
				ai.giveTurn();
			} else {
				if (GameManager.isServer) {
					Server.send(new MessageWrapper("giveTurn", null));
				} else {
					Client.send(new MessageWrapper("giveTurn", null));
				}
			}
		}
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
			tilePos = enemyField.getSelctedTilePosition();
			if (tilePos == null) return;
			
			if(GameManager.playWithAI) {
				Object[] positionInfo = ai.checkPosition(tilePos);
				setPositionInfo(positionInfo);
				
			} else {
				if (GameManager.isServer) {
					Server.send(new MessageWrapper("intArray", tilePos));
				} else {
					Client.send(new MessageWrapper("intArray", tilePos));
				}
			}
			
			System.out.println(enemyField.getSunkShips().size());
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
		if (playerField.getSunkShips().size() == 10) {
			endGame(false);
		} else if (enemyField.getSunkShips().size() == 10) {
			endGame(true);
		}
	}
}
