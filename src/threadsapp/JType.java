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
 * 
 */
/*JTypeA  
{
  "protocol_version": "1.4",
  "type":"JTypeA",
  "device_id":"000001",
  "report_time":"2014-11-21T08:43:02.001Z",
  "event_name": "**"
}
*/
public class JType {
    private double protocol_version = 1.4;
    private String type; 
    private String device_id;
    private Instant report_time;
    private String event_name;

    public JType(){
        device_id = generateDevice_id();
        event_name = generateEvent_name();
    }
    public JType(double protocol_version, String device_id, Instant report_time, String event_name ){
        this.protocol_version = protocol_version;
        this.device_id = device_id;
        this.report_time = report_time;
        this.event_name = event_name;
    }
    public String getType(){
        return type;
    }
    public void setType(String type){
        this.type = type;
    }
    public double getProtocol_version(){
        return protocol_version;
    }
    public String getDevice_id(){
        return device_id;
    }
    public void setDevice_id(String device_id){
        this.device_id = device_id;
    }
    public Instant getReport_time(){
        return report_time;
    }
    public void setReport_time(Instant report_time){
        this.report_time = report_time;
    }
    public String getEvent_name(){
        return event_name;
    }
    public void setEvent_name(String event_name){
        this.event_name = event_name;
    }
    //метод генерирует валидный идентификатор устройства
    public static String generateDevice_id(){
        StringBuffer device_id = new StringBuffer("000");
        /*псевдослучайно выбирается чем будет заполнен символ. Если метод 
        возвращает:
        0 - 0-9
        1 - A-Z
        2 - a-z
        */
        Random r = new Random(System.currentTimeMillis());
        int j = r.nextInt(1) + 1;
        for(int i = 0; i < j ; i++){
            int code; //хранит код символа
            char ch;
            int symbolVariant = r.nextInt(2);
            switch (symbolVariant){
                case 0:
                    code = r.nextInt(9) + 48;
                    ch = (char)code;
                    device_id.append(ch);
                    break;
                case 1:
                    ch = generateRandomSymbol(false);
                    device_id.append(ch);
                    break;
                case 2:
                    ch = generateRandomSymbol(true);
                    device_id.append(ch);
                    break;
            }
            
        }
        return device_id.toString();
    }
    //метод генерирует валидное имя события
    public static String generateEvent_name(){
        StringBuffer event_name = new StringBuffer();
        /*псевдослучайно выбирается чем будет заполнен символ. Если метод 
        возвращает:
        0 - 0-9
        1 - A-Z
        2 - a-z
        */
        Random r = new Random(System.currentTimeMillis());
        for(int i = 0; i < 2 ; i++){
            int code; //хранит код символа
            char ch;
            int symbolVariant = r.nextInt(2);
            switch (symbolVariant){
                case 0:
                    code = r.nextInt(9) + 48;
                    ch = (char)code;
                    event_name.append(ch);
                    break;
                case 1:
                    ch = generateRandomSymbol(true);
                    event_name.append(ch);
                    break;
                case 2:
                    //a-z в int 97-122
                    ch = generateRandomSymbol(false);
                    event_name.append(ch);
                    break;
            }
        }
        return event_name.toString();
    }
    //метод получает время создания объекта
    public static Instant generateReport_time(){
        Instant report_time = Instant.now();
        //report_time_inst = report_time.minus(1,ChronoUnit.DAYS);
        return report_time;
    }
    public static char generateRandomSymbol(boolean isUpperCase){
        //0-9 в int 49-58
        //a-z в int 97-122
        //A-Z в int 65-90
        char ch;
        Random r = new Random();
        int code = r.nextInt(25);
        if(isUpperCase)
            ch = (char)((int)code + 97);
        else
            ch = (char)((int)code + 65);
        return ch;
    }
}
