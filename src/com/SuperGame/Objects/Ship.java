package com.SuperGame.Objects;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.SuperGame.Utils.ResourceLoader;

public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int x, y, type;
    private int temp_x, temp_y;
    private int init_x, init_y;
    private int w, h;
    private boolean ishorizontal = true;
    private transient Image image;
    private String imagePath;
    private int rotationAngle;
    private double scale = 1.0;

    public boolean seted = false;

    public Ship(int x, int y, int id) {
        this.x = x;
        this.y = y;
        init_x = x;
        init_y = y;

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

        this.imagePath = String.format("/images/ships/ship%s.png", type);
        loadImage();
        rotationAngle = 0;

        this.w = image.getWidth(null);
        this.h = image.getHeight(null);
    }

    public Ship(int x, int y, int id, double scale) {
        this(x, y, id);
        setScale(scale);
    }

    private void loadImage() {
        image = ResourceLoader.loadImageAsURL(imagePath);
    }

    public void setScale(double scale) {
        this.scale = scale;
        updateDimensions();
    }

    private void updateDimensions() {
        if (ishorizontal) {
            this.w = (int) (image.getWidth(null) * scale);
            this.h = (int) (image.getHeight(null) * scale);
        } else {
            this.w = (int) (image.getHeight(null) * scale);
            this.h = (int) (image.getWidth(null) * scale);
        }
    }

    public void setImage() {
        this.imagePath = String.format("/images/ships/shipDead%s.png", type);
        loadImage();
        updateDimensions();
    }

    public int getID() {
        return id;
    }

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

    public int getWidth() {
    	return w;
    }
    
    public int getHeight() {
    	return h;
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
        g.drawImage(getRotatedImage(), x, y, w, h, null);
    }

    public void setPosition(int x, int y) {
        this.x = x - (ishorizontal ? 0 + h / 2 : w / 2);
        this.y = y - (ishorizontal ? h / 2 : h - w / 2);
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
    	if (image == null) { // Handle the case where image loading failed
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); // Return a dummy image
        }


        int width = image.getWidth(null);
        int height = image.getHeight(null);

        int newWidth = rotationAngle % 180 == 0 ? width : height;
        int newHeight = rotationAngle % 180 == 0 ? height : width;

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) rotatedImage.getGraphics();

        g2d.rotate(Math.toRadians(rotationAngle), newWidth / 2.0, newHeight / 2.0);
        g2d.drawImage(image, (newWidth - width) / 2, (newHeight - height) / 2, (int) (width), (int) (height) , null);
        
        g2d.dispose();

        return rotatedImage;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loadImage();
    }
}