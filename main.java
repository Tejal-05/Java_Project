import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.io.*;

public class MovieTicketApp {
    private Connection connection;
    private JFrame frame;
    private LoginPanel loginPanel;
    private InfoPanel infoPanel;
    private BookingPanel bookingPanel;
    private BufferedImage backgroundImage;
    private String loggedUsername;
    private String loggedCity;
    private String loggedTheater;
    private AdminLoginPanel adminLoginPanel;
    private MovieTicketApp app; 

    public MovieTicketApp() {
        frame = new JFrame("Movie Ticket Booking");
        frame.setBounds(400, 200, 600, 650); // Set an appropriate size for the frame
        frame.setLayout(new BorderLayout(5,1));
        createMainPage();
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket", "root", "arati2003");
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
        //AdminLoginPanel adminLoginPanel = new AdminLoginPanel(movieTicketApp);
    }
    public void showMainPage() {
        frame.getContentPane().removeAll();
        createMainPage(); // Call the method to create the main page
        frame.revalidate();
        frame.repaint();
    }

    private void createMainPage() {
    	 JPanel mainPagePanel = new JPanel(new GridBagLayout()) {
             // Override paintComponent method to set background image
             @Override
             protected void paintComponent(Graphics g) {
                 super.paintComponent(g);
                 // Load your image - replace "path_to_your_image" with the path to your image file
                 ImageIcon image = new ImageIcon("th.jpg");
                 // Draw the image at the panel's size
                 g.drawImage(image.getImage(), 0, 0, getWidth(), getHeight(), this);
             }
         };
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.insets = new Insets(5, 10, 5, 10); // Padding around components

         JButton userButton = new JButton("User");
         JButton adminButton = new JButton("Admin");

         // Set a preferred size for the buttons
         Dimension buttonSize = new Dimension(120, 40); // Adjusted button size
         userButton.setPreferredSize(buttonSize);
         adminButton.setPreferredSize(buttonSize);

         // Increase font size for buttons
         Font buttonFont = userButton.getFont().deriveFont(Font.BOLD, 18); // Increased font size
         userButton.setFont(buttonFont);
         adminButton.setFont(buttonFont);

         userButton.addActionListener(e -> showUserLoginPage());
         adminButton.addActionListener(e -> showAdminLoginPage());

         // Add User button
         constraints.gridx = 0;
         constraints.gridy = 0;
         mainPagePanel.add(userButton, constraints);

         // Add Admin button
         constraints.gridx = 0;
         constraints.gridy = 1;
         mainPagePanel.add(adminButton, constraints);

         frame.getContentPane().add(mainPagePanel, BorderLayout.CENTER);
    }


    private void showUserLoginPage() {                   // Add this method to show the user login page
        frame.getContentPane().removeAll();
        loginPanel = new LoginPanel(this);
        frame.getContentPane().add(loginPanel.getPanel());
        frame.revalidate();
        frame.repaint();
    }

    private void showAdminLoginPage() {                    // Add this method to show the admin login page
        frame.getContentPane().removeAll();
        AdminLoginPanel adminLoginPanel = new AdminLoginPanel(this);
        frame.getContentPane().add(adminLoginPanel.getPanel());
        frame.revalidate();
        frame.repaint();
    }

    public void setLoggedUsername(String username) {              // Add this method to set the logged-in username
        this.loggedUsername = username;
    }

    public String getLoggedUsername() {                         // Add this method to retrieve the logged-in username
        return loggedUsername;
    }
    
    public void showInfoPanel() {
        frame.getContentPane().removeAll();
        infoPanel = new InfoPanel(this);
        frame.getContentPane().add(infoPanel.getPanel());
        frame.revalidate();
        frame.repaint();
    }

    public void showBookingPanel() {
        frame.getContentPane().removeAll();
        bookingPanel = new BookingPanel(this);
        frame.getContentPane().add(bookingPanel.getPanel());
        frame.revalidate();
        frame.repaint();
    }
 // Add this method to show the admin home page or perform other actions for admin
    public void showAdminHomePage() {
        JOptionPane.showMessageDialog(frame, "Welcome to Admin Home Page!");
    }

