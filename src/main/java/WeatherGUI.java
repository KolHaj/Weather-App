import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WeatherGUI extends Application {
    //Variables
    Button enterValue;
    static TextField zipValue;
    static TextArea weatherFirst;
    static TextArea weatherSecond;
    static TextArea weatherThird;
    static Label locationName;
    static Label timeDate;
    static Label zipLabel;
    static Label dayOneDate;
    static Label dayTwoDate;
    static Label dayThreeDate;
    static Label dayOneWeatherType;
    static Label dayTwoWeatherType;
    static Label dayThreeWeatherType;
    WeatherTimeDate timeDateInt = new WeatherTimeDate();
    static Image dayOneImage;
    static Image dayTwoImage;
    static Image dayThreeImage;
    static ImageView dayOne;
    static ImageView dayTwo;
    static ImageView dayThree;
    String initialDayOne = "file:";
    String initialDayTwo = "file:";
    String initialDayThree = "file:";
    private static String zipInput;

    public void start (Stage weatherStage) throws Exception {

        //Placeholder for BorderPane
        Text rightText = new Text("");
        Text leftText = new Text("");

        //Image for weather for first of three day weather forecast
        dayOneImage = new Image(initialDayOne);
        dayOne = new ImageView();
        dayOne.setImage(dayOneImage);
        dayOne.setFitHeight(170);
        dayOne.setFitWidth(170);

        //Image for weather for second of three day weather forecast
        dayTwoImage = new Image(initialDayTwo);
        dayTwo = new ImageView();
        dayTwo.setImage(dayTwoImage);
        dayTwo.setFitHeight(170);
        dayTwo.setFitWidth(170);

        // Image for weather for third of three day weather forecast
        dayThreeImage = new Image(initialDayThree);
        dayThree = new ImageView();
        dayThree.setImage(dayThreeImage);
        dayThree.setFitHeight(170);
        dayThree.setFitWidth(170);

        // TextArea holding weather data output
        // Default location name
        locationName = new Label();
        dayOneWeatherType = new Label();
        dayTwoWeatherType = new Label();
        dayThreeWeatherType = new Label();
        timeDate = new Label(timeDateInt.getTimeDate());
        dayOneDate = new Label(timeDateInt.getDayOne());
        dayTwoDate = new Label(timeDateInt.getDayTwo());
        dayThreeDate = new Label(timeDateInt.getDayThree());
        zipLabel = new Label("Enter Zip Code");

        zipValue = new TextField();
        zipValue.setPromptText("Zip Code");
        zipValue.setMaxWidth(130);

        enterValue = new Button("Enter");
        // Input field box with label, input and enter
        HBox inputField = new HBox(10, zipLabel, zipValue, enterValue);
        inputField.setAlignment(Pos.CENTER);

        // TextArea Properties
        weatherFirst = new TextArea();
        weatherFirst.setMaxHeight(140);
        weatherFirst.setMaxWidth(250);
        weatherFirst.setEditable(false);

        // TextArea Properties
        weatherSecond = new TextArea();
        weatherSecond.setMaxHeight(140);
        weatherSecond.setMaxWidth(250);
        weatherSecond.setEditable(false);

        // TextArea Properties
        weatherThird = new TextArea();
        weatherThird.setMaxHeight(140);
        weatherThird.setMaxWidth(250);
        weatherThird.setEditable(false);

        // Layout container for weather data
        HBox weatherDisplay = new HBox(40, weatherFirst, weatherSecond, weatherThird);
        weatherDisplay.setAlignment(Pos.CENTER);

        // Layout container for weather images
        HBox weatherImage = new HBox(120, dayOne, dayTwo, dayThree);
        weatherImage.setAlignment(Pos.CENTER);

        // Labels for each forecast box date on top
        HBox weatherDateLabels = new HBox(195, dayOneDate, dayTwoDate, dayThreeDate);
        weatherDateLabels.setAlignment(Pos.CENTER);

        // Labels for each forecast box weather type
        HBox weatherTypeLabels = new HBox(265, dayOneWeatherType, dayTwoWeatherType, dayThreeWeatherType);
        weatherTypeLabels.setAlignment(Pos.CENTER);

        // Layout container for weather data and images
        VBox weatherView = new VBox(1, weatherImage, weatherDateLabels, weatherTypeLabels, weatherDisplay);
        weatherDisplay.setAlignment(Pos.CENTER);

        // Label for the top of the GUI with location name, time and date
        VBox labelView = new VBox(1, locationName, timeDate);
        labelView.setAlignment(Pos.CENTER);

        // Set the alignment of the Top Text to Center
        BorderPane.setAlignment(labelView, Pos.TOP_CENTER);

        // Set the alignment of the Bottom Text to Center
        BorderPane.setAlignment(inputField, Pos.BOTTOM_CENTER);

        // Create a BorderPane with a Text node in each of the five regions
        BorderPane root = new BorderPane(weatherView, labelView, rightText, inputField, leftText);
        // Set the Size of the VBox
        root.setPrefSize(900, 500);
        // Set the Style-properties of the BorderPane
        root.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;");

        // Create the Scene
        Scene scene = new Scene(root);
        // Add the scene to the Stage
        weatherStage.setScene(scene);
        // Set the title of the Stage
        weatherStage.setTitle("Weather Application");
        // Display the Stage
        weatherStage.show();
        // Create instance of the handler class
        WeatherInput handler = new WeatherInput();
        // Call to action after user enters input
        enterValue.setOnAction(handler);
        zipInput = "10001";
        enterValue.fire();

        weatherStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if(KeyCode.ESCAPE.equals(event.getCode())){
                weatherStage.close();
            }
        });

    }
    // Retrieve the zip code input from the textfield and store it in
    // the zipInput String value
    public void storeZipInput() {
    	zipInput = zipValue.getText();
    }
    
    // Return the zipInput String value
    public static String getZipInput() {
        return zipInput;
    }
    // Set the zip code value to an empty string
    public static void setZipInput() {
        zipValue.setText("");
    }
    // Set the text inside the first text area on the GUI
    public static void setWeatherFirst(String firstWeatherBox) {
        weatherFirst.setText(firstWeatherBox);
    }
    // Set the text inside the second text area on the GUI
    public static void setWeatherSecond(String secondWeatherBox) {
        weatherSecond.setText(secondWeatherBox);
    }
    // Set the text inside the third text area on the GUI
    public static void setWeatherThird(String thirdWeatherBox) {
        weatherThird.setText(thirdWeatherBox);
    }
    // Sets the text in the label above the three images
    public static void setLabelInput(String location) {
        locationName.setText(location);
    }
    // Sets the text in the label above the first image
    public static void setWeatherOne(String boxOneLabel) {
        dayOneWeatherType.setText(boxOneLabel);
    }
    // Sets the text in the label above the second image
    public static void setWeatherTwo(String boxTwoLabel) {
        dayTwoWeatherType.setText(boxTwoLabel);
    }
    // Sets the text in the label above the third image
    public static void setWeatherThree(String boxThreeLabel) {
        dayThreeWeatherType.setText(boxThreeLabel);
    }
    // Setter for day one Image
    public void setDayOneImage(String imageFile) {
        Platform.runLater(() -> {
                initialDayOne = imageFile;
                dayOneImage = new Image(getClass().getResourceAsStream(imageFile));
                dayOne.setImage(dayOneImage);
            }
        );
    }
    // Setter for day two Image
    public void setDayTwoImage(String imageFile) {
        Platform.runLater(() -> {
                initialDayTwo = imageFile;
                dayTwoImage = new Image(getClass().getResourceAsStream(imageFile));
                dayTwo.setImage(dayTwoImage);
            }
        );
    }
    // Setter for day three Image
    public void setDayThreeImage(String imageFile) {
        Platform.runLater(() -> {
                initialDayThree = imageFile;
                dayThreeImage = new Image(getClass().getResourceAsStream(imageFile));
                dayThree.setImage(dayThreeImage);
            }
        );
    }
    // Settings for the error message box if user enters invalid zip code input
    public static void dialogBox (){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Warning");
        alert.setHeaderText("Invalid Input");
        alert.setContentText("Invalid input or zipcode!");

        alert.showAndWait();
    }
    // Settings for the error message box if user attempts to update before 10 minutes
    public void upToDateDialogBox() {
    	Alert alert = new Alert(Alert.AlertType.WARNING);
    	alert.initStyle(StageStyle.UTILITY);
    	alert.setTitle("Warning");
        alert.setHeaderText("Information Already Up To Date");
        alert.setContentText("Weather can only be updated every 10 minutes!");
        alert.showAndWait();
    }
    // Settings for the error message box if the internet connection fails
    public static void intCheckBox () {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Desktop Weather Application");
        alert.setHeaderText("Not connected to internet!");
        alert.setContentText("Please check your internet connection!");
        alert.showAndWait();
    }
    // Settings for the error message box if the internet connection fails
    public static void intAlertBox () throws InterruptedException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Desktop Weather Application");
        alert.setHeaderText("Checking Internet Connection!");
        alert.setContentText("Please wait!");
        alert.show();
        Platform.runLater(() -> {
                try {
                    Thread.sleep(3000);
                    alert.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        );
    }

}