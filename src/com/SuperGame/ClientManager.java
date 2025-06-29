package com.SuperGame;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import com.SuperGame.Objects.FieldPlay;
import com.SuperGame.Scenes.InGamePanel;
import com.SuperGame.Scenes.ServerClientMessagePanel;
import com.SuperGame.Utils.SceneManager;

public class ClientManager {

    private static final int PORT = 12345;
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static boolean isRunning = true;
    private static boolean isListening = false;
    private static String gamesList = "";
    private static int[] stats = new int[2];
    public static String username;
    private static String hostname;
    
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
            isListening = true;
        } else {
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
            isListening = true;
        } else {
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
        	String[] parts = hostId.split("@");
        	hostname = parts[0];
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
        }
    }

    // Обработка входящих сообщений от сервера
    private static void handleServerMessage(MessageWrapper message) {
        switch (message.getType()) {
            case "gameCreated":
            	System.out.println("Игра создана. ID игры: " + message.getPayload());
            	JOptionPane.showMessageDialog(null, "Игра создана. ID игры: " + message.getPayload());
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
            	JOptionPane.showMessageDialog(null, message.getPayload());
                System.out.println("Сообщение: " + message.getPayload());
                break;
                
            case "error":
            	JOptionPane.showMessageDialog(null, message.getPayload());
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
            
            case "saveRequest":
            	Object[] oponentData = (Object[]) message.getPayload();
            	String oponentName = (String) oponentData[0];
            	FieldPlay oponentFpp = (FieldPlay) oponentData[1];
            	FieldPlay oponentFpe = (FieldPlay) oponentData[2];
            	ArrayList<int[]> oponentMissed = (ArrayList<int[]>) oponentData[3];
            	ArrayList<int[]> oponentHited = (ArrayList<int[]>) oponentData[4];
            	String saveName = (String) oponentData[5];
            	
            	FieldPlay[] playerFp = SceneManager.getPlayerData();
            	FieldPlay playerFpp = playerFp[0];
            	FieldPlay playerFpe = playerFp[1];
            	
            	if (GameManager.isServer) {
            		saveGame(new Object[] {username, oponentName, playerFpp, playerFpe, oponentFpp, oponentFpe, GameManager.missed, GameManager.hited, oponentMissed, oponentHited, saveName, GameManager.isTurn});
            	} else {
            		saveGame(new Object[] {oponentName, username, oponentFpp, oponentFpe, playerFpp, playerFpe, oponentMissed, oponentHited, GameManager.missed, GameManager.hited, saveName, !GameManager.isTurn});
            	}
            	break;
            
            case "gameLoaded":
            	Object[] loadData = (Object[]) message.getPayload();
            	FieldPlay hostFpp = (FieldPlay) loadData[0];
            	FieldPlay hostFpe = (FieldPlay) loadData[1];
            	FieldPlay clientFpp = (FieldPlay) loadData[2];
            	FieldPlay clientFpe = (FieldPlay) loadData[3];
            	ArrayList<int[]> hostMissed = (ArrayList<int[]>) loadData[4];
            	ArrayList<int[]> hostHited = (ArrayList<int[]>) loadData[5];
            	ArrayList<int[]> clientMissed = (ArrayList<int[]>) loadData[6];
            	ArrayList<int[]> clientHited = (ArrayList<int[]>) loadData[7];
            	boolean ishostturn = (boolean) loadData[8];
            	
            	if (GameManager.isServer) {
            		GameManager.newGame();
            		GameManager.missed = hostMissed;
            		GameManager.hited = hostHited;
            		hostFpp.saveField();
            		send(new MessageWrapper("gameLoadedFromOponent", new Object[]{clientFpp, clientFpe, clientMissed, clientHited, ishostturn}));
            		SceneManager.loadScene(new InGamePanel(hostFpp,hostFpe,ishostturn));
            	} else {
            		GameManager.newGame();
            		GameManager.missed = clientMissed;
            		GameManager.hited = clientHited;
            		hostFpp.saveField();
            		send(new MessageWrapper("gameLoadedFromOponent", new Object[]{hostFpp, hostFpe, hostMissed, hostHited, ishostturn}));
            		SceneManager.loadScene(new InGamePanel(clientFpp,clientFpe,ishostturn));
            	}
            	break;
            
            case "gameLoadedFromOponent":
            	Object[] loadDataFromOponent = (Object[]) message.getPayload();
            	FieldPlay LFpp = (FieldPlay) loadDataFromOponent[0];
            	FieldPlay LFpe = (FieldPlay) loadDataFromOponent[1];
            	ArrayList<int[]> LMissed = (ArrayList<int[]>) loadDataFromOponent[2];
            	ArrayList<int[]> LHited = (ArrayList<int[]>) loadDataFromOponent[3];
            	boolean Lishostturn = (boolean) loadDataFromOponent[4];
            	
            	
            	GameManager.newGame();
            	GameManager.missed = LMissed;
            	GameManager.hited = LHited;
            	LFpp.saveField();
            	SceneManager.loadScene(new InGamePanel(LFpp,LFpe,Lishostturn));
            	break;
            
            case "savedGames":
                showSavedGamesDialog((List<Map<String, Object>>) message.getPayload());
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
    
    public static void saveGame(Object[] message){
        try {
			out.writeObject(new MessageWrapper("saveGame", message));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void loadGame(int id){
        try {
        	if (GameManager.isServer) {
        		out.writeObject(new MessageWrapper("loadGame", id));
        		out.flush();
        	} else {
        		out.writeObject(new MessageWrapper("loadGame", id));
        		out.flush();
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void send(MessageWrapper message){
        try {
			out.writeObject(new MessageWrapper("sendMW", message));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void listSavedGames(){
        try {
			out.writeObject(new MessageWrapper("listSavedGames", GameManager.isServer)); // Отправляем запрос на список сохраненных игр
			out.flush();
        } catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static void showSavedGamesDialog(List<Map<String, Object>> savedGames) {
        if (savedGames.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Сохраненных игр нет.");
            return;
        }

        // Создаем массив строк для отображения в диалоговом окне
        String[] gameNames = savedGames.stream()
                .map(game -> String.format("ID:%d, %s vs %s (%s) - %s", 
                        game.get("id"), game.get("hostname"), game.get("clientname"), game.get("save_date"), game.get("save_name")))
                .toArray(String[]::new);

        // Отображаем диалоговое окно с выбором сохраненной игры
    	String selectedGame = (String) JOptionPane.showInputDialog(null,
    			"Выберите сохраненную игру:", "Загрузка игры",
                JOptionPane.QUESTION_MESSAGE, null, gameNames, gameNames[0]);


        // Если пользователь выбрал игру, загружаем ее по ID
        if (selectedGame != null) {
            int gameId = Integer.parseInt(selectedGame.split(",")[0].split(":")[1].trim()); // Извлекаем ID из строки
            loadGame(gameId);
        }
    }
}
