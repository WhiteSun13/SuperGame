package com.SuperGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.SuperGame.Scenes.InGamePanel;

public class GameManager {
    private static GameManager instance;
    public static boolean isEnemyReady = false;
    public static boolean isServer = false;
    public static boolean playWithAI = false;

    public static Map<Integer, int[][]> shipsPosCurrent = new HashMap<>();
    public static Map<Integer, Boolean> shipsIsHorizontalCurrent = new HashMap<>();
    public static Map<Integer, int[]> shipsImagesPosCurrent = new HashMap<>();

    public static boolean isTurn = false;
    public static ArrayList<int[]> missed = new ArrayList<int[]>();
    public static ArrayList<int[]> hited = new ArrayList<int[]>();
    public static InGamePanel GamePanel;
    
    // Список слушателей событий
    private static ArrayList<GameEventListener> listeners = new ArrayList<>();

    private GameManager() {
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

//    public enum GameState {
//        MAIN_MENU,
//        PLAYING,
//        SETING
//    }

    public enum TileState {
        EMPTY,		// пустой
        MISS,		// мимо
        HIT,		// попал
        DEAD,		// убил
        ALLOW,		// можно разместить корабль (перед игрой)
    	SHIP_SET,	// занят / установлен корабль
        BLOCK, 		// нельзя ставить корабль (перед игрой)
        HOVER 		// выбрана клетка (во время игры)
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
    
    public static void newGame() {
    	missed = new ArrayList<int[]>();
        hited = new ArrayList<int[]>();
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
	                    boolean found = false;
	                    for (int[] hit : hited) {
	                        if (Arrays.equals(hit, pos)) {
	                            found = true;
	                            break;
	                        }
	                    }
	                    if (!found) {
	                        shipDestroyed = false;
	                        break;
	                    }
	                }

                    // Если корабль уничтожен
                    if (shipDestroyed) {
                        System.out.println("PLAYER SHIP DESTROYED");

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
    
    public static void setTurn(boolean i) {
        isTurn = i;
    }
}
