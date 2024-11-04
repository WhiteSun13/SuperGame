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


public class SetGamePanel extends JPanel implements KeyListener, ActionListener, MouseListener, MouseMotionListener {
	private Image backgroundImage;
	private JLabel enemyReadyLabel;
	
	private FieldSet field;
	private Ship[] ships = new Ship[10]; // Корабли
    private Ship selectedShip; // Текущий перетаскиваемый корабль
    private boolean dragging = false; // Перетаскивается ли сейчас корабль

    public SetGamePanel() {
        Timer tim = new Timer(50, this);
        tim.start();
        
        GameManager.newGame();
        
        SoundManager.playMusic("/music/q.wav", true);
        backgroundImage = ResourceLoader.loadImageAsURL("/images/Ocean.png");
        
     // Инициализация кнопки "Сброс"
        JButton random_btn = new AnimatedButton("Cлучайно", 1089, 240, 245, 100);
        
        random_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	RandomShips randomField = new RandomShips();
            	randomField.placeShips();
            	
            	field.fullCleanField();
            	field.loadField(randomField.shipsPos, randomField.shipsIsHorizontal, randomField.shipsImagesPos);
            	
            	for (int i = 0; i<10; i++) {
                    ships[i].seted = false;
                    int[] tPos = randomField.shipsImagesPos.get(i+1);
                    ships[i].setIshorizontal(randomField.shipsIsHorizontal.get(i+1));
                    ships[i].setPosition(field.tiles[tPos[0]][tPos[1]].getX() + 32, field.tiles[tPos[0]][tPos[1]].getY() + 32);
                    ships[i].seted = true;
            	}
            }
          });
        
        add(random_btn);
        
     // Инициализация кнопки "Сброс"
        JButton reset_btn = new AnimatedButton("Сброс", 1089, 350, 245, 100);
        
        reset_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	for (Ship ship : ships) {
                    ship.resetPos();
                }
            	field.fullCleanField();
            }
          });
        
        add(reset_btn);
        
        // Инициализация кнопки "Перевернуть"
        JButton rotate_btn = new AnimatedButton("Перевернуть", 1089, 460, 245, 100);
        
        rotate_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	for (Ship ship : ships) {
                    ship.rotateImage();
                    field.shipsIsHorizontal.put(ship.getID(), ship.getIshorizontal());
                }
                repaint();                
            }          
          });
        
        add(rotate_btn);
        
        // Инициализация кнопки "Готово"
        JButton ready_btn = new AnimatedButton("Готово", 966, 570, 368, 100);
        
        ready_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (field.allShipsIsSet()) {
					setFocusable(false);
					field.saveField();
					if (GameManager.playWithAI) {
						SceneManager.loadScene(new InGamePanel());
					} else {
						SceneManager.loadScene(new WaitEnemy(new InGamePanel()));
					}
				} else {
					JOptionPane.showMessageDialog(null, "Все корабли должны быть раставлены!");
				}
				
			}
		});
        
        add(ready_btn);
        
        enemyReadyLabel = new JLabel();
        enemyReadyLabel.setForeground(Color.WHITE);
        enemyReadyLabel.setHorizontalTextPosition(JLabel.CENTER);
        enemyReadyLabel.setFont(GameWindow.customFont.deriveFont(24f));
        enemyReadyLabel.setText("Противник готов!");
        enemyReadyLabel.setBounds(1095, 30, 750, 320);
        enemyReadyLabel.setVisible(false);
	    add(enemyReadyLabel);
        
        field = new FieldSet(30, 30);
        
        // Инициализация кораблей
        ships[0] = new Ship(990, 158, 1); 
        ships[1] = new Ship(990, 222, 2);
        ships[2] = new Ship(990, 286, 3);
        ships[3] = new Ship(990, 350, 4);
        
        ships[4] = new Ship(798, 94, 5);
        ships[5] = new Ship(862, 158, 6);
        ships[6] = new Ship(926, 94, 7);
        
        ships[7] = new Ship(734, 286, 8);
        ships[8] = new Ship(798, 350, 9);
        
        ships[9] = new Ship(734, 30, 10);

        setLayout(null);
        
        // Добавление обработчика клавиатуры
        addKeyListener(this);
        
        // Добавление фокуса
        setFocusable(true);
        
        // Добавление обработчиков мыши
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this);
        
        // Отрисовка поля 
        field.draw(g);
        
        // Отрисовка кораблей
        for (Ship ship : ships) {
            ship.draw(g);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
//    	int key = e.getKeyCode();
//    	if (key == KeyEvent.VK_SPACE && selectedShip != null) {
//    		selectedShip.rotateImage();
//    		selectedShip.setPosition(selectedShip.getX(), selectedShip.getY());
//            field.check(selectedShip.getX(), selectedShip.getY(), selectedShip, true);
//            repaint();
//    	}
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    // Типа Update (fixedUpdate) у Unity 
    // Обновляется каждые 50 миллисекунд (указывается в Timer)
    @Override
    public void actionPerformed(ActionEvent e) {
    	if (GameManager.isEnemyReady) {
    		enemyReadyLabel.setVisible(true);
    	}
        repaint();
    }

    // Обработка нажатия кнопки мыши
    @Override
    public void mousePressed(MouseEvent e) {
    	if (SwingUtilities.isLeftMouseButton(e)) {
    		requestFocusInWindow(); // Запрос фокуса
    		for (Ship ship : ships) {
    			if (ship.contains(e.getX(), e.getY())) {
    				selectedShip = ship; // Запоминаем выбранный корабль
    				selectedShip.savePosition(); // Сохраняем местоположение
    				selectedShip.seted = false;
                
    				System.out.println("Это " + selectedShip.getType() + " клеточный корабль №" + selectedShip.getID());
    				dragging = true;
    				break; // Выход из цикла, так как мы нашли корабль
    			}
    		}
    	} else if (SwingUtilities.isRightMouseButton(e) && selectedShip != null) {
    		selectedShip.rotateImage();
    		selectedShip.setPosition(e.getX(), e.getY());
            field.check(e.getX(), e.getY(), selectedShip, true);
            repaint();
    	}
    }

    // Обработка отпускания кнопки мыши
    @Override
    public void mouseReleased(MouseEvent e) {
    	if (SwingUtilities.isRightMouseButton(e)) return;
    	dragging = false;
        
        if (selectedShip != null) {
        	if (field.selectedTile == null || !field.canSetShip) {
            	selectedShip.returnPosition(); // Возвращаем корабль на место
            	selectedShip.setIshorizontal(field.shipsIsHorizontal.get(selectedShip.getID()));;
            	field.drawWarships();
            } else {
            	selectedShip.setPosition(field.selectedTile.getX() + 32, field.selectedTile.getY() + 32);
            	selectedShip.savePosition(); // Сохраняем местоположение

                // Добавляем в словарь
            	field.setShip(selectedShip);
                System.out.println(field.shipsPos);
                
                int[][] retrievedValue = field.shipsPos.get(selectedShip.getID());
                for (int[] array : retrievedValue) {
                    for (int num : array) {
                        System.out.print(num + " ");
                    }
                    System.out.println();
                }
            }
        }
        
        for (Ship ship : ships) {
        	if (field.shipsPos.containsKey(ship.getID())) {
        		ship.seted = true;
        	} else {
        		ship.seted = false;
        	}
        }
        
        selectedShip = null; // Сбрасываем выбранный корабль
    }

    // Обработка движения мыши
    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging && selectedShip != null) {
            selectedShip.setPosition(e.getX(), e.getY());
            field.check(e.getX(), e.getY(), selectedShip, false);
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
