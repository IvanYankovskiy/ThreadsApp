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
public class pollAndWriteToFile implements Callable<String> {
    private final CountDownLatch start;
    private final String OUTPUTFILENAME;
    private final Lock fileWriterLock = new ReentrantLock();
    private final LinkedBlockingQueue<String> inputQueue;
    private final String jsonType;
    public pollAndWriteToFile(String FILENAME, LinkedBlockingQueue<String> inputQueue, String jsonType, CountDownLatch countDownLatch){
        this.OUTPUTFILENAME = FILENAME;
        this.inputQueue = inputQueue;
        this.jsonType = jsonType;
        this.start = countDownLatch;
    }

    @Override
    public String call() throws Exception {
        

                inputQueue.stream().forEach(line -> { 
                    try{
                        inputQueue.poll();
                        //fileWriterLock.lock();
                        writeJSON_objectToFile(line ,OUTPUTFILENAME);
                        
                        //fileWriterLock.unlock();
                    }catch(Exception ex){
                        System.out.println(" Вы брос исключения в " + this.toString() + " " + ex.getMessage() + "\n");
                        Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
                    }
               }); 
        return "Объекты типа " + jsonType + " записаны";
    }
    
    public void writeJSON_objectToFile(String object, String FILENAME){
        try (FileWriter writer = new FileWriter(FILENAME, true)){
            //writer.append(JSON.toJSONString(object));
            writer.write(JSON.toJSONString(object) + "\n");
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ThreadsApp.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
