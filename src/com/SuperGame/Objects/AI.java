package com.SuperGame.Objects;

import com.SuperGame.GameManager.TileState;

public class AI {
	private FieldPlay playerField;
    
	public AI() {
		playerField = new FieldPlay(0, 0, true);
		playerField.loadField();
		playerField.drawWarships();
	}
}
