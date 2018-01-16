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
public class Eligiendo extends ArduinoState{

    @Override
    public void sendMSG(ArduinoUser user, String msg) {
        super.sendMSGtoSpread(user.getNombre()+"@"+msg, user.getConnection(), user.getConnectionGroup());
    }

    @Override
    public void reciveMSG(ArduinoUser user, SpreadMessage sm) {
        String [] tokens = (new String(sm.getData())).split("@");
        System.out.println("\033[31m" + user.getNombre() + "_INFO:\033[30m received from: " + tokens[0] + " content: " + tokens[1]);
        
        switch(tokens[1]){
            case "ELIGIENDO" : if(user.getNombre().compareTo(tokens[0]) < 0){
                                    user.setEstado(new Participante()); //sync
                                    System.out.println("\033[31m" + user.getNombre() + "_INFO:\033[35m Change state to Participante");
                               }
                break;
            case "TIME_OUT" : user.setEstado(new Lider()); //leader is dead
                              System.out.println("\033[31m" + user.getNombre() + "_INFO:\033[35m Change state to Lider");
                break;
            default : System.err.println("\033[31m" + user.getNombre() + "_INFO:\033[30m Recived strange msg");
                break;
        }
    }

    @Override
    public void work(ArduinoUser user) {
        try{
           user.sendELIGIENDO();
           Thread.sleep(500);
           user.checkMSG(); 
        }catch (SpreadException | InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void sendData(ArduinoUser user, String grupo, Double media) {
        //DO-NOTHING
    }

    @Override
    public boolean isLeader() {
        return false;
    }
    
    

    

}
