import java.io.File;
import java.io.FileOutputStream;

public class virtualModem {

    static String echo_request_code = "E9082\r";
    static String image_request_code = "M9974\r";
    static String image_request_code_with_errors = "G1491\r";
    static String gps_request_code = "\r";

    

    public static void main(String[] param) {
        Modem modem = new Modem();
        modem.setSpeed(1000);
        modem.setTimeout(2000);

        modem.open("ithaki");

        demo(modem);
        echo(modem);
        image_with_errors(modem);

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



}