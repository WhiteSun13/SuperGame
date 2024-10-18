package com.SuperGame.Objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomShips {
	public final int BOARD_SIZE = 10;
    public int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    
    public Map<Integer, int[][]> shipsPos = new HashMap<>();
    public Map<Integer, Boolean> shipsIsHorizontal = new HashMap<>();
    public Map<Integer, int[]> shipsImagesPos = new HashMap<>();
    
    private Random random = new Random();

    // Метод для расстановки всех кораблей
    public void placeShips() {
    	board = new int[BOARD_SIZE][BOARD_SIZE];
    	
    	shipsPos = new HashMap<>();
    	shipsIsHorizontal = new HashMap<>();
    	shipsImagesPos = new HashMap<>();
    	
        int[] shipSizes = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        int shipIndex = 10; // Идентификаторы кораблей начинаются с 10 и уменьшаются до 1

        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                placed = tryPlaceShip(shipIndex, size);
            }
            shipIndex--;
        }
    }

    // Метод для попытки разместить корабль
    private boolean tryPlaceShip(int shipId, int size) {
        boolean horizontal = random.nextBoolean(); // Определяем горизонтальное или вертикальное положение
        int x = random.nextInt(BOARD_SIZE);
        int y = random.nextInt(BOARD_SIZE);
        
        if (canPlaceShip(x, y, size, horizontal)) {
            placeShipOnBoard(shipId, x, y, size, horizontal);
            return true;
        }
        return false;
    }

    // Проверяем, можно ли разместить корабль
    private boolean canPlaceShip(int x, int y, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int nx = horizontal ? x + i : x;
            int ny = horizontal ? y : y + i;
            if (nx >= BOARD_SIZE || ny >= BOARD_SIZE || board[ny][nx] != 0) {
                return false;
            }
            // Проверка на то, что корабль не касается других
            if (!checkSurroundings(nx, ny)) {
                return false;
            }
        }
        return true;
    }

    // Проверяем, что клетка не касается других кораблей
    private boolean checkSurroundings(int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nx = x + i;
                int ny = y + j;
                if (nx >= 0 && ny >= 0 && nx < BOARD_SIZE && ny < BOARD_SIZE) {
                    if (board[ny][nx] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Размещаем корабль на доске
    private void placeShipOnBoard(int shipId, int x, int y, int size, boolean horizontal) {
        int[][] positions = new int[size][2]; // Позиции корабля
        
        for (int i = 0; i < size; i++) {
            int nx = horizontal ? x + i : x;
            int ny = horizontal ? y : y + i;
            board[ny][nx] = 1;
            positions[i] = new int[]{nx, ny};
        }

        // Сохраняем позиции корабля
        shipsPos.put(shipId, positions);
        shipsIsHorizontal.put(shipId, horizontal);

        // Позиция изображения (самая левая или нижняя клетка корабля)
        if (horizontal) {
            shipsImagesPos.put(shipId, new int[]{x, y});
        } else {
            shipsImagesPos.put(shipId, new int[]{x, y + size - 1});
        }
    }

    // Метод для визуализации игрового поля
    public void printBoard() {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (board[y][x] == 1) {
                    System.out.print("* ");
                } else {
                    System.out.print("- ");
                }
            }
            System.out.println(); // Переход на новую строку после каждой строки поля
        }
    }

//    public static void main(String[] args) {
//    	RandomShips game = new RandomShips();
//        game.placeShips();
//        
//     // Вывод результата (например, можно вывести позиции кораблей)
//        game.shipsPos.forEach((key, value) -> {
//            System.out.println("Корабль " + key + ":");
//            for (int[] pos : value) {
//                System.out.println("[" + pos[0] + ", " + pos[1] + "]");
//            }
//            System.out.println("Горизонтальный: " + game.shipsIsHorizontal.get(key));
//            System.out.println("Изображение в: [" + game.shipsImagesPos.get(key)[0] + ", " + game.shipsImagesPos.get(key)[1] + "]");
//        });
//        
//        // Вывод поля с кораблями
//        game.printBoard();
//    }
}
