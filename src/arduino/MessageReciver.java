/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arduino;

import java.io.InterruptedIOException;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadMessage;

/**
 *
 * @author Alvaro
 */
public class MessageReciver extends Thread{
    
    private ArduinoUser user;
    private SpreadConnection connection;
    private volatile boolean EXIT = false;
    
    /**
     * Constructor
     * @param connection conexion que se va a inspeccionar en busca de mensajes
     * @param user usuario al que se le añadiran los mensajes recibidos
     */
    public MessageReciver(SpreadConnection connection, ArduinoUser user){
        this.connection = connection;
        this.user = user;
    }
    
    /**
     * stop the curren thread waiting for finish him
     */
    public void stopExecution(){
        EXIT = true;
    }
    
    /**
     * devuelve true si el mensaje ha sido mandado por el mismo usuario
     * que maneja la hebra
     * @param msg mensaje recibido
     * @return 
     *
     */
    private boolean notMe(SpreadMessage msg){
        String s = new String(msg.getData());
        return !user.getNombre().equals(s.split("@")[0]);
    }
    
    /**
     * añadira al usuario en cuestion el mensaje recibido
     * @param msg mensaje recibido
     */
    private void DisplayMessage(SpreadMessage msg){
        if(msg.isRegular() && notMe(msg)){
            user.addMSGReceived(msg);
        }
    }
    
    @Override
    public void run(){
        while(!EXIT){
            try{
                if(connection.poll()){
                   DisplayMessage(connection.receive()); 
                }
	    }catch(InterruptedIOException | SpreadException e){
                System.err.println("Error on MessageReciver");
	    }
	}
    }
}
