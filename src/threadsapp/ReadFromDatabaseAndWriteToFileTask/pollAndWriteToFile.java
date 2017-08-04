/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp.ReadFromDatabaseAndWriteToFileTask;

import com.alibaba.fastjson.JSON;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import threadsapp.JType;
import threadsapp.JTypeA;
import threadsapp.JTypeB;
import threadsapp.JTypeC;
import threadsapp.ReadFileAndWriteToSqlTask.ReadFromFile;
import threadsapp.ThreadsApp;

/**
 *
 * @author Ivan
 */
public class pollAndWriteToFile implements Runnable {
    private final String OUTPUTFILENAME;
    private final Lock fileWriterLock = new ReentrantLock();
    private final LinkedBlockingQueue<String> inputQueue;
    private final String jsonType;
    AtomicBoolean isReadingFromDBisDone;
    private AtomicLong countWritten;
    public pollAndWriteToFile(String FILENAME, LinkedBlockingQueue<String> inputQueue, String jsonType, AtomicBoolean isReadingFromDBisDone){
        this.OUTPUTFILENAME = FILENAME;
        this.inputQueue = inputQueue;
        this.jsonType = jsonType;
        this.isReadingFromDBisDone = isReadingFromDBisDone;
        this.countWritten = new AtomicLong(0);

    }

    @Override
    public void run() {
        try {
            Thread.currentThread().sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(pollAndWriteToFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(Thread.currentThread().getName()+ " запись считанного из БД в файл");
        while(checker()){
            //fileWriterLock.lock();
            try {
                writeJSON_objectToFile(inputQueue.poll(), OUTPUTFILENAME);
            }catch(Exception ex){
                System.out.println(" Выброс исключения в " + this.toString() + " " + ex.getMessage() + "\n");
                Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
            } /*finally {
                //fileWriterLock.unlock();
            }  */           
        }
        System.out.println("Записано " + countWritten.get() + " объектов  типа " + jsonType);
    }
    
    public void writeJSON_objectToFile(String object, String FILENAME){
        try (FileWriter writer = new FileWriter(FILENAME, true)){
            //writer.append(JSON.toJSONString(object));
            if(!object.equals(null)){
                writer.write(JSON.toJSONString(object) + "\n");
                writer.flush();
                writer.close();
                countWritten.incrementAndGet();
            }
                
        } catch (IOException ex) {
            Logger.getLogger(ThreadsApp.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    private boolean checker(){
        if(!isReadingFromDBisDone.get())
            return true;
        else if(isReadingFromDBisDone.get() & inputQueue.isEmpty())
            return false;
        else
            return true;
    }
}
