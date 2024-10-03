package com.SuperGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements KeyListener, ActionListener, MouseListener, MouseMotionListener {
    private Tile[][] tiles = new Tile[10][10];
	private Ship[] ships = new Ship[10]; // Корабли
    private Ship selectedShip; // Текущий перетаскиваемый корабль
    private boolean dragging = false; // Перетаскивается ли сейчас корабль

    public GamePanel() {
        Timer tim = new Timer(50, this);
        tim.start();
        
        // Инициализация кнопки "Перевернуть"
        JButton rotate_btn = new JButton("Перевернуть");
        rotate_btn.setBounds(1089, 415, 245, 64);
        
        rotate_btn.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	for (Ship ship : ships) {
                    ship.rotateImage();
                }
                repaint();                
            }          
          });
        
        add(rotate_btn);
        
        // Инициализация кнопки "Готово"
        JButton ready_btn = new JButton("Готово");
        ready_btn.setBounds(1089, 492, 245, 64);   
        add(ready_btn);
        
        // Инициализация поля
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
        		tiles[i][j] = new Tile(i * 64 + 30, j * 64 + 30);
        	}
        }
        
        // Инициализация кораблей
        ships[0] = new Ship(990, 158, 1, 1); 
        ships[1] = new Ship(990, 222, 1, 2);
        ships[2] = new Ship(990, 286, 1, 3);
        ships[3] = new Ship(990, 350, 1, 4);
        
        ships[4] = new Ship(798, 94, 2, 5);
        ships[5] = new Ship(862, 158, 2, 6);
        ships[6] = new Ship(926, 94, 2, 7);
        
        ships[7] = new Ship(734, 286, 3, 8);
        ships[8] = new Ship(798, 350, 3, 9);
        
        ships[9] = new Ship(734, 30, 4, 10);

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
        
        // Отрисовка поля 
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
            	tiles[i][j].draw(g);
            }
        }
        
        // Отрисовка кораблей
        for (Ship ship : ships) {
            ship.draw(g);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    // Типа Update (fixedUpdate) у Unity 
    // Обновляется каждые 50 миллисекунд (указывается в Timer)
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    // Обработка нажатия кнопки мыши
    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow(); // Запрос фокуса
        for (Ship ship : ships) {
            if (ship.contains(e.getX(), e.getY())) {
                selectedShip = ship; // Запоминаем выбранный корабль
                selectedShip.savePosition(); // Сохраняем местоположение
                System.out.println("Это " + selectedShip.getType() + " клеточный корабль №" + selectedShip.getID());
                dragging = true;
                break; // Выход из цикла, так как мы нашли корабль
            }
        }
    }

    // Обработка отпускания кнопки мыши
    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
        
        // ЗАГЛУШКА
        if (selectedShip != null) {
        	if (true) {
            	selectedShip.returnPosition(); // Возвращаем корабль на место
            } else {
            	selectedShip.savePosition(); // Сохраняем местоположение
            }
        }
        
        selectedShip = null; // Сбрасываем выбранный корабль
    }

    // Обработка движения мыши
    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging && selectedShip != null) {
            selectedShip.setPosition(e.getX(), e.getY());
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
