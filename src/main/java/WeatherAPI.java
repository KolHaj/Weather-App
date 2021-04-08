import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class WeatherAPI {
    private static String API_KEY;
    static{
        try {
            API_KEY = DataReader.getProperty("API_KEY");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final String API_KEY_PHRASE = "&appid=" + API_KEY;
    private static final String CALL_BY_ZIPCODE = "?zip=";
    private static final String DATA_FORMAT = "&mode=xml";
    private String urlCallAddress;
    private final UserHandler userHandler = new UserHandler();
    private final ForecastHandler forecastHandler = new ForecastHandler();
    private String zipCode;
    private String countryCode;
    private Properties currentWeather = new Properties();
    private Properties forecastWeather = new Properties();
    private boolean canUpdate, startupDefaultWeatherCall = true;
    private static LocalDateTime currentTimeDate;

    /**
     * Default configuration of weather API. Set zipcode and/or country code for different location. Call update weather
     * to return current weather information.
     */
    public WeatherAPI() {
        zipCode = "10001";
        countryCode = "us";
        canUpdate = true;
    }

    /**
     * Method organizes the weather call and timing.
     * @throws AlreadyUpToDateException & IOException thrown by call to callWeather() so
     * that they can be handled in the GUI subsystem.
     */
    public void updateWeather() throws IOException, AlreadyUpToDateException, NetworkConnectionException {
        LocalDateTime localTime = LocalDateTime.now(ZoneOffset.UTC);
        
        if (localTime.isAfter(currentTimeDate.plusMinutes(10))){
            canUpdate = true;
        }

        if (canUpdate){
            checkNetworkConnection();
        	if(!startupDefaultWeatherCall) {
        		canUpdate = false;
        	}
        	else {
        		startupDefaultWeatherCall = false;
        	}

            getWeatherDataByZipCode(false);
            callWeather(userHandler);
            currentWeather = userHandler.readWeather();
            getWeatherDataByZipCode(true);
            callWeather(forecastHandler);
            forecastWeather = forecastHandler.readWeather();
        }else{
            throw new AlreadyUpToDateException();
        }
    }
    
    static class AlreadyUpToDateException extends Exception{
		private static final long serialVersionUID = 1L;
		
		public AlreadyUpToDateException() {
			super("Weather information already up to date");
		}
    }

    /**
     * Method creates and runs the SAX parser to read in XML data from the Open Weather Map API url.
     * Handles exceptions that occur during the creation of the parser or its attempt to process 
     * incoming XML data.
     * @throws IOException thrown by call to saxParser.parse(new URL(urlCallAddress).openStream(), userHandler)
     */
    private void callWeather(DefaultHandler userHandler) throws IOException{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new URL(urlCallAddress).openStream(), userHandler);
        } catch(SAXException | ParserConfigurationException e) {
        	e.printStackTrace();
        }
    }

    /**
     * Method executes the necessary instructions to check if the user's computer has an active internet
     * connection.
     * Handles exceptions that occur when the JVM attempts to create a thread to execute these
     * instructions or when that thread is interrupted before completing these instructions.
     * @throws NetworkConnectionException if an active internet connection is not detected.
     */
    void checkNetworkConnection() throws NetworkConnectionException {
    	try {
			Process netConnectionChecker = Runtime.getRuntime().exec("ping www.google.com");
			int threadTermination = netConnectionChecker.waitFor();
			
			if(threadTermination != 0) {
				throw new NetworkConnectionException();
			}
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Method builds the URL string using the zip code format.
     */
    private void getWeatherDataByZipCode(boolean isForecast) {
    	String serverName, forecastCount;
        String temperatureFormat = "&units=imperial";
    	
    	if(isForecast) {
    		serverName = "http://api.openweathermap.org/data/2.5/forecast";
    		forecastCount = "&cnt=24";
    	}
    	else {
    		serverName = "http://api.openweathermap.org/data/2.5/weather";
    		forecastCount = "";
    	}
        urlCallAddress = serverName +
                CALL_BY_ZIPCODE +
                zipCode +
                "," +
                countryCode +
                DATA_FORMAT +
                temperatureFormat +
                forecastCount +
                API_KEY_PHRASE;
    }

    /**
     * Method allows the zip code to be changed for the location to retrieve weather information for.
     * @param zipCode 5 digit standard zip code.
     */
    public void setZipCode(String zipCode) {
        if (!zipCode.equals(this.zipCode)){
            canUpdate = true;
        }
        this.zipCode = zipCode;
    }

    /**
     * Method allows the country code to be changed for the location to retrieve weather information for.
     * Follow ISO 3166 format.
     * @param countryCode ISO 3166 designated code for Country.
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    /**
     * Method allows externally created objects of this class to retrieve the current temperature.
     * @return "currentTemperature" String from the currentWeather Properties object.
     */
    public String getTemperature() {
    	return currentWeather.getProperty("currentTemperature");
    }
    
    /**
     * The following two methods allow externally created objects of this class to retrieve
     * the minimum and maximum temperature for the current day plus one and the current
     * day plus two.
     * @return "minTemperatureTodayPlusOne" String from the forecastWeather Properties
     * object.
     */
    public String getMinTempTodayPlusOne() {
    	return forecastWeather.getProperty("minTemperatureTodayPlusOne");
    }
    
    /**
     * @return "maxTemperatureTodayPlusOne" String from the forecastWeather Properties
     * object.
     */
    public String getMaxTempTodayPlusOne() {
    	return forecastWeather.getProperty("maxTemperatureTodayPlusOne");
    }
    
    /**
     * The following two methods allow externally created objects of this class to retrieve
     * the minimum and maximum temperature for the current day plus one and the current
     * day plus two.
     * @return "minTemperatureTodayPlusTwo" String from the forecastWeather Properties
     * object.
     */
    public String getMinTempTodayPlusTwo() {
    	return forecastWeather.getProperty("minTemperatureTodayPlusTwo");
    }
    
    /**
     * @return "maxTemperatureTodayPlusTwo" String from the forecastWeather Properties
     * object.
     */
    public String getMaxTempTodayPlusTwo() {
    	return forecastWeather.getProperty("maxTemperatureTodayPlusTwo");
    }
    
    /**
     * Method allows externally created objects of this class to retrieve the chance of
     * precipitation for the current day.
     * @return "precipitationChanceToday" String from the forecastWeather Properties
     * object.
     */
    public String getPrecipChanceToday() {
    	return forecastWeather.getProperty("precipitationChanceToday");
    }
    
    /**
     * The following two methods allow externally created objects of this class to retrieve
     * the chance of precipitation for the current day plus one and the current day plus two.
     * @return "precipitationChanceTodayPlusOne" String from the forecastWeather
     * Properties object.
     */
    public String getPrecipChanceTodayPlusOne() {
    	return forecastWeather.getProperty("precipitationChanceTodayPlusOne");
    }
    
    /**
     * @return "precipitationChanceTodayPlusTwo" String from the forecastWeather
     * Properties object.
     */
    public String getPrecipChanceTodayPlusTwo() {
    	return forecastWeather.getProperty("precipitationChanceTodayPlusTwo");
    }
    
    /**
     * Method allows externally created objects of this class to retrieve the humidity
     * for the current day.
     * @return "humidity" String from the currentWeather Properties object.
     */
    public String getHumidity() {
    	return currentWeather.getProperty("humidity");
    }
    
    /**
     * The following two methods allow externally created objects of this class to retrieve
     * the chance maximum humidity for the current day plus one and the current day plus two.
     * @return "maxHumidityTodayPlusOne" String from the forecastWeather Properties object.
     */
    public String getHumidityTodayPlusOne() {
    	return forecastWeather.getProperty("maxHumidityTodayPlusOne");
    }
    
    /**
     * @return "maxHumidityTodayPlusTwo" String from the forecastWeather Properties object.
     */
    public String getHumidityTodayPlusTwo() {
    	return forecastWeather.getProperty("maxHumidityTodayPlusTwo");
    }
    
    /**
     * Method allows externally created objects of this class to retrieve the pressure
     * for the current day.
     * @return "pressure" String from the currentWeather Properties object.
     */
    public String getPressure() {
    	return currentWeather.getProperty("pressure");
    }
    
    /**
     * The following two methods allow externally created objects of this class to retrieve
     * the chance maximum pressure for the current day plus one and the current day plus two.
     * @return "maxPressureTodayPlusOne" String from the forecastWeather Properties object.
     */
    public String getPressureTodayPlusOne() {
    	return forecastWeather.getProperty("maxPressureTodayPlusOne");
    }
    
    /**
     * @return "maxPressureTodayPlusTwo" String from the forecastWeather Properties object.
     */
    public String getPressureTodayPlusTwo() {
    	return forecastWeather.getProperty("maxPressureTodayPlusTwo");
    }
    
    /**
     * Method allows externally created objects of this class to retrieve the 
     * weather type by name for today.
     * @return "weatherValue" String from currentWeather Properties object.
     */
    public String getWeatherName() {
    	return currentWeather.getProperty("weatherValue");
    }
    
    /**
     * The following two methods allow externally created objects of this class to
     * retrieve the weather type by name for the current day plus one and the
     * current day plus two.
     * @return "todayPlusOneWeatherName" String from forecastWeather Properties
     * object.
     */
    public String getWeatherNameTodayPlusOne() {
    	return forecastWeather.getProperty("todayPlusOneWeatherName");
    }
    
    /**
     * @return "todayPlusTwoWeatherName" String from forecastWeather Properties
     * object.
     */
    public String getWeatherNameTodayPlusTwo() {
    	return forecastWeather.getProperty("todayPlusTwoWeatherName");
    }
    
    /**
     * Method allows externally created objects of this class to retrieve the 
     * weather type by number for today.
     * @return "weatherNumber" String from currentWeather Properties object.
     */
    public String getWeatherNum() {
    	return currentWeather.getProperty("weatherNumber");
    }
    
    /**
     * The following two methods allow externally created objects of this class to
     * retrieve the weather type by number for the current day plus one and the
     * current day plus two.
     * @return "todayPlusOneWeatherNum" String from forecastWeather Properties
     * object.
     */
    public String getWeatherNumTodayPlusOne() {
    	return forecastWeather.getProperty("todayPlusOneWeatherNum");
    }
    
    /**
     * @return "todayPlusTwoWeatherNum" String from forecastWeather Properties
     * object.
     */
    public String getWeatherNumTodayPlusTwo() {
    	return forecastWeather.getProperty("todayPlusTwoWeatherNum");
    }
    
    /**
     * Method allows externally created objects of this class to retrieve the "cityName" property
     * from the "weather" Properties object.
     */
    public String getCityName() {
    	return currentWeather.getProperty("cityName");
    }

    /**
     * Method allows externally created objects of this class to retrieve the "lastUpdate" property
     * from the "weather" Properties object.
     */
    public String getLastWeatherUpdate(){return currentWeather.getProperty("lastUpdate");}

    /**
     * Method allows externally created objects of this class to retrieve the "isEmpty" condition
     * from the "weather" Properties object.
     */
    public Boolean getWeatherIsEmpty(){return currentWeather.isEmpty();}

    /**
     * Method sets internal LocalDateTime "currentTimeDate" variable of this class to the current time
     * from the "WeatherTimeDate" Class.
     */
    public void setWeatherTimeDate(LocalDateTime input){
        currentTimeDate = input;}

}

/**
 * Class reads the XML data stream, for the current day, using SAX Parser.
 */
class UserHandler extends DefaultHandler {

    private String cityID, cityName, longitude, latitude, country, timezone, sunrise, sunset, currentTemperature,
            minimumTemperature, maximumTemperature, feelsLike, humidity, pressure, windSpeed, windName,
            windDirectionValue, windDirectionCode, windDirectionName, cloudyValue, cloudyName, visibility,
            precipitationMode, weatherNumber, weatherValue, weatherIcon, lastUpdate;
    private boolean hasCountry, hasTimezone = false;
    private final Properties weather = new Properties();

    /**
     * Method looks for specific string values in an XML document and then stores their associated values.
     * @param uri The Namespace URI.
     * @param localName The local name (without prefix).
     * @param qName The qualified name (with prefix).
     * @param attributes The attributes attached to the element.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {

        if (qName.equalsIgnoreCase("city")) {
            cityID = attributes.getValue("id");
            cityName = attributes.getValue("name");
        } else if (qName.equalsIgnoreCase("coord")) {
            longitude = attributes.getValue("lon");
            latitude = attributes.getValue("lat");
        } else if (qName.equalsIgnoreCase("country")) {
            hasCountry = true;
        } else if (qName.equalsIgnoreCase("timezone")) {
            hasTimezone = true;
        } else if (qName.equalsIgnoreCase("sun")) {
            sunrise = attributes.getValue("rise");
            sunset = attributes.getValue("set");
        } else if (qName.equalsIgnoreCase("temperature")) {
            currentTemperature = attributes.getValue("value");
            minimumTemperature = attributes.getValue("min");
            maximumTemperature = attributes.getValue("max");
        } else if (qName.equalsIgnoreCase("feels_like")) {
            feelsLike = attributes.getValue("value");
        } else if (qName.equalsIgnoreCase("humidity")) {
            humidity = attributes.getValue("value");
        } else if (qName.equalsIgnoreCase("pressure")) {
            pressure = attributes.getValue("value");
        } else if (qName.equalsIgnoreCase("speed")) {
            windSpeed = attributes.getValue("value");
            windName = attributes.getValue("name");
        } else if (qName.equalsIgnoreCase("direction")) {
            windDirectionValue = attributes.getValue("value");
            windDirectionCode = attributes.getValue("code");
            windDirectionName = attributes.getValue("name");
        } else if (qName.equalsIgnoreCase("clouds")) {
            cloudyValue = attributes.getValue("value");
            cloudyName = attributes.getValue("name");
        } else if (qName.equalsIgnoreCase("visibility")) {
            visibility = attributes.getValue("value");
        } else if (qName.equalsIgnoreCase("precipitation")) {
            precipitationMode = attributes.getValue("mode");
        } else if (qName.equalsIgnoreCase("weather")) {
            weatherNumber = attributes.getValue("number");
            weatherValue = attributes.getValue("value");
            weatherIcon = attributes.getValue("icon");
        } else if (qName.equalsIgnoreCase("lastupdate")) {
            lastUpdate = attributes.getValue("value");
        }
    }

    /**
     * Method reads in commented text in XML for country and timezone.
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters to use from the character array.
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        if (hasCountry){
            country = new String(ch, start, length);
            hasCountry = false;
        } else if (hasTimezone){
            timezone = new String(ch, start, length);
            hasTimezone = false;
        }
    }

    /**
     * Method assigns all of the stored variables into the Parameter weather.
     * @return Returns Properties of weather.
     */
    public Properties readWeather(){
        weather.put("cityID", cityID);
        weather.put("cityName", cityName);
        weather.put("longitude", longitude);
        weather.put("latitude", latitude);
        weather.put("country", country);
        weather.put("timezone", timezone);
        weather.put("sunrise", sunrise);
        weather.put("sunset", sunset);
        weather.put("currentTemperature", currentTemperature);
        weather.put("minimumTemperature", minimumTemperature);
        weather.put("maximumTemperature", maximumTemperature);
        weather.put("feelsLike", feelsLike);
        weather.put("humidity", humidity);
        weather.put("pressure", pressure);
        weather.put("windSpeed", windSpeed);
        weather.put("windName", windName);
        //weather.put("windDirectionValue", windDirectionValue);
        //weather.put("windDirectionCode", windDirectionCode);
        //weather.put("windDirectionName", windDirectionName);
        weather.put("cloudyValue", cloudyValue);
        weather.put("cloudyName", cloudyName);
        weather.put("visibility", visibility);
        weather.put("precipitationMode", precipitationMode);
        
        int weatherNumI = Integer.parseInt(weatherNumber);
        
        if(weatherNumI >= 200 && weatherNumI <= 232) {
        	weatherNumber = "211";
        	weatherValue = "Thunderstorm";
        }
        else if(weatherNumI >= 300 && weatherNumI <= 321) {
        	weatherNumber = "301";
        	weatherValue = "Drizzle";
        }
        else if(weatherNumI >= 500 && weatherNumI <= 531) {
        	weatherNumber = "501";
        	weatherValue = "Rain";
        }
        else if(weatherNumI >= 600 && weatherNumI <= 622) {
        	weatherNumber = "601";
        	weatherValue = "Snow";
        }
        else if(weatherNumI >= 701 && weatherNumI <= 781) {
        	weatherNumber = "701";
        	weatherValue = "Mist";
        }
        else if(weatherNumI == 800) {
        	weatherNumber = "800";
        	weatherValue = "Clear Sky";
        }
        else if(weatherNumI >= 801 && weatherNumI <= 804) {
        	weatherNumber = "801";
        	weatherValue = "Cloudy";
        }
        
        weather.put("weatherNumber", weatherNumber);
        weather.put("weatherValue", weatherValue);
        weather.put("weatherIcon", weatherIcon);
        weather.put("lastUpdate", lastUpdate);

        return weather;
    }
}

/**
 * This class defines a custom exception that is instantiated in the event that the 
 * application is unable to establish a connection with the internet.
 */
class NetworkConnectionException extends Exception {
	private static final long serialVersionUID = 1L;

	public NetworkConnectionException() {
		super("No active internet connection. "
				+ "Please establish a connection and try again.");
	}
}

/**
 * Class reads the XML data stream, for the current day plus one and the current day plus
 * two, using SAX Parser.
 */
class ForecastHandler extends DefaultHandler {
    private final Properties weather = new Properties();
    private final String[] temperature = new String[24];
    private final String[] humidity = new String[24];
    private final String[] pressure = new String[24];
    private final String[] precipitation = new String[24];
    private final String[] weatherNumber = new String[24];
    //private String[] weatherName = new String[24];
    private final String[] timeDate = new String[24];
    private int incremental = 0;

    /**
     * Method looks for specific string values in an XML document and then stores their associated values.
     * @param uri The Namespace URI.
     * @param localName The local name (without prefix).
     * @param qName The qualified name (with prefix).
     * @param attributes The attributes attached to the element.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equalsIgnoreCase("weatherData")){
            incremental = 0;
        } else if(qName.equalsIgnoreCase("time")){
            timeDate[incremental] = attributes.getValue("from");
        } else if(qName.equalsIgnoreCase("symbol")){
            weatherNumber[incremental] = attributes.getValue("number");
            //weatherName[incremental] = attributes.getValue("name");
        } else if(qName.equalsIgnoreCase("precipitation")){
            precipitation[incremental] = attributes.getValue("probability");
        } else if (qName.equalsIgnoreCase("temperature")){
            temperature[incremental] = attributes.getValue("value");
        } else if (qName.equalsIgnoreCase("pressure")){
            pressure[incremental] = attributes.getValue("value");
        } else if (qName.equalsIgnoreCase("humidity")){
            humidity[incremental] = attributes.getValue("value");
        }
    }

    /**
     * Method looks for "time" to know when the end of the document has been reached.
     * @param uri The Namespace URI.
     * @param localName The local name (without prefix).
     * @param qName The qualified name (with prefix).
     */
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("time")) {
                incremental++;
        }
    }

    /**
     * Method assigns all of the stored variables into the Parameter weather.
     * @return Returns Properties of weather.
     */
    public Properties readWeather(){
        LocalDateTime localTime = LocalDateTime.now(ZoneOffset.UTC);
        LocalDate today = localTime.toLocalDate();
        LocalDate todayPlusOne = today.plusDays(1);
        LocalDate todayPlusTwo = today.plusDays(2);
        float maxTempToday = -1000.0f;
        float minTempToday = 1000.0f;
        float maxTempTodayPlusOne = -1000.0f;
        float minTempTodayPlusOne = 1000.0f;
        float maxTempTodayPlusTwo = -1000.0f;
        float minTempTodayPlusTwo = 1000.0f;

        //Min Max temperature per day
        for (int i = 0; i < temperature.length; i++) {
            String tempS = temperature[i];
            float tempF = Float.parseFloat(tempS);

            if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(today)){
                if (tempF > maxTempToday){
                    maxTempToday = tempF;
                } else if (tempF < minTempToday){
                    minTempToday = tempF;
                }
            } else if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusOne)){
                if (tempF > maxTempTodayPlusOne){
                    maxTempTodayPlusOne = tempF;
                } else if (tempF < minTempTodayPlusOne){
                    minTempTodayPlusOne = tempF;
                }
            } else if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusTwo)){
                if (tempF > maxTempTodayPlusTwo){
                    maxTempTodayPlusTwo = tempF;
                } else if (tempF < minTempTodayPlusTwo){
                    minTempTodayPlusTwo = tempF;
                }
            }
        }
        weather.put("maxTemperatureTodayPlusOne", Float.toString(maxTempTodayPlusOne));
        weather.put("maxTemperatureTodayPlusTwo", Float.toString(maxTempTodayPlusTwo));
        weather.put("minTemperatureTodayPlusOne", Float.toString(minTempTodayPlusOne));
        weather.put("minTemperatureTodayPlusTwo", Float.toString(minTempTodayPlusTwo));

        //Precipitation chance per day
        float precipProbabilityToday = 0.0f;
        float precipProbabilityTodayPlusOne = 0.0f;
        float precipProbabilityTodayPlusTwo = 0.0f;
        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMinimumFractionDigits(1);

        for (int i = 0; i < precipitation.length; i++) {
            String precipS = precipitation[i];
            float precipF = Float.parseFloat(precipS);

            if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(today)){
                if(precipF > precipProbabilityToday){
                    precipProbabilityToday = precipF;
                }
            }else if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusOne)){
                if(precipF > precipProbabilityTodayPlusOne){
                    precipProbabilityTodayPlusOne = precipF;
                }
            }else if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusTwo)){
                if(precipF > precipProbabilityTodayPlusTwo){
                    precipProbabilityTodayPlusTwo= precipF;
                }
            }
        }

        weather.put("precipitationChanceToday", defaultFormat.format(precipProbabilityToday));
        weather.put("precipitationChanceTodayPlusOne", defaultFormat.format(precipProbabilityTodayPlusOne));
        weather.put("precipitationChanceTodayPlusTwo", defaultFormat.format(precipProbabilityTodayPlusTwo));

        //humidity
        String humidityS;
        float humidityF, maxHumidityTodayPlusOne = 0, maxHumidityTodayPlusTwo = 0;
        
        for(int i = 8; i < humidity.length; i++) {
        	humidityS = humidity[i];
        	humidityF = Float.parseFloat(humidityS);
        	
        	if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusOne)){
            	if(humidityF > maxHumidityTodayPlusOne) {
            		maxHumidityTodayPlusOne = humidityF;
            	}
            } else if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusTwo)){
            	if(humidityF > maxHumidityTodayPlusTwo) {
            		maxHumidityTodayPlusTwo = humidityF;
            	}
            }
        }
        
        weather.put("maxHumidityTodayPlusOne", Float.toString(maxHumidityTodayPlusOne));
        weather.put("maxHumidityTodayPlusTwo", Float.toString(maxHumidityTodayPlusTwo));
        
        //pressure
        String pressureS;
        int pressureI, maxPressureTodayPlusOne = 0, maxPressureTodayPlusTwo = 0;
        
        for(int i = 8; i < pressure.length; i++) {
        	pressureS = pressure[i];
        	pressureI = Integer.parseInt(pressureS);
        	
	        if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusOne)){
	        	if(pressureI > maxPressureTodayPlusOne) {
	        		maxPressureTodayPlusOne = pressureI;
	        	}
	        } else if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusTwo)){
	        	if(pressureI > maxPressureTodayPlusTwo) {
	        		maxPressureTodayPlusTwo = pressureI;
	        	}
	        }
        }
        
        weather.put("maxPressureTodayPlusOne", Integer.toString(maxPressureTodayPlusOne));
        weather.put("maxPressureTodayPlusTwo", Integer.toString(maxPressureTodayPlusTwo));
        
        //weather name & weather number
        String weatherNumS;
        int weatherNumI;
        int[] todayPlusOneCounter = {0, 0, 0, 0, 0, 0, 0}, 
        		todayPlusTwoCounter = {0, 0, 0, 0, 0, 0, 0};
        
        for(int i = 8; i < weatherNumber.length; i++) {
        	weatherNumS = weatherNumber[i];
        	weatherNumI = Integer.parseInt(weatherNumS);
        	
	        if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusOne)){
	        	if(weatherNumI >= 200 && weatherNumI <= 232) {
	    			todayPlusOneCounter[0]++;
	    		}
	    		else if(weatherNumI >= 300 && weatherNumI <= 321) {
	    			todayPlusOneCounter[1]++;
	    		}
	    		else if(weatherNumI >= 500 && weatherNumI <= 531) {
	    			todayPlusOneCounter[2]++;
	    		}
	    		else if(weatherNumI >= 600 && weatherNumI <= 622) {
	    			todayPlusOneCounter[3]++;
	    		}
	    		else if(weatherNumI >= 701 && weatherNumI <= 781) {
	    			todayPlusOneCounter[4]++;
	    		}
	    		else if(weatherNumI == 800) {
	    			todayPlusOneCounter[5]++;
	    		}
	    		else if(weatherNumI >= 801 && weatherNumI <= 804) {
	    			todayPlusOneCounter[6]++;
	    		}
	        } else if (LocalDateTime.parse(timeDate[i],DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().equals(todayPlusTwo)){
	        	if(weatherNumI >= 200 && weatherNumI <= 232) {
	    			todayPlusTwoCounter[0]++;
	    		}
	    		else if(weatherNumI >= 300 && weatherNumI <= 321) {
	    			todayPlusTwoCounter[1]++;
	    		}
	    		else if(weatherNumI >= 500 && weatherNumI <= 531) {
	    			todayPlusTwoCounter[2]++;
	    		}
	    		else if(weatherNumI >= 600 && weatherNumI <= 622) {
	    			todayPlusTwoCounter[3]++;
	    		}
	    		else if(weatherNumI >= 701 && weatherNumI <= 781) {
	    			todayPlusTwoCounter[4]++;
	    		}
	    		else if(weatherNumI == 800) {
	    			todayPlusTwoCounter[5]++;
	    		}
	    		else if(weatherNumI >= 801 && weatherNumI <= 804) {
	    			todayPlusTwoCounter[6]++;
	    		}
	        }
        }
        
        int maxTodayPlusOne = 0, maxTodayPlusOneIndex = 0, 
        		maxTodayPlusTwo = 0, maxTodayPlusTwoIndex = 0;
        String todayPlusOneWeatherName = null, todayPlusTwoWeatherName = null, 
        		todayPlusOneWeatherNum = null, todayPlusTwoWeatherNum = null;
        
      	for(int i = 0; i < 7; i++) {
      		if(todayPlusOneCounter[i] > maxTodayPlusOne) {
      			maxTodayPlusOne = todayPlusOneCounter[i];
      			maxTodayPlusOneIndex = i;
      		}
      		
      		if(todayPlusTwoCounter[i] > maxTodayPlusTwo) {
      			maxTodayPlusTwo = todayPlusTwoCounter[i];
      			maxTodayPlusTwoIndex = i;
      		}
      	}
      	
      	if(maxTodayPlusOneIndex == 0) {
      		todayPlusOneWeatherNum = "211";
      		todayPlusOneWeatherName = "Thunderstorm";
      	}
      	else if(maxTodayPlusOneIndex == 1) {
      		todayPlusOneWeatherNum = "301";
      		todayPlusOneWeatherName = "Drizzle";
      	}
      	else if(maxTodayPlusOneIndex == 2) {
      		todayPlusOneWeatherNum = "501";
      		todayPlusOneWeatherName = "Rain";
      	}
      	else if(maxTodayPlusOneIndex == 3) {
      		todayPlusOneWeatherNum = "601";
      		todayPlusOneWeatherName = "Snow";
      	}
      	else if(maxTodayPlusOneIndex == 4) {
      		todayPlusOneWeatherNum = "701";
      		todayPlusOneWeatherName = "Mist";
      	}
      	else if(maxTodayPlusOneIndex == 5) {
      		todayPlusOneWeatherNum = "800";
      		todayPlusOneWeatherName = "Clear Sky";
      	}
      	else if(maxTodayPlusOneIndex == 6) {
      		todayPlusOneWeatherNum = "801";
      		todayPlusOneWeatherName = "Cloudy";
      	}
      	
      	if(maxTodayPlusTwoIndex == 0) {
      		todayPlusTwoWeatherNum = "211";
      		todayPlusTwoWeatherName = "Thunderstorm";
      	}
      	else if(maxTodayPlusTwoIndex == 1) {
      		todayPlusTwoWeatherNum = "301";
      		todayPlusTwoWeatherName = "Drizzle";
      	}
      	else if(maxTodayPlusTwoIndex == 2) {
      		todayPlusTwoWeatherNum = "501";
      		todayPlusTwoWeatherName = "Rain";
      	}
      	else if(maxTodayPlusTwoIndex == 3) {
      		todayPlusTwoWeatherNum = "601";
      		todayPlusTwoWeatherName = "Snow";
      	}
      	else if(maxTodayPlusTwoIndex == 4) {
      		todayPlusTwoWeatherNum = "701";
      		todayPlusTwoWeatherName = "Mist";
      	}
      	else if(maxTodayPlusTwoIndex == 5) {
      		todayPlusTwoWeatherNum = "800";
      		todayPlusTwoWeatherName = "Clear Sky";
      	}
      	else if(maxTodayPlusTwoIndex == 6) {
      		todayPlusTwoWeatherNum = "801";
      		todayPlusTwoWeatherName = "Cloudy";
      	}
        
        weather.put("todayPlusOneWeatherName", todayPlusOneWeatherName);
        weather.put("todayPlusTwoWeatherName", todayPlusTwoWeatherName);
        weather.put("todayPlusOneWeatherNum", todayPlusOneWeatherNum);
        weather.put("todayPlusTwoWeatherNum", todayPlusTwoWeatherNum);
        
        return weather;
    }
}


