/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PoolWrapper;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import threadsapp.JType;
import threadsapp.ReadFileAndWriteToSqlTask.ReadFromFile;
import threadsapp.ReadFileAndWriteToSqlTask.RecognizeAndValidate;
import threadsapp.ReadFileAndWriteToSqlTask.WriteParsedToDataBase;
import static threadsapp.ThreadsApp.configureConnectionPool;

/**
 *
 * @author Ivan
 */
public class PoolB implements Callable<String> {
    private final String FILENAME;
    private final ExecutorService task_B_Executor;
    private final ComboPooledDataSource cpds;
    private AtomicBoolean readFileIsDone;
    private AtomicBoolean validationIsDone;
    private final int queueFromFile_size;
    private final int queueParsedObjectse_size;
    private Phaser phaser;
    public PoolB(String FILENAME, ComboPooledDataSource cpds, Phaser phaser){
        queueFromFile_size = 50000;
        queueParsedObjectse_size = 10000;
        this.FILENAME = FILENAME;
        task_B_Executor = Executors.newFixedThreadPool(5);
        this.cpds = cpds;
        readFileIsDone = new AtomicBoolean(false);
        validationIsDone = new AtomicBoolean(false);
        this.phaser = phaser;
    }
    @Override
    public String call() throws InterruptedException, BrokenBarrierException{
        LinkedBlockingQueue<String> queueFromFile = new LinkedBlockingQueue<String>(queueFromFile_size);
        LinkedBlockingQueue<JType> queueParsedObjects = new LinkedBlockingQueue<JType>(queueParsedObjectse_size);
        //Список для объектов CompleteFuture
        //List<CompletableFuture<String>> futures = new ArrayList<CompletableFuture<String>>();
        List<Future<String>> futures = new ArrayList<Future<String>>();
        phaser.arriveAndAwaitAdvance();
        try{
            futures.add((Future<String>) task_B_Executor.submit(new ReadFromFile(FILENAME, queueFromFile, readFileIsDone)));
            futures.add((Future<String>) task_B_Executor.submit(new RecognizeAndValidate(queueFromFile, queueParsedObjects, readFileIsDone, validationIsDone)));
            for(int i = 0; i < 3; i++)
            futures.add((Future<String>) task_B_Executor.submit(new WriteParsedToDataBase(queueParsedObjects, validationIsDone, cpds)));
        /*  CompletableFuture.runAsync(new ReadFromFile(FILENAME, queueFromFile, readFileIsDone), task_B_Executor);
            CompletableFuture.runAsync(new RecognizeAndValidate(queueFromFile, queueParsedObjects, readFileIsDone, validationIsDone), task_B_Executor);
            CompletableFuture.runAsync(new WriteParsedToDataBase(queueParsedObjects, validationIsDone, cpds), task_B_Executor);
            CompletableFuture.runAsync(new WriteParsedToDataBase(queueParsedObjects, validationIsDone, cpds), task_B_Executor);
            CompletableFuture.runAsync(new WriteParsedToDataBase(queueParsedObjects, validationIsDone, cpds), task_B_Executor);
        */
        }catch(Exception ex) {
            System.out.println(" Выброс исключения " + ex.getMessage()+"\n");
            Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            task_B_Executor.shutdown();
            boolean done = false;
            while(!done){
                done = true;
                for(Future<String> ft : futures){
                    done &=ft.isDone();
                }
            }
            phaser.arriveAndDeregister();
        }
        
        
        return "Пул потоков Б завершил работу";
    }
    
}
