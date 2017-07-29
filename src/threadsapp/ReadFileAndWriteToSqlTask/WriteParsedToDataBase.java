/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp.ReadFileAndWriteToSqlTask;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import threadsapp.JType;
import java.sql.*;
/**
 *
 * @author Ivan
 */
public class WriteParsedToDataBase implements Callable<String> {
    Connection connection;
    AtomicLong written;
    private LinkedBlockingQueue<JType> objectQueue;
    private CountDownLatch countDownLatch;
    public WriteParsedToDataBase (LinkedBlockingQueue<JType> objectQueue, CountDownLatch countDownLatch, Connection connection){
        this.objectQueue = objectQueue;
        this.countDownLatch = countDownLatch;
        this.connection = connection;
        written = new AtomicLong(0);
    }
    @Override
    public String call() throws Exception {
        try{
            while((countDownLatch.getCount() > 0) | !objectQueue.isEmpty()){
                if(!objectQueue.isEmpty()){
                    JType currentObject = objectQueue.poll(5, TimeUnit.SECONDS);
                    System.out.println(currentObject.toString());
                    written.addAndGet(1);
                }else
                    countDownLatch.await();
            }
        }
        catch(Exception ex){
            System.out.println(" Выброс исключения в " + this.toString() + " " + ex.getMessage()+"\n");
            Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Объекты " + written.get() + " записаны в базу!\n";
    }
    private void writeToDataBase(Connection connection, JType obj){
        Switch(obj.get)
    }
}
