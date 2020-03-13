import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.time.LocalDate;
import java.lang.System;

public class virtualModem {

    static String echo_request_code = "E0818\r";
    static String image_request_code = "M9974\r";
    static String image_request_code_with_errors = "G1491\r";
    static String gps_request_code = "P9256";
    static String ack_request_code = "Q8054\r";
    static String nack_request_code = "R4191\r";

    

    public static void main(String[] param) {
        long startTime;
        long echoDurationInMins = 5;
        String command;
        
        String echoOutput = "";
        String encryptedOutput = "";

        /*########### SETUP CONNECTION ###############*/
        Modem modem = new Modem();
        modem.setSpeed(80000);
        modem.setTimeout(2000);

        modem.open("ithaki");

        //A simple echo to trigger the initial greeting from ithaki
        echo(modem);


        // /*########### ECHO  REQUEST ###############*/
        // //Echo Requests for echoDurationInMins time
        // startTime = System.currentTimeMillis();

        // while (System.currentTimeMillis() - startTime < echoDurationInMins * 60 * 1000) {
            
        //    echoOutput += echo(modem);
        // }
        
        // System.out.println(echoOutput);

        // //Save echoOutput on a txt file
        // try (PrintWriter out = new PrintWriter("echoResponseTime.txt")) {
        //     out.println(echoOutput);
        // }catch (Exception e) {
        //     System.out.println(e.toString());
        // } 
        

        /*########### IMAGE WITH ERRORS ###############*/
        // image_with_errors(modem);


        /*########### GPS ###############*/
        // command = gps_parse(modem);
        // echo(modem);
        // System.out.println(command);
        // gps_image(modem, command);


        /*########### ACK NACK REQUEST ###############*/
        startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < 0.3 * 60 * 1000) {
            
           encryptedOutput += encrypted_message(modem);
        }

        System.out.println(encryptedOutput);

        //Save echoOutput on a txt file
        try (PrintWriter out = new PrintWriter("ackNackResponseTime.txt")) {
            out.println(encryptedOutput);
        }catch (Exception e) {
            System.out.println(e.toString());
        } 
        
        /*########### CLOSE CONNECTION ###############*/
        modem.close();
    }

    public static String echo(Modem modem) {
        int k;
        long start = 0;
        long end = 0;
        byte[] bytes = echo_request_code.getBytes();
        start = System.currentTimeMillis();
        modem.write(bytes);

        String output = "";

        for(;;) {
            try {
                k = modem.read();
                if(k == -1) break;
                System.out.print((char)k);
                output += (char)k;
                if(output.contains("PSTOP")) {
                    end = System.currentTimeMillis();
                }
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
        System.out.println();

        output += " Start Time in ms: " + start + " End Time in ms: " + end + " Response Time:" + (end - start) + "\n";

        return output;
    }

    public static void image(Modem modem) {
        int k;
        byte[] bytes = image_request_code.getBytes();
        modem.write(bytes);     
        
        File file = null;
        FileOutputStream image = null;

        try {
            file = new File("image.jpg");
            image = new FileOutputStream(file);
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }

        for(;;) {
            try {
                k = modem.read();
                if(k == -1) break;
                image.write(k);
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }

        
    }

    public static void image_with_errors(Modem modem) {
        int k;
        byte[] bytes = image_request_code.getBytes();
        modem.write(bytes);
        
        File file = null;
        FileOutputStream image = null;

        try {
            file = new File("image_with_errors.jpg");
            image = new FileOutputStream(file);
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }

        for(;;) {
            try {
                k = modem.read();
                if(k == -1) break;
                image.write(k);
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }

        
    }

    public static String gps_parse(Modem modem) {
        int counter = 0;
        int k;
        String command = gps_request_code + "R=1006950\r";
        byte[] bytes = command.getBytes();
        ArrayList<String> returnedCoordinates = new ArrayList<String>();
        HashMap<Integer, String> points = new HashMap<Integer, String>();
        String tempCoords = "";

        modem.write(bytes);

        for(;;) {
            try {
                k = modem.read();
                if(k == -1) break;
                System.out.print((char)k);
                if((char)k == '$') {
                    returnedCoordinates.add(tempCoords);
                    if(counter == 0) {
                        counter++;
                        returnedCoordinates.remove(0);
                    }
                    tempCoords = "";
                }
                tempCoords += (char)k;                
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
        System.out.println(tempCoords);
        System.out.println(returnedCoordinates);

        //Every trace has a difference of 1 second from the previous one
        //Pick one every i = 10 seconds 
        
        counter = 0;
        for(int i = 0; i < returnedCoordinates.size(); i += 10) {
            String longtitudeA, latitudeA, longtitudeB, latitudeB;
            longtitudeA = returnedCoordinates.get(i).substring(31, 35);
            longtitudeB = returnedCoordinates.get(i).substring(36, 40);
            latitudeA = returnedCoordinates.get(i).substring(18, 22);
            latitudeB = returnedCoordinates.get(i).substring(23, 27);

            System.out.println("LongitudeA: " + longtitudeA);
            System.out.println("LongitudeB: " + longtitudeB);
            System.out.println("LatitudeA: " + longtitudeA);
            System.out.println("LatitudeB: " + longtitudeB);

            //Could use floor
            int a =  (int)(Double.parseDouble(longtitudeB) * 0.006);
            longtitudeA += Integer.toString(a);
            int b =  (int)(Double.parseDouble(latitudeB) * 0.006);
            latitudeA += Integer.toString(b);

            // //now the String longtitudeA and latitudeA hold the String value of the stigma's coordinates
            // //We can now save those coordinates in the hashmap with a unique stigma number

            String coOrds = "T=" + longtitudeA + latitudeA;

            points.put(counter, coOrds);
            counter++;
        }
        

        //Prepare the message ask for a map with the above coordinates
        command = gps_request_code;
        for(int i = 0; i < points.size(); i++) {
            command += points.get(i);
            System.out.println(i + ": " + points.get(i));
        }
        command += "\r";
        modem.setTimeout(20000);

        return command;

    }

    public static void gps_image(Modem modem, String command) {
        int k;

        //Send it to server and listen for the answer
        command = "P9256T=225733403737\r";
        System.out.println(command);
        byte[] bytes = command.getBytes();
        modem.write(bytes);

        File file = null;
        FileOutputStream image = null;

        try {
            file = new File("map.jpg");
            image = new FileOutputStream(file);
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }

        for(;;) {
            try {
                k = modem.read();
                if(k == -1) break;
                System.out.println((char)k);
                image.write(k);
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
        modem.setTimeout(2000);
    }

    public static String encrypted_message(Modem modem) {
        int k;
        long startTime = 0;
        long endTime = 0;
        int retries = 0;
        String output = "";

        int[] message = new int[16];
        int[] transmit_fcs = new int[3];
        int receive_fcs = 0;
        String fcsString = "";
        int fcsInt;

        int counter = 0;
        int message_counter = 0;
        int fcs_counter = 0;

        byte[] bytes = ack_request_code.getBytes();
        startTime = System.currentTimeMillis();
        modem.write(bytes);

    
        //ask for the initial ack command
        for(;;) {
            try {
                k = modem.read();
                if(k == -1) break;
                System.out.print((char)k);
                output += (char)k;
                if(output.contains("PSTOP")) {
                    endTime = System.currentTimeMillis();
                }
                if(counter > 30 && counter < 47) {
                    // System.out.println("mphka sto message me counter " + counter);
                    message[message_counter] = k;
                    message_counter++;
                }
                if (counter > 48 && counter < 52) {
                    // System.out.println("mphka sto fcs me counter " + counter);
                    transmit_fcs[fcs_counter] = (char)k;
                    fcsString += (char)k;
                    fcs_counter++;
                }
                counter++;
                // System.out.println(counter);
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
        System.out.println();
        System.out.println(Arrays.toString(message));
        System.out.println(Arrays.toString(transmit_fcs));

        //check if we have the right message
        receive_fcs = message[0];
        for(int i = 1; i < message.length; i++) {
            receive_fcs = (receive_fcs ^ message[i]);
        }

        //Cast fcsString to fcsInt
        fcsInt = Integer.parseInt(fcsString);
        System.out.println("Sum " + receive_fcs);
        System.out.println("fcs " + fcsInt);


        //Compare the sum with the fcs and if they are not equal send a nack request
        while(receive_fcs != fcsInt) {
            //Every time fcs is not the same with received_fcs we have a retry
            retries++;
            System.out.println("We received the wrong package");
            //Send a nack request and do the same analysis until sum == fcsInt
            bytes = nack_request_code.getBytes();
            modem.write(bytes);

            //Reset the variables
            fcsString = "";
            counter = 0;
            message_counter = 0;
            fcs_counter = 0;
            output = "";

        
            //Read the response from the nack request
            for(;;) {
                try {
                    k = modem.read();
                    if(k == -1) break;
                    System.out.print((char)k);
                    output += (char)k;
                    if(output.contains("PSTOP")) {
                        endTime = System.currentTimeMillis();
                    }
                    if(counter > 30 && counter < 47) {
                        // System.out.println("mphka sto message me counter " + counter);
                        message[message_counter] = k;
                        message_counter++;
                    }
                    if (counter > 48 && counter < 52) {
                        // System.out.println("mphka sto fcs me counter " + counter);
                        transmit_fcs[fcs_counter] = (char)k;
                        fcsString += (char)k;
                        fcs_counter++;
                    }
                    counter++;
                    // System.out.println(counter);
                } catch (Exception e) {
                    System.out.println(e.toString());
                    break;
                }
            }
            System.out.println();
            System.out.println(Arrays.toString(message));
            System.out.println(Arrays.toString(transmit_fcs));

            //check if we have the right message
            receive_fcs = message[0];
            for(int i = 1; i < message.length; i++) {
                receive_fcs = (receive_fcs ^ message[i]);
            }

            //Cast fcsString to fcsInt
            fcsInt = Integer.parseInt(fcsString);
            System.out.println("Sum " + receive_fcs);
            System.out.println("fcs " + fcsInt);

        }

        output += " Start Time in ms: " + startTime + " End Time in ms: " + endTime + " Response Time:" + (endTime - startTime) + " Retries: " + retries + "\n";
        return output;

            
        
    }

    
    


}