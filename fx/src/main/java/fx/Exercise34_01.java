package fx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Exercise 34.1 - Staff Database Application
 * A JavaFX program that allows users to view, insert, and update staff information
 * in a MySQL database.
 */
public class Exercise34_01 extends Application {
    
    // Database connection parameters for XAMPP MySQL
    private static final String DB_URL = "jdbc:mysql://localhost:3306/york";
    private static final String USERNAME = "york";
    private static final String PASSWORD = "yorky";
    
    // GUI components
    private TextField tfId = new TextField();
    private TextField tfLastName = new TextField();
    private TextField tfFirstName = new TextField();
    private TextField tfMi = new TextField();
    private TextField tfAddress = new TextField();
    private TextField tfCity = new TextField();
    private TextField tfState = new TextField();
    private TextField tfTelephone = new TextField();
    private TextField tfEmail = new TextField();
    
    private Button btView = new Button("View");
    private Button btInsert = new Button("Insert");
    private Button btUpdate = new Button("Update");
    private Button btClear = new Button("Clear");
    
    private Label lblStatus = new Label("Ready");
    
    // Database connection
    private Connection connection;
    private PreparedStatement preparedStatement;

    @Override
    public void start(Stage primaryStage) {
        // Initialize database
        initializeDatabase();
        
        // Create the GUI
        VBox root = createGUI();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Create and show the scene
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Staff Database - Exercise 34.1");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Close database connection when window closes
        primaryStage.setOnCloseRequest(e -> closeConnection());
    }
    
