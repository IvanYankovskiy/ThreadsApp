/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp.ReadFileAndWriteToSqlTask;
import com.alibaba.fastjson.JSON;
import threadsapp.GenerateAndWriteTask.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;
import java.text.*;
import java.time.*;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.*;
import java.nio.file.Paths;
import java.nio.charset.*;
/**
 *
 * @author Ivan
 */
public class ReadFromFile implements Runnable {
    private String FILENAME;
    private LinkedBlockingQueue<String> queue;
    public ReadFromFile(String FILENAME,LinkedBlockingQueue<String> queue){
        this.FILENAME = FILENAME;
        this.queue = queue;
    }
    
    @Override
    public void run() {
        try (Stream<String> stream = Files.lines(Paths.get(FILENAME), StandardCharsets.UTF_8)) {
                stream.forEach(line -> {
                    try {
                        queue.put(line);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
                    }

               });
        } catch (IOException e) {
                System.out.println(" Выброс исключения в " + this.toString() + " " + e.getMessage() + "\n");
                e.printStackTrace();
        }
        System.out.println();
        System.out.println();
        System.out.println("Все объекты в файле " + FILENAME + "прочитаны");
        System.out.println();
        System.out.println();
    }



}
