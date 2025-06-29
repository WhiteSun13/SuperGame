package com.SuperGame;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.SuperGame.Objects.FieldPlay;

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
            try {
    			System.out.println("IP-Адрес: " + InetAddress.getLocalHost().getHostAddress());
    		} catch (UnknownHostException e1) {
    			e1.printStackTrace();
    		}

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
                    + "clientname VARCHAR(50) DEFAULT NULL,"
                    + "host_field_player BLOB DEFAULT NULL,"
                    + "host_field_enemy BLOB DEFAULT NULL,"
                    + "client_field_player BLOB DEFAULT NULL,"
                    + "client_field_enemy BLOB DEFAULT NULL,"
                    + "host_missed BLOB DEFAULT NULL,"
                    + "host_hited BLOB DEFAULT NULL,"
                    + "client_missed BLOB DEFAULT NULL,"
                    + "client_hited BLOB DEFAULT NULL,"
                    + "save_date VARCHAR(50) NOT NULL,"
                    + "save_name VARCHAR(50) DEFAULT NULL,"
                    + "host_turn BOOLEAN DEFAULT TRUE"
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

        private void handleClientMessage(MessageWrapper message) throws ClassNotFoundException {
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
                    case "listSavedGames":
                        sendSavedGames((boolean) message.getPayload());
                        break;
                    case "saveGame":
                        saveGame((Object[]) message.getPayload());
                        break;
                    case "loadGame":
                    	loadGame((int) message.getPayload());
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
            hostHandler.out.writeObject(new MessageWrapper("gameMessage", clientId + " подключился!"));
            
            this.hostId = hostId;

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
        
        private void sendSavedGames(boolean isHost) throws SQLException, IOException {
            List<Map<String, Object>> savedGames = new ArrayList<>(); // Список для хранения информации о сохраненных играх
            String[] parts = clientId.split("@");

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            	PreparedStatement statement;
            	if (isHost) {
            		statement = connection.prepareStatement(
                            "SELECT id, hostname, clientname, save_date, save_name FROM saved_games WHERE hostname = ?"); // Запрос на выборку сохраненных игр для текущего пользователя
                    statement.setString(1, parts[0]);
            	}
            	else {
            		statement = connection.prepareStatement(
                            "SELECT id, hostname, clientname, save_date, save_name FROM saved_games WHERE clientname = ?"); // Запрос на выборку сохраненных игр для текущего пользователя
                    statement.setString(1, parts[0]);
            	}
                
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    Map<String, Object> gameInfo = new HashMap<>(); // Map для хранения информации об одной игре
                    gameInfo.put("id", resultSet.getInt("id"));
                    gameInfo.put("hostname", resultSet.getString("hostname"));
                    gameInfo.put("clientname", resultSet.getString("clientname"));
                    gameInfo.put("save_date", resultSet.getString("save_date"));
                    gameInfo.put("save_name", resultSet.getString("save_name"));
                    savedGames.add(gameInfo);
                }
            }

            out.writeObject(new MessageWrapper("savedGames", savedGames)); // Отправка списка сохраненных игр клиенту
        }
        
        private void saveGame(Object[] payload) throws IOException, SQLException {
            String hostname = (String) payload[0];
            String clientname = (String) payload[1];
            FieldPlay hostFieldPlayer = (FieldPlay) payload[2];
            FieldPlay hostFieldEnemy = (FieldPlay) payload[3];
            FieldPlay clientFieldPlayer = (FieldPlay) payload[4];
            FieldPlay clientFieldEnemy = (FieldPlay) payload[5];
            ArrayList<int[]> hostMissed = (ArrayList<int[]>) payload[6];
            ArrayList<int[]> hostHited = (ArrayList<int[]>) payload[7];
            ArrayList<int[]> clientMissed = (ArrayList<int[]>) payload[8];
            ArrayList<int[]> clientHited = (ArrayList<int[]>) payload[9];
            String savename = (String) payload[10];
            boolean isHostTurn = (boolean) payload[11];
            
            
            // Добавляем дату и время сохранения
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);


            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO saved_games (hostname, clientname, host_field_player, host_field_enemy, client_field_player, client_field_enemy, host_missed, host_hited, client_missed, client_hited, save_date, save_name, host_turn) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                statement.setString(1, hostname);
                statement.setString(2, clientname);
                statement.setBytes(3, serialize(hostFieldPlayer));
                statement.setBytes(4, serialize(hostFieldEnemy));
                statement.setBytes(5, serialize(clientFieldPlayer));
                statement.setBytes(6, serialize(clientFieldEnemy));
                statement.setBytes(7, serialize(hostMissed));
                statement.setBytes(8, serialize(hostHited));
                statement.setBytes(9, serialize(clientMissed));
                statement.setBytes(10, serialize(clientHited));
                statement.setString(11, formattedDateTime);
                statement.setString(12, savename);
                statement.setBoolean(13, isHostTurn);
                
                statement.executeUpdate();
                System.out.println("Игра сохранена в базе данных.");
                out.writeObject(new MessageWrapper("gameMessage", "Игра сохранена!"));
           }
        }
        
    private void loadGame(int id) throws IOException, SQLException, ClassNotFoundException {
    	FieldPlay hostFieldPlayer = null;
    	FieldPlay hostFieldEnemy = null;
    	FieldPlay clientFieldPlayer = null;
    	FieldPlay clientFieldEnemy = null;
    	ArrayList<int[]> hostMissed = null;
        ArrayList<int[]> hostHited = null;
        ArrayList<int[]> clientMissed = null;
        ArrayList<int[]> clientHited = null;
        boolean isHostTurn;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT hostname, clientname, host_field_player, host_field_enemy, client_field_player, client_field_enemy, host_missed, host_hited, client_missed, client_hited, host_turn FROM saved_games WHERE id = ?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
            	hostFieldPlayer = (FieldPlay) deserialize(resultSet.getBytes("host_field_player"));
            	hostFieldEnemy = (FieldPlay) deserialize(resultSet.getBytes("host_field_enemy"));
                clientFieldPlayer = (FieldPlay) deserialize(resultSet.getBytes("client_field_player"));
                clientFieldEnemy = (FieldPlay) deserialize(resultSet.getBytes("client_field_enemy"));
                hostMissed = (ArrayList<int[]>) deserialize(resultSet.getBytes("host_missed"));
                hostHited = (ArrayList<int[]>) deserialize(resultSet.getBytes("host_hited"));
                clientMissed = (ArrayList<int[]>) deserialize(resultSet.getBytes("client_missed"));
                clientHited = (ArrayList<int[]>) deserialize(resultSet.getBytes("client_hited"));
                isHostTurn = resultSet.getBoolean("host_turn");

                System.out.println("Игра загружена из базы данных.");
                out.writeObject(new MessageWrapper("gameLoaded", new Object[]{hostFieldPlayer, hostFieldEnemy, clientFieldPlayer, clientFieldEnemy, hostMissed, hostHited, clientMissed, clientHited, isHostTurn}));

            } else {
                System.out.println("Сохраненная игра не найдена для хоста: " + id);
                out.writeObject(new MessageWrapper("error", "Сохраненная игра не найдена."));
            }
        }
    }


    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        return bos.toByteArray();
    }

    private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
    }
    }
}
