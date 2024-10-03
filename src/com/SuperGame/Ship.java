package com.SuperGame;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Ship {
	private int id; // id Корабля
	
	private int x, y, type; // x y и размер корабля 
	private int temp_x, temp_y; // Предыдущее положение
	
	private int w, h; // ширина и высота
	private boolean ishorizontal = true; // Находится ли сейчас корабль в горизонтальном положении
    private Image image;
    private int rotationAngle; // Угол поворота в градусах

    public Ship(int x, int y, int type, int id) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.id = id;
        
        // Загрузка изображения (замените "path/to/image.png" на путь к вашему изображению)
        image = new ImageIcon(String.format("src/com/SuperGame/ship%s.png",type)).getImage();
        rotationAngle = 0; // Начальный угол
        
        this.w = image.getWidth(null);
        this.h = image.getHeight(null);
    }
    
    // Узнать номер корабля
    public int getID() {
    	return id;
    }
    
    // Узнать тип корабля
    public int getType() {
    	return type;
    }

    public void draw(Graphics g) {
        // Отрисовка изображения с учетом поворота
        g.drawImage(getRotatedImage(), x, y, null);
    }

    // Считается что его центральная точка (Pivot) это центр задней части корабля с отступом 32 (потому что одна клетка это 64*64 пикселя)
    public void setPosition(int x, int y) {    	
        this.x = x - (ishorizontal ? 0 + 32 : w / 2); // Центрирование для горизонтального
        this.y = y - (ishorizontal ? h / 2 : h - 32); // Центрирование для вертикального
    }
    
    // Запомнить текущее местоположение
    public void savePosition() {
    	temp_x = x;
    	temp_y = y;
    }
    
    // Вернуть корабль на предыдущее место
    public void returnPosition() {
    	x = temp_x;
    	y = temp_y;
    }

    // Находиться ли что-то (например мышь) с координатами (mx, my) на объекте 
    public boolean contains(int mx, int my) {
        return (mx >= x && mx <= x + w) && (my >= y && my <= y + h);
    }
    
    public void rotateImage() {
        // Изменяем угол поворота на 90 градусов
        rotationAngle = (rotationAngle - 90) % 180; // Угол поворота: 0, 90
        
        // Меняем положение
        ishorizontal = !ishorizontal;
        
        // Картинка переворачивается, но её размеры не меняются, поэтому меняем размер корабля 
        // чтобы захват мышью работало корректно
        if (ishorizontal) {
        	this.w = image.getWidth(null);
            this.h = image.getHeight(null);
        } else {
        	this.w = image.getHeight(null);
            this.h = image.getWidth(null);
        }
    }

    private Image getRotatedImage() {
    	int width = image.getWidth(null);
        int height = image.getHeight(null);

        int newWidth = rotationAngle % 180 == 0 ? width : height;
        int newHeight = rotationAngle % 180 == 0 ? height : width;

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) rotatedImage.getGraphics();

        g2d.rotate(Math.toRadians(rotationAngle), newWidth / 2.0, newHeight / 2.0);
        g2d.drawImage(image, (newWidth - width) / 2, (newHeight - height) / 2, null);
        g2d.dispose();

        return rotatedImage;
    }
}
