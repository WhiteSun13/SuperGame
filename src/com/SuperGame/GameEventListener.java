package com.SuperGame;

public interface GameEventListener {
	void onPositionChecked(int[] tilePos, boolean isHit);  // Метод, который вызывается при событии
}
