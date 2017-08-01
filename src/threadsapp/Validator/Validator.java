/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp.Validator;
import com.alibaba.fastjson.JSON;
import threadsapp.*;
import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author Ivan
 */
public class Validator {
 /*   public static void main(String[] args){
      boolean r = checkReport("10\"fo\"a");
        System.out.println(r);
        JTypeA obj = new JTypeA();
        validate(obj);

{"device_id":"000V","event_name":"5s","protocol_version":1.4,"report_time":"2017-07-29T18:31:56.806Z","type":"JTypeA"}
{"device_id":"000C","event_name":"12","protocol_version":1.4,"report_time":"2017-07-29T18:31:56.806Z","reports":"20\"PK\"o","type":"JTypeB"}
{"device_id":"000U","event_name":"6b","protocol_version":1.4,"report_time":"2017-07-29T18:31:56.823Z","reports":[{"time":"2017-07-28T17:48:56.806Z"},{"time":"2017-07-28T17:27:56.806Z"}],"type":"JTypeC"}
    
    }*/
    public static boolean validate(String obj){
        JType jTypeObj = new JType();
        if(obj.contains("JTypeA"))
            jTypeObj = JSON.parseObject(obj, JTypeA.class);
        else if (obj.contains("JTypeB"))
            jTypeObj = JSON.parseObject(obj, JTypeB.class);
        else if (obj.contains("JTypeC"))
            jTypeObj = JSON.parseObject(obj, JTypeC.class);
            
        boolean isValid = true;
        switch(jTypeObj.getClass().getSimpleName()){
            case "JTypeA":
                JTypeA objA = (JTypeA)jTypeObj;
                isValid &= checkJTypeA(objA.getType());
                isValid &= checkDevice_id(objA.getDevice_id());
                isValid &= checkEvent_name(objA.getEvent_name());
                isValid &= checkProtocol_version(objA.getProtocol_version());
                break;
            case "JTypeB":
                JTypeB objB = (JTypeB)jTypeObj;
                isValid &= checkJTypeB(objB.getType());
                isValid &= checkDevice_id(objB.getDevice_id());
                isValid &= checkEvent_name(objB.getEvent_name());
                isValid &= checkProtocol_version(objB.getProtocol_version());
                isValid &= checkReports(objB.getReports());
                break;
            case "JTypeC":
                JTypeC objC = (JTypeC)jTypeObj;
                isValid &= checkJTypeC(objC.getType());
                isValid &= checkDevice_id(objC.getDevice_id());
                isValid &= checkEvent_name(objC.getEvent_name());
                isValid &= checkProtocol_version(objC.getProtocol_version());
                break;
            default:
                isValid = false;
                break;
        }
        return isValid;
    }
    public static boolean checkReports(String input){
        Pattern p = Pattern.compile("[0-9]{2}\\\"[a-z]{2}\\\"[A-Z]{1,2}");
        Matcher m = p.matcher(input);
        return m.matches();
    }
    public static boolean checkDevice_id (String input){
        Pattern p = Pattern.compile("000[0-9a-zA-Z]{1,2}");
        Matcher m = p.matcher(input);
        return m.matches();
    }
    public static boolean checkEvent_name(String input){
        if (input.length() == 2)
            return true;
        else
            return false;
    }
    public static boolean checkProtocol_version(double input){
        if (input == 1.4)
            return true;
        else
            return false;
    }
    public static boolean checkJTypeA(String input){
        if (input.equals("JTypeA"))
            return true;
        else
            return false;
    }
    
    public static boolean checkJTypeB(String input){
        if (input.equals("JTypeB"))
            return true;
        else
            return false;
    }
    public static boolean checkJTypeC(String input){
        if (input.equals("JTypeC"))
            return true;
        else
            return false;
    }
}
