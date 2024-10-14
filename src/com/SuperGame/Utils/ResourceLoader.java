package com.SuperGame.Utils;

import java.awt.Image;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

public final class ResourceLoader {
	
	public static InputStream load(String path) {
		InputStream input = ResourceLoader.class.getResourceAsStream(path);
		if (input == null) {
			input = ResourceLoader.class.getResourceAsStream("/"+path);
		}
		return input;
	}
	
	public static URL loadAsURL(String path) {
        return ResourceLoader.class.getResource(path);
    }
	
	public static Image loadImageAsURL(String path) {
		Image img = new ImageIcon(ResourceLoader.loadAsURL(path)).getImage();
		return img;
	}
	
}
