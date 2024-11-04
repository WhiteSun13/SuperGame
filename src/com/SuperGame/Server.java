package com.SuperGame;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import com.SuperGame.Scenes.ServerClientMessagePanel;
import com.SuperGame.Utils.SceneManager;

public class Server {
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static final int PORT = 12345;
    private static boolean isRunning = true;

    public static void startServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                System.out.println("Сервер запущен. Ожидание подключения...");
                clientSocket = serverSocket.accept();
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress().getHostAddress());
                GameManager.isServer = true;
                SceneManager.loadScene(new ServerClientMessagePanel(true));

                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                // Слушаем клиента
                while (isRunning) {
                    MessageWrapper messageWrapper = (MessageWrapper) in.readObject();
                    if ("objectArray".equals(messageWrapper.getType())) {
                        Object[] message = (Object[]) messageWrapper.getPayload();
                        GameManager.GamePanel.setPositionInfo(message);
                        System.out.println("Получено от клиента: " + message[0]);
                    } else if ("intArray".equals(messageWrapper.getType())) {
                        int[] message = (int[]) messageWrapper.getPayload();
                        System.out.println("Получено от клиента: " + message[0] + " " + message[1]);
                        Object[] s = GameManager.checkPosition(message);
                        send(new MessageWrapper("objectArray", s));
                    } else if ("giveTurn".equals(messageWrapper.getType())){
                    	GameManager.giveTurn();
                    } else if ("enemyReady".equals(messageWrapper.getType())){
                    	GameManager.isEnemyReady = true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Ошибка сервера: " + e.getMessage());
                SceneManager.loadScene(new ServerClientMessagePanel(false));
            } finally {
                stopServer();
            }
        });
    }

    public static void send(MessageWrapper message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при отправке данных клиенту: " + e.getMessage());
        }
    }

    public static String getIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Не удалось получить IP";
        }
    }

    public static void stopServer() {
        try {
            isRunning = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
            System.out.println("Сервер остановлен.");
        } catch (IOException e) {
            System.out.println("Ошибка при остановке сервера: " + e.getMessage());
        }
    }
}
