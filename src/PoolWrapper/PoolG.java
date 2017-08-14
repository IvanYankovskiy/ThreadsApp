/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PoolWrapper;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import threadsapp.ReadFileAndWriteToSqlTask.ReadFromFile;
import threadsapp.ReadFromDatabaseAndWriteToFileTask.ReadDataAndGenerateJSON;
import threadsapp.ReadFromDatabaseAndWriteToFileTask.pollAndWriteToFile;
import static threadsapp.ThreadsApp.configureConnectionPool;

/**
 *
 * @author Ivan
 */
public class PoolG implements Callable<String>{
    private final List<String> tasks;
    private final String OUTPUTFILENAME;
    private final ComboPooledDataSource cpds;
    //private Phaser phaser;
    public PoolG(String OUTPUTFILENAME, List<String> tasks,ComboPooledDataSource cpds ){
        this.tasks = tasks;
        this.OUTPUTFILENAME = OUTPUTFILENAME;
        this.cpds = cpds;
        //this.phaser = phaser;
    }
    @Override
    public String call() throws Exception {
        //Список для объектов CompleteFuture
        List<CompletableFuture<String>> futures = new ArrayList<CompletableFuture<String>>();
        //Списко для строк резульатов выполнения потоков
        List<String> results = new ArrayList<String>();
        //Пул потоков для обработки задачи а)
        ExecutorService task_G_Executor = Executors.newFixedThreadPool(tasks.size()*2);
        //phaser.arriveAndAwaitAdvance();
        Thread.sleep(500);
        //phaser.arriveAndAwaitAdvance();
        try{
            
            for(String task : tasks){
                AtomicBoolean isReadingFromDBisDone = new AtomicBoolean(false);
                LinkedBlockingQueue<String> jsonFromDBQueue = new LinkedBlockingQueue<String>(10000);
                CompletableFuture.runAsync(new ReadDataAndGenerateJSON(task, jsonFromDBQueue, isReadingFromDBisDone, cpds),task_G_Executor);
                CompletableFuture.runAsync(new pollAndWriteToFile(OUTPUTFILENAME, jsonFromDBQueue, task, isReadingFromDBisDone),task_G_Executor);
            }
        }catch(Exception ex) {
            System.out.println(" Вы брос исключения " + ex.getMessage()+"\n");
            Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            task_G_Executor.shutdown();
            //phaser.arriveAndDeregister();
        }
        return "Пул потоков Г завершил работу";
    }
    
}
