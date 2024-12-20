package com.SuperGame.Objects;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.SuperGame.Utils.ResourceLoader;
import com.SuperGame.Utils.Settings;

public class Ship {
    private int id; // id Корабля
    private int x, y, type; // x y и размер корабля
    private int temp_x, temp_y; // Предыдущее положение
    private int init_x, init_y; // Начальное положение 
    private int w, h; // ширина и высота
    private boolean ishorizontal = true; // Находится ли сейчас корабль в горизонтальном положении
    private Image image;
    private int rotationAngle; // Угол поворота в градусах
    private double scale = 1.0; // Масштаб по умолчанию
    
    public boolean seted = false;

    // Основной конструктор
    public Ship(int x, int y, int id) {
        this.x = (int) (x * Settings.scale);
        this.y = (int) (y * Settings.scale);
        init_x = (int) (x * Settings.scale);
        init_y = (int) (y * Settings.scale);
        
        this.id = id;
        
        if (id == 10) {
            this.type = 4;
        } else if (id >= 8) {
            this.type = 3;
        } else if (id >= 5) {
            this.type = 2;
        } else {
            this.type = 1;
        }
        
        // Загрузка изображения
        image = ResourceLoader.loadImageAsURL(String.format("/images/ships/ship%s.png", type));
        rotationAngle = 0; // Начальный угол
        
        this.w = image.getWidth(null);
        this.h = image.getHeight(null);
    }
    
    // Перегруженный конструктор с параметром масштаба
    public Ship(int x, int y, int id, double scale) {
        this(x, y, id); // Вызов основного конструктора
        setScale(scale); // Установка масштаба
    }
    
    // Установка масштаба
    public void setScale(double scale) {
        this.scale = scale;
        updateDimensions();
    }

    // Обновление размеров с учётом масштаба и ориентации
    private void updateDimensions() {
        if (ishorizontal) {
            this.w = (int) (image.getWidth(null) * scale);
            this.h = (int) (image.getHeight(null) * scale);
        } else {
            this.w = (int) (image.getHeight(null) * scale);
            this.h = (int) (image.getWidth(null) * scale);
        }
    }

    // Метод для замены изображения корабля
    public void setImage() {
        this.image = ResourceLoader.loadImageAsURL(String.format("/images/ships/shipDead%s.png", type));
        updateDimensions(); // Обновляем размеры с учётом нового изображения и масштаба
    }

    // Узнать номер корабля
    public int getID() {
        return id;
    }

    // Узнать тип корабля
    public int getType() {
        return type;
    }

    public boolean getIshorizontal() {
        return ishorizontal;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void resetPos() {
        x = init_x;
        y = init_y;
        seted = false;
        setIshorizontal(true);
    }

    public void setIshorizontal(boolean b) {
        if (ishorizontal != b) {
            rotateImage();
        }
    }

    public void draw(Graphics g) {
//        // Отрисовка изображения с учетом поворота и масштаба
//        setScale(Settings.scale);
        g.drawImage(getRotatedImage(), x, y, w, h, null);
    }

    public void setPosition(int x, int y) {       
        this.x = x - (ishorizontal ? 0 + h / 2 : w / 2); // Центрирование для горизонтального
        this.y = y - (ishorizontal ? h / 2 : h - w / 2); // Центрирование для вертикального
    }

    public void savePosition() {
        temp_x = x;
        temp_y = y;
    }

    public void returnPosition() {
        x = temp_x;
        y = temp_y;
    }

    public boolean contains(int mx, int my) {
        return (mx >= x && mx <= x + w) && (my >= y && my <= y + h);
    }

    public void rotateImage() {
        if (seted) return;

        // Изменяем угол поворота на 90 градусов
        rotationAngle = (rotationAngle - 90) % 180; // Угол поворота: 0, 90
        
        // Меняем ориентацию
        ishorizontal = !ishorizontal;

        // Обновляем размеры корабля с учетом поворота и масштаба
        updateDimensions();
    }

    private Image getRotatedImage() {
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        int newWidth = rotationAngle % 180 == 0 ? width : height;
        int newHeight = rotationAngle % 180 == 0 ? height : width;

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) rotatedImage.getGraphics();

        g2d.rotate(Math.toRadians(rotationAngle), newWidth / 2.0, newHeight / 2.0);
        g2d.drawImage(image, (newWidth - width) / 2, (newHeight - height) / 2, (int) (width), (int) (height), null);
        g2d.dispose();

        return rotatedImage;
    }
}
