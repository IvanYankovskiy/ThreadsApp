/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp.GenerateAndWriteTask;
import com.alibaba.fastjson.JSON;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import threadsapp.*;

/**
 *
 * @author Ivan
 */
public class GenerateAndWriteType implements Callable<Boolean> {
    private AtomicLong counter;
    private String FILENAME;
    private Lock fileWriterLock = new ReentrantLock();
    private final long N;
    private final String type;
    public GenerateAndWriteType(String FILENAME, Long N, String type){
        this.FILENAME = FILENAME;
        this.N = N;
        this.type = type;
    }
    public Boolean call()throws Exception{
        fileWriterLock.lock();
        try{
            System.out.println("Поток задачи а - генерации и записи JSON type A начал работу");
            while(counter.get() < N){
                JType obj = new JType();
                switch (type){
                    case "JTypeA":
                        obj = new JTypeA();
                        break;
                    case "JTypeB":
                        obj = new JTypeB();
                        break;    
                    case "JTypeC":
                        obj = new JTypeB();
                        break;  
                }
                writeJSON_objectToFile(obj, FILENAME); 
                if ((counter.get())%10000 == 0)
                    System.out.println("Поток задачи а - генерации и записи JSON type A сгенерировал " + counter + " записей");
                counter.incrementAndGet();
            }
        }finally{
            fileWriterLock.unlock();
        }    
        String msg = "Поток задачи а - генерации и записи JSON " + type + "закончил работу. Записано "
                + counter.get() + " объектов";
        System.out.println(msg); 
        return true;
    }
    
    public void writeJSON_objectToFile(JType object, String FILENAME){
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
