package com.SuperGame.Objects;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import com.SuperGame.GameManager;

public class Field {
    public Tile[][] tiles = new Tile[10][10];
    protected int x, y;
    protected double scale = 1;
    
    public Map<Integer, int[][]> shipsPos = new HashMap<>();
    public Map<Integer, Boolean> shipsIsHorizontal = new HashMap<>();
    public Map<Integer, int[]> shipsImagesPos = new HashMap<>();

    public Field(int x, int y) {
        this(x, y, 1.0);
    }

    public Field(int x, int y, double scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;

        // Инициализация поля
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                tiles[i][j] = new Tile(i, j, this.x, this.y, scale);
            }
        }

        for (int i = 1; i <= 10; i++) {
            shipsIsHorizontal.put(i, true);
        }
    }

    public void draw(Graphics g) {
        // Отрисовка поля
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                tile.setImage(getTileImage(tile.status));
                tile.draw(g);
            }
        }
    }
    
    public void saveField() {
    	GameManager.shipsPosCurrent = shipsPos;
    	GameManager.shipsIsHorizontalCurrent = shipsIsHorizontal;
    	GameManager.shipsImagesPosCurrent = shipsImagesPos;
    }
    
    public void loadField(Map<Integer, int[][]> Pos, Map<Integer, Boolean> IsHorizontal, Map<Integer, int[]> ImagesPos) {
    	shipsIsHorizontal = IsHorizontal;
    	shipsPos = Pos;
    	shipsImagesPos = ImagesPos;
    	drawWarships();
    }
    
    public void loadField() {
    	shipsIsHorizontal = GameManager.shipsIsHorizontalCurrent;
    	shipsPos = GameManager.shipsPosCurrent;
    	shipsImagesPos = GameManager.shipsImagesPosCurrent;
    	drawWarships();
    }

    protected String getTileImage(GameManager.TileState status) {
        switch (status) {
        	case HOVER: return "/images/tiles/tile_cyan.png";
            case MISS: return "/images/tiles/tile_blue.png";
            case HIT: return "/images/tiles/tile_red.png";// "/images/tiles/tile_lightRed.png";
            case DEAD: return "/images/tiles/tile_orange.png";
            case ALLOW: return "/images/tiles/tile_green.png";
            case SHIP_SET: return "/images/tiles/tile_cyan.png";
            case BLOCK: return "/images/tiles/tile_red.png";
            default: return "/images/tiles/tile_empty.png";
        }
    }
    
    public boolean allShipsIsSet() {
        for (int i = 1; i <= 10; i++) {
            if (!shipsPos.containsKey(i)) {
                return false;
            }
        }
        return true;
    }

    public void drawWarships() {
        cleanField();

        // Рисуем корабли по их позициям
        shipsPos.forEach((key, positions) -> {
            for (int[] position : positions) {
                Tile tile = tiles[position[0]][position[1]];
                tile.shipId = key;
                tile.status = GameManager.TileState.SHIP_SET;
            }
        });
    }

    public void fullCleanField() {
        cleanField();
        shipsPos.clear();
        shipsImagesPos.clear();
        shipsIsHorizontal.clear();
        for (int i = 1; i <= 10; i++) {
            shipsIsHorizontal.put(i, true);
        }
    }

    protected void cleanField() {
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                tile.status = GameManager.TileState.EMPTY;
                tile.shipId = 0;
            }
        }
    }
}
