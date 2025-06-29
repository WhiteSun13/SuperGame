package com.SuperGame.Objects;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.SuperGame.GameManager.TileState;
import com.SuperGame.Utils.ResourceLoader;

public class Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    private int i, j;
    private int x, y;
    private transient Image image; 
    private double scale;
    private String imagePath;

    public int shipId = 0;
    public TileState status = TileState.EMPTY;

    public Tile(int i, int j, int xOffset, int yOffset) {
        this(i, j, xOffset, yOffset, 1.0);
    }

    public Tile(int i, int j, int xOffset, int yOffset, double scale) {
        this.i = i;
        this.j = j;
        this.scale = scale;
        this.imagePath = "/images/tiles/tile_empty.png";
        loadImage();

        x = i * (int) (image.getWidth(null) * scale) + xOffset;
        y = j * (int) (image.getHeight(null) * scale) + yOffset;
    }

    private void loadImage() {
        image = ResourceLoader.loadImageAsURL(imagePath);
    }

    public void draw(Graphics g) {
        int scaledWidth = (int) (image.getWidth(null) * scale);
        int scaledHeight = (int) (image.getHeight(null) * scale);
        g.drawImage(image, x, y, scaledWidth, scaledHeight, null);
    }

    public void setImage(String imagePath) {
        this.imagePath = imagePath;
        loadImage();
    }

    public boolean contains(int mx, int my) {
        return (mx >= x && mx <= x + (int) (image.getWidth(null) * scale)) &&
                (my >= y && my <= y + (int) (image.getHeight(null) * scale));
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


	public String getImagePath() {
		return imagePath;
	}


	public Image getImage() {
		return image;
	}


	public int getShipId() {
		return shipId;
	}


	public TileState getStatus() {
		return status;
	}

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loadImage();
    }
}