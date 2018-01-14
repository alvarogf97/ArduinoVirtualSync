/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arduino;

import spread.SpreadException;
import spread.SpreadMessage;

/**
 *
 * @author Alvaro
 */
public class Participante extends ArduinoState{

    @Override
    public void sendMSG(ArduinoUser user, String msg) {
        System.err.println("Why call sendMSG, I am Participant!");
    }

    @Override
    public void reciveMSG(ArduinoUser user, SpreadMessage sm) {
        if(sm != null){
            String s = new String(sm.getData());
        String [] tokens = s.split("@");
        if(!tokens[1].equals("FINISH_CHECK_MSG")){
            System.out.println("\033[31m" + user.getNombre() + "_INFO:\033[30m received from: " + tokens[0] + " content: " + tokens[1]);
        }
        
        switch(tokens[1]){
            case "ELIGIENDO" : //DO-NOTHING
                break;
            case "TIME_OUT" : user.setEstado(new Eligiendo()); //leader is dead
                              System.out.println("\033[31m" + user.getNombre() + "_INFO:\033[35m Change state to Eligiendo");
                break;
            case "FINISH_CHECK_MSG" : //DO-NOTHING
                break;
            default : //supose to leader is alive
                break;
        }
        }
    }

    @Override
    public void work(ArduinoUser user) {
        try{
            Thread.sleep(500);
            user.checkMSG();
        }catch (SpreadException | InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
