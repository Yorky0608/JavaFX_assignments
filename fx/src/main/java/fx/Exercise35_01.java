package fx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Exercise 35.1 - Batch Update Performance Demonstration
 * A JavaFX program that compares performance of batch updates vs individual updates
 * when inserting 1000 records into a database.
 */
public class Exercise35_01 extends Application {
    
    // Database connection
    private Connection connection;
    
    // GUI components
    private TextArea taOutput = new TextArea();
    private Button btnBatchUpdate = new Button("Batch Update");
    private Button btnNonBatchUpdate = new Button("Non Batch Update");
    private Button btnConnectDB = new Button("Connect to Database");
    
    @Override
    public void start(Stage primaryStage) {
        // Create main layout
        VBox root = createMainLayout();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Create and show the scene
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Exercise35_01 - Batch Update Performance");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Close database connection when window closes
        primaryStage.setOnCloseRequest(e -> closeConnection());
    }
    
    /**
     * Creates the main layout for the application
     */
    private VBox createMainLayout() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        
        // Title
        Label lblTitle = new Label("Batch Update Performance Test");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Button panel
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().addAll(btnBatchUpdate, btnNonBatchUpdate);
        
        // Connect button
        HBox connectPanel = new HBox();
        connectPanel.setAlignment(Pos.CENTER);
        connectPanel.getChildren().add(btnConnectDB);
        
        // Output area
        taOutput.setPrefRowCount(10);
        taOutput.setEditable(false);
        taOutput.setStyle("-fx-font-family: monospace;");
        
        root.getChildren().addAll(lblTitle, connectPanel, buttonPanel, new Label("Results:"), taOutput);
        
        // Initially disable update buttons
        btnBatchUpdate.setDisable(true);
        btnNonBatchUpdate.setDisable(true);
        
        return root;
    }
    
    /**
     * Sets up event handlers for buttons
     */
    private void setupEventHandlers() {
        btnConnectDB.setOnAction(e -> showConnectionDialog());
        btnBatchUpdate.setOnAction(e -> performBatchUpdate());
        btnNonBatchUpdate.setOnAction(e -> performNonBatchUpdate());
    }
    
    /**
     * Shows the database connection dialog
     */
    private void showConnectionDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Connect to DB");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        
        // Create connection panel
        DBConnectionPanel connectionPanel = new DBConnectionPanel();
        
        // Create dialog layout
        VBox dialogRoot = new VBox(10);
        dialogRoot.setPadding(new Insets(10));
        
        Button btnConnect = new Button("Connect to DB");
        Button btnClose = new Button("Close Dialog");
        
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().addAll(btnConnect, btnClose);
        
        dialogRoot.getChildren().addAll(connectionPanel, buttonPanel);
        
        // Event handlers for dialog buttons
        btnConnect.setOnAction(e -> {
            if (connectToDatabase(connectionPanel)) {
                dialogStage.close();
                createTempTable();
            }
        });
        
        btnClose.setOnAction(e -> dialogStage.close());
        
        Scene dialogScene = new Scene(dialogRoot, 400, 200);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }
    
    /**
     * Attempts to connect to the database using the provided connection parameters
     */
    private boolean connectToDatabase(DBConnectionPanel panel) {
        try {
            String url = panel.getConnectionURL();
            String username = panel.getUsername();
            String password = panel.getPassword();
            
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to database
            connection = DriverManager.getConnection(url, username, password);
            
            taOutput.setText("Connected to database successfully!\n");
            
            // Enable update buttons
            btnBatchUpdate.setDisable(false);
            btnNonBatchUpdate.setDisable(false);
            
            return true;
            
        } catch (ClassNotFoundException ex) {
            showAlert("Driver Error", "MySQL JDBC driver not found: " + ex.getMessage());
            return false;
        } catch (SQLException ex) {
            showAlert("Connection Error", "Failed to connect to database: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Creates the Temp table if it doesn't exist
     */
    private void createTempTable() {
        try {
            Statement statement = connection.createStatement();
            
            // Drop table if exists and create new one
            statement.executeUpdate("DROP TABLE IF EXISTS Temp");
            statement.executeUpdate("CREATE TABLE Temp(num1 DOUBLE, num2 DOUBLE, num3 DOUBLE)");
            
            statement.close();
            taOutput.appendText("Temp table created successfully.\n");
            
        } catch (SQLException ex) {
            showAlert("Database Error", "Failed to create Temp table: " + ex.getMessage());
        }
    }
    
    /**
     * Performs batch update - inserts 1000 records using batch processing
     */
    private void performBatchUpdate() {
        if (connection == null) {
            showAlert("Connection Error", "Please connect to database first.");
            return;
        }
        
        try {
            // Clear previous data
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM Temp");
            stmt.close();
            
            long startTime = System.currentTimeMillis();
            
            // Prepare statement for batch insert
            String sql = "INSERT INTO Temp (num1, num2, num3) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            // Add 1000 records to batch
            for (int i = 0; i < 1000; i++) {
                pstmt.setDouble(1, Math.random());
                pstmt.setDouble(2, Math.random());
                pstmt.setDouble(3, Math.random());
                pstmt.addBatch();
            }
            
            // Execute batch
            pstmt.executeBatch();
            pstmt.close();
            
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            
            taOutput.appendText("Batch update successful\n");
            taOutput.appendText("The elapsed time is " + elapsedTime + " milliseconds\n\n");
            
        } catch (SQLException ex) {
            showAlert("Database Error", "Batch update failed: " + ex.getMessage());
        }
    }
    
    /**
     * Performs non-batch update - inserts 1000 records individually
     */
    private void performNonBatchUpdate() {
        if (connection == null) {
            showAlert("Connection Error", "Please connect to database first.");
            return;
        }
        
        try {
            // Clear previous data
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM Temp");
            stmt.close();
            
            long startTime = System.currentTimeMillis();
            
            // Prepare statement for individual inserts
            String sql = "INSERT INTO Temp (num1, num2, num3) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            // Execute 1000 individual inserts
            for (int i = 0; i < 1000; i++) {
                pstmt.setDouble(1, Math.random());
                pstmt.setDouble(2, Math.random());
                pstmt.setDouble(3, Math.random());
                pstmt.executeUpdate();
            }
            
            pstmt.close();
            
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            
            taOutput.appendText("Non-batch update completed\n");
            taOutput.appendText("The elapsed time is " + elapsedTime + " milliseconds\n\n");
            
        } catch (SQLException ex) {
            showAlert("Database Error", "Non-batch update failed: " + ex.getMessage());
        }
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
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException ex) {
            System.err.println("Error closing database connection: " + ex.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

/**
 * Database Connection Panel - A reusable component for database connection settings
 */
class DBConnectionPanel extends GridPane {
    
    private TextField tfDriverClass = new TextField("com.mysql.cj.jdbc.Driver");
    private TextField tfDatabaseURL = new TextField("jdbc:mysql://localhost:3306/york");
    private TextField tfUsername = new TextField();
    private PasswordField pfPassword = new PasswordField();
    
    public DBConnectionPanel() {
        setupLayout();
    }
    
    private void setupLayout() {
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(10));
        
        // Add form fields
        add(new Label("JDBC Driver:"), 0, 0);
        add(tfDriverClass, 1, 0);
        
        add(new Label("Database URL:"), 0, 1);
        add(tfDatabaseURL, 1, 1);
        
        add(new Label("Username:"), 0, 2);
        add(tfUsername, 1, 2);
        
        add(new Label("Password:"), 0, 3);
        add(pfPassword, 1, 3);
        
        // Make text fields wider
        tfDriverClass.setPrefColumnCount(25);
        tfDatabaseURL.setPrefColumnCount(25);
        tfUsername.setPrefColumnCount(15);
        pfPassword.setPrefColumnCount(15);
    }
    
    
    // Getter methods
    public String getDriverClass() {
        return tfDriverClass.getText().trim();
    }
    
    public String getConnectionURL() {
        return tfDatabaseURL.getText().trim();
    }
    
    public String getUsername() {
        return tfUsername.getText().trim();
    }
    
    public String getPassword() {
        return pfPassword.getText();
    }
}