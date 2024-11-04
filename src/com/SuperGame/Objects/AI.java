package com.SuperGame.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.SuperGame.GameManager;

public class AI {
    private FieldPlay playerField;  // Поле игрока
    private Random random = new Random();  // Генератор случайных чисел
    
    private Map<Integer, int[][]> shipsPos = new HashMap<>();  // Позиции кораблей
    private Map<Integer, Boolean> shipsIsHorizontal = new HashMap<>();  // Ориентация кораблей
    private Map<Integer, int[]> shipsImagesPos = new HashMap<>();  // Позиции изображений кораблей
    
    private boolean isTurn = false;  // Флаг, указывающий, чья сейчас очередь
    private ArrayList<int[]> missed = new ArrayList<>();  // Массив пропущенных выстрелов
    private ArrayList<int[]> hited = new ArrayList<>();  // Массив попавших выстрелов
    
    private ArrayList<int[]> enemyMissed = new ArrayList<>();  // Пропущенные выстрелы противника
    private ArrayList<int[]> enemyHited = new ArrayList<>();  // Попавшие выстрелы противника
    
    private int[] lastHit = null;  // Координаты последнего попадания
    private boolean isTargetingMode = false; // Режим прицеливания AI
    private boolean isHorizontal = false;    // Ориентация корабля, если она определена

    public AI() {
        RandomShips r = new RandomShips();
        
        r.placeShips();  // Размещаем корабли случайным образом
        r.printBoard();  // Печатаем поле с кораблями
        
        shipsPos = r.shipsPos;  // Получаем позиции кораблей
        shipsIsHorizontal = r.shipsIsHorizontal;  // Получаем ориентацию кораблей
        shipsImagesPos = r.shipsImagesPos;  // Получаем позиции изображений кораблей
        
        playerField = new FieldPlay(0, 0, true);  // Инициализируем игровое поле
        playerField.loadField(r.shipsPos, r.shipsIsHorizontal, r.shipsImagesPos);  // Загружаем поле
    }
    
    // Метод для проверки позиции выстрела
    public Object[] checkPosition(int[] position) {
        // Проверка на попадание в уже обстрелянные клетки
        for (int[] hit : hited) {
            if (Arrays.equals(hit, position)) {
                return new Object[]{false, false, null, null, null, null};  // Попадание, но уже обстрелянная клетка
            }
        }

        for (int[] miss : missed) {
            if (Arrays.equals(miss, position)) {
                return new Object[]{false, false, null, null, null, null};  // Промах, но уже обстрелянная клетка
            }
        }

        // Проверка, попал ли выстрел в корабль
        for (Map.Entry<Integer, int[][]> entry : shipsPos.entrySet()) {
            int shipId = entry.getKey();  // ID корабля
            int[][] shipPositions = entry.getValue();  // Позиции корабля

            // Проверяем каждую позицию корабля
            for (int[] shipPos : shipPositions) {
                if (Arrays.equals(shipPos, position)) {  // Если попали
                    hited.add(position);  // Добавляем в массив попаданий
                    boolean shipDestroyed = true;  // Предполагаем, что корабль уничтожен
                    
                    // Проверяем, уничтожен ли действительно корабль
                    for (int[] pos : shipPositions) {
                        boolean found = false;
                        for (int[] hit : hited) {
                            if (Arrays.equals(hit, pos)) {
                                found = true;  // Если нашли попадание в позицию корабля
                                break;
                            }
                        }
                        if (!found) {  // Если не нашли
                            shipDestroyed = false;  // Корабль не уничтожен
                            break;
                        }
                    }

                    // Возвращаем информацию о попадании
                    if (shipDestroyed) {
                        boolean isHorizontal = shipsIsHorizontal.get(shipId);  // Получаем ориентацию
                        int[] imagePos = shipsImagesPos.get(shipId);  // Получаем позицию изображения

                        return new Object[]{true, true, shipId, shipPositions, isHorizontal, imagePos};  // Попадание и уничтожение
                    } else {
                        return new Object[]{true, false, shipId, null, null, null};  // Попадание, но не уничтожение
                    }
                }
            }
        }

        // Если не попали
        missed.add(position);  // Добавляем в массив промахов
        return new Object[]{false, false, null, null, null, null};  // Не попали
    }
    
    // Метод, который передает ход AI
    public void giveTurn() {
        isTurn = true;  // Устанавливаем флаг хода
        fire();  // Начинаем стрельбу
    }

