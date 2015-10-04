package com.iha.group2.dronecontrol;

/*REFERENCE:
http://www.tutorialspoint.com/java/java_using_singleton.htm
 */

/*Drone class
This class was made to store the Drone IP to allow other classes to get this IP and it controls
if the Drone is connected or not.
Also, it was implemented as a Singleton, as we only want one initialization.
 */

public class Drone {
    static String IPdrone;
    static boolean connection; //connected or not

    private static Drone drone = new Drone( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private Drone(){
        connection=false;
    }

    /* Static 'instance' method */
    public static Drone getInstance( ) {
        return drone;
    }
    /* Other methods protected by singleton-ness */
    protected void setIP(String IP){
        IPdrone=IP;
    }

    protected String getIP() {
        return IPdrone;
    }
    protected void setStatus(boolean connect){
        connection=connect;
    }
    protected boolean getStatus(){
        return connection;
    }
}
