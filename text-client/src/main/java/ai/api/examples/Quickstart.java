
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Random;
import java.util.Calendar;
import java.util.Date;

import java.text.SimpleDateFormat;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class Quickstart {
    static Calendar cal = Calendar.getInstance();

    private static final String INPUT_PROMPT = "> ";
    /**
     * Default exit code in case of error
     */
    private static final int ERROR_EXIT_CODE = 1;




    /**
     * Application name.
     */
    private static final String APPLICATION_NAME =
            "Google Calendar API Java Quickstart";

    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/calendar-java-quickstart");

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                Quickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar
    getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        // Note: Do not confuse this class with the
        //   com.google.api.services.calendar.model.Calendar class.
        com.google.api.services.calendar.Calendar service =
                getCalendarService();

        DateTime now = new DateTime(System.currentTimeMillis());
        Date twoWeeksLater = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(twoWeeksLater);
        calendar.add(Calendar.DAY_OF_YEAR, 14);
        twoWeeksLater = calendar.getTime();

        DateTime twoWeeks = new DateTime(twoWeeksLater);
        Events events = service.events().list("primary")
  //            .setMaxResults(10)
                .setTimeMin(now)
                .setTimeMax(twoWeeks)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();    // list of events from now to two weeks later
        if (items.size() == 0) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                if (end == null) {
                    end = event.getEnd().getDate();
                }
                System.out.printf("%s (%s) (%s)\n", event.getSummary(), start, end);
            }
        }

        if (args.length < 1) {
            showHelp("Please specify API key", ERROR_EXIT_CODE);
        }
        AIConfiguration configuration = new AIConfiguration(args[0]);
        AIDataService dataService = new AIDataService(configuration);

        Random rand = new Random();  // to select dates randomly
        int a, b, c;  // represents 3 chosen dates
        int dayA, dayB, dayC;

        String line;

        String prompt = "Which time works best for you? ";
        String prompt2 = "Type 'another time' to view more times.";

        // schedule is a 2 dimensional array representing 14 days and 32 15-minute intervals
        // from 9 AM to 5 PM
        boolean [][] schedule;
        schedule = new boolean[14][32];
        // fill in busy times for schedule
        // 9:15 - 9:45, 12:00 - 1:30, 2:00 - 4:00 for 2 weeks
        for(int i = 0; i < 14; i++) {
            schedule[i][1] = true;   schedule[i][2] = true;
            schedule[i][12] = true;  schedule[i][13] = true;
            schedule[i][14] = true;  schedule[i][15] = true;
            schedule[i][16] = true;  schedule[i][17] = true;
            schedule[i][20] = true;  schedule[i][21] = true;
            schedule[i][22] = true;  schedule[i][23] = true;
            schedule[i][24] = true;  schedule[i][25] = true;
            schedule[i][26] = true;  schedule[i][27] = true;
        }

        ArrayList<Integer> free = new ArrayList<Integer>(); // list of free slots for meetings

        int count = 0;
        for(int i = 0; i < 14; i++) {
            for(int j = 0; j < 32; j++) {
                if (schedule[i][j] == false) {
                    free.add(count);
                }
                count++;
            }
        }

        // find free time in user's schedule
        DateTime temp = items.get(0).getStart().getDateTime();
        ArrayList<String> meetings = new ArrayList<String>(); // List of meeting times
        Calendar cal = Calendar.getInstance();
        Date d = new Date(System.currentTimeMillis());
        cal.setTime(d);
        int dayOfMonth, t;

        for(int j = 1; j <= 14; j++) {
            int nextHour = 0;
            cal.add(Calendar.DATE, 1);
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            for (int i = 900; i < 1660; i += 15) {
                if (nextHour == 4) {
                    i += 40;
                    nextHour = 0;
                }
                Boolean available = true;
                for (Event event : items) {
                    DateTime start = event.getStart().getDateTime();
                    DateTime end = event.getEnd().getDateTime();
                    String day = "" + start;
                    day = day.substring(8, 10);
                    int dayMonth = Integer.parseInt(day);
                    if(dayOfMonth == dayMonth) {
                        int begin = dateTimeToInt(start);
                        int finish = dateTimeToInt(end);
                        if (finish <= 500 && finish >= 100 )
                            finish += 1200;
                        if ((i >= begin && i <= finish) || Math.abs(begin - i) < 15) {
                            available = false;
                            break;
                        }
                    }
                }
                if (available) {
                    if(i >= 1300)
                        t = i - 1200;
                    else
                        t = i;
                    meetings.add(t + " " + j);
                }
                nextHour++;
            }
        }
       // for(String s : meetings)
       //     System.out.println(s);
       // meetings now contains all free meeting times


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print(INPUT_PROMPT);
            while (null != (line = reader.readLine())) {
                try {
                    AIRequest request = new AIRequest(line);
                    AIResponse response = dataService.request(request);

                    if (response.getStatus().getCode() == 200) {
                        String reply = response.getResult().getFulfillment().getSpeech();
                        if(reply.contains("A, B, or C")) {
                            if(reply.contains("Okay, meeting length will be"))
                                System.out.print("Okay, meeting length will be 15 minutes. ");
                            System.out.print(prompt);
                            printDates(rand, meetings);
                            System.out.println(prompt2);
                            //select 3 random dates from free time in Calendar
                        }
                        else {
                            System.out.println(reply);
                        }

                    } else {
                        System.err.println(response.getStatus().getErrorDetails());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                System.out.print(INPUT_PROMPT);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("See ya!");


    }

    /**
     * Output application usage information to stdout and exit. No return from function.
     *
     * @param errorMessage Extra error message. Would be printed to stderr if not null and not empty.
     */
    private static void showHelp(String errorMessage, int exitCode) {
        if (errorMessage != null && errorMessage.length() > 0) {
            System.err.println(errorMessage);
            System.err.println();
        }

        System.out.println("Usage: APIKEY");
        System.out.println();
        System.out.println("APIKEY  Your unique application key");
        System.out.println("        See https://docs.api.ai/docs/key-concepts for details");
        System.out.println();
        System.exit(exitCode);
    }

    public static String retrieveDate(Calendar cal, int offset) {
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, offset);
        Date date = cal.getTime();
        SimpleDateFormat newDateFormat = new SimpleDateFormat("dd/MM");
        newDateFormat.applyPattern("EEEE MMMM d");

        return newDateFormat.format(date);
    }

    /**
     * returns a random time out of list of free times from Calendar
     *
     * @param rand - random functionality
     * @param list - list of free times
     * @return random time from list
     */
    public static String randomTime(Random rand, ArrayList<String> list) {
        String randomStr = list.get(rand.nextInt(list.size()));
        list.remove(randomStr);
        return randomStr;
    }

    public static int dayOfWeek(String meeting) {
        int space = meeting.indexOf(" ");
        return Integer.parseInt(meeting.substring(space+1));
    }

    public static String timeOfDay(String meeting) {
        String result = "";
        int space = meeting.indexOf(" ");
        String time = meeting.substring(0, space);
        String minutes = time.substring(time.length() - 2);
        String hour = time.substring(0, time.length() - 2);
        result += hour + ":" + minutes + " ";
        if(Integer.parseInt(hour) >= 9 && Integer.parseInt(hour) <= 12)
            result += "AM";
        else
            result += "PM";
        return result;
    }

    public static void printDates(Random rand, ArrayList<String> meetings) {
        String a = randomTime(rand, meetings);
        String b = randomTime(rand, meetings);
        String c = randomTime(rand, meetings);
        //convert a, b, c into calendar times
        int dayA = dayOfWeek(a);  // offset for first date
        int dayB = dayOfWeek(b);  // offset for second date
        int dayC = dayOfWeek(c);  // offset for third date

        System.out.println(retrieveDate(cal, dayA) + " at " + timeOfDay(a) + ", " + retrieveDate(cal, dayB) + " at " + timeOfDay(b) + ", or " + retrieveDate(cal, dayC) + " at " + timeOfDay(c) + "?");
    }

    public static int dateTimeToInt(DateTime dt) {
        String time = "" + dt;
        time = time.substring(11);
        time = time.substring(0, time.length() - 13);
        time = time.substring(0, 2) + time.substring(3, 5);
        return Integer.parseInt(time);
    }


}

