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
    //Atributtes for the Drone class
    static String IPdrone;
    static boolean connection; //connected or not

    private static Drone drone = new Drone( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private Drone(){
        connection=false;
    }

    /* Static 'instance' method
    * By calling this function we get an instance of this class, it makes that
    * just one initialization is made */
    public static Drone getInstance( ) {
        return drone;
    }
    /* Other methods protected by singleton-ness */

    //this functions sets an IP to the drone
    protected void setIP(String IP){
        IPdrone=IP;
    }

    //this functions gets the IP of the drone
    protected String getIP() {
        return IPdrone;
    }

    /*this function update the status of the drone,
    true means connected
    false means disconnected
    When the drone is disconnected, it does not allow the app to send messages
     */
    protected void setStatus(boolean connect){
        connection=connect;
    }

    //this function return the status of the drone (connected or not)
    protected boolean getStatus(){
        return connection;
    }
}
