package com.SuperGame;

import javax.swing.*;

import com.SuperGame.Utils.SoundManager;

import java.awt.*;
import java.awt.event.*;


public class AnimatedButton extends JButton {
	private Color standartBackgroundColor;
	private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;
    private Color textColor;
    private Color hoverTextColor;
    private Color pressedTextColor;
    
    private Font customFont;
    private Font bigCustomFont;

    private int currentWidth;
    private int currentHeight;
    private int originalWidth;
    private int originalHeight;

    private int xInitOffset = 20;
    private int yInitOffset = 10;
    
    private int xOffset = 0;
    private int yOffset = 0;
    
    public AnimatedButton(String text, int x, int y, int w, int h) { this(text,x,y,w,h,new Color(27, 24, 90), new Color(65, 105, 225), new Color(45, 50, 100)); }

    public AnimatedButton(String text, int x, int y, int w, int h, Color sbgc, Color hbgc, Color pbgc) {
        super(text);
        setOpaque(false); // Allows custom painting
        setContentAreaFilled(false); // Remove default button background
        setBorderPainted(false); // Remove default button border
        setBounds(x, y, w, h);
        
        standartBackgroundColor = sbgc;
        hoverBackgroundColor = hbgc; // Cornflower blue on hover
        pressedBackgroundColor = pbgc; // Royal blue on press
        textColor = Color.GRAY;
        hoverTextColor = Color.WHITE;
        pressedTextColor = Color.WHITE;

        originalWidth = w - xInitOffset * 2;
        originalHeight = h - yInitOffset * 2;
        currentWidth = originalWidth;
        currentHeight = originalHeight;

        setPreferredSize(new Dimension(originalWidth, originalHeight));
        
        customFont = GameWindow.customFont.deriveFont(24f); //размер шрифта
		bigCustomFont = GameWindow.customFont.deriveFont(26f);
		setFont(customFont); // Применение шрифта к кнопке

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateSize(true); // Start size increase animation
                setFont(bigCustomFont);
                SoundManager.playSound("/sounds/cursor_aif.wav");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateSize(false); // Start size decrease animation
                setFont(customFont);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(pressedBackgroundColor);
                setForeground(pressedTextColor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(standartBackgroundColor);
                setForeground(hoverTextColor);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background fill with smooth color transition
        ButtonModel model = getModel();
        if (model.isPressed()) {
            g2.setColor(pressedBackgroundColor);
        } else if (model.isRollover()) {
            g2.setColor(hoverBackgroundColor);
        } else {
            g2.setColor(standartBackgroundColor);
        }

        // Create rounded rectangle, adjusted for centering
        g2.fillRoundRect(xOffset + xInitOffset, yOffset + yInitOffset, currentWidth, currentHeight, 30, 30);

        // Draw button text
        g2.setColor(model.isPressed() ? pressedTextColor : model.isRollover() ? hoverTextColor : textColor);
        FontMetrics fontMetrics = g2.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(getText());
        int textHeight = fontMetrics.getAscent();
        g2.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 3);
    }

    private void animateSize(boolean enlarge) {
        Timer timer = new Timer(10, new ActionListener() {
            private int step = 0;
            private final int maxStep = 10;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (enlarge) {
                    int targetWidth = originalWidth + 20;  // Размеры при увеличении
                    int targetHeight = originalHeight + 10;
                    
                    currentWidth = Math.min(targetWidth, currentWidth + 2);
                    currentHeight = Math.min(targetHeight, currentHeight + 1);
                } else {
                    currentWidth = Math.max(originalWidth, currentWidth - 2);
                    currentHeight = Math.max(originalHeight, currentHeight - 1);
                }

                // Расчет смещения, чтобы увеличивать от центра
                xOffset = (originalWidth - currentWidth) / 2;
                yOffset = (originalHeight - currentHeight) / 2;

                repaint();

                step++;
                if (step >= maxStep) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(currentWidth, currentHeight);
    }
}
