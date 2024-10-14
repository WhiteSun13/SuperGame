package com.SuperGame.Utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {
    private static Clip musicClip;

    // Глобальные значения громкости в процентах
    private static float musicVolumePercent = 100.0f; // Громкость музыки по умолчанию (100%)
    private static float soundVolumePercent = 100.0f; // Громкость звуков по умолчанию (100%)

    private static FloatControl musicVolumeControl;
    private static FloatControl soundVolumeControl;

    public static void playMusic(String filePath, boolean loop) {
        try {
            if (musicClip != null && musicClip.isRunning()) {
                musicClip.stop();
                musicClip.close();
            }

            URL musicURL = ResourceLoader.loadAsURL(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicURL);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);

            musicVolumeControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
            setMusicVolume(musicVolumePercent); // Установить текущую громкость музыки

            if (loop) {
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                musicClip.start();
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void playSound(String filePath) {
        new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                URL soundURL = ResourceLoader.loadAsURL(filePath);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
                clip.open(audioStream);

                soundVolumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setSoundVolume(soundVolumePercent); // Установить текущую громкость звуков

                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }

    // Методы для установки и получения громкости музыки в процентах
    public static void setMusicVolume(float volumePercent) {
        musicVolumePercent = volumePercent;
        if (musicVolumeControl != null) {
            float dB = percentToDecibels(volumePercent);
            musicVolumeControl.setValue(dB);
        }
    }

    public static float getMusicVolumePercent() {
        return musicVolumePercent;
    }

    // Методы для установки и получения громкости звуков в процентах
    public static void setSoundVolume(float volumePercent) {
        soundVolumePercent = volumePercent;
        if (soundVolumeControl != null) {
            float dB = percentToDecibels(volumePercent);
            soundVolumeControl.setValue(dB);
        }
    }

    public static float getSoundVolumePercent() {
        return soundVolumePercent;
    }

    // Метод для конвертации процентов в децибелы
    private static float percentToDecibels(float percent) {
        return (percent / 100.0f) * 60.0f - 60.0f;
    }
}
