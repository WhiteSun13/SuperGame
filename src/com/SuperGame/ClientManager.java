package com.SuperGame;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import com.SuperGame.Objects.FieldPlay;
import com.SuperGame.Scenes.ServerClientMessagePanel;
import com.SuperGame.Utils.SceneManager;

public class ClientManager {

    private static final int PORT = 12345;
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static boolean isRunning = true;
    private static boolean isListening = true;
    private static String gamesList = "";
    private static String username;
    private static int[] stats = new int[2];
    
    public static void startClient(String serverIp, String name, String password, boolean newUser) {
    	ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
    	try {
    	socket = new Socket(serverIp, PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(name);
        if (newUser) {
        	registerClient(name, password);
        } else {
        	loginClient(name, password);
        }
        
        Thread listenerThread = new Thread(ClientManager::listenForServerMessages);
        listenerThread.start();
        
        while (isRunning) { }
    	} catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка клиента: " + e.getMessage());
        } finally {
            stopClient();
        }
        });
    }

    private static void registerClient(String name, String password) throws IOException, ClassNotFoundException {
        out.writeObject(new MessageWrapper("registerClient", name + ";" + password));
        MessageWrapper response = (MessageWrapper) in.readObject();
        if ("registerSuccess".equals(response.getType())) {
            System.out.println("Регистрация успешна.");
            username = name;
            listAvailableGames();
            getStats();
            GameManager.setAuthorizated(true);
        } else {
            stopClient();
            System.out.println("Ошибка: " + response.getPayload());
            JOptionPane.showMessageDialog(null, response.getPayload());
        }
    }
    
    private static void loginClient(String name, String password) throws IOException, ClassNotFoundException {
        out.writeObject(new MessageWrapper("loginClient", name + ";" + password));
        MessageWrapper response = (MessageWrapper) in.readObject();
        if ("loginSuccess".equals(response.getType())) {
            System.out.println("Регистрация успешна.");
            username = name;
            listAvailableGames();
            getStats();
            GameManager.setAuthorizated(true);
        } else {
            stopClient();
            System.out.println("Ошибка: " + response.getPayload());
            JOptionPane.showMessageDialog(null, response.getPayload());
        }
    }

    public static void createGame(){
    	try {
        out.writeObject(new MessageWrapper("createGame", null));
        out.flush();
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static void connectToGame(String hostId) {
        try {
    	out.writeObject(new MessageWrapper("connectToGame", hostId));
    	out.flush();
        } catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static String listAvailableGames(){
        try {
			out.writeObject(new MessageWrapper("listAvailableGames", null));
			out.flush();
			return gamesList;
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Ошибка: не удалось получить список игр.";
		}
    }


    public static void stopClient() {
    	GameManager.setAuthorizated(false);
        try {
            isRunning = false;
            if (out != null) {
            	send(new MessageWrapper("stopClient", null));
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
            System.out.println("Клиент отключён.");
        } catch (IOException e) {
            System.out.println("Ошибка при отключении клиента: " + e.getMessage());
        }
    }

    // Новый метод для получения сообщений от сервера
    private static void listenForServerMessages() {
        try {
            while (isRunning) {
            	if(isListening) {
            		MessageWrapper message = (MessageWrapper) in.readObject();
            		handleServerMessage(message);
            	}
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Соединение с сервером потеряно: " + e.getMessage());
            stopClient();
        }
    }

    // Обработка входящих сообщений от сервера
    private static void handleServerMessage(MessageWrapper message) {
        switch (message.getType()) {
            case "gameCreated":
            	System.out.println("Игра создана. ID игры: " + message.getPayload());
                break;
                
            case "availableGames":
            	@SuppressWarnings("unchecked")
                List<String> availableGames = (List<String>) message.getPayload();
                if (availableGames.isEmpty()) {
                	gamesList = "Нет доступных игр.";
                } else {
                    StringBuilder result = new StringBuilder("Доступные игры:\n");
                    availableGames.forEach(game -> result.append("- Хост: ").append(game).append("\n"));
                    gamesList = result.toString();
                }
                break;
                
            case "clientConnected":
            	System.out.println("Успешно подключились к игре хоста");
            	GameManager.isServer = false;
                SceneManager.loadScene(new ServerClientMessagePanel(true));
                send(new MessageWrapper("hostConnected", null));
            	break;
            
            case "hostConnected":
            	System.out.println("Клиент подключен");
            	GameManager.isServer = true;
                SceneManager.loadScene(new ServerClientMessagePanel(true));
            	break;
                
            case "gameMessage":
            	JOptionPane.showMessageDialog(null, "Сообщение: " + message.getPayload());
                System.out.println("Сообщение: " + message.getPayload());
                break;
            
            case "objectArray":
            	Object[] messageObj = (Object[]) message.getPayload();
                GameManager.GamePanel.setPositionInfo(messageObj);
                System.out.println("Получено от клиента: " + messageObj[0]);
            	break;
                
            case "intArray":
            	int[] messagePosition = (int[]) message.getPayload();
                System.out.println("Получено от клиента: " + messagePosition[0] + " " + messagePosition[1]);
                Object[] s = GameManager.checkPosition(messagePosition);
                send(new MessageWrapper("objectArray", s));
            	break;
                
            case "giveTurn":
            	GameManager.giveTurn();
            	break;
                
            case "enemyReady":
            	GameManager.isEnemyReady = true;
            	break;
            	
            case "stats":
            	stats = (int[]) message.getPayload();
            	break;
            
            case "stopClient":
            	stopClient();
            	SceneManager.loadScene(new ServerClientMessagePanel(false));
            	break;
                
            default:
                System.out.println("Неизвестное сообщение от сервера: " + message.getType());
        }
    }
    
    public static void incrementWins() {
        try {
    	out.writeObject(new MessageWrapper("incrementWins", username));
    	out.flush();
        } catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void incrementLoses() {
        try {
    	out.writeObject(new MessageWrapper("incrementLoses", username));
    	out.flush();
        } catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static int[] getStats() {
        try {
    	out.writeObject(new MessageWrapper("getPlayerStats", username));
    	out.flush();
        } catch (IOException e) {
			e.printStackTrace();
		}
        return stats;
    }
    
    public static void sendMessage(String message){
        try {
			out.writeObject(new MessageWrapper("sendMessage", message));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
//    public static void saveGame(FieldPlay player, FieldPlay enemy){
//        try {
//			out.writeObject(new MessageWrapper("sendMessage", message));
//			out.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
    
    public static void send(MessageWrapper message){
        try {
			out.writeObject(new MessageWrapper("sendMW", message));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
