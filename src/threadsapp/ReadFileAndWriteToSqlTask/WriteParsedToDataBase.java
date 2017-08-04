/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp.ReadFileAndWriteToSqlTask;
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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 *
 * @author Ivan
 */
public class WriteParsedToDataBase implements Runnable {
    AtomicLong writtenA;
    AtomicLong writtenB;
    AtomicLong writtenC;
    AtomicBoolean validationIsDone;
    ReentrantLock lock; 
    private LinkedBlockingQueue<JType> objectQueue;
    ComboPooledDataSource cpds;
    private final String sqlJTypeA = "INSERT INTO JTypeA " + 
                        "(protocol_version, type, device_id, report_time, event_name) " +
                        "VALUES(?,?,?,?,?);";
    private final String sqlJTypeB = "INSERT INTO JTypeB " + 
                        "(protocol_version, type, device_id, report_time, event_name, reports) " +
                        "VALUES(?,?,?,?,?,?);";
    private final String sqlJTypeC = "INSERT INTO JTypeC " + 
                        "(protocol_version, type, device_id, report_time, event_name, reports) " +
                        "VALUES(?,?,?,?,?,?);";
    //Задать имя JDBC драйвера и указать базу данных
    final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    final String DB_URL = "jdbc:mysql://localhost/Devices";
    //Задать данные авторизации в БД 
    final String USER = "root";
    final String PASS = "12345";
    
    public WriteParsedToDataBase (LinkedBlockingQueue<JType> objectQueue, AtomicBoolean validationIsDone, ComboPooledDataSource cpds){
        this.objectQueue = objectQueue;
        this.validationIsDone = validationIsDone;
        writtenA = new AtomicLong(0);
        writtenB = new AtomicLong(0);
        writtenC = new AtomicLong(0);
        this.cpds = cpds;
        lock = new ReentrantLock();
    }
    
    @Override
    public void run(){
        System.out.println("Поток задачи Б - запись в БД начал работу");
        // a - поток валидации, если true, то работу закончил
        // b - objectQueue.isEmpty() - переменная хранит true, если очередь пуста
        // !((a^b)&a) - логическое выражение, которое принимает значение false тогда и только тогда, когда
        // a == true, и b == true и позволит выйти из цикла. Это будет означать, что все объекты валидированы и отправлены в БД
        while(checker()) {
            try{
                identifyAndWrite(objectQueue.poll());              
            }
            catch(Exception ex){
                System.out.println(" Выброс исключения в " + this.toString() + " " + ex.getMessage()+"\n");
                Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lock.lock();
        AtomicLong written = new AtomicLong(0);
        written.addAndGet(writtenA.get());
        written.addAndGet(writtenB.get());
        written.addAndGet(writtenC.get());
        System.out.println("Поток записи в БД " + this.toString() + " завершил свою работу записано объектов: " + written.get());
        lock.unlock();
    }
    private void identifyAndWrite(JType obj) throws InterruptedException{
        JType currentObject = objectQueue.poll(5, TimeUnit.SECONDS);
        jTypes_to_DataBase(currentObject);
    }
    private boolean jTypes_to_DataBase(JType obj){
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = cpds.getConnection();
            Timestamp objectTimestamp = convertToTimeStamp(obj.getReport_time());
            //Идентифицировать тип объекта и получить уникальные для типа поля для запроса
            switch(obj.getType()){
                case "JTypeA":
                    stmt = conn.prepareStatement(this.sqlJTypeA);
                    stmt.setDouble(1, obj.getProtocol_version());
                    stmt.setString(2, obj.getType());
                    stmt.setString(3, obj.getDevice_id());
                    stmt.setTimestamp(4, objectTimestamp);
                    stmt.setString(5, obj.getEvent_name());
                    if(stmt.executeUpdate() > 0)
                        writtenA.incrementAndGet();
                    break;
                case "JTypeB":
                    stmt = conn.prepareStatement(this.sqlJTypeB);
                    stmt.setDouble(1, obj.getProtocol_version());
                    stmt.setString(2, obj.getType());
                    stmt.setString(3, obj.getDevice_id());
                    stmt.setTimestamp(4, objectTimestamp);
                    stmt.setString(5, obj.getEvent_name());
                    stmt.setString(6, ((JTypeB)obj).getReports());
                    if(stmt.executeUpdate() > 0)
                        writtenB.incrementAndGet();
                    break;
                case "JTypeC":
                    stmt = conn.prepareStatement(this.sqlJTypeC);
                    ArrayList <JTypeTime> reports = ((JTypeC)obj).getReports();
                    for(JTypeTime time : reports){
                        stmt.setDouble(1, obj.getProtocol_version());
                        stmt.setString(2, obj.getType());
                        stmt.setString(3, obj.getDevice_id());
                        stmt.setTimestamp(4, objectTimestamp);
                        stmt.setString(5, obj.getEvent_name());
                        Instant report_time = time.getTime();
                        stmt.setString(6, convertToTimeStamp(report_time).toString());
                        stmt.addBatch();
                    }                 
                    for(int r : stmt.executeBatch()){
                        if(r > 0)
                        writtenC.incrementAndGet();
                    }
                    break;
                    
            }       
        //System.out.println("Inserted records into the table...");
        }catch(SQLException se){
            //Handle errors for JDBC
            System.out.println(obj.toString());
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            System.out.println(obj.toString());
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
                return false;
            }
            try{
                if(conn!=null)
                    conn.close();
                return true;
            }catch(SQLException se){
                se.printStackTrace();
                return false;
            }//end finally try
        }//end try       
    }
    private Timestamp convertToTimeStamp(Instant objTime){ 
        //System.out.println(objTime.toString());         
        LocalDateTime ldt = LocalDateTime.ofInstant(objTime, ZoneOffset.UTC);
        //System.out.println(ldt.toString());
        java.sql.Timestamp objectTimestamp = java.sql.Timestamp.valueOf(ldt);
        //System.out.println(objectTimestamp.toString());
        return objectTimestamp;
    }
    private void printProgress(){
        AtomicLong written = new AtomicLong(0);
        written.addAndGet(writtenA.get());
        written.addAndGet(writtenB.get());
        written.addAndGet(writtenC.get());
        if (((writtenA.get()%1000) == 0)){
            System.out.println("Записано объектов типа JTypeA: " + writtenA.get());
            System.out.println("Записано объектов типа JTypeB: " + writtenB.get());
            System.out.println("Записано объектов типа JTypeC: " + writtenC.get());
            System.out.println("Всего записано объектов: " + written.get());
        }
    }
    private boolean checker(){
        if(!validationIsDone.get())
            return true;
        else if(validationIsDone.get() & objectQueue.isEmpty())
            return false;
        else
            return true;
    }
}
