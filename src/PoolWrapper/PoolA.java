/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PoolWrapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import threadsapp.GenerateAndWriteTask.GenerateAndWriteTypeA;
import threadsapp.GenerateAndWriteTask.GenerateAndWriteTypeB;
import threadsapp.GenerateAndWriteTask.GenerateAndWriteTypeC;

/**
 *
 * @author Ivan
 */
public class PoolA implements Callable<String> {
    private ExecutorService task_A_Executor;
    private final String FILENAME;
    private long generatedObjectsCounter;
    private final long N;
    public PoolA(String FILENAME, long N){
        task_A_Executor = Executors.newFixedThreadPool(3);
        this.FILENAME = FILENAME;
        this.N = N;
    }
    @Override
    public String call() throws Exception {
        List<Future<String>> futures = new ArrayList<Future<String>>();
        //Списко для строк резульатов выполнения потоков
        List<String> results = new ArrayList<String>();
        //Пул потоков для обработки задачи а)
        ExecutorService task_A_Executor = Executors.newFixedThreadPool(3);
        try{
            //futures.add((Future<String>) task_A_Executor.submit(new GenerateAndWriteType(FILENAME, N, "JTypeA")));
            futures.add((Future<String>) task_A_Executor.submit(new GenerateAndWriteTypeA(FILENAME,N)));
            futures.add((Future<String>) task_A_Executor.submit(new GenerateAndWriteTypeB(FILENAME,N)));
            futures.add((Future<String>) task_A_Executor.submit(new GenerateAndWriteTypeC(FILENAME,N)));
            for(Future<String> ftr : futures){
                results.add(ftr.get());
            }
        }finally{
            task_A_Executor.shutdown();
            generatedObjectsCounter = Files.lines(Paths.get(FILENAME), StandardCharsets.UTF_8).count();
        }
        for(String result : results){
            System.out.println(result);
        }
        return "Файл " + FILENAME + " содержит " + generatedObjectsCounter + " строк";
    }
    
    
}
