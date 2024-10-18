package com.SuperGame;

import java.util.HashMap;
import java.util.Map;

import com.SuperGame.Objects.Tile;

public class GameManager {

    // Синглтон для управления состояниями игры
    private static GameManager instance;
    
    // Состояние игры (например, MainMenu, Playing, Paused, GameOver)
    private GameState currentState;
    
    public static Map<Integer, int[][]> shipsPosCurrent = new HashMap<>();
    public static Map<Integer, Boolean> shipsIsHorizontalCurrent = new HashMap<>();
    public static Map<Integer, int[]> shipsImagesPosCurrent = new HashMap<>();
    
    // Конструктор приватный для реализации паттерна Singleton
    private GameManager() {
    	currentState = GameState.MAIN_MENU;
    }

    // Метод для получения единственного экземпляра GameManager
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    // Перечисление для состояний игры  (Chat GPT сгенерировала)
    public enum GameState {
        MAIN_MENU,
        PLAYING,
        PAUSED,
        GAME_OVER
    }
    
 // Перечисление для состояний игры
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
}
