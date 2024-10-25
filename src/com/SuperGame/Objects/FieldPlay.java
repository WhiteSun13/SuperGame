package com.SuperGame.Objects;

import java.util.*;
import java.awt.Graphics;
import java.util.ArrayList;

import com.SuperGame.GameEventListener;
import com.SuperGame.GameManager;
import com.SuperGame.GameManager.TileState;
import com.SuperGame.Utils.SoundManager;

public class FieldPlay extends Field implements GameEventListener {
	private Ship[] ships = new Ship[10]; // Корабли
	private ArrayList<Ship> enemyShips = new ArrayList<>(); // Корабли для вражеского поля
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
				int[] importTile = shipsImagesPos.get(i+1);
				Tile localTile = tiles[importTile[0]][importTile[1]];
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
        
        // Отрисовка вражеских убитых кораблей
        for (Ship ship : enemyShips) {
        	if (ship != null) ship.draw(g);      
        }
    }
	
	private void drawMissedAndHited() {
		cleanField();
		for (int[] array : missed) {
			tiles[array[0]][array[1]].status = TileState.MISS;
		}
		for (int[] array : hited) {
			if (tiles[array[0]][array[1]].status != TileState.DEAD) {
				tiles[array[0]][array[1]].status = TileState.HIT;
			}
		}
	}
	
	public void addSunkShip(int id, int[][] pos, boolean horizontal, int[] imgPos) {
		if (isPlayer) return;
		Ship enemyShip = new Ship(0, 0, id, scale);
		enemyShip.setIshorizontal(horizontal);
		enemyShip.setPosition((int)(tiles[imgPos[0]][imgPos[1]].getX() + 32 * scale), (int)(tiles[imgPos[0]][imgPos[1]].getY() + 32 * scale));
		enemyShip.setImage();
		
		for (int[] p : pos) {
			tiles[p[0]][p[1]].status = TileState.DEAD;
		}
		
		enemyShips.add(enemyShip);
	}
	
	public List<Integer> getSunkShips() {
		if (!isPlayer) {
			List<Integer> sunkShips = new ArrayList<>();
	        for (Ship ship : enemyShips) {
	        	if (ship != null) sunkShips.add(ship.getID());      
	        }
			return sunkShips;
		}
		
	    List<Integer> sunkShips = new ArrayList<>();
	    
	    for (Map.Entry<Integer, int[][]> entry : shipsPos.entrySet()) {
	        int shipKey = entry.getKey();
	        int[][] shipCoordinates = entry.getValue();

	        boolean isSunk = true;

	        for (int[] coord : shipCoordinates) {
	            if (!isCoordinateHit(coord)) {
	                isSunk = false;
	                break;
	            }
	        }

	        if (isSunk) {
	            ships[shipKey-1].setImage();
	            System.out.println("Ship " + shipKey + " is sunk.");
	            sunkShips.add(shipKey);  // Добавляем потопленный корабль в список
	        }
	    }
	    
	    return sunkShips;  // Возвращаем список потопленных кораблей
	}


    // Метод для проверки, было ли попадание в данную координату
    private boolean isCoordinateHit(int[] coord) {
        for (int[] hit : hited) {
            if (Arrays.equals(coord, hit)) {
                return true;
            }
        }
        return false;
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
	
	public int[] getSelctedTilePosition() {
		if (selectedTile != null && selectedTile.status == TileState.HOVER) {
			int I = selectedTile.getI();
			int J = selectedTile.getJ();
			return new int[]{I,J};
		} else {
			return null;
		}
	}
	
	public void HitOrMiss(int[] tilePos, boolean isHited) {
		Tile t = tiles[tilePos[0]][tilePos[1]];
		
		if (isHited) {
			t.status = TileState.HIT;
			hited.add(tilePos);
			SoundManager.playSound("/sounds/Boom.wav");
		} else {
			t.status = TileState.MISS;
			missed.add(tilePos);
			SoundManager.playSound("/sounds/Miss.wav");
		}
	}

	@Override
	public void onPositionChecked(int[] tilePos, boolean isHit) {
		System.out.println("Event");
		if (!isPlayer) return;
		HitOrMiss(tilePos, isHit);
		getSunkShips();
	}
}
