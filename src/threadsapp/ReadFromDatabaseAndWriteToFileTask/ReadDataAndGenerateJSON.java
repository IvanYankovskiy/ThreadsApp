/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp.ReadFromDatabaseAndWriteToFileTask;
import com.alibaba.fastjson.JSON;
import java.sql.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import threadsapp.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.ArrayList;
/**
 *
 * @author Ivan
 */
public class ReadDataAndGenerateJSON implements Callable<String> {
    //Задать имя JDBC драйвера и указать базу данных
    final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    final String DB_URL = "jdbc:mysql://localhost/Devices";
    //Задать данные авторизации в БД 
    final String USER = "root";
    final String PASS = "12345";
    private final String sql; 
    private final LinkedBlockingQueue<String> outputQueue;
    private final Lock lock = new ReentrantLock();
    private final String jsonType;
    public ReadDataAndGenerateJSON(String jsonType, LinkedBlockingQueue<String> outputQueue){
        sql = "SELECT* FROM " + jsonType + ";";
        this.outputQueue = outputQueue;
        this.jsonType = jsonType;
    }

    @Override
    public String call() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        lock.lock();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);
            stmt= conn.createStatement();
            stmt.setFetchSize(50000);
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    
                    outputQueue.put(createJsonABtypeString(jsonType,rs));
                    
                }
                rs.close();
            }
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            lock.unlock();
            //finally block used to close resources
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try 
        return "Поток чтения из БД работу закончил";
    }
    public String createJsonABtypeString(String JType, ResultSet rs) throws SQLException{
        String result = null;
        double protocol_version  = rs.getDouble("protocol_version");
        String type = rs.getString("type");
        String device_id = rs.getString("device_id");
        Timestamp objTime = rs.getTimestamp("report_time");
        Instant report_time =  objTime.toInstant();
        String event_name = rs.getString("event_name");
        if(JType.equals("JTypeA")){
            JTypeA obj = new JTypeA(protocol_version, type, device_id,report_time,event_name);
            result = JSON.toJSONString(obj);
        }
        if(JType.equals("JTypeB")){
            String reports = rs.getString("reports");
            JTypeB obj = new JTypeB(protocol_version, type, device_id, report_time, event_name, reports);
            result = JSON.toJSONString(obj);
        }
        if(JType.equals("JTypeC")){
            ArrayList <JTypeTime> reports = new ArrayList <JTypeTime>();
            boolean createAndMoveOn = true;
            do {
                if((device_id.equals(rs.getString("type")))
                        &(report_time.equals(timestampToInstant(rs.getTimestamp("report_time"))))
                        &(event_name.equals(rs.getString("event_name")))){
                    Instant temp_time = timestampToInstant(rs.getTimestamp("report_time"));
                    JTypeTime time = new JTypeTime(temp_time);
                    reports.add(time);
                    rs.next();
                    createAndMoveOn = false;
                }else{
                    createAndMoveOn = true;
                    rs.previous(); 
                }       
            }while(!createAndMoveOn);
            JTypeC obj = new JTypeC(protocol_version, type, device_id, report_time, event_name, reports);
            result = JSON.toJSONString(obj);
        }
        return result;
    }
    public Instant timestampToInstant(Timestamp objTime){
            return  objTime.toInstant();
    }    
                
}
