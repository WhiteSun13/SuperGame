package com.SuperGame;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.SuperGame.Scenes.ServerClientMessagePanel;
import com.SuperGame.Utils.SceneManager;

//Клиентский класс
public class Client {
 private static Socket socket;
 private static ObjectOutputStream out;
 private static ObjectInputStream in;
// private static final String SERVER_IP = "127.0.0.1"; // Локальный IP
 private static final int PORT = 12345;
 private static boolean isRunning = true;

 public static void startClient(String Server_IP) {
     ExecutorService executor = Executors.newSingleThreadExecutor();
     executor.submit(() -> {
         try {
             socket = new Socket(Server_IP, PORT);
             System.out.println("Подключен к серверу: " + Server_IP);
             GameManager.isServer = false;
             SceneManager.loadScene(new ServerClientMessagePanel(true));

             out = new ObjectOutputStream(socket.getOutputStream());
             in = new ObjectInputStream(socket.getInputStream());

             // Слушаем сервер
             while (isRunning) {
            	 MessageWrapper messageWrapper = (MessageWrapper) in.readObject();
                 if ("objectArray".equals(messageWrapper.getType())) {
                     Object[] message = (Object[]) messageWrapper.getPayload();
                     GameManager.GamePanel.setPositionInfo(message);
                     System.out.println("Получено от сервера: " + message[0]);
                 } else if ("intArray".equals(messageWrapper.getType())) {
                     int[] message = (int[]) messageWrapper.getPayload();
                     System.out.println("Получено от сервера: " + message[0] + " " + message[1]);
                     Object[] s = GameManager.checkPosition(message);
                     send(new MessageWrapper("objectArray", s));
                 } else if ("giveTurn".equals(messageWrapper.getType())){
                 	GameManager.giveTurn();
                 } else if ("enemyReady".equals(messageWrapper.getType())){
                 	GameManager.isEnemyReady = true;
                 }
             }
         } catch (Exception e) {
             System.out.println("Ошибка клиента: " + e.getMessage());
             SceneManager.loadScene(new ServerClientMessagePanel(false));
         } finally {
             stopClient();
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

 public static void stopClient() {
     try {
         isRunning = false;
         if (in != null) in.close();
         if (out != null) out.close();
         if (socket != null) socket.close();
         System.out.println("Клиент отключен.");
     } catch (IOException e) {
         System.out.println("Ошибка при отключении клиента: " + e.getMessage());
     }
 }
}