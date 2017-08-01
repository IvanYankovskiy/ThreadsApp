/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp;

import java.time.Instant;
import java.util.Random;
//import static threadsapp.JType.generateRandomSymbol;

/**
 *
 * @author Ivan
 */
/*
Уникальный ключ для сохранения информации - {"type", "device_id", "event_name", "reports"} 
JTypeB
{
  "protoсcol_version": "1.4",
  "type":"JTypeB",
  "deviсe_id":"000001",
  "report_time":"2014-11-21T08:43:02.001Z",
  "event_name": "**"
  "reports": "**"
}
где
"protocol_version" - всегда "1.4",
"type"  - всегда "JTypeB",
"device_id" - строка, может принимать значения из диапазона "000[0-9, a-z, A-Z]{1,2}"
"report_time" - время генерации JSON-объекта с точностью до миллисекунд
"event_name" - произвольная строка длиной из 2 символов
"reports"  - строка, может принимать значения из диапазона "[0-9]{2} \"[a-z]{2}\" [A-Z]{1,2}"

*/
public class JTypeB extends JType {
    private String reports;
    public JTypeB(){
        super.setType("JTypeB");
        reports = generateReports();
        Instant report_time = generateReport_time();
        super.setReport_time(report_time);
    }
    public JTypeB(double protocol_version, String type, String device_id, Instant report_time, String event_name, String reports){
        super(protocol_version, device_id, report_time, event_name);
        super.setType(type);
        this.reports = reports;
    }
    public String getReports(){
        return reports;
    }
    public void setRepots(String reports){
        this.reports = reports;
    }
    public static String generateReports(){
        StringBuilder reports = new StringBuilder();
        Random r = new Random();
        for(int i = 0; i < 2; i++){
            reports.append(r.nextInt(9));
        }
        reports.append("\"");
        for(int i = 0; i < 2; i++){
            char ch = generateRandomSymbol(false);
            reports.append(ch);
        }
        reports.append("\"");
        int j = r.nextInt(1) + 1;
        for(int i = 0; i < j; i++){
            char ch = generateRandomSymbol(true);
            reports.append(ch);
        }
        return reports.toString();
    }
    public String toString(){
        Instant temp_report_time = getReport_time(); 
            return "JSON-object type B: \n" +
                    "\t\"protocol_version\": \"" + getProtocol_version() + "\"\n" +
                    "\t\"type\": \"" + getType() + "\"\n" +
                    "\t\"device_id\": \"" + getDevice_id() + "\"\n" +
                    "\t\"event_name\": \"" + getEvent_name() + "\"\n" +
                    "\t\"report_time\": \"" + temp_report_time.toString() + "\"\n" +
                    "\t\"reports\": \"" + reports + "\"";
    }
}
