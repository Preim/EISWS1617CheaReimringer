package eis.bikefriends;


public class GlobalClass {

    private static GlobalClass instance;

    public static GlobalClass getInstance() {
        if (instance == null)
            instance = new GlobalClass();
        return instance;
    }

    private GlobalClass(){
    }
    private static String ipAddresse = "https://192.168.0.100:3000";


    public String getIpAddresse(){
        return ipAddresse;
    }
}
