package com.SuperGame.Objects;

import java.awt.Graphics;
import java.util.ArrayList;

import com.SuperGame.GameManager.TileState;
import com.SuperGame.Utils.SoundManager;

public class FieldPlay extends Field{
	private Ship[] ships = new Ship[10]; // Корабли
	private Tile selectedTile = null;
	
	private ArrayList<int[]> missed = new ArrayList<>();
	private ArrayList<int[]> hited = new ArrayList<>();
	
	public boolean isPlayer;
	
	public FieldPlay(int x, int y, boolean isPlayer) {
		super(x, y);
		this.isPlayer = isPlayer;
	}
	
	public FieldPlay(int x, int y, boolean isPlayer, double scale) {
		super(x, y, scale);
		this.isPlayer = isPlayer;
		
		if (isPlayer) {
			loadField();
		
			// Инициализация кораблей
			for (int i = 0; i < 10; i++) {
				Tile importTile = shipsImagesPos.get(i+1);
				Tile localTile = tiles[importTile.getI()][importTile.getJ()];
				ships[i] = new Ship(0, 0, i+1, scale);
				ships[i].setIshorizontal(shipsIsHorizontal.get(i+1));
				ships[i].setPosition((int)(localTile.getX() + 32 * scale), (int)(localTile.getY() + 32 * scale));
			}
		}
	}
	
	@Override
	public void draw(Graphics g) {
        // Отрисовка поля
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                tile.setImage(getTileImage(tile.status));
                tile.draw(g);
            }
        }
        
        // Отрисовка кораблей
        for (Ship ship : ships) {
        	if (ship != null)ship.draw(g);      
        } 
    }
	
	private void drawMissedAndHited() {
		cleanField();
		for (int[] array : missed) {
			tiles[array[0]][array[1]].status = TileState.MISS;
		}
		for (int[] array : hited) {
			tiles[array[0]][array[1]].status = TileState.HIT;
		}
	}
	
	public void hover(int mx, int my) {
	    Tile hoveredTile = null;
	    
	    // Проверяем, над какой плиткой находится мышь
	    for (Tile[] row : tiles) {
	        for (Tile tile : row) {
	            if (tile.contains(mx, my)) {
	                hoveredTile = tile;
	                break;
	            }
	        }
	        if (hoveredTile != null) break;
	    }

	    // Если мышь находится над новой плиткой
	    if (hoveredTile != null && hoveredTile != selectedTile && hoveredTile.status == TileState.EMPTY) {
	        // Если была ранее выделенная плитка, сбрасываем ее состояние
	        if (selectedTile != null) {
	        	drawMissedAndHited();
	        }

	        // Обновляем состояние новой плитки и проигрываем звук
	        selectedTile = hoveredTile;
	        selectedTile.status = TileState.HOVER;
	        SoundManager.playSound("/sounds/cursor_aif.wav");
	    } 
	    // Если курсор не над плиткой, сбрасываем выделение
	    else if (hoveredTile == null && selectedTile != null && selectedTile.status == TileState.HOVER) {
	        selectedTile.status = TileState.EMPTY;
	        selectedTile = null;  // Сбрасываем ссылку на выбранную плитку
	    }
	}
	
	public TileState getTileStatus(Tile tile) {
		int I = tile.getI();
		int J = tile.getJ();
		
		return tiles[I][J].status;
	}
	
	public void Hit() {
		if (selectedTile != null && selectedTile.status == TileState.HOVER) {
			if (selectedTile.getI() % 2 == 0) {
				selectedTile.status = TileState.MISS;
				int[] missPos = {selectedTile.getI(),selectedTile.getJ()};
				missed.add(missPos);
				SoundManager.playSound("/sounds/Miss.wav");
			} else {
				selectedTile.status = TileState.HIT;
				int[] hitPos = {selectedTile.getI(),selectedTile.getJ()};
				hited.add(hitPos);
				SoundManager.playSound("/sounds/Boom.wav");
			}
		}
	}
	
	public TileState getStatus() {
		return TileState.MISS;
	}
}