    // Метод для выполнения стрельбы
    private void fire() {
        while (isTurn) {
            int[] tilePos;

            if (isTargetingMode && lastHit != null) {
                tilePos = getNextTargetPosition();  // Прицеливаемся на соседние клетки
            } else {
                tilePos = getRandomPosition();  // Случайный выбор в охотничьем режиме
            }
            
            System.out.println(tilePos[0] + " / " + tilePos[1]);  // Выводим позицию для отладки
            Object[] enemyTileInfo = GameManager.checkPosition(tilePos);  // Проверяем позицию у противника

            boolean isHited = (boolean) enemyTileInfo[0];  // Попадание
            boolean isSink = (boolean) enemyTileInfo[1];  // Уничтожение корабля

            if (isHited) {
                enemyHited.add(tilePos);  // Добавляем в массив попаданий
                lastHit = tilePos;  // Сохраняем последнюю позицию попадания

                if (isSink) {
                    resetTargetingMode();  // Сбрасываем режим прицеливания, если корабль уничтожен
                } else {
                    isTargetingMode = true;  // Включаем режим прицеливания
                    determineOrientation(tilePos);  // Определяем ориентацию корабля
                    fireAtAdjacentTiles(tilePos); // Стреляем в соседние сегменты
                }
            } else {
                enemyMissed.add(tilePos);  // Добавляем в массив промахов
                isTurn = false;  // Завершаем ход
                GameManager.giveTurn();  // Передаем ход противнику
            }
        }
    }
    
    // Метод для стрельбы в соседние клетки
    private void fireAtAdjacentTiles(int[] hitTile) {
        int[][] directions = {
            {0, 1},   // Вправо
            {0, -1},  // Влево
            {1, 0},   // Вниз
            {-1, 0}   // Вверх
        };

        for (int[] direction : directions) {
            int[] nextPos = {hitTile[0] + direction[0], hitTile[1] + direction[1]};
            if (isValidPosition(nextPos) && !isPositionUsed(nextPos)) {
                Object[] enemyTileInfo = GameManager.checkPosition(nextPos);  // Проверяем позицию

                boolean isHit = (boolean) enemyTileInfo[0];  // Попадание

                if (isHit) {
                    enemyHited.add(nextPos);  // Добавляем в массив попаданий
                    lastHit = nextPos;  // Сохраняем последнюю позицию попадания
                    boolean isSink = (boolean) enemyTileInfo[1];  // Проверяем, уничтожен ли корабль
                    if (!isSink) {
                        fireAtAdjacentTiles(nextPos); // Продолжаем обстрел соседних клеток
                    } else {
                        resetTargetingMode(); // Сбрасываем режим прицеливания, если корабль уничтожен
                    }
                }
            }
        }
    }

    // Метод для получения случайной позиции
    private int[] getRandomPosition() {
        int[] position;
        do {
            position = new int[]{random.nextInt(10), random.nextInt(10)};  // Генерация случайных координат
        } while (isPositionUsed(position));  // Проверяем, использовалась ли позиция
        return position;  // Возвращаем случайную позицию
    }

    // Метод для получения следующей цели
    private int[] getNextTargetPosition() {
        // Определяем направления в зависимости от ориентации
        int[][] directions = isHorizontal
            ? new int[][]{{0, 1}, {0, -1}}  // Горизонтальная ориентация
            : new int[][]{{1, 0}, {-1, 0}}; // Вертикальная ориентация

        // Проверяем соседние позиции
        for (int[] direction : directions) {
            int[] nextPos = {lastHit[0] + direction[0], lastHit[1] + direction[1]};
            if (isValidPosition(nextPos) && !isPositionUsed(nextPos)) {
                return nextPos;  // Возвращаем соседнюю позицию
            }
        }
        
        isTargetingMode = false;  // Возвращаемся в случайный режим, если нет валидных соседей
        return getRandomPosition(); // Возвращаем случайную позицию
    }

    // Метод для определения ориентации корабля
    private void determineOrientation(int[] newHit) {
        if (lastHit != null) {
            isHorizontal = lastHit[0] == newHit[0];  // Если координаты по оси X совпадают, значит корабль горизонтальный
        }
    }

    // Метод для проверки, использовалась ли позиция
    private boolean isPositionUsed(int[] position) {
        for (int[] hit : enemyHited) {
            if (Arrays.equals(hit, position)) return true;  // Если позиция уже была попаданием
        }
        for (int[] miss : enemyMissed) {
            if (Arrays.equals(miss, position)) return true;  // Если позиция уже была промахом
        }
        return false;  // Если позиция не использовалась
    }

    // Метод для проверки, валидна ли позиция
    private boolean isValidPosition(int[] position) {
        return position[0] >= 0 && position[0] < 10 && position[1] >= 0 && position[1] < 10;  // Проверка границ поля
    }

    // Сброс режима прицеливания
    private void resetTargetingMode() {
        isTargetingMode = false;  // Выключаем режим прицеливания
        lastHit = null;  // Сбрасываем координаты последнего попадания
        isHorizontal = false;  // Сбрасываем ориентацию
    }
}