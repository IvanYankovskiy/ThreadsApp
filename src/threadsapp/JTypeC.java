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
Уникальный ключ сохранения информации - {"type", "device_id", "report_time","event_name","reports"."time"} 
JTypeС
{
  "protocol_version": "1.4",
  "type":"JTypeС",
  "device_id":"000001",
  "report_time":"2014-11-21T08:43:02.001Z",
  "event_name": "**"
  "reports": [
    {"time":"2016-07-21T08:30:00.000Z"},
    {"time":"2016-07-21T08:35:00.000Z"}
  ]
}
где
"protocol_version" - всегда "1.4",
"type"  - всегда "JTypeС",
"device_id" - строка, может принимать значения из диапазона "000[0-9, a-z, A-Z]{1,2}"
"report_time" - время генерации JSON-объекта с точностью до миллисекунд
"event_name" - произвольная строка длиной из 2 символов
"reports"  - массив JSON-объектов емкостью до пяти элементов, содержащих единственное поле - произвольное время из вчерашнего(отностительно запуска программы) дня

*/
public class JTypeC extends JType{
    private ArrayList <JTypeTime> reports;
    public JTypeC(){
        super.setType("JTypeC");
        reports = generateRandomNuberInReports();
        Instant report_time = generateReport_time();
        super.setReport_time(report_time);
    }
    public JTypeC(double protocol_version, String type, String device_id, Instant report_time, String event_name, ArrayList <JTypeTime> reports){
        super(protocol_version, device_id, report_time, event_name);
        super.setType(type);
        this.reports = reports;
    }
    public ArrayList <JTypeTime> getReports(){
        return reports;
    }
    public void setReports(ArrayList <JTypeTime> reports){
        this.reports = reports;
    }
    public static ArrayList <JTypeTime> generateRandomNuberInReports(){
        ArrayList <JTypeTime> reports = new ArrayList <JTypeTime>();
        Random r = new Random();
        Instant reference_time = generateReport_time();
        reference_time = reference_time.minus(1,ChronoUnit.DAYS);
        
        int j = r.nextInt(4) + 1;
        for(int i = 0; i < j; i++){
            reference_time = reference_time.minus(r.nextInt(59),ChronoUnit.MINUTES);
            reference_time = reference_time.minus(r.nextInt(100)+1,ChronoUnit.MILLIS);
            JTypeTime JTimeObject = new JTypeTime(reference_time); 
            reports.add(JTimeObject);
        }
        //System.out.println(reports.toString());
        return reports;
    }
    public String toString(){
        Instant temp_report_time = getReport_time(); 
            return "JSON-object type C: \n" +
                    "\t\"protocol_version\": \"" + getProtocol_version() + "\"\n" +
                    "\t\"type\": \"" + getType() + "\"\n" +
                    "\t\"device_id\": \"" + getDevice_id() + "\"\n" +
                    "\t\"report_time\": \"" + temp_report_time.toString() + "\"\n" +
                    "\t\"event_name\": \"" + getEvent_name() + "\"\n" +
                    "\t\"reports\": \"" + reports.toString() + "\"";
    }
}
