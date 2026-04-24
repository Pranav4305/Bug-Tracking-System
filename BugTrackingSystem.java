import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BugTrackingSystem {

    private JFrame frame;
    private JPanel upperPanel;
    private JPanel lowerPanel;
    private JTable bugTable;
    private JButton submitButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JTextField bugTitleField;
    private JTextField bugDescriptionField;
    private JComboBox<String> priorityComboBox;
    private JComboBox<String> statusComboBox;

    private DefaultTableModel bugTableModel;
    private Connection connection;

    private JFrame loginFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private void initializeLoginComponents() {
        loginFrame = new JFrame("Login");
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel()); // Empty label for spacing
        loginPanel.add(loginButton);

        loginFrame.add(loginPanel);
        loginFrame.setSize(300, 150);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);
        loginFrame.setLocationRelativeTo(null); // Center the login window

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);

                if (username.equals("admin") && password.equals("password")) {
                    loginFrame.dispose(); // Close the login window
                    showMainWindow(); // Show the main Bug Tracking System window
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid credentials. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    // Clear fields on failed login
                    usernameField.setText("");
                    passwordField.setText("");
                }
            }
        });
    }

    private void showMainWindow() {
        initializeComponents();
        connectToDatabase();
        loadData(); // Load existing bug data from the database
        addListeners();

        frame.setVisible(true);
    }

    public BugTrackingSystem() {
        initializeLoginComponents();
    }

    private void initializeComponents() {
        frame = new JFrame("Bug Tracking System");
        upperPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        lowerPanel = new JPanel(new BorderLayout());

        bugTableModel = new DefaultTableModel();
        bugTableModel.addColumn("Bug ID");
        bugTableModel.addColumn("Title");
        bugTableModel.addColumn("Description");
        bugTableModel.addColumn("Priority");
        bugTableModel.addColumn("Status");

        bugTable = new JTable(bugTableModel);
        bugTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        submitButton = new JButton("Submit Bug Report");
        updateButton = new JButton("Update Bug");
        deleteButton = new JButton("Delete Bug");

        bugTitleField = new JTextField(20);
        bugDescriptionField = new JTextField(50);

        priorityComboBox = new JComboBox<>();
        priorityComboBox.addItem("Low");
        priorityComboBox.addItem("Medium");
        priorityComboBox.addItem("High");

        statusComboBox = new JComboBox<>();
        statusComboBox.addItem("Open");
        statusComboBox.addItem("In Progress");
        statusComboBox.addItem("Closed");

        upperPanel.add(new JLabel("Bug Title:"));
        upperPanel.add(bugTitleField);
        upperPanel.add(new JLabel("Description:"));
        upperPanel.add(bugDescriptionField);
        upperPanel.add(new JLabel("Priority:"));
        upperPanel.add(priorityComboBox);
        upperPanel.add(new JLabel("Status:"));
        upperPanel.add(statusComboBox);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        int verticalSpace = 20;
        upperPanel.setBorder(BorderFactory.createEmptyBorder(verticalSpace, 0, verticalSpace, 0));

        lowerPanel.add(new JScrollPane(bugTable), BorderLayout.CENTER);
        lowerPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(upperPanel, BorderLayout.NORTH);
        frame.add(lowerPanel, BorderLayout.CENTER);

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(false);
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/db";
            String username = "root";
            String password = "1234";
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        bugTableModel.setRowCount(0);
        try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM bugs")) {
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                String priority = resultSet.getString("priority");
                String status = resultSet.getString("status");
                String[] bugData = {id, title, description, priority, status};
                bugTableModel.addRow(bugData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertBugToDatabase(String title, String description, String priority, String status) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO bugs (title, description, priority, status) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setString(3, priority);
            statement.setString(4, status);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBugInDatabase(int bugId, String title, String description, String priority, String status) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE bugs SET title=?, description=?, priority=?, status=? WHERE id=?")) {
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setString(3, priority);
            statement.setString(4, status);
            statement.setInt(5, bugId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteBugFromDatabase(int bugId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM bugs WHERE id=?")) {
            statement.setInt(1, bugId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addListeners() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bugTitle = bugTitleField.getText();
                String bugDescription = bugDescriptionField.getText();
                String priority = (String) priorityComboBox.getSelectedItem();
                String status = (String) statusComboBox.getSelectedItem();
                String[] bugData = {String.valueOf(bugTableModel.getRowCount() + 1), bugTitle, bugDescription, priority, status};
                bugTableModel.addRow(bugData);
                insertBugToDatabase(bugTitle, bugDescription, priority, status);
                bugTitleField.setText("");
                bugDescriptionField.setText("");
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = bugTable.getSelectedRow();
                if (selectedRow != -1) {
                    int bugId = Integer.parseInt((String) bugTableModel.getValueAt(selectedRow, 0));
                    String bugTitle = bugTitleField.getText();
                    String bugDescription = bugDescriptionField.getText();
                    String priority = (String) priorityComboBox.getSelectedItem();
                    String status = (String) statusComboBox.getSelectedItem();
                    bugTableModel.setValueAt(bugTitle, selectedRow, 1);
                    bugTableModel.setValueAt(bugDescription, selectedRow, 2);
                    bugTableModel.setValueAt(priority, selectedRow, 3);
                    bugTableModel.setValueAt(status, selectedRow, 4);
                    updateBugInDatabase(bugId, bugTitle, bugDescription, priority, status);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = bugTable.getSelectedRow();
                if (selectedRow != -1) {
                    int bugId = Integer.parseInt((String) bugTableModel.getValueAt(selectedRow, 0));
                    bugTableModel.removeRow(selectedRow);
                    deleteBugFromDatabase(bugId);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Show login window first
                new BugTrackingSystem();
            }
        });
    }
}
