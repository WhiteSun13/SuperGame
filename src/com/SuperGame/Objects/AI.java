package com.SuperGame.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.SuperGame.GameManager;

public class AI {
	private FieldPlay playerField;
	Random random = new Random();
	
	private Map<Integer, int[][]> shipsPos = new HashMap<>();
    private Map<Integer, Boolean> shipsIsHorizontal = new HashMap<>();
    private Map<Integer, int[]> shipsImagesPos = new HashMap<>();
    
    private boolean isTurn = false;
    private ArrayList<int[]> missed = new ArrayList();
    private ArrayList<int[]> hited = new ArrayList();
    
    private ArrayList<int[]> enemyMissed = new ArrayList();
    private ArrayList<int[]> enemyHited = new ArrayList();
    
	public AI() {
		RandomShips r = new RandomShips();
		
		r.placeShips();
		r.printBoard();
		
		shipsPos = r.shipsPos;
		shipsIsHorizontal = r.shipsIsHorizontal;
		shipsImagesPos = r.shipsImagesPos;
		
		playerField = new FieldPlay(0, 0, true);
		playerField.loadField(r.shipsPos,r.shipsIsHorizontal,r.shipsImagesPos);
	}
	
	public Object[] checkPosition(int[] position) {
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
	    for (Map.Entry<Integer, int[][]> entry : shipsPos.entrySet()) {
	        int shipId = entry.getKey();
	        int[][] shipPositions = entry.getValue();

	        for (int[] shipPos : shipPositions) {
	            if (Arrays.equals(shipPos, position)) {
	                System.out.println(position[0] + " " + position[1]);
	                hited.add(position);
	                System.out.println("HIT");

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
	                    System.out.println("SHIP DESTROYED");

	                    // Получаем дополнительные данные о корабле
	                    boolean isHorizontal = shipsIsHorizontal.get(shipId);
	                    int[] imagePos = shipsImagesPos.get(shipId);

	                    System.out.println(shipId);

	                    // Возвращаем информацию о попадании, уничтожении и данные корабля
	                    return new Object[]{true, true, shipId, shipPositions, isHorizontal, imagePos};
	                } else {
	                    System.out.println(shipId);
	                    // Если корабль не уничтожен, возвращаем только информацию о попадании
	                    return new Object[]{true, false, shipId, null, null, null};
	                }
	            }
	        }
	    }

	    // Если это был промах
	    missed.add(position);
	    System.out.println("MISS");

	    // Возвращаем информацию о промахе
	    return new Object[]{false, false, null, null, null, null};
	}

	
//	public Object[] checkPosition(int[] position) {
//	    // Проверяем, есть ли позиция в списке hited
//	    for (int[] hit : hited) {
//	        if (Arrays.equals(hit, position)) {
//	        	System.out.println("Позиция уже была поражена");
//	        	return new Object[]{false, false, null, null, null, null}; // Позиция уже была поражена
//	        }
//	    }
//
//	    // Проверяем, есть ли позиция в списке missed
//	    for (int[] miss : missed) {
//	        if (Arrays.equals(miss, position)) {
//	        	System.out.println("Позиция уже была промахом");
//	        	return new Object[]{false, false, null, null, null, null}; // Позиция уже была промахом
//	        }
//	    }
//
//	    // Проверяем, содержится ли позиция в значениях карты shipsPos
//	    for (Map.Entry<Integer, int[][]> entry : shipsPos.entrySet()) {
//	    	int shipId = entry.getKey();
//	        int[][] shipPositions = entry.getValue();
//	        for (int[] shipPos : shipPositions) {
//	            if (Arrays.equals(shipPos, position)) {
//	            	// Получаем дополнительные данные о корабле
//                    boolean isHorizontal = shipsIsHorizontal.get(shipId);
//                    int[] imagePos = shipsImagesPos.get(shipId);
//                    
//	            	return new Object[]{true, true, shipId, shipPositions, isHorizontal, imagePos}; // Позиция найдена среди кораблей
//	            }
//	        }
//	    }
//
//	    // Позиция не найдена
//	    return new Object[]{false, false, null, null, null, null};
//	}
	
	public void giveTurn() {
		isTurn = true;
		fire();
	}
	
	private void fire() {
		while (isTurn) {
			// Создаем массив из 2 случайных чисел от 0 до 9
	        int[] tilePos = new int[2];
	        boolean searchPos = false;
	        
	        while(!searchPos) {
	        for (int i = 0; i < tilePos.length; i++) {
	        	tilePos[i] = random.nextInt(10); // Генерируем случайное число от 0 до 9
	        }
	        
	        // Проверяем, есть ли позиция в списке hited
		    for (int[] hit : enemyHited) {
		        if (Arrays.equals(hit, tilePos)) {
		            continue; // Позиция уже была поражена
		        }
		    }

		    // Проверяем, есть ли позиция в списке missed
		    for (int[] miss : enemyMissed) {
		        if (Arrays.equals(miss, tilePos)) {
		        	continue; // Позиция уже была промахом
		        }
		    }
		    searchPos = true; 
	        }
			
	        System.out.println(tilePos[0] + " " + tilePos[1]);
	        if ((boolean) GameManager.checkPosition(tilePos)[0]){
	        	enemyHited.add(tilePos);
	        } else {
	        	enemyMissed.add(tilePos);
				isTurn = false;
				GameManager.giveTurn();
			}
		}
	} 

}
