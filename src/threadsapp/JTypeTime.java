/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
/**
 *
 * @author Ivan
 */
public class JTypeTime {
    private Instant time;
    public JTypeTime(){

    }
    public JTypeTime(Instant time){
        this.time = time;
    }
    public Instant getTime(){
        return time;
    }
    public void setTime(Instant time){
        this.time = time;
    }
    public String toString(){
        return time.toString();
    }
    
}
