package com.SuperGame;

import javax.swing.*;

import com.SuperGame.Utils.SoundManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsDialog extends JDialog {
    private JSlider musicVolumeSlider;
    private JSlider soundVolumeSlider;

    private static SettingsDialog settingsDialogInstance;
    
    float oldMusicVolume = SoundManager.getMusicVolumePercent();
    float oldSoundVolume = SoundManager.getSoundVolumePercent();

    public static void showSettingsDialog(Frame parent) {
        if (settingsDialogInstance == null || !settingsDialogInstance.isVisible()) {
            settingsDialogInstance = new SettingsDialog(parent);
            settingsDialogInstance.setVisible(true);
        }
    }

    private SettingsDialog(Frame parent) {
        super(parent, "Настройки", true);
        setLayout(new BorderLayout());

        // Панель с ползунками
        JPanel slidersPanel = new JPanel(new GridLayout(2, 1));

        // Ползунок громкости музыки в процентах (0-100)
        musicVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) SoundManager.getMusicVolumePercent());
        musicVolumeSlider.setMajorTickSpacing(25);
        musicVolumeSlider.setPaintTicks(true);
        musicVolumeSlider.setPaintLabels(true);
        slidersPanel.add(createLabeledPanel("Громкость музыки:", musicVolumeSlider));

        // Ползунок громкости звуков в процентах (0-100)
        soundVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) SoundManager.getSoundVolumePercent());
        soundVolumeSlider.setMajorTickSpacing(25);
        soundVolumeSlider.setPaintTicks(true);
        soundVolumeSlider.setPaintLabels(true);
        slidersPanel.add(createLabeledPanel("Громкость звуков:", soundVolumeSlider));

        // Кнопки
        JPanel buttonsPanel = new JPanel();
        JButton confirmButton = new JButton("Подтвердить");
        JButton cancelButton = new JButton("Отмена");

        // Добавление обработчиков событий
        musicVolumeSlider.addChangeListener(e -> applyChanges());
        soundVolumeSlider.addChangeListener(e -> applyChanges());

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float musicVolume = musicVolumeSlider.getValue();
                float soundVolume = soundVolumeSlider.getValue();

                // Сохранить настройки громкости
                SoundManager.setMusicVolume(musicVolume);
                SoundManager.setSoundVolume(soundVolume);

                dispose(); // Закрыть окно
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Отменить изменения и вернуться к предыдущим значениям
                musicVolumeSlider.setValue((int) oldMusicVolume);
                soundVolumeSlider.setValue((int) oldSoundVolume);

                dispose(); // Закрыть окно
            }
        });

        buttonsPanel.add(confirmButton);
        buttonsPanel.add(cancelButton);

        // Добавление компонентов в диалог
        add(slidersPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private JPanel createLabeledPanel(String labelText, JSlider slider) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        panel.add(label, BorderLayout.WEST);
        panel.add(slider, BorderLayout.CENTER);
        oldMusicVolume = SoundManager.getMusicVolumePercent();
        oldSoundVolume = SoundManager.getSoundVolumePercent();
        return panel;
    }

    private void applyChanges() {
        float musicVolume = musicVolumeSlider.getValue();
        float soundVolume = soundVolumeSlider.getValue();

        // Применить текущие значения к параметрам SoundManager
        SoundManager.setMusicVolume(musicVolume);
        SoundManager.setSoundVolume(soundVolume);
        
        SoundManager.playSound("/sounds/cursor_aif.wav");
    }
}
