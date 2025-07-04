package com.SuperGame.Scenes;

import com.SuperGame.AnimatedButton;
import com.SuperGame.ClientManager;
import com.SuperGame.GameManager;
import com.SuperGame.GameWindow;
import com.SuperGame.MessageWrapper;
import com.SuperGame.Objects.AI;
import com.SuperGame.Objects.FieldPlay;
import com.SuperGame.Utils.ResourceLoader;
import com.SuperGame.Utils.SceneManager;
import com.SuperGame.Utils.SoundManager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;


public class InGamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener  {
	private Image backgroundImage;
	JButton save_btn;
	JButton load_btn;
	
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
	    if (GameManager.playWithAI) {
	    	playerField = new FieldPlay(686, 30, true, 1);
	    } else {
	    	playerField = new FieldPlay(686, 30, true, 0.75);	    	
	    }
	    GameManager.addGameEventListener(playerField);
	    
	    if (GameManager.isServer) {
	    	GameManager.setTurn(true);
	    } else {
	    	GameManager.setTurn(false);
	    }
	    
	    save_btn = new AnimatedButton("Сохранить", 686, 560, 300, 100);
	    save_btn.addActionListener(e -> { saveBtnAction(); });
	    add(save_btn);
	    
	    load_btn = new AnimatedButton("Загрузить", 996, 560, 300, 100);
	    load_btn.addActionListener(e -> {
	    	ClientManager.listSavedGames();
	    });
	    add(load_btn);
	    
	    if (GameManager.playWithAI) {
	    	ai = new AI();
	    	save_btn.setVisible(false);
	    	load_btn.setVisible(false);
	    } 
	    
	    setLayout(null);
	    
	    // Добавление фокуса
	    setFocusable(true);
	    requestFocusInWindow();  // Запросить фокус
	    
	    // Добавление обработчиков мыши
	    addMouseListener(this);
	    addMouseMotionListener(this); // Добавить слушатель движения мыши
	}

	private void saveBtnAction() {
		// Создаем диалоговое окно для ввода имени сохранения
		JTextField saveNameField = new JTextField(20);
		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(new JLabel("Введите имя сохранения:"));
		panel.add(saveNameField);

		int result = JOptionPane.showConfirmDialog(null, panel, "Сохранить игру", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		// Если пользователь нажал "OK"
		if (result == JOptionPane.OK_OPTION) {
		    String saveName = saveNameField.getText();

		    // Проверяем, введено ли имя сохранения
		    if (saveName.isEmpty()) {
		        JOptionPane.showMessageDialog(null, "Введите имя сохранения!", "Ошибка", JOptionPane.ERROR_MESSAGE);
		        return; // Прерываем выполнение, если имя не введено
		    }

		    // Отправляем команду сохранения
		        try {
		            ClientManager.send(new MessageWrapper("saveRequest", new Object[]{ClientManager.username, playerField, enemyField, GameManager.missed, GameManager.hited, saveName}));
		            JOptionPane.showMessageDialog(null, "Игра успешно сохранена!", "Успех", JOptionPane.INFORMATION_MESSAGE);
		        }
		        catch (Exception ex){
		            JOptionPane.showMessageDialog(null, "Ошибка при сохранении", "Ошибка", JOptionPane.ERROR_MESSAGE);
		        }
		}
		
		requestFocusInWindow();
	}
	
	public InGamePanel(FieldPlay loadedPlayerField, FieldPlay loadedEnemyField, boolean loadedHostTurn) {
		GameManager.GamePanel = this;
	    Timer tim = new Timer(16, (ActionListener) this);
	    tim.start();
	    
	    setBackground(new Color(0, 0, 50));
	    backgroundImage = ResourceLoader.loadImageAsURL("/images/Ocean.png");
	    SoundManager.playMusic("/music/Sea.wav", true);
	    
	    enemyField = loadedEnemyField;
	    playerField = loadedPlayerField;
	    GameManager.addGameEventListener(playerField);
	    
	    if (GameManager.isServer) {
	    	GameManager.setTurn(loadedHostTurn);
	    } else {
	    	GameManager.setTurn(!loadedHostTurn);
	    }
	    
	    save_btn = new AnimatedButton("Сохранить", 686, 560, 300, 100);
	    save_btn.addActionListener(e -> { saveBtnAction(); });
	    add(save_btn);
	    
	    load_btn = new AnimatedButton("Загрузить", 996, 560, 300, 100);
	    load_btn.addActionListener(e -> {
	    	ClientManager.listSavedGames();
	    });
	    add(load_btn);
	    
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
	    
	    save_btn.setVisible(false);
    	load_btn.setVisible(false);

	    JLabel resultLabel = new JLabel();
	    resultLabel.setForeground(Color.WHITE);
	    resultLabel.setHorizontalTextPosition(JLabel.CENTER);
	    resultLabel.setFont(GameWindow.customFont.deriveFont(48f)); // Применяем шрифт к метке
	    
	    if (isWin) {
	        resultLabel.setText("Победа!");
	        resultLabel.setBounds(586, 200, 400, 100);
	        if (!GameManager.playWithAI) ClientManager.incrementWins();
	    } else {
	        resultLabel.setText("Поражение!");
	        resultLabel.setBounds(550, 200, 400, 100);
	        if (!GameManager.playWithAI) ClientManager.incrementLoses();
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
	        ClientManager.stopClient();
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
				ClientManager.send(new MessageWrapper("giveTurn", null));
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
				ClientManager.send(new MessageWrapper("intArray", tilePos));
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
		if (playerField != null) {
			if (playerField.getSunkShips().size() == 10) {
				endGame(false);
			} else if (enemyField.getSunkShips().size() == 10) {
				endGame(true);
			}
		}
	}
	
	public FieldPlay[] getFields() {
		return new FieldPlay[] {playerField, enemyField};
	}
}
