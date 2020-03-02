import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class virtualModem {

    static String echo_request_code = "E1322\r";
    static String image_request_code = "M9974\r";
    static String image_request_code_with_errors = "G1491\r";
    static String gps_request_code = "P1215";

    

    public static void main(String[] param) {
        String command;

        Modem modem = new Modem();
        modem.setSpeed(1000);
        modem.setTimeout(2000);

        modem.open("ithaki");

        // demo(modem);
        echo(modem);
        // image_with_errors(modem);
        command = gps_parse(modem);
        System.out.println(command);
        gps_image(modem, command);

        modem.close();
    }

    public static void demo(Modem modem) {
        int k;

        for(;;) {
            try {
                k = modem.read();
                if(k == -1) break;
                System.out.print((char)k);
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
    }

    public static void echo(Modem modem) {
        int k;
        byte[] bytes = echo_request_code.getBytes();
        modem.write(bytes);

        for(;;) {
            try {
                k = modem.read();
                if(k == -1) break;
                System.out.print((char)k);
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
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
        modem.write(bytes);
        ArrayList<String> returnedCoordinates = new ArrayList<String>();
        HashMap<Integer, String> points = new HashMap<Integer, String>();
        String tempCoords = "";

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

        return command;

    }

    public static void gps_image(Modem modem, String command) {
        int k;

        //Send it to server and listen for the answer
        command = "P1215T=225733403737\r";
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
                // image.write(k);
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
    }


}