package ai.api.examples;

/***********************************************************************************************************************
 *
 * API.AI Java SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2016 by Speaktoit, Inc. (https://www.speaktoit.com) https://www.api.ai
 *
 * *********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 ***********************************************************************************************************************/

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


/**
 * Text client reads requests line by line from stdandart input.
 */
public class TextClientApplication {
  private static Calendar cal = Calendar.getInstance();

  private static final String INPUT_PROMPT = "> ";
  /**
   * Default exit code in case of error
   */
  private static final int ERROR_EXIT_CODE = 1;

  /**
   * @param args List of parameters:<br>
   *        First parameters should be valid api key<br>
   *        Second and the following args should be file names containing audio data.
   */
  public static void main(String[] args) {
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
                printDates(rand, free);
                System.out.println(prompt2);
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
   * 
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
     * @param rand - random functionality
     * @param list - list of free times
     * @return random time from list
     */
    public static int randomTime(Random rand, ArrayList<Integer> list) {
        Integer randomInt = list.get(rand.nextInt(list.size()));
        list.remove(randomInt);
        return randomInt;
    }

    public static int dayOfWeek(int time) {
        return (time / 32) + 1;
    }

    public static String timeOfDay(int time) {
        String result, suffix, min;
        int day = dayOfWeek(time);
        day = (day-1) * 32;
        int remainder = time - day;
        int hour = 9 + remainder / 4;
        remainder -= (hour-9) * 4;
        int minutes = remainder * 15;
        if(hour > 12)
            hour -= 12;
        if(hour >= 9 && hour < 12)
            suffix = "AM";
        else
            suffix = "PM";

        if(minutes == 0)
            min = "00";
        else
            min = "" + minutes;

        result = hour + ":" + min + " " + suffix;
        return result;
    }

    public static void printDates(Random rand, ArrayList<Integer> free) {
        int a = randomTime(rand, free);
        int b = randomTime(rand, free);
        int c = randomTime(rand, free);
        //convert a, b, c into calendar times
        int dayA = dayOfWeek(a);  // offset for first date
        int dayB = dayOfWeek(b);  // offset for second date
        int dayC = dayOfWeek(c);  // offset for third date

        System.out.println(retrieveDate(cal, dayA) + " at " + timeOfDay(a) + ", " + retrieveDate(cal, dayB) + " at " + timeOfDay(b) + ", or " + retrieveDate(cal, dayC) + " at " + timeOfDay(c) + "?");
    }

}
