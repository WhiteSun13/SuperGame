package com.SuperGame.Objects;

import com.SuperGame.GameManager;
import com.SuperGame.GameManager.TileState;
import com.SuperGame.Utils.SoundManager;

public class FieldSet extends Field{
	public int[][] selectedShipTiles;
	public Tile selectedTile;
    public boolean canSetShip = true;

	public FieldSet(int x, int y) {
		super(x, y);
	}
	
	public FieldSet(int x, int y, double scale) {
		super(x, y, scale);
	}

    public void setShip(Ship ship) {
        shipsPos.put(ship.getID(), selectedShipTiles);
        shipsIsHorizontal.put(ship.getID(), ship.getIshorizontal());
        int[] selectedTilePos = {selectedTile.getI(), selectedTile.getJ()};
        shipsImagesPos.put(ship.getID(), selectedTilePos);
        drawWarships();
        selectedTile = null;
    }
	
    public void check(int mx, int my, Ship ship, boolean forced) {
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

        // Если мышь находится над новой плиткой или forced = true
        if ((hoveredTile != null && hoveredTile != selectedTile) || forced) {
            // Сбрасываем состояние предыдущей выделенной плитки
            if (selectedTile != null && !forced) {
                selectedTile.status = TileState.EMPTY;
            }

            // Обновляем выделенную плитку и проигрываем звук
            selectedTile = hoveredTile;
            SoundManager.playSound("/sounds/cursor_aif.wav");

            // Проверка размещения корабля
            if (ship.getIshorizontal()) {
                checkShipPlacement(ship, true);
            } else {
                checkShipPlacement(ship, false);
            }
        } 
        // Если курсор не над плиткой и принудительного выделения нет
        else if (hoveredTile == null && selectedTile != null && !forced) {
            selectedTile.status = TileState.EMPTY;
            selectedTile = null;  // Сбрасываем ссылку на выделенную плитку
            cleanField();
            drawWarships();
        }
    }


    private void checkShipPlacement(Ship ship, boolean isHorizontal) {
        int shipSize = ship.getType();
        selectedShipTiles = new int[shipSize][2];

        cleanField();
        drawWarships();
        canSetShip = true;
        
        if (selectedTile == null) {
        	canSetShip = false;
        	return;
        }

        int startX = selectedTile.getI();
        int startY = selectedTile.getJ();

        // Проверка границ и соседних клеток
        if (!checkBordersAndNeighbors(ship, isHorizontal)) {
            canSetShip = false;
        }

        // Расстановка корабля или пометка ошибок
        for (int k = 0; k < shipSize; k++) {
            int i = isHorizontal ? startX + k : startX;
            int j = isHorizontal ? startY : startY - k;

            if (i >= 0 && i < 10 && j >= 0 && j < 10) {
                if (tiles[i][j].shipId == 0 || tiles[i][j].shipId == ship.getID()) {
                    // Если корабль можно разместить
                    if (canSetShip) {
                        tiles[i][j].status = GameManager.TileState.ALLOW;  // Зеленая клетка для корректного размещения
                    } else {
                        tiles[i][j].status = GameManager.TileState.BLOCK;  // Красная клетка при ошибке размещения
                    }
                    selectedShipTiles[k][0] = i;
                    selectedShipTiles[k][1] = j;
                } else {
                    tiles[i][j].status = GameManager.TileState.BLOCK;  // Красная клетка при ошибке размещения
                    canSetShip = false;
                }
            } else {
                if (i<10 && j>=0) tiles[i][j].status = GameManager.TileState.BLOCK;  // Красная клетка, если выходит за границы поля
                canSetShip = false;
            }
        }
    }

    private boolean checkBordersAndNeighbors(Ship ship, boolean isHorizontal) {
        int shipSize = ship.getType();
        int startX = selectedTile.getI();
        int startY = selectedTile.getJ();

        for (int k = 0; k < shipSize; k++) {
            int i = isHorizontal ? startX + k : startX;
            int j = isHorizontal ? startY : startY - k;

            if (i >= 10 || j >= 10 || i < 0 || j < 0) {
                return false;  // Выход за пределы поля
            }

            // Проверка соседних клеток
            if (!isPositionValid(i, j, ship.getID())) {
                return false;
            }
        }

        return true;
    }

    private boolean isPositionValid(int i, int j, int shipId) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int ni = i + dx;
                int nj = j + dy;
                if (ni >= 0 && ni < 10 && nj >= 0 && nj < 10) {
                    if (tiles[ni][nj].status == GameManager.TileState.SHIP_SET && tiles[ni][nj].shipId != shipId) {
                        return false;  // Вокруг корабля есть другой корабль
                    }
                }
            }
        }
        return true;
    }

}
