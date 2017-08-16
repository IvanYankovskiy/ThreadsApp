/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp.ReadFromDatabaseAndWriteToFileTask;
import com.alibaba.fastjson.JSON;
import com.mchange.v2.c3p0.ComboPooledDataSource;
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
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 *
 * @author Ivan
 */
public class ReadDataAndGenerateJSON implements Runnable {
    private String sql; 
    private final LinkedBlockingQueue<String> outputQueue;
    private final Lock lock = new ReentrantLock();
    private final String jsonType;
    private final AtomicBoolean isReadingFromDBisDone;
    private AtomicLong offset;
    private final ComboPooledDataSource cpds;
    private final int pagingQueueStep = 50;
    public ReadDataAndGenerateJSON(String jsonType, LinkedBlockingQueue<String> outputQueue, AtomicBoolean isReadingFromDBisDone, ComboPooledDataSource cpds){
        this.outputQueue = outputQueue;
        this.jsonType = jsonType;
        this.isReadingFromDBisDone = isReadingFromDBisDone;
        this.cpds = cpds;
        this.offset = new AtomicLong(0);
    }

    @Override
    public void run() {
        boolean isDone = false;
        System.out.println(Thread.currentThread().getName()+ " начал чтение из БД");
        try {
            while(true & !isDone){
                lock.lock();
                PreparedStatement stmt = null;
                Connection conn = null; 
                try{
                    if (jsonType.equals("JTypeC"))
                        sql = "SELECT* FROM reports RIGHT JOIN " + 
                                "(SELECT* FROM jtypec ORDER BY jtypec.id DESC LIMIT ? OFFSET ?) jtypec " +
                                " using(id);";
                    else
                        sql = "SELECT* FROM " + jsonType + " ORDER BY report_time DESC LIMIT ? OFFSET ?;";
                    conn = cpds.getConnection();
                    stmt = conn.prepareStatement(sql);
                    //stmt.setTimestamp(1, nextReportTime);
                    stmt.setInt(1, pagingQueueStep);
                    stmt.setLong(2, offset.get());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next())
                                isDone = true;
                        else
                            rs.previous();
                        while (rs.next()) {
                            JType jTypeObj;
                            jTypeObj = createJsonABtypeString(jsonType,rs);
                            String jsonString = JSON.toJSONString(jTypeObj);
                            outputQueue.put(jsonString);
                        }
                        rs.close();
                    } catch(SQLException se){
                    //Handle errors for JDBC
                    se.printStackTrace();
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
                offset.addAndGet(pagingQueueStep);
            }
        } finally {
            isReadingFromDBisDone.set(true); 
            System.out.println("Поток чтения объектов типа " + jsonType + " закончил работу"); 
        }
        
        
    }
    private JType createJsonABtypeString(String JType, ResultSet rs) throws SQLException{
        
        JType result = new JType();
        try{
            double protocol_version  = rs.getDouble("protocol_version");
            String type = rs.getString("type");
            String device_id = rs.getString("device_id");
            Timestamp objTime = rs.getTimestamp("report_time");
            Instant report_time =  objTime.toInstant();
            String event_name = rs.getString("event_name");
            if(JType.equals("JTypeA")){
                JTypeA obj = new JTypeA(protocol_version, type, device_id,report_time,event_name);
                result = obj;
            }
            if(JType.equals("JTypeB")){
                String reports = rs.getString("reports");
                JTypeB obj = new JTypeB(protocol_version, type, device_id, report_time, event_name, reports);
                result = obj;
            }
            if(JType.equals("JTypeC")){
                long id = rs.getLong("id");
                LinkedList <JTypeTime> reports = new LinkedList <JTypeTime>();
                boolean createAndMoveOn = true;
                do {
                    if(id == rs.getLong("id")){
                        Instant temp_time = timestampToInstant(rs.getTimestamp("time"));
                        JTypeTime time = new JTypeTime(temp_time);
                        reports.add(time);
                        //перемещаем курсор и проверям, есть ли следующий элемент и проверяем id следующей строки
                        //если он совпадает с рассмтариваемым, то цикл повторится
                        if(rs.next() & (id == rs.getLong("id")))
                            createAndMoveOn = false;
                        else{
                            createAndMoveOn = true;
                            rs.previous();
                        }
                    }else{
                        createAndMoveOn = true;
                        rs.previous(); 
                    }       
                }while(!createAndMoveOn);
                JTypeC obj = new JTypeC(protocol_version, type, device_id, report_time, event_name, reports);
                result = obj;
            }
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        finally{
            
        }
        return result;
    }
    private Instant timestampToInstant(Timestamp objTime){
            return  objTime.toInstant();
    }    
    private Timestamp convertToTimeStamp(Instant objTime){ 
        //System.out.println(objTime.toString());         
        LocalDateTime ldt = LocalDateTime.ofInstant(objTime, ZoneOffset.UTC);
        //System.out.println(ldt.toString());
        java.sql.Timestamp objectTimestamp = java.sql.Timestamp.valueOf(objTime.toString());
        //System.out.println(objectTimestamp.toString());
        return objectTimestamp;
    }
}