    public void addCity(String cityName) {          // Add this method to handle adding a city
        try {
            // Use your connection object to execute an SQL statement to insert the city into the database
            String query = "INSERT INTO cities (city_name) VALUES (?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, cityName);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
    public void addTheater(String theaterName, String cityName) {          // Add this method to handle adding a theater
        try {
            String query = "INSERT INTO theaters (theater_name, city_id) VALUES (?, (SELECT city_id FROM cities WHERE city_name = ?))";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, theaterName);
                preparedStatement.setString(2, cityName);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
    public java.util.List<String> getCities() {
        java.util.List<String> cityNames = new ArrayList<>();
        try {
            String query = "SELECT city_name FROM cities";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String cityName = resultSet.getString("city_name");
                    cityNames.add(cityName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
        return cityNames;
    }
    public void addMovie(String movieName, String genre, int durationMinutes, String releaseDate, String theaterName, String timeSlot) {
        try {
            java.sql.Date releaseDateSQL = null;        // Parse the releaseDate string to a java.sql.Date object
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedDate = dateFormat.parse(releaseDate);
                releaseDateSQL = new java.sql.Date(parsedDate.getTime());
            } catch (ParseException e) {
                e.printStackTrace(); // Handle the exception appropriately
                return;
            }

            // Check if the time slot is already reserved for the given theater and date
            if (isTimeSlotReserved(theaterName, releaseDateSQL, timeSlot)) {
                // Display a message that the time is already reserved
                System.out.println("This time slot is already reserved for another movie.");
                return;
            }

            // Use connection object to execute an SQL statement to insert the movie into the database
            String query = "INSERT INTO movies (movie_name, genre, duration_minutes, release_date, theater_id, time_slot) " +
                           "VALUES (?, ?, ?, ?, (SELECT theater_id FROM theaters WHERE theater_name = ?), ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, movieName);
                preparedStatement.setString(2, genre);
                preparedStatement.setInt(3, durationMinutes);
                preparedStatement.setDate(4, releaseDateSQL);
                preparedStatement.setString(5, theaterName);
                preparedStatement.setString(6, timeSlot);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    // Helper method to check if the time slot is already reserved
    private boolean isTimeSlotReserved(String theaterName, Date releaseDate, String timeSlot) {
        try {
            String query = "SELECT COUNT(*) FROM movies " +
                           "WHERE theater_id = (SELECT theater_id FROM theaters WHERE theater_name = ?) " +
                           "AND release_date = ? AND time_slot = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, theaterName);
                preparedStatement.setDate(2, releaseDate);
                preparedStatement.setString(3, timeSlot);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    int count = resultSet.getInt(1);
                    return count > 0; // If count is greater than 0, the time slot is already reserved
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return false; // Assuming an error means the time slot is not reserved
        }
    }
    public java.util.List<String> getTheaters() {
        java.util.List<String> theaterNames = new ArrayList<>();
        try {
            // Use connection object to execute an SQL query to retrieve theater names
            String query = "SELECT theater_name FROM theaters";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String theaterName = resultSet.getString("theater_name");
                    theaterNames.add(theaterName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
        return theaterNames;
    }

    // Add this method to retrieve a list of movie names from the database
    public java.util.List<String> getMovies() {
        java.util.List<String> movieNames = new ArrayList<>();
        try {
            // Use your connection object to execute an SQL query to retrieve movie names
            String query = "SELECT movie_name FROM movies";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String movieName = resultSet.getString("movie_name");
                    movieNames.add(movieName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
        return movieNames;
    }
    public void showCombinedCityTheaterMovieData() {
        java.util.List<String[]> data = getCombinedCityTheaterMovieData(); // Implement this method accordingly
        String[] columnNames = {"City ID", "City Name", "Theater ID", "Theater Name", "Movie ID", "Movie Name"};

        JTable dataTable = new JTable(data.toArray(new Object[0][0]), columnNames);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        JFrame tableFrame = new JFrame("CombinedCityTheaterMovie Data");
        tableFrame.getContentPane().add(scrollPane);
        tableFrame.setSize(800, 600);
        tableFrame.setLocationRelativeTo(null);
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tableFrame.setVisible(true);
    }
 // Add this method to handle deleting a city and its associated data
    public void deleteCity(String cityName) {
        try {
            // Use your connection object to execute SQL statements to delete the city and associated data
            String deleteTheatersQuery = "DELETE FROM theaters WHERE city_id = (SELECT city_id FROM cities WHERE city_name = ?)";
            try (PreparedStatement deleteTheatersStatement = connection.prepareStatement(deleteTheatersQuery)) {
                deleteTheatersStatement.setString(1, cityName);
                deleteTheatersStatement.executeUpdate();
            }

            String deleteMoviesQuery = "DELETE FROM movies WHERE theater_id IN (SELECT theater_id FROM theaters WHERE city_id = (SELECT city_id FROM cities WHERE city_name = ?))";
            try (PreparedStatement deleteMoviesStatement = connection.prepareStatement(deleteMoviesQuery)) {
                deleteMoviesStatement.setString(1, cityName);
                deleteMoviesStatement.executeUpdate();
            }

            String deleteCityQuery = "DELETE FROM cities WHERE city_name = ?";
            try (PreparedStatement deleteCityStatement = connection.prepareStatement(deleteCityQuery)) {
                deleteCityStatement.setString(1, cityName);
                deleteCityStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
 // Add this method to handle deleting a theater and its associated data
    public void deleteTheater(String theaterName) {
        try {
            // Use your connection object to execute SQL statements to delete the theater and associated data
            String deleteMoviesQuery = "DELETE FROM movies WHERE theater_id = (SELECT theater_id FROM theaters WHERE theater_name = ?)";
            try (PreparedStatement deleteMoviesStatement = connection.prepareStatement(deleteMoviesQuery)) {
                deleteMoviesStatement.setString(1, theaterName);
                deleteMoviesStatement.executeUpdate();
            }

            String deleteTheaterQuery = "DELETE FROM theaters WHERE theater_name = ?";
            try (PreparedStatement deleteTheaterStatement = connection.prepareStatement(deleteTheaterQuery)) {
                deleteTheaterStatement.setString(1, theaterName);
                deleteTheaterStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
    public List<String[]> getCombinedCityTheaterMovieData() {
        List<String[]> data = new ArrayList<>();

        try {
            String query = "SELECT c.city_id, c.city_name, t.theater_id, t.theater_name, m.movie_id, m.movie_name\r\n"
            		+ "FROM cities c\r\n"
            		+ "LEFT JOIN theaters t ON c.city_id = t.city_id\r\n"
            		+ "LEFT JOIN movies m ON t.theater_id = m.theater_id;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String cityId = resultSet.getString("city_id");
                    String cityName = resultSet.getString("city_name");
                    String theaterId = resultSet.getString("theater_id");
                    String theaterName = resultSet.getString("theater_name");
                    String movieId = resultSet.getString("movie_id");
                    String movieName = resultSet.getString("movie_name");

                    String[] rowData = {cityId, cityName, theaterId, theaterName, movieId, movieName};
                    data.add(rowData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

        return data;
    }
    public void deleteMovie(String movieName) {
        try {
            // Use your connection object to execute SQL statements to delete the movie and associated data
            String deleteTicketsQuery = "DELETE FROM tickets WHERE movie_name = ?";
            try (PreparedStatement deleteTicketsStatement = connection.prepareStatement(deleteTicketsQuery)) {
                deleteTicketsStatement.setString(1, movieName);
                deleteTicketsStatement.executeUpdate();
            }

            String deleteMovieQuery = "DELETE FROM movies WHERE movie_name = ?";
            try (PreparedStatement deleteMovieStatement = connection.prepareStatement(deleteMovieQuery)) {
                deleteMovieStatement.setString(1, movieName);
                deleteMovieStatement.executeUpdate();
            }

            // Optionally, you can perform additional actions after deleting the movie and associated data

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	MovieTicketApp app = new MovieTicketApp();
                app.showMainPage();
            }
        });
    }
}
class AdminLoginPanel {
	 private MovieTicketApp app;
	    private JPanel panel;
	    private JTextField adminUsernameField;
	    private JPasswordField adminPasswordField;
	    private JButton adminLoginButton;
	    private JButton addCityButton;
	    private JButton addTheaterButton;
	    private JButton addMovieButton;
	    private JButton deleteCityButton;
	    private JButton deleteTheaterButton;
	    private JButton deleteMovieButton;
	    private JButton updateButton;
	    private JButton backButton;

	    public AdminLoginPanel(MovieTicketApp app) {
	        this.app = app;  // Make sure to initialize the 'app' field with the provided 'app' parameter
	        panel = new JPanel();
	        createAdminLoginPanel();
	        addBackButtonListener();
	    }
	  

    private void createAdminLoginPanel() {
    	panel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        panel.setBackground(Color.cyan);

        JLabel adminUsernameLabel = new JLabel("Admin Username:");
        adminUsernameField = new JTextField();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        panel.add(adminUsernameLabel, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(adminUsernameField, gridBagConstraints);

        JLabel adminPasswordLabel = new JLabel("Admin Password:");
        JPasswordField adminPasswordField = new JPasswordField();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        panel.add(adminPasswordLabel, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(adminPasswordField, gridBagConstraints);

        adminLoginButton = new JButton("Admin Login");
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(adminLoginButton, gridBagConstraints);

        addCityButton = new JButton("Add City");
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(addCityButton, gridBagConstraints);

        addTheaterButton = new JButton("Add Theater");
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(addTheaterButton, gridBagConstraints);

        addMovieButton = new JButton("Add Movie");
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(addMovieButton, gridBagConstraints);

        deleteCityButton = new JButton("Delete City");
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(deleteCityButton, gridBagConstraints);

        deleteTheaterButton = new JButton("Delete Theater");
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(deleteTheaterButton, gridBagConstraints);

        deleteMovieButton = new JButton("Delete Movie");
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(deleteMovieButton, gridBagConstraints);

        updateButton = new JButton("Update");
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(updateButton, gridBagConstraints);

        backButton = new JButton("Back");
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(backButton, gridBagConstraints);
        addCityButton.setEnabled(false);
        addTheaterButton.setEnabled(false);
        addMovieButton.setEnabled(false);
        deleteCityButton.setEnabled(false);
        deleteTheaterButton.setEnabled(false);
        deleteMovieButton.setEnabled(false);
        updateButton.setEnabled(false);
        backButton.setEnabled(true);

        // Action listeners for the new buttons
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpdate();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBack();
            }
        });

        // Add action listeners for the existing buttons
        adminLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String enteredAdminUsername = adminUsernameField.getText();
                char[] enteredAdminPassword = adminPasswordField.getPassword();
                if (authenticateAdmin(enteredAdminUsername, enteredAdminPassword)) {
                    app.showAdminHomePage();
                    addCityButton.setEnabled(true);
        	        addTheaterButton.setEnabled(true);
        	        addMovieButton.setEnabled(true);
        	        deleteCityButton.setEnabled(true);
        	        deleteTheaterButton.setEnabled(true);
        	        deleteMovieButton.setEnabled(true);
        	        updateButton.setEnabled(true);
                    
                } else {
                    JOptionPane.showMessageDialog(panel, "Invalid admin credentials.");
                }
            }
        });

        addCityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddCityDialog();
            }
        });

        addTheaterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddTheaterDialog();
            }
        });

        addMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddMovieDialog();
            }
        });

        deleteCityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDeleteCityDialog();
            }
        });

        deleteTheaterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDeleteTheaterDialog();
            }
        });

        deleteMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDeleteMovieDialog();
            }
        });
    }
    private void addBackButtonListener() {
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBack();
            }
        });
        
    }
    
    private void handleUpdate() {
        JOptionPane.showMessageDialog(panel, "Update functionality to be implemented.");
        app.showCombinedCityTheaterMovieData();
    }

    private void handleBack() {
       app.showMainPage();
    }
    private boolean authenticateAdmin(String enteredUsername, char[] enteredPassword) {
        // Implement authentication logic
        return true; // Replace with your authentication logic
    }

    private void showAddCityDialog() {
    	JTextField cityNameField = new JTextField();

        JPanel addCityPanel = new JPanel();
        addCityPanel.setLayout(new GridLayout(2, 2));
        addCityPanel.add(new JLabel("City Name:"));
        addCityPanel.add(cityNameField);

        int result = JOptionPane.showConfirmDialog(null, addCityPanel, "Add City", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String cityName = cityNameField.getText();
            if (!cityName.isEmpty()) {
                app.addCity(cityName); // Call the backend method to add the city
                // Optionally, update the UI or show a message to indicate success
                JOptionPane.showMessageDialog(panel, "City added successfully!");
            } else {
                JOptionPane.showMessageDialog(panel, "City name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddTheaterDialog() {
    	 JTextField theaterNameField = new JTextField();
    	    JComboBox<String> cityComboBox = new JComboBox<>();

    	    java.util.List<String> cityNames = app.getCities();
    	    for (String cityName : cityNames) {
    	        cityComboBox.addItem(cityName);
    	    }

        JPanel addTheaterPanel = new JPanel();
        addTheaterPanel.setLayout(new GridLayout(3, 2));
        addTheaterPanel.add(new JLabel("Theater Name:"));
        addTheaterPanel.add(theaterNameField);
        addTheaterPanel.add(new JLabel("City:"));
        addTheaterPanel.add(cityComboBox);

        int result = JOptionPane.showConfirmDialog(null, addTheaterPanel, "Add Theater", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String theaterName = theaterNameField.getText();
            String selectedCity = (String) cityComboBox.getSelectedItem();
            if (!theaterName.isEmpty() && selectedCity != null) {
                app.addTheater(theaterName, selectedCity); // Call the backend method to add the theater
                // Optionally, update the UI or show a message to indicate success
                JOptionPane.showMessageDialog(panel, "Theater added successfully!");
            } else {
                JOptionPane.showMessageDialog(panel, "Theater name and city cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void addMovie(Connection connection ,String movieName, String genre, int durationMinutes, String releaseDate, String theaterName, String timeSlot) {
        try {
            java.sql.Date releaseDateSQL = null;
            java.sql.Time movieTime = null;
            
            // Parse the releaseDate string to a java.sql.Date object
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedDate = dateFormat.parse(releaseDate);
                releaseDateSQL = new java.sql.Date(parsedDate.getTime());
            } catch (ParseException e) {
                e.printStackTrace(); // Handle the exception appropriately
                return;
            }

            // Parse the timeSlot string to a java.sql.Time object
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                java.util.Date parsedTime = timeFormat.parse(timeSlot);
                movieTime = new java.sql.Time(parsedTime.getTime());
            } catch (ParseException e) {
                e.printStackTrace(); // Handle the exception appropriately
                return;
            }

            // Check if the time slot is already reserved for the given theater and date
            if (isTimeSlotReserved(null, theaterName, releaseDateSQL, movieTime)) {
                // Display a message that the time is already reserved
                System.out.println("This time slot is already reserved for another movie.");
                return;
            }

            // Use connection object to execute an SQL statement to insert the movie into the database
            String query = "INSERT INTO movies (movie_name, genre, duration_minutes, release_date, movie_time, theater_id) " +
                           "VALUES (?, ?, ?, ?, ?, (SELECT theater_id FROM theaters WHERE theater_name = ?))";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, movieName);
                preparedStatement.setString(2, genre);
                preparedStatement.setInt(3, durationMinutes);
                preparedStatement.setDate(4, releaseDateSQL);
                preparedStatement.setTime(5, movieTime);
                preparedStatement.setString(6, theaterName);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
    
    private void showAddMovieDialog() {
        JTextField movieNameField = new JTextField();
        JTextField genreField = new JTextField();
        JTextField durationMinutesField = new JTextField();
        JTextField releaseDateField = new JTextField();
        JComboBox<String> theaterComboBox = new JComboBox<>();
        JComboBox<String> timeSlotComboBox = new JComboBox<>(new String[]{"12:00 PM to 3:00 PM", "3:00 PM to 6:00 PM", "6:00 PM to 9:00 PM"});

        // Populate the theaterComboBox with existing theaters
        List<String> theaterNames = app.getTheaters();
        for (String theaterName : theaterNames) {
            theaterComboBox.addItem(theaterName);
        }

        JPanel addMoviePanel = new JPanel();
        addMoviePanel.setLayout(new GridLayout(6, 2)); // Increased rows to accommodate the new combo box
        addMoviePanel.add(new JLabel("Movie Name:"));
        addMoviePanel.add(movieNameField);
        addMoviePanel.add(new JLabel("Genre:"));
        addMoviePanel.add(genreField);
        addMoviePanel.add(new JLabel("Duration (minutes):"));
        addMoviePanel.add(durationMinutesField);
        addMoviePanel.add(new JLabel("Release Date (yyyy-MM-dd):"));
        addMoviePanel.add(releaseDateField);
        addMoviePanel.add(new JLabel("Theater:"));
        addMoviePanel.add(theaterComboBox);
        addMoviePanel.add(new JLabel("Select Time Slot:"));
        addMoviePanel.add(timeSlotComboBox);

        int result = JOptionPane.showConfirmDialog(null, addMoviePanel, "Add Movie", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String movieName = movieNameField.getText();
            String genre = genreField.getText();
            int durationMinutes = Integer.parseInt(durationMinutesField.getText());
            String releaseDate = releaseDateField.getText();
            String selectedTheater = (String) theaterComboBox.getSelectedItem();
            String selectedTimeSlot = (String) timeSlotComboBox.getSelectedItem();

            if (!movieName.isEmpty() && !genre.isEmpty() && durationMinutes > 0 && !releaseDate.isEmpty() && selectedTheater != null && selectedTimeSlot != null) {
                // Call the backend method to add the movie
                app.addMovie(movieName, genre, durationMinutes, releaseDate, selectedTheater, selectedTimeSlot);
                // Optionally, update the UI or show a message to indicate success
                JOptionPane.showMessageDialog(addMoviePanel, "Movie added successfully!");
            } else {
                JOptionPane.showMessageDialog(addMoviePanel, "Please fill in all fields with valid values.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Helper method to check if the time slot is already reserved
    private boolean isTimeSlotReserved(Connection connection,String theaterName, Date releaseDate, Time movieTime) {
        try {
            String query = "SELECT COUNT(*) FROM movies " +
                           "WHERE theater_id = (SELECT theater_id FROM theaters WHERE theater_name = ?) " +
                           "AND release_date = ? AND movie_time = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, theaterName);
                preparedStatement.setDate(2, releaseDate);
                preparedStatement.setTime(3, movieTime);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    int count = resultSet.getInt(1);
                    return count > 0; // If count is greater than 0, the time slot is already reserved
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return false; // Assuming an error means the time slot is not reserved
        }
    }

 // Add this method to show the delete city dialog
    private void showDeleteCityDialog() {
    	 JComboBox<String> cityComboBox = new JComboBox<>();

    	    // Populate the cityComboBox with existing cities
    	    // You may need to modify this based on how you retrieve city names from your database
    	    java.util.List<String> cityNames = app.getCities(); // Implement getCities method accordingly
    	    for (String cityName : cityNames) {
    	        cityComboBox.addItem(cityName);
    	    }

    	    JPanel deleteCityPanel = new JPanel();
    	    deleteCityPanel.setLayout(new GridLayout(2, 2));
    	    deleteCityPanel.add(new JLabel("Select City to Delete:"));
    	    deleteCityPanel.add(cityComboBox);

    	    int result = JOptionPane.showConfirmDialog(null, deleteCityPanel, "Delete City", JOptionPane.OK_CANCEL_OPTION);
    	    if (result == JOptionPane.OK_OPTION) {
    	        String selectedCity = (String) cityComboBox.getSelectedItem();
    	        if (selectedCity != null) {
    	            // Call the backend method to delete the city and associated data
    	            app.deleteCity(selectedCity);

    	            // Optionally, update the UI or show a message to indicate success
    	            JOptionPane.showMessageDialog(panel, "City and associated data deleted successfully!");
    	        } else {
    	            JOptionPane.showMessageDialog(panel, "Please select a city.", "Error", JOptionPane.ERROR_MESSAGE);
    	        }
    	    }
    }

    // Add this method to show the delete theater dialog
    private void showDeleteTheaterDialog() {
    	JComboBox<String> theaterComboBox = new JComboBox<>();

        // Populate the theaterComboBox with existing theaters
        // You may need to modify this based on how you retrieve theater names from your database
        java.util.List<String> theaterNames = app.getTheaters(); // Implement getTheaters method accordingly
        for (String theaterName : theaterNames) {
            theaterComboBox.addItem(theaterName);
        }

        JPanel deleteTheaterPanel = new JPanel();
        deleteTheaterPanel.setLayout(new GridLayout(2, 2));
        deleteTheaterPanel.add(new JLabel("Select Theater to Delete:"));
        deleteTheaterPanel.add(theaterComboBox);

        int result = JOptionPane.showConfirmDialog(null, deleteTheaterPanel, "Delete Theater", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String selectedTheater = (String) theaterComboBox.getSelectedItem();
            if (selectedTheater != null) {
                // Call the backend method to delete the theater and associated data
                app.deleteTheater(selectedTheater);

                // Optionally, update the UI or show a message to indicate success
                JOptionPane.showMessageDialog(panel, "Theater and associated data deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a theater.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Add this method to show the delete movie dialog
    private void showDeleteMovieDialog() {
    	JComboBox<String> movieComboBox = new JComboBox<>();

        // Populate the movieComboBox with existing movies
        // You may need to modify this based on how you retrieve movie names from your database
        java.util.List<String> movieNames = app.getMovies(); // Implement getMovies method accordingly
        for (String movieName : movieNames) {
            movieComboBox.addItem(movieName);
        }

        JPanel deleteMoviePanel = new JPanel();
        deleteMoviePanel.setLayout(new GridLayout(2, 2));
        deleteMoviePanel.add(new JLabel("Select Movie to Delete:"));
        deleteMoviePanel.add(movieComboBox);

        int result = JOptionPane.showConfirmDialog(null, deleteMoviePanel, "Delete Movie", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String selectedMovie = (String) movieComboBox.getSelectedItem();
            if (selectedMovie != null) {
                // Call the backend method to delete the movie and associated data
                app.deleteMovie(selectedMovie);

                // Optionally, update the UI or show a message to indicate success
                JOptionPane.showMessageDialog(panel, "Movie and associated data deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a movie.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public JPanel getPanel() {
        return panel;
    }
}
class LoginPanel {
    private MovieTicketApp app;
    private JPanel panel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPanel(MovieTicketApp app) {
        this.app = app;
        panel = new JPanel();
        createLoginPanel();
    }

    private void createLoginPanel() {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        JPanel picturePanel = new JPanel();   // Left Panel for the picture
        picturePanel.setLayout(new BorderLayout());

        ImageIcon originalImageIcon = new ImageIcon("cinema.jpg");
        ImageIcon scaledImageIcon = scaleImage(originalImageIcon, 700, 700); // Adjust width and height as needed

        JLabel imageLabel = new JLabel(scaledImageIcon);
        picturePanel.add(imageLabel, BorderLayout.CENTER);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;

        panel.add(picturePanel, gridBagConstraints);

        JPanel loginFieldsPanel = new JPanel();           // Right Panel for login fields
        loginFieldsPanel.setLayout(new GridLayout(5, 2));
        loginFieldsPanel.setBackground(Color.gray);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;

        panel.add(loginFieldsPanel, gridBagConstraints);

        JLabel messageLabel = new JLabel("BOOK YOUR TICKETS NOW...");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setForeground(Color.BLACK);
        Font messageFont = messageLabel.getFont();
        messageLabel.setFont(new Font(messageFont.getName(),Font.ITALIC,20));

        loginFieldsPanel.add(messageLabel);
        loginFieldsPanel.add(new JLabel("")); // Empty label for spacing
        
        Icon usernameIcon = new ImageIcon(new ImageIcon("user.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        Icon passwordIcon = new ImageIcon(new ImageIcon("lock.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        JLabel usernameLabel = new JLabel("Username:",usernameIcon,JLabel.LEFT);
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:",passwordIcon,JLabel.LEFT);
        passwordField = new JPasswordField();
        usernameLabel.setFont(new Font("Serif",Font.PLAIN,22));
        passwordLabel.setFont(new Font("Serif",Font.PLAIN,22));
        
        loginFieldsPanel.add(usernameLabel);
        loginFieldsPanel.add(usernameField);
        loginFieldsPanel.add(passwordLabel);
        loginFieldsPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(Color.PINK);
        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.setBackground(Color.pink);
        Font buttonFont = loginButton.getFont().deriveFont(Font.BOLD, 18);
        Font buttonFont1 = createAccountButton.getFont().deriveFont(Font.BOLD, 18);
        loginButton.setFont(buttonFont);
        createAccountButton.setFont(buttonFont);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	String enteredUsername=usernameField.getText();
            	char[] enteredPassword = passwordField.getPassword();
                if (authenticateUser (enteredUsername, enteredPassword)) {
                	app.setLoggedUsername(enteredUsername);
                    app.showInfoPanel();
                } else {
                    JOptionPane.showMessageDialog(panel, "Invalid username or password.");
                }
            }
        });

        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistrationDialog();
            }
        });

        loginFieldsPanel.add(loginButton);
        loginFieldsPanel.add(createAccountButton);
    }

    private ImageIcon scaleImage(ImageIcon icon, int width, int height) {
        Image image = icon.getImage();
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private boolean authenticateUser(String enteredUsername,char[] enteredPassword ) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket","root","arati2003");

            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, enteredUsername);
                preparedStatement.setString(2, new String(enteredPassword));

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void openRegistrationDialog() {
    	LoginPanel l = new LoginPanel(app);
        JTextField newUserField = new JTextField();
        JPasswordField newPasswordField = new JPasswordField();
        JTextField emailField = new JTextField();
        JTextField ageField = new JTextField();

        JPanel registrationPanel = new JPanel();
        registrationPanel.setLayout(new GridLayout(2, 2));
        registrationPanel.add(new JLabel("New Username:"));
        registrationPanel.add(newUserField);
        registrationPanel.add(new JLabel("New Password:"));
        registrationPanel.add(newPasswordField);
        registrationPanel.add(new JLabel("Email:"));
        registrationPanel.add(emailField);
        registrationPanel.add(new JLabel("Age:"));
        registrationPanel.add(ageField);

        int result = JOptionPane.showConfirmDialog(panel, registrationPanel, "Create Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newUsername = newUserField.getText();
            char[] newPassword = newPasswordField.getPassword();
            String email = emailField.getText();
            int age = Integer.parseInt(ageField.getText());

            // Store the new user information
            registerUser(newUsername, new String(newPassword), email, age);
            JOptionPane.showMessageDialog(panel, "Account created for " + newUsername);
            
            //Log in the new user automatically
            app.setLoggedUsername(newUsername);
            l.createLoginPanel();
            
        }
    }
    private void registerUser(String username, String password, String email, int age) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket","root","arati2003");
            if(age<=18) {
            	JOptionPane.showMessageDialog(panel, "Enter valid age");
            }
            String query = "INSERT INTO users (username, password, email, age) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, email);
                preparedStatement.setInt(4, age);

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JPanel getPanel() {
        return panel;
    }
}

class InfoPanel {
    private MovieTicketApp app;
    private JPanel panel;
    private JTable infoTable;
    private DefaultTableModel tableModel;

    public InfoPanel(MovieTicketApp app) {
        this.app = app;
        panel = new JPanel();
        showInfoPanel();
    }

    private void showInfoPanel() {
        panel.setLayout(new BorderLayout());

        String[] columnNames = {"Information Type", "Data"};
        tableModel = new DefaultTableModel(columnNames, 0);

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket","root","arati2003");

            String cityQuery = "SELECT city_name FROM cities";
            addRow("Available Cities:", fetchOptions(connection, cityQuery));

            String theaterQuery = "SELECT theater_name FROM theaters";
            addRow("Available Theaters:", fetchOptions(connection, theaterQuery));

            String movieQuery = "SELECT movie_name FROM movies";
            addRow("Available Movies:", fetchOptions(connection, movieQuery));

            addRow("Available Seats:", "50");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        infoTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(infoTable);
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(panel, "Proceeding to the next step.");
                app.showBookingPanel();
            }
        });

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(scrollPane, BorderLayout.CENTER);
        infoPanel.add(nextButton, BorderLayout.SOUTH);

        panel.add(infoPanel, BorderLayout.CENTER);
    }

    private String fetchOptions(Connection connection, String query) {
        StringBuilder result = new StringBuilder();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                result.append(resultSet.getString(1)).append(", ");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result.toString().replaceAll(", $", "");
    }

    private void addRow(String type, String data) {
        tableModel.addRow(new Object[]{type, data});
    }

    public JPanel getPanel() {
        return panel;
    }
}

 class BookingPanel {
	 private MovieTicketApp app;
	    private JPanel panel;
	    private JComboBox<String> cityComboBox;
	    private JComboBox<String> theaterComboBox;
	    private JComboBox<String> movieComboBox;
	    private JComboBox<String> timeComboBox;
	    private SeatReservationSystem seatReservationSystem;

	    public BookingPanel(MovieTicketApp app) {
	        this.app = app;
	        panel = new JPanel();
	        showBookingPanel();
	    }

	    private void showBookingPanel() {
	        panel.setLayout(new GridLayout(8, 2));

	        JLabel cityLabel = new JLabel("Select City:");
	        JLabel theaterLabel = new JLabel("Select Theater:");
	        JLabel movieLabel = new JLabel("Select Movie:");
	        JLabel timeLabel = new JLabel("Select Time:");

	     // Fetch available cities, theaters, and movies from the database
	        String[] cities = fetchAvailableCities();

	        // Check if cities array is not empty before accessing its first element
	        String[] theaters = cities.length > 0 ? fetchAvailableTheaters(cities[0]) : new String[0];

	        // Check if theaters array is not empty before accessing its first element
	        String[] movies = theaters.length > 0 ? fetchAvailableMovies(theaters[0]) : new String[0];

	        // Check if movies array is not empty before accessing its first element
	        String[] times = movies.length > 0 ? fetchAvailableTimes(movies[0]) : new String[0];

	        cityComboBox = new JComboBox<>(cities);
	        theaterComboBox = new JComboBox<>(theaters);
	        movieComboBox = new JComboBox<>(movies);
	        timeComboBox = new JComboBox<>(times);

	        cityComboBox.setBackground(Color.PINK);
	        movieComboBox.setBackground(Color.PINK);
	        theaterComboBox.setBackground(Color.PINK);
	        timeComboBox.setBackground(Color.PINK);

	        cityComboBox.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String selectedCity = (String) cityComboBox.getSelectedItem();
	                String[] theatersForCity = fetchAvailableTheaters(selectedCity);
	                theaterComboBox.setModel(new DefaultComboBoxModel<>(theatersForCity));

	                String[] moviesForTheater = fetchAvailableMovies(theatersForCity[0]);
	                movieComboBox.setModel(new DefaultComboBoxModel<>(moviesForTheater));

	                String[] timesForMovie = fetchAvailableTimes(moviesForTheater[0]);
	                timeComboBox.setModel(new DefaultComboBoxModel<>(timesForMovie));
	            }
	        });

	        theaterComboBox.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String selectedTheater = (String) theaterComboBox.getSelectedItem();
	                String[] moviesForTheater = fetchAvailableMovies(selectedTheater);
	                movieComboBox.setModel(new DefaultComboBoxModel<>(moviesForTheater));

	                String[] timesForMovie = fetchAvailableTimes(moviesForTheater[0]);
	                timeComboBox.setModel(new DefaultComboBoxModel<>(timesForMovie));
	            }
	        });

	        movieComboBox.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String selectedMovie = (String) movieComboBox.getSelectedItem();
	                String[] timesForMovie = fetchAvailableTimes(selectedMovie);
	                timeComboBox.setModel(new DefaultComboBoxModel<>(timesForMovie));
	            }
	        });

	        JButton reserveSeatsButton = new JButton("Reserve Seats");
	        reserveSeatsButton.addActionListener(e -> reserveSeats());

	        JButton nextButton = new JButton("Next");
	        nextButton.addActionListener(e -> showNextStep());

	        panel.add(cityLabel);
	        panel.add(cityComboBox);
	        panel.add(theaterLabel);
	        panel.add(theaterComboBox);
	        panel.add(movieLabel);
	        panel.add(movieComboBox);
	        panel.add(timeLabel);
	        panel.add(timeComboBox);
	        panel.add(reserveSeatsButton);
	        panel.add(nextButton);
	    }

	    private String[] fetchAvailableCities() {
	        java.util.List<String> cities = new ArrayList<>();
	        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket", "root", "arati2003");
	             Statement statement = connection.createStatement();
	             ResultSet resultSet = statement.executeQuery("SELECT DISTINCT city_name FROM cities")) {

	            while (resultSet.next()) {
	                cities.add(resultSet.getString("city_name"));
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return cities.toArray(new String[0]);
	    }

	    private String[] fetchAvailableTheaters(String city) {
	    	java.util.List<String> theaters = new ArrayList<>();
	        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket", "root", "arati2003");
	             PreparedStatement preparedStatement = connection.prepareStatement("SELECT theater_name FROM theaters WHERE city_id = (SELECT city_id FROM cities WHERE city_name = ?)")) {

	            preparedStatement.setString(1, city);

	            try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                    theaters.add(resultSet.getString("theater_name"));
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return theaters.toArray(new String[0]);
	    }

	    private String[] fetchAvailableMovies(String theater) {
	    	java.util.List<String> movies = new ArrayList<>();
	        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket", "root", "arati2003");
	             PreparedStatement preparedStatement = connection.prepareStatement("SELECT movie_name FROM movies WHERE theater_id = (SELECT theater_id FROM theaters WHERE theater_name = ?)")) {

	            preparedStatement.setString(1, theater);

	            try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                    movies.add(resultSet.getString("movie_name"));
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return movies.toArray(new String[0]);
	    }

	    private String[] fetchAvailableTimes(String movie) {
	    	java.util.List<String> times = new ArrayList<>();
	        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket", "root", "arati2003");
	             PreparedStatement preparedStatement = connection.prepareStatement("SELECT movie_time FROM movies WHERE movie_name = ?")) {

	            preparedStatement.setString(1, movie);

	            try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                    times.add(resultSet.getString("movie_time"));
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return times.toArray(new String[0]);
	    }
    private void reserveSeats() {
        seatReservationSystem = new SeatReservationSystem(0, null, null);   // Your seat reservation logic goes here
    }

    private void showNextStep() {
        // Proceed to the next step (e.g., payment)
        if (seatReservationSystem != null && seatReservationSystem.isSeatsReserved()) {
            String selectedCity = cityComboBox.getSelectedItem().toString();
            String selectedTheater = theaterComboBox.getSelectedItem().toString();
            String selectedMovie = movieComboBox.getSelectedItem().toString();
            String selectedTime = timeComboBox.getSelectedItem().toString();
            // Create an instance of the Payment class
            Payment payment = new Payment(selectedCity, selectedTheater, selectedMovie, selectedTime);
        } else {
            JOptionPane.showMessageDialog(panel, "Please reserve seats before proceeding.");
        }
    }

    public JPanel getPanel() {
        return panel;
    }
}
class SeatReservationSystem extends JFrame implements ActionListener {
	    private JButton[] seatButtons;
	    private JLabel remainingSeatsLabel;
	    private JLabel reservedSeatsLabel;
	    private JLabel TotalcostLabel;
	    private static int bookingsCount = 0;

	    private Map<Integer, Boolean> seatStatusMap;
		private int theaterId;
		private static String movieName;
		private String username;

	    public SeatReservationSystem(int theaterId, String movieName, String username) {
	        setTitle("Seat Reservation System");
	        setSize(800, 400);
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        this.theaterId = theaterId;
	        this.movieName = movieName;
	        this.username = username;

	        JTabbedPane tabbedPane = new JTabbedPane();

	        JPanel reservationPanel = new JPanel();    
	        reservationPanel.setLayout(new FlowLayout());

	        int numberOfSeats = 50;
	        seatButtons = new JButton[numberOfSeats];
	        seatStatusMap = new HashMap<>();

	        for (int i = 0; i < numberOfSeats; i++) {
	            seatButtons[i] = new JButton("Seat " + (i + 1));
	            seatButtons[i].addActionListener(this);
	            reservationPanel.add(seatButtons[i]);
	            seatStatusMap.put(i + 1, false); 
	        }
	        tabbedPane.addTab("Seat Reservation", null, reservationPanel, "Select and reserve seats");

	        JPanel remainingSeatsPanel = new JPanel();
	        remainingSeatsLabel = new JLabel("Remaining Seats: " + numberOfSeats);
	        remainingSeatsPanel.add(remainingSeatsLabel);
	        tabbedPane.addTab("Remaining Seats", null, remainingSeatsPanel, "Display remaining seats");

	        JPanel reservedSeatsPanel = new JPanel();
	        reservedSeatsLabel = new JLabel("Reserved Seats: 0");
	        reservedSeatsPanel.add(reservedSeatsLabel);
	        tabbedPane.addTab("Reserved Seats", null, reservedSeatsPanel, "Display reserved seats");
	        
	        JPanel TotalcostPanel = new JPanel(); 
	        TotalcostLabel = new JLabel("Total Cost: 0");
	        TotalcostPanel.add(TotalcostLabel);
	        tabbedPane.addTab("Total Cost", null, TotalcostPanel, "DisplayTotal Cost");

	        add(tabbedPane);
	        setVisible(true);
	    }
	    @Override
	    public void actionPerformed(ActionEvent e) {
	        for (int i = 0; i < seatButtons.length; i++) {
	            if (e.getSource() == seatButtons[i]) {
	                int seatNumber = i + 1;
	                if (seatStatusMap.get(seatNumber)) {
	                    JOptionPane.showMessageDialog(this, "Seat " + seatNumber + " is already reserved!");
	                } else {
	                	int theaterId = 0;
	                	String movieName = null;
	                	String username = null;
	                    updateReservationStatus(theaterId, movieName, seatNumber, username);

	                    seatStatusMap.put(seatNumber, true);
	                    updateSeatButtonStatus();
	                    bookingsCount++;
	                    remainingSeatsLabel.setText("Remaining Seats: " + (seatButtons.length - bookingsCount));
	                    reservedSeatsLabel.setText("Reserved Seats: " + bookingsCount);
	                    TotalcostLabel.setText("Total Cost: "+bookingsCount*90);
	                    JOptionPane.showMessageDialog(this, "Seat " + seatNumber + " reserved successfully!");
	                }
	            }
	        }
	    }
	    
	    private void updateReservationStatus(int theaterId, String movieName, int seatNumber, String username) {
	        try {
	            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket", "root", "arati2003");

	            String updateQuery = "UPDATE ticket_info SET is_reserved = true WHERE theater_id = ? AND movie_name = ? AND seat_number = ? AND is_reserved = false";
	            try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
	                statement.setInt(1, theaterId);
	                statement.setString(2, movieName);
	                statement.setInt(3, seatNumber);

	                int rowsUpdated = statement.executeUpdate();

	                if (rowsUpdated > 0) {
	                    // insert the reservation details into the 'tickets' table
	                    String insertTicketQuery = "INSERT INTO tickets (username, movie_name, seat_number) VALUES (?, ?, ?)";
	                    try (PreparedStatement insertStatement = connection.prepareStatement(insertTicketQuery)) {
	                        insertStatement.setString(1, username);
	                        insertStatement.setString(2, movieName);
	                        insertStatement.setInt(3, seatNumber);
	                        insertStatement.executeUpdate();

	                        System.out.println("Seat reserved successfully!");
	                    }
	                } else {
	                    System.out.println("Seat is already reserved or unavailable.");
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    private void updateSeatButtonStatus() {
	        for (int i = 0; i < seatButtons.length; i++) {
	            int seatNumber = i + 1;
	            boolean reserved = seatStatusMap.get(seatNumber);
	            if (reserved) {
	                seatButtons[i].setBackground(Color.RED); // red color for reserved seats
	            } else {
	                seatButtons[i].setBackground(Color.GREEN); // green color for available seats
	            }
	        }
	    }

	    public boolean isSeatsReserved() {
	        return bookingsCount > 0; 
	    }

	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	            SeatReservationSystem seatReservationSystem = new SeatReservationSystem(bookingsCount, movieName, movieName);
	        });
	    }
	}
class Payment extends JFrame implements ActionListener {
    private JRadioButton creditCardButton, debitCardButton, paytmButton, netBankingButton;
    private JButton payButton;
    private JLabel qrCodeLabel;
    private JPanel paymentPanel;
    private String selectedCity, selectedTheater, selectedMovie, selectedTime, numberOfTickets;

    public Payment(String selectedCity, String selectedTheater, String selectedMovie, String selectedTime) {
        this.selectedCity = selectedCity;
        this.selectedTheater = selectedTheater;
        this.selectedMovie = selectedMovie;
        this.selectedTime = selectedTime;
        this.numberOfTickets = numberOfTickets;

        setTitle("Payment Options");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        creditCardButton = new JRadioButton("Credit Card");   // Create Radio Buttons
        debitCardButton = new JRadioButton("Debit Card");
        paytmButton = new JRadioButton("Paytm");
        netBankingButton = new JRadioButton("Net Banking");

        ButtonGroup group = new ButtonGroup();    // Create Button Group
        group.add(creditCardButton);
        group.add(debitCardButton);
        group.add(paytmButton);
        group.add(netBankingButton);

        payButton = new JButton("Pay");    // Create Pay Button
        payButton.addActionListener(this);

        paymentPanel = new JPanel();        // Create Payment Panel
        paymentPanel.setLayout(new GridLayout(5, 1));
        paymentPanel.add(creditCardButton);
        paymentPanel.add(debitCardButton);
        paymentPanel.add(paytmButton);
        paymentPanel.add(netBankingButton);
        paymentPanel.add(payButton);

        qrCodeLabel = new JLabel();    // Create QR Code Label

        JPanel mainPanel = new JPanel();     // Create Main Panel
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(paymentPanel, BorderLayout.NORTH);
        mainPanel.add(qrCodeLabel, BorderLayout.CENTER);

        add(mainPanel);     // Add Main Panel to Frame

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (creditCardButton.isSelected()) {
            processPayment("Credit Card");
        } else if (debitCardButton.isSelected()) {
            processPayment("Debit Card");
        } else if (paytmButton.isSelected()) {
            displayPaytmQRCode("QR2.jpg"); // Replace with the actual path
        } else if (netBankingButton.isSelected()) {
            processPayment("Net Banking");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a payment option");
        }
    }

