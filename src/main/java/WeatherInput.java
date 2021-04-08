//import DesktopWeather.WeatherAPI.AlreadyUpToDateException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.io.IOException;

public class WeatherInput implements EventHandler<ActionEvent> {
    private final WeatherAPI weatherAPI = new WeatherAPI();
    private final WeatherTimeDate timeDate = new WeatherTimeDate();
    private final WeatherGUI gui = new WeatherGUI();
    private boolean startupDefaultWeatherCall = true;

    /**
     * If the event being handled is from the manual call that occurs
     * on startup to get weather information for the default
     * location, the value of zipInput is returned from the
     * GUI without attempting to store the input from the GUI's
     * textField. At that time the returned value of zipInput
     * will be the zipcode for the default location.
     *
     * All other events will be from button presses performed by the
     * user. When these occur, text from the textField will be stored
     * and then returned as the new zipInput value.
     **/
    @Override
    public void handle(ActionEvent event){
    	if(!startupDefaultWeatherCall) {
    		gui.storeZipInput();
    	}
    	else {
    		startupDefaultWeatherCall = false;
    	}
        
    	String value = gui.getZipInput();
        //takes the user input value and passes it to WeatherAPI.java class
        timeDate.UpdateTimeDate();
        weatherAPI.setZipCode(value);
        try {
            weatherAPI.updateWeather();
        } catch (IOException | NullPointerException e) {
            gui.dialogBox();
        } catch (WeatherAPI.AlreadyUpToDateException e) {
        	gui.upToDateDialogBox();
        } catch (NetworkConnectionException e) {
            Platform.runLater(() -> {
                        gui.intCheckBox();
                    }
            );
        }

        /*
          Sends string value for current, day one,
          and day two weather values to the GUI.
          Calls weather name method and sends to weatherType
          method to send over correct file type for icon
          @throws NullPointerException if not zipcode is provided
         */
        try {
            gui.setWeatherFirst( "Temperature: " + weatherAPI.getTemperature() + "\u00B0" +
                    "\nPrecipitation: " + weatherAPI.getPrecipChanceToday() +
                    "\nHumidity: " + weatherAPI.getHumidity() + "\u0025" +
                    "\nPressure: " + weatherAPI.getPressure() + " mbar");
            gui.setDayOneImage(weatherType(weatherAPI.getWeatherName()));

            gui.setWeatherSecond( "Temperature Max: " + weatherAPI.getMaxTempTodayPlusOne()+ "\u00B0" +
                    "\nTemperature Min: " + weatherAPI.getMinTempTodayPlusOne() + "\u00B0" +
                    "\nPrecipitation: " + weatherAPI.getPrecipChanceTodayPlusOne() +
                    "\nHumidity: " + weatherAPI.getHumidityTodayPlusOne() + "\u0025" +
                    "\nPressure: " + weatherAPI.getPressureTodayPlusOne() + " mbar");
            gui.setDayTwoImage(weatherType(weatherAPI.getWeatherNameTodayPlusOne()));

            gui.setWeatherThird( "Temperature Max: " + weatherAPI.getMaxTempTodayPlusTwo()+ "\u00B0" +
                    "\nTemperature Min: " + weatherAPI.getMinTempTodayPlusTwo() + "\u00B0" +
                    "\nPrecipitation: " + weatherAPI.getPrecipChanceTodayPlusTwo() +
                    "\nHumidity: " + weatherAPI.getHumidityTodayPlusTwo() + "\u0025" +
                    "\nPressure: " + weatherAPI.getPressureTodayPlusTwo() + " mbar");
            gui.setDayThreeImage(weatherType(weatherAPI.getWeatherNameTodayPlusTwo()));

            gui.setLabelInput(weatherAPI.getCityName());
            gui.setWeatherOne(weatherAPI.getWeatherName());
            gui.setWeatherTwo(weatherAPI.getWeatherNameTodayPlusOne());
            gui.setWeatherThree(weatherAPI.getWeatherNameTodayPlusTwo());
        }
        //invalid inputs throw null pointer exceptions, so when caught, the program displays an error box.
        catch (NullPointerException e){
            gui.dialogBox();
            gui.setZipInput();
        }
    }
    /**
     * Takes input string value of weather type
     * matches the weather type to icon list
     * returns value of correct icon file
     */
    private String weatherType (String value){
        String weatherType;
        switch (value) {
            case "Thunderstorm":
                weatherType = "/weather_icons/icon_lightning_cloudy.png";
                break;
            case "Drizzle":
                weatherType = "/weather_icons/icon_drip.png";
                break;
            case "Rain":
                weatherType = "/weather_icons/icon_raining.png";
                break;
            case "Snow":
                weatherType = "/weather_icons/icon_snowing.png";
                break;
            case "Mist":
                weatherType = "/weather_icons/icon_foggy_cloud.png";
                break;
            case "Clear Sky":
                weatherType = "/weather_icons/icon_sunny.png";
                break;
            case "Cloudy":
                weatherType = "/weather_icons/icon_cloudy.png";
                break;
            default:
                weatherType = "/weather_icons/icon_windy.png";
        }
        return weatherType;
    }
}