    /**
     * Creates the GUI layout
     */
    private VBox createGUI() {
        // Create main container
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        // Create form grid
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));
        
        // Add form fields
        formGrid.add(new Label("ID:"), 0, 0);
        formGrid.add(tfId, 1, 0);
        
        formGrid.add(new Label("Last Name:"), 0, 1);
        formGrid.add(tfLastName, 1, 1);
        
        formGrid.add(new Label("First Name:"), 2, 1);
        formGrid.add(tfFirstName, 3, 1);
        
        formGrid.add(new Label("MI:"), 4, 1);
        tfMi.setPrefColumnCount(2);
        formGrid.add(tfMi, 5, 1);
        
        formGrid.add(new Label("Address:"), 0, 2);
        formGrid.add(tfAddress, 1, 2, 5, 1); // Span 5 columns
        
        formGrid.add(new Label("City:"), 0, 3);
        formGrid.add(tfCity, 1, 3);
        
        formGrid.add(new Label("State:"), 2, 3);
        tfState.setPrefColumnCount(3);
        formGrid.add(tfState, 3, 3);
        
        formGrid.add(new Label("Telephone:"), 0, 4);
        formGrid.add(tfTelephone, 1, 4);
        
        formGrid.add(new Label("Email:"), 2, 4);
        formGrid.add(tfEmail, 3, 4, 3, 1); // Span 3 columns
        
        // Create button panel
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10));
        buttonPanel.getChildren().addAll(btView, btInsert, btUpdate, btClear);
        
        // Create status panel
        HBox statusPanel = new HBox();
        statusPanel.setPadding(new Insets(5));
        statusPanel.getChildren().add(lblStatus);
        
        // Add components to root
        root.getChildren().addAll(formGrid, buttonPanel, statusPanel);
        
        return root;
    }
    
    /**
     * Sets up event handlers for buttons
     */
    private void setupEventHandlers() {
        btView.setOnAction(e -> viewRecord());
        btInsert.setOnAction(e -> insertRecord());
        btUpdate.setOnAction(e -> updateRecord());
        btClear.setOnAction(e -> clearFields());
    }
    
    /**
     * Initializes database connection and creates table if it doesn't exist
     */
    private void initializeDatabase() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to database
            connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            
            // Create Staff table if it doesn't exist
            createStaffTable();
            
            lblStatus.setText("Connected to database 'york' successfully");
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert("Database Error", "Failed to initialize database: " + ex.getMessage());
        }
    }
    
    /**
     * Creates the Staff table with the specified schema
     */
    private void createStaffTable() {
        try {
            Statement statement = connection.createStatement();
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Staff (" +
                    "id CHAR(9) NOT NULL PRIMARY KEY, " +
                    "lastName VARCHAR(15), " +
                    "firstName VARCHAR(15), " +
                    "mi CHAR(1), " +
                    "address VARCHAR(20), " +
                    "city VARCHAR(20), " +
                    "state CHAR(2), " +
                    "telephone CHAR(10), " +
                    "email VARCHAR(40)" +
                    ")";
            
            statement.executeUpdate(createTableSQL);
            statement.close();
            
        } catch (SQLException ex) {
            showAlert("Database Error", "Failed to create Staff table: " + ex.getMessage());
        }
    }
    
    /**
     * Views a record based on the ID entered
     */
    private void viewRecord() {
        String id = tfId.getText().trim();
        
        if (id.isEmpty()) {
            showAlert("Input Error", "Please enter an ID to view");
            return;
        }
        
        try {
            String query = "SELECT * FROM Staff WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, id);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                // Populate fields with retrieved data
                tfLastName.setText(resultSet.getString("lastName"));
                tfFirstName.setText(resultSet.getString("firstName"));
                tfMi.setText(resultSet.getString("mi"));
                tfAddress.setText(resultSet.getString("address"));
                tfCity.setText(resultSet.getString("city"));
                tfState.setText(resultSet.getString("state"));
                tfTelephone.setText(resultSet.getString("telephone"));
                tfEmail.setText(resultSet.getString("email"));
                
                lblStatus.setText("Record found for ID: " + id);
            } else {
                lblStatus.setText("Record not found for ID: " + id);
                clearDataFields(); // Clear all fields except ID
            }
            
            resultSet.close();
            preparedStatement.close();
            
        } catch (SQLException ex) {
            lblStatus.setText("Error viewing record");
            showAlert("Database Error", "Failed to retrieve record: " + ex.getMessage());
        }
    }
    
    /**
     * Inserts a new record into the database
     */
    private void insertRecord() {
        if (!validateInput()) {
            return;
        }
        
        try {
            String query = "INSERT INTO Staff (id, lastName, firstName, mi, address, city, state, telephone, email) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            preparedStatement = connection.prepareStatement(query);
            setStatementParameters();
            
            int result = preparedStatement.executeUpdate();
            
            if (result > 0) {
                lblStatus.setText("Record inserted successfully");
            } else {
                lblStatus.setText("Failed to insert record");
            }
            
            preparedStatement.close();
            
        } catch (SQLException ex) {
            lblStatus.setText("Error inserting record");
            if (ex.getErrorCode() == 1062) { // Duplicate key error
                showAlert("Insert Error", "A record with ID '" + tfId.getText() + "' already exists.");
            } else {
                showAlert("Database Error", "Failed to insert record: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Updates an existing record in the database
     */
    private void updateRecord() {
        if (!validateInput()) {
            return;
        }
        
        try {
            String query = "UPDATE Staff SET lastName = ?, firstName = ?, mi = ?, address = ?, " +
                    "city = ?, state = ?, telephone = ?, email = ? WHERE id = ?";
            
            preparedStatement = connection.prepareStatement(query);
            
            // Set parameters (note different order for UPDATE)
            preparedStatement.setString(1, tfLastName.getText().trim());
            preparedStatement.setString(2, tfFirstName.getText().trim());
            preparedStatement.setString(3, tfMi.getText().trim());
            preparedStatement.setString(4, tfAddress.getText().trim());
            preparedStatement.setString(5, tfCity.getText().trim());
            preparedStatement.setString(6, tfState.getText().trim());
            preparedStatement.setString(7, tfTelephone.getText().trim());
            preparedStatement.setString(8, tfEmail.getText().trim());
            preparedStatement.setString(9, tfId.getText().trim());
            
            int result = preparedStatement.executeUpdate();
            
            if (result > 0) {
                lblStatus.setText("Record updated successfully");
            } else {
                lblStatus.setText("No record found with ID: " + tfId.getText());
            }
            
            preparedStatement.close();
            
        } catch (SQLException ex) {
            lblStatus.setText("Error updating record");
            showAlert("Database Error", "Failed to update record: " + ex.getMessage());
        }
    }
    
    /**
     * Clears all input fields
     */
    private void clearFields() {
        tfId.clear();
        clearDataFields();
        lblStatus.setText("Fields cleared");
    }
    
    /**
     * Clears all data fields except ID
     */
    private void clearDataFields() {
        tfLastName.clear();
        tfFirstName.clear();
        tfMi.clear();
        tfAddress.clear();
        tfCity.clear();
        tfState.clear();
        tfTelephone.clear();
        tfEmail.clear();
    }
    
    /**
     * Validates input fields
     */
    private boolean validateInput() {
        if (tfId.getText().trim().isEmpty()) {
            showAlert("Input Error", "ID is required");
            return false;
        }
        
        if (tfLastName.getText().trim().isEmpty()) {
            showAlert("Input Error", "Last Name is required");
            return false;
        }
        
        if (tfFirstName.getText().trim().isEmpty()) {
            showAlert("Input Error", "First Name is required");
            return false;
        }
        
        return true;
    }
    
    /**
     * Sets parameters for prepared statement (for INSERT operation)
     */
    private void setStatementParameters() throws SQLException {
        preparedStatement.setString(1, tfId.getText().trim());
        preparedStatement.setString(2, tfLastName.getText().trim());
        preparedStatement.setString(3, tfFirstName.getText().trim());
        preparedStatement.setString(4, tfMi.getText().trim());
        preparedStatement.setString(5, tfAddress.getText().trim());
        preparedStatement.setString(6, tfCity.getText().trim());
        preparedStatement.setString(7, tfState.getText().trim());
        preparedStatement.setString(8, tfTelephone.getText().trim());
        preparedStatement.setString(9, tfEmail.getText().trim());
    }
    
    /**
     * Shows an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    


    /**
     * Closes the database connection
     */
    private void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            System.err.println("Error closing database connection: " + ex.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}