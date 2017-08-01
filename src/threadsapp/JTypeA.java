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
/*
JTypeA  
{
  "protocol_version": "1.4",
  "type":"JTypeA",
  "device_id":"000001",
  "report_time":"2014-11-21T08:43:02.001Z",
  "event_name": "**"
}
где
"protocol_version" - всегда "1.4",
"type"  - всегда "JTypeA",
"device_id" - может принимать значения из диапазона "000[0-9, a-z, A-Z]{1,2}"
"report_time" - время генерации JSON-объекта с точностью до миллисекунд
"event_name" - произвольная строка длиной из 2 символов

*/
public class JTypeA extends JType {
    public JTypeA(){
        super.setType("JTypeA");        
        Instant report_time = generateReport_time();
        super.setReport_time(report_time);
    }
    public JTypeA(double protocol_version, String type, String device_id, Instant report_time, String event_name){
        super(protocol_version, device_id, report_time, event_name);
        super.setType(type);        
    }

    public String toString(){
        Instant temp_report_time = getReport_time(); 
            return "JSON-object type A: \n" +
                    "\t\"protocol_version\": \"" + getProtocol_version() + "\"\n" +
                    "\t\"type\": \"" + getType() + "\"\n" +
                    "\t\"device_id\": \"" + getDevice_id() + "\"\n" +
                    "\t\"event_name\": \"" + getEvent_name() + "\"\n" +
                    "\t\"report_time\": \"" + temp_report_time.toString() + "\"";
    }
}
