package com.SuperGame.Objects;

import java.awt.Graphics;
import java.awt.Image;

import com.SuperGame.GameManager.TileState;
import com.SuperGame.Utils.ResourceLoader;

public class Tile {
    private int i, j;
    private int x, y;
    private Image image;
    private double scale;  // Добавлено поле для масштаба

    public int shipId = 0;
    public TileState status = TileState.EMPTY;

    // Конструктор по умолчанию с масштабом 1.0 (без изменений размера)
    public Tile(int i, int j, int xOffset, int yOffset) {
        this(i, j, xOffset, yOffset, 1.0);  // Вызов перегруженного конструктора с масштабом 1.0
    }

    // Перегруженный конструктор с параметром масштаба
    public Tile(int i, int j, int xOffset, int yOffset, double scale) {
        this.i = i;
        this.j = j;
        this.scale = scale;

        // Загрузка изображения
        image = ResourceLoader.loadImageAsURL("/images/tiles/tile_empty.png");

        // Применение смещения и расчет координат
        x = i * (int)(image.getWidth(null) * scale) + xOffset;
        y = j * (int)(image.getHeight(null) * scale) + yOffset;
    }

    public void draw(Graphics g) {
        // Отрисовка изображения с учетом масштаба
        int scaledWidth = (int)(image.getWidth(null) * scale);
        int scaledHeight = (int)(image.getHeight(null) * scale);
        g.drawImage(image, x, y, scaledWidth, scaledHeight, null);
    }

    // Метод для замены изображения
    public void setImage(String imagePath) {
        image = ResourceLoader.loadImageAsURL(imagePath);
    }

    // Находится ли что-то (например, мышь) с координатами (mx, my) на объекте 
    public boolean contains(int mx, int my) {
        return (mx >= x && mx <= x + (int)(image.getWidth(null) * scale)) && 
               (my >= y && my <= y + (int)(image.getHeight(null) * scale));
    }

    public void getInfo() {
        System.out.println(i + ", " + j + ", " + shipId + ", " + status);
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getScale() {
        return scale;
    }

//    public void setScale(double scale) {
//        this.scale = scale;
//        // Обновление координат x и y при изменении масштаба
//        x = i * (int)(image.getWidth(null) * scale) + xOffset;
//        y = j * (int)(image.getHeight(null) * scale) + yOffset;
//    }
}
