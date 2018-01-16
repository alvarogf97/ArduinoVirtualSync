/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualsync;

import arduino.ArduinoUser;
import java.util.Scanner;

/**
 *
 * @author Alvaro
 * link servidor:   http://medicionesdgm.appspot.com/grafica.jsp?id=7
 */
public class VirtualSync {

    //el primero que se conecte crea el grupo
    public static final String GROUP_NAME = "miGrupo";
    
    /**
     * @param args the command line arguments
     */
    
    public static void main(String [] args){
        
        String local;
        String isL;
        String name;
        String serialport;
        boolean leader;
        boolean correct = true;
        String address;
        int port = 0;
        ArduinoUser user;
        
        try(Scanner sc = new Scanner(System.in)){
            System.out.println("\033[31mINFO: Crea un nuevo usuario: ");
            System.out.print("\033[32m    Nombre->\033[32m ");
            name = sc.next();
            System.out.print("\033[32m    Puerto serie->\033[30m COM");
            serialport = "COM"+sc.next();
            System.out.print("\033[32m    ¿Lider?(S/N)->\033[32m ");
            isL = sc.next();
            leader = isL.equals("S") || isL.equals("s");
            System.out.print("\033[32m    ¿LocalHost?(S/N)->\033[32m ");
            local = sc.next();
            if(local.equals("S") || local.equals("s")){
                user = new ArduinoUser(name,serialport,leader);
            }else{
                System.out.print("\033[32m    Direccion del servidor:\033[32m "); 
                address = sc.next();
                while(correct){
                    System.out.print("\033[32m    Puerto del servidor:\033[30m ");
                    try{
                        port = Integer.valueOf(sc.next());
                        correct = false;
                    }catch(NumberFormatException ex){
                        System.err.println("escribe un puerto valido");
                    }
                }
                user = new ArduinoUser(name,address,port,serialport,leader);
            }
            user.start();
        }
        
    }
    
}
