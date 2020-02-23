public class virtualModem {

    public static void main(String[] param) {
        (new virtualModem()).demo();
    }

    public void demo() {
        int k;

        Modem modem = new Modem();
        modem.setSpeed(1000);
        modem.setTimeout(2000);

        modem.open("ithaki");

        for(;;) {
            try {
                k = modem.read();
                if(k == -1) break;
                System.out.print((char)k);
            } catch (Exception e) {
                //TODO: handle exception
                break;
            }
        }

        // NOTE : Break endless loop by catching sequence "\r\n\n\n"
        // NOTE: Stop program execution when "NO CARRIER" is detected.
        // NOTE : A time-out option will enhance program behavior.
        // NOTE : Continue with further Java code.
        // NOTE : Enjoy :)

        modem.close();
    }



}