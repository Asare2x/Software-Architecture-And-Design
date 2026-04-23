package com.example.trading.service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
public class AuthenticationService implements IAuthService, ILogin {
    private static AuthenticationService instance;
    private boolean loggedIn = false;
    private String currentUser = null;
    private Map<String, String> users; // username -> password
    private static final String USERS_FILE = "users.properties";
    
    // Private constructor for singleton pattern
    private AuthenticationService() {
        users = new HashMap<>();
        loadUsers();
    }
    
    // Singleton getInstance method
    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }
    
    /**
     * Load users from file
     */
    private void loadUsers() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(USERS_FILE)) {
            props.load(fis);
            for (String username : props.stringPropertyNames()) {
                users.put(username, props.getProperty(username));
            }
            System.out.println("Loaded " + users.size() + " users from file.");
        } catch (FileNotFoundException e) {
            System.out.println("Users file not found. Starting with empty user database.");
            // Create some default users for testing
            createDefaultUsers();
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
            createDefaultUsers();
        }
    }
    
    /**
     * Create some default users for testing
     */
    private void createDefaultUsers() {
        users.put("admin", "admin123");
        users.put("user", "password");
        users.put("test", "test123");
        saveUsers(); // Save the default users to file
    }
    
    /**
     * Save users to file
     */
    private void saveUsers() {
        Properties props = new Properties();
        for (Map.Entry<String, String> entry : users.entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue());
        }
        
        try (FileOutputStream fos = new FileOutputStream(USERS_FILE)) {
            props.store(fos, "Trading System User Credentials");
            System.out.println("Users saved to file.");
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user with username and password
     * @param username the username
     * @param password the password
     * @return true if authentication successful, false otherwise
     */
    public boolean authenticate(String username, String password) {
        if (username != null && password != null && 
            users.containsKey(username) && users.get(username).equals(password)) {
            this.currentUser = username;
            this.loggedIn = true;
            return true;
        }
        return false;
    }
    
    /**
     * Check if user is currently logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    /**
     * Get the current logged in user
     * @return username of current user, or null if not logged in
     */
    public String getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        this.loggedIn = false;
        this.currentUser = null;
    }
    
    /**
     * Register a new user
     * @param username the username
     * @param password the password
     * @return true if registration successful, false otherwise
     */
    public boolean register(String username, String password) {
        if (username != null && !username.trim().isEmpty() && 
            password != null && !password.trim().isEmpty()) {
            
            // Check if user already exists
            if (users.containsKey(username)) {
                return false; // User already exists
            }
            
            // Add new user
            users.put(username, password);
            saveUsers(); // Save to file immediately
            return true;
        }
        return false;
    }
    
    /**
     * Get all users (for debugging/admin purposes)
     */
    public Map<String, String> getAllUsers() {
        return new HashMap<>(users);
    }
    
    /**
     * Check if username exists
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}
