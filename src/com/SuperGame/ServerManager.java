// File: com/SuperGame/ServerManager.java
package com.SuperGame;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class ServerManager {

    private static final int PORT = 12345;
    private static ServerSocket serverSocket;
    private static final Map<String, ClientHandler> activeGames = new HashMap<>();
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    // MySQL Connection Info
    private static final String DB_URL = "jdbc:mysql://localhost:3306/warshipsdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "SnakeEater313";

    public static void main(String[] args) {
        try {
            // Initialize MySQL connection
            initializeDatabase();

            serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущен на порту: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    private static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS games ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "host VARCHAR(50) NOT NULL,"
                    + "connected_client VARCHAR(50) DEFAULT NULL"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(50) NOT NULL UNIQUE,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "wins INT DEFAULT 0,"
                    + "loses INT DEFAULT 0"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS saved_games ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "hostname VARCHAR(50) NOT NULL,"
                    + "clientname VARCHAR(50) NOT NULL"
                    + ")");
            System.out.println("База данных инициализирована.");
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    private static void stopServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("Сервер остановлен.");
        } catch (IOException e) {
            System.out.println("Ошибка при остановке сервера: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String clientId;
        private String hostId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                // Register or manage connections
                clientId = (String) in.readObject();
                System.out.println("Клиент идентифицирован: " + clientId);

                while (true) {
                    MessageWrapper message = (MessageWrapper) in.readObject();
                    handleClientMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Ошибка обработки клиента: " + e.getMessage());
            } finally {
                disconnectClient();
            }
        }

        private void handleClientMessage(MessageWrapper message) {
            try {
                switch (message.getType()) {
                    case "registerClient":
                        registerClient((String) message.getPayload());
                        break;
                    case "loginClient":
                        loginClient((String) message.getPayload());
                        break;
                    case "incrementWins":
                    	incrementWins((String) message.getPayload());
                        break;
                    case "incrementLoses":
                    	incrementLoses((String) message.getPayload());
                        break;
                    case "getPlayerStats":
                    	getPlayerStats((String) message.getPayload());
                        break;
                    case "createGame":
                        createGame();
                        break;
                    case "connectToGame":
                        connectToGame((String) message.getPayload());
                        break;
                    case "listAvailableGames":
                        sendAvailableGames();
                        break;
                    case "sendMessage":
                        handleGameMessage((String) message.getPayload());
                        break;
                    case "sendMW":
                        handleMessageWrapper((MessageWrapper) message.getPayload());
                        break;
                    default:
                        System.out.println("Неизвестный тип сообщения." + message.getType());
                }
            } catch (SQLException | IOException e) {
                System.out.println("Ошибка обработки сообщения: " + e.getMessage());
            }
        }

        private void handleGameMessage(String message) throws IOException, SQLException { 
            // Получаем идентификатор игры (по хосту клиента)
            String gameHostId = findGameHostForClient(clientId);
            if (gameHostId == null) {
                out.writeObject(new MessageWrapper("error", "Вы не подключены к игре."));
                return;
            }

            System.out.println("Сообщение в игре от " + clientId + ": " + message);

            // Отправляем сообщение всем клиентам, подключённым к игре, кроме отправителя
            for (Map.Entry<String, ClientHandler> entry : activeGames.entrySet()) {
                String playerId = entry.getKey();
                ClientHandler handler = entry.getValue();
                String handlerHostId = findGameHostForClient(playerId);

                // Проверяем, что клиент в той же игре и не является отправителем
                if (gameHostId.equals(handlerHostId) && !playerId.equals(clientId)) {
                    handler.out.writeObject(new MessageWrapper("gameMessage", clientId + ": " + message));
                }
            }
        }
        
        private void handleMessageWrapper(MessageWrapper message) throws IOException, SQLException { 
            // Получаем идентификатор игры (по хосту клиента)
            String gameHostId = findGameHostForClient(clientId);
            if (gameHostId == null) {
                out.writeObject(new MessageWrapper("error", "Вы не подключены к игре."));
                return;
            }

            System.out.println("Сообщение в игре от " + clientId + ": " + message.toString());

            // Отправляем сообщение всем клиентам, подключённым к игре, кроме отправителя
            for (Map.Entry<String, ClientHandler> entry : activeGames.entrySet()) {
                String playerId = entry.getKey();
                ClientHandler handler = entry.getValue();
                String handlerHostId = findGameHostForClient(playerId);

                // Проверяем, что клиент в той же игре и не является отправителем
                if (gameHostId.equals(handlerHostId) && !playerId.equals(clientId)) {
                    handler.out.writeObject(message);
                }
            }
        }


        private String findGameHostForClient(String clientId) throws SQLException {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT host FROM games WHERE host = ? OR connected_client = ?");
                statement.setString(1, clientId);
                statement.setString(2, clientId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("host");
                }
            }
            return null;
        }
        
        private void registerClient(String payload) throws IOException, SQLException {
            String[] parts = payload.split(";");
            String username = parts[0];
            String password = parts[1];

            // Хешируем пароль
            String hashedPassword = hashPassword(password);

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM users WHERE name = ?");
                checkStmt.setString(1, username);
                ResultSet resultSet = checkStmt.executeQuery();

                if (resultSet.next()) {
                    out.writeObject(new MessageWrapper("error", "Имя уже существует."));
                    disconnectClient();
                    return;
                }

                // Добавление нового пользователя
                PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO users (name, password) VALUES (?, ?)");
                insertStmt.setString(1, username);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
                
                registrationClient(username);

                out.writeObject(new MessageWrapper("registerSuccess", "Регистрация успешна"));
            }
        }

        private void loginClient(String payload) throws SQLException, IOException {
            String[] parts = payload.split(";");
            String username = parts[0];
            String password = parts[1];

            // Хешируем пароль
            String hashedPassword = hashPassword(password);

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE name = ? AND password = ?");
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);
                ResultSet resultSet = stmt.executeQuery();

                if (resultSet.next()) {
                	registrationClient(username);
                    out.writeObject(new MessageWrapper("loginSuccess", "Вход успешен"));
                } else {
                    out.writeObject(new MessageWrapper("error", "Неверное имя пользователя или пароль"));
                    disconnectClient();
                }
            }
        }

        private String hashPassword(String password) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(password.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    hexString.append(String.format("%02x", b));
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        private void registrationClient(String name) throws IOException {
            // Уникальный идентификатор клиента: "имя@ip"
            String ip = socket.getInetAddress().getHostAddress();
            this.clientId = name + "@" + ip;

            // Сохраняем клиент в activeGames
            if (!activeGames.containsKey(clientId)) {
                activeGames.put(clientId, this);
            }

            System.out.println("Клиент зарегистрирован: " + clientId);
        }


        private void createGame() throws SQLException, IOException {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO games (host) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, clientId);
                statement.executeUpdate();

                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    int gameId = keys.getInt(1);
                    activeGames.put(clientId, this);
                    out.writeObject(new MessageWrapper("gameCreated", gameId));
                }
            }
        }


        private void connectToGame(String hostId) throws SQLException, IOException {
            System.out.println(clientId + " Попытка подключения к игре с хостом: " + hostId);

            if (!activeGames.containsKey(hostId)) {
                System.out.println("Ошибка: игра с ID хоста " + hostId + " не найдена.");
                out.writeObject(new MessageWrapper("error", "Игра не найдена."));
                return;
            }

            ClientHandler hostHandler = activeGames.get(hostId);
            System.out.println("Игра найдена. Устанавливается соединение с хостом: " + hostId);

            // Сообщить хосту о подключении клиента
            out.writeObject(new MessageWrapper("clientConnected", clientId));

            // Обновить базу данных
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE games SET connected_client = ? WHERE host = ?");
                statement.setString(1, clientId);
                statement.setString(2, hostId);
                int updatedRows = statement.executeUpdate();

                if (updatedRows > 0) {
                    System.out.println("База данных обновлена: клиент подключён к хосту " + hostId);
                } else {
                    System.out.println("Ошибка обновления базы данных для игры с хостом " + hostId);
                }
            }
        }


        private void sendAvailableGames() throws SQLException, IOException {
            List<String> availableGames = new ArrayList<>();
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT host FROM games WHERE connected_client IS NULL");
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    availableGames.add(resultSet.getString("host"));
                }
            }
            out.writeObject(new MessageWrapper("availableGames", availableGames));
        }
        
        private void saveGame(Object[] payload) throws IOException, SQLException {
        	try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT host FROM games WHERE connected_client IS NULL");
                ResultSet resultSet = statement.executeQuery();
            }
        	
        	// Если игрок - хост
        	if ((boolean) payload[0]) {
        		
        	} else {
        		
        	}
        }
        
        private void incrementWins(String username) throws SQLException {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Обновляем количество побед для игрока
                String updateQuery = "UPDATE users SET wins = wins + 1 WHERE name = ?";
                PreparedStatement statement = connection.prepareStatement(updateQuery);
                statement.setString(1, username);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Победа пользователя " + username + " учтена.");
                } else {
                    System.out.println("Ошибка: не удалось найти пользователя с именем " + username);
                }
            }
        }

        private void incrementLoses(String username) throws SQLException {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Обновляем количество поражений для игрока
                String updateQuery = "UPDATE users SET loses = loses + 1 WHERE name = ?";
                PreparedStatement statement = connection.prepareStatement(updateQuery);
                statement.setString(1, username);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Поражение пользователя " + username + " учтено.");
                } else {
                    System.out.println("Ошибка: не удалось найти пользователя с именем " + username);
                }
            }
        }
        
        private void getPlayerStats(String username) throws SQLException, IOException {
            int[] stats = new int[2]; // [0] - победы, [1] - поражения

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String query = "SELECT wins, loses FROM users WHERE name = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, username);
                
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    stats[0] = resultSet.getInt("wins");   // Победы
                    stats[1] = resultSet.getInt("loses");  // Поражения
                } else {
                    // Если пользователя не существует
                    System.out.println("Ошибка: Игрок с именем " + username + " не найден.");
                }
            }
            
            out.writeObject(new MessageWrapper("stats", stats));
        }


        private void disconnectClient() {
            try {
                socket.close();
                activeGames.remove(clientId);
                System.out.println("Клиент отключён: " + clientId);
                
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM games WHERE connected_client = ? OR host = ?");
                    statement.setString(1, clientId);
                    statement.setString(2, clientId);
                    int updatedRows = statement.executeUpdate();
                } catch (SQLException e) {
					e.printStackTrace();
				}
            } catch (IOException e) {
                System.out.println("Ошибка при отключении клиента: " + e.getMessage());
            }
        }
    }
}