    private void displayPaytmQRCode(String qrCodeImagePath) {
        try {
            BufferedImage qrCodeImage = ImageIO.read(new File(qrCodeImagePath));   // Load existing QR Code Image
            qrCodeLabel.setIcon(new ImageIcon(qrCodeImage));     // Display QR Code Image
            paymentPanel.setVisible(false);  // Hide the payment panel
            Timer timer = new Timer(10000, new ActionListener() {  // Set up a timer to wait for 10 seconds
                @Override
                public void actionPerformed(ActionEvent e) {
                    // After 20 seconds, display "Payment Successful" message
                    JOptionPane.showMessageDialog(Payment.this, "Payment Successful using Paytm");
                    dispose();  // Close the payment window
                    generateTicket();   // Generate ticket after successful payment
                }
            });
            timer.setRepeats(false); // Execute the timer only once
            timer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processPayment(String paymentMethod) {
        JOptionPane.showMessageDialog(this, "Payment Successful using " + paymentMethod);
        dispose();   // Close the payment window
        generateTicket();  // Generate ticket after successful payment
    }

    private void generateTicket() {
        int ticketId = TicketGenerator.generateTicket(getName(),selectedMovie, selectedTime);
       
        JOptionPane.showMessageDialog(this, "Ticket Generated Successfully!\nTicket ID: " + ticketId); // Display the generated ticket ID
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Payment("City", "Theater", "Movie", "Time"));
    }
}

class TicketGenerator {
    public static int generateTicket(String username, String selectedMovie, String selectedTime) {
        Connection connection1 = null;
        try {
            connection1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ticket", "root", "arati2003");

            String insertTicketQuery = "INSERT INTO tickets (username, movie_name, seat_number, movie_time) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertTicketStatement = connection1.prepareStatement(insertTicketQuery, Statement.RETURN_GENERATED_KEYS)) {
                insertTicketStatement.setString(1, username);
                insertTicketStatement.setString(2, selectedMovie);
                insertTicketStatement.setInt(3, generateRandomSeatNumber()); // Replace this with your seat selection logic
                insertTicketStatement.setString(4, selectedTime);

                int affectedRows = insertTicketStatement.executeUpdate();

                if (affectedRows > 0) {
                    ResultSet generatedKeys = insertTicketStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int ticketId = generatedKeys.getInt(1);
                        String ticketDetails = getTicketDetails(connection1, ticketId);
                        JOptionPane.showMessageDialog(null, "Ticket Generated Successfully!\n" + ticketDetails);
                        return ticketId;
                    } else {
                        System.err.println("Error retrieving ticket ID.");
                    }
                } else {
                    System.err.println("Error booking ticket.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        } finally {
            // Close the database connection in a finally block
            if (connection1 != null) {
                try {
                    connection1.close();
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle exceptions appropriately
                }
            }
        }
        return -1; // Return -1 to indicate an error
    }

    private static String getTicketDetails(Connection connection, int ticketId) {
        try {
            String query = "SELECT * FROM tickets WHERE ticket_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, ticketId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String username = resultSet.getString("username");
                        String movieName = resultSet.getString("movie_name");
                        int seatNumber = resultSet.getInt("seat_number");
                        String movieTime = resultSet.getString("movie_time");

                        return String.format("Username: %s\nMovie: %s\nSeat Number: %d\nTime: %s",
                                username, movieName, seatNumber, movieTime);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }

        return "Error retrieving ticket details.";
    }

    private static int generateRandomSeatNumber() {
        return (int) (Math.random() * 100) + 1;  // Replace this with your logic to generate a seat number
    }
}

