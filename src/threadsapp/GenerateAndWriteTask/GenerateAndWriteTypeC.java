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
public class GenerateAndWriteTypeC implements Callable<String> {
    private AtomicLong counter;
    String FILENAME;
    private Lock fileWriterLock = new ReentrantLock();
    private final long N;
    public GenerateAndWriteTypeC(String FILENAME, long N){
        this.FILENAME = FILENAME;
        this.counter = new AtomicLong(0);
        this.N = N;
    }
    public String call()throws Exception{
        System.out.println("Поток задачи а - генерации и записи JSON type C начал работу");
        while(counter.get() < N){
            JTypeC objC = new JTypeC();
            fileWriterLock.lock();
            try{
                writeJSON_objectToFile(objC, FILENAME);
                if ((counter.get())%10000 == 0)
                    System.out.println("Поток задачи а - генерации и записи JSON type C сгенерировал " + counter + " записей");
            }
            finally{
                fileWriterLock.unlock();
            }
            counter.incrementAndGet();
        }
        String result = "Поток задачи а - генерации и записи JSON type C закончил работу. Записано "
                + counter + " объектов";
        System.out.println(result);
        return result;
    }
    
    public static void writeJSON_objectToFile(JType object, String FILENAME){
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
