package com.SuperGame;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Tile {
	private int x,y;
	private Image image;
	
	public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        
     // Загрузка изображения
        image = new ImageIcon(String.format("src/com/SuperGame/tile_empty.png")).getImage();
	}
	
	public void draw(Graphics g) {
		// Отрисовка изображения
        g.drawImage(image, x, y, null);
	}
	
	// Находиться ли что-то (например мышь) с координатами (mx, my) на объекте 
    public boolean contains(int mx, int my) {
        return (mx >= x && mx <= x + image.getWidth(null)) && (my >= y && my <= y + image.getHeight(null));
    }
}
