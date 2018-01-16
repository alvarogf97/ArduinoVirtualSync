/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arduino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import spread.SpreadMessage;

/**
 *
 * @author Alvaro
 */
public class Lider extends ArduinoState{

    @Override
    public void sendMSG(ArduinoUser user, String msg) {
        super.sendMSGtoSpread(user.getNombre()+"@"+msg, user.getConnection(), user.getConnectionGroup());
    }

    @Override
    public void reciveMSG(ArduinoUser user, SpreadMessage sm) {
        String [] tokens = (new String(sm.getData())).split("@");
        System.out.println("Not lider send MSG whitout data \n \033[31m" + user.getNombre()+ "_INFO:\033[30m received from: " + tokens[0] + " content: " + tokens[1]);     
    }

    @Override
    public void work(ArduinoUser user) {
        try{
           Thread.sleep(500);
           sendData(user,"7", user.getMediaPuertoSerie());
           user.sendVIVO(); 
        } catch (InterruptedException ex) {
            System.out.println("interruped Thread");
        }
        
    }

    @Override
    public void sendData(ArduinoUser arduinoUser, String grupo, Double media) {
        if (media != 0 && media != Double.NaN) {
            try {
                System.out.println(arduinoUser.getINFO() + "enviando informacion al servidor");
                URL url = new URL("http://medicionesdgm.appspot.com/save?id=" + grupo + "&valor=" + media);
                BufferedReader res = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = res.readLine()) != null) {
                    System.out.println(arduinoUser.getINFO() + "\u001b[32mServer response: " + line);
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }
    
    @Override
    public boolean isLeader(){
        return true;
    }

    
}
