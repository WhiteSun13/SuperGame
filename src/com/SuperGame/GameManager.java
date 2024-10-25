package com.SuperGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GameManager {
    private static GameManager instance;
    private GameState currentState;

    public static Map<Integer, int[][]> shipsPosCurrent = new HashMap<>();
    public static Map<Integer, Boolean> shipsIsHorizontalCurrent = new HashMap<>();
    public static Map<Integer, int[]> shipsImagesPosCurrent = new HashMap<>();

    public static boolean isTurn = true;
    public static ArrayList<int[]> missed = new ArrayList();
    public static ArrayList<int[]> hited = new ArrayList();
    
    // Список слушателей событий
    private static ArrayList<GameEventListener> listeners = new ArrayList<>();

    private GameManager() {
        currentState = GameState.MAIN_MENU;
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public enum GameState {
        MAIN_MENU,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    public enum TileState {
        EMPTY, MISS, HIT, DEAD, ALLOW, SHIP_SET, BLOCK, HOVER
    }

    // Добавление слушателя
    public static void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    // Удаление слушателя
    public static void removeGameEventListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    // Уведомление всех слушателей
    private void notifyListeners(int[] tilePos, boolean isHit) {
        for (GameEventListener listener : listeners) {
            listener.onPositionChecked(tilePos, isHit);  // Уведомление слушателей
        }
    }
    
    public static Object[] checkPosition(int[] position) {
        // Проверяем, была ли эта позиция уже атакована
        for (int[] hit : hited) {
            if (Arrays.equals(hit, position)) {
                return new Object[]{false, false, null, null, null, null};
            }
        }

        for (int[] miss : missed) {
            if (Arrays.equals(miss, position)) {
                return new Object[]{false, false, null, null, null, null};
            }
        }

        // Проверяем, попали ли мы в какой-то корабль
        for (Map.Entry<Integer, int[][]> entry : shipsPosCurrent.entrySet()) {
            int shipId = entry.getKey();
            int[][] shipPositions = entry.getValue();

            for (int[] shipPos : shipPositions) {
                if (Arrays.equals(shipPos, position)) {
                    hited.add(position);
                    System.out.println("HIT");

                    // Уведомляем подписчиков об успешном попадании
                    GameManager.getInstance().notifyListeners(position, true);

                    // Проверяем, уничтожен ли корабль
                    boolean shipDestroyed = true;
                    for (int[] pos : shipPositions) {
                        if (!hited.contains(pos)) {
                            shipDestroyed = false;
                            break;
                        }
                    }

                    // Если корабль уничтожен
                    if (shipDestroyed) {
                        System.out.println("SHIP DESTROYED");

                        // Получаем дополнительные данные о корабле
                        boolean isHorizontal = shipsIsHorizontalCurrent.get(shipId);
                        int[] imagePos = shipsImagesPosCurrent.get(shipId);

                        // Возвращаем информацию о попадании, уничтожении и данные корабля
                        return new Object[]{true, true, shipId, shipPositions, isHorizontal, imagePos};
                    } else {
                        // Если корабль не уничтожен, возвращаем только информацию о попадании
                        return new Object[]{true, false, shipId, null, null, null};
                    }
                }
            }
        }

        // Если это был промах
        missed.add(position);
        System.out.println("MISS");

        // Уведомляем подписчиков о промахе
        GameManager.getInstance().notifyListeners(position, false);

        // Возвращаем информацию о промахе
        return new Object[]{false, false, null, null, null, null};
    }

    public static void giveTurn() {
        isTurn = true;
    }
}
