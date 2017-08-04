/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp;
//import org.json.simple.*;
import NamedQueues.StringTypeQueue;
import PoolWrapper.PoolA;
import PoolWrapper.PoolB;
import PoolWrapper.PoolG;
import com.alibaba.fastjson.JSON;
import com.mchange.v2.c3p0.*;
import java.beans.PropertyVetoException;
import threadsapp.GenerateAndWriteTask.*;
import threadsapp.JType;
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
import threadsapp.ReadFileAndWriteToSqlTask.*;
import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;
import threadsapp.ReadFromDatabaseAndWriteToFileTask.*;


/**
 *
 * @author Ivan
 */
public class ThreadsApp {
//Задать имя JDBC драйвера и указать базу данных
static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
static final String DB_URL = "jdbc:mysql://localhost/Devices";
//Задать данные авторизации в БД 
static final String USER = "root";
static final String PASS = "12345";
static final List<String> tasks = Arrays.asList("JTypeA", "JTypeB", "JTypeC");
static final long N = 100000;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        final String FILENAME;
        final String OUTPUTFILENAME;
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        configureConnectionPool(cpds);
        File file = new File("generated.json");
        FILENAME = file.getAbsolutePath();
        createFile(file);
        
        File outputfile = new File("output.json");
        OUTPUTFILENAME = outputfile.getAbsolutePath();
        createFile(outputfile);
        
        List<String> results = new ArrayList<String>();
        
        ExecutorService appExecutor = Executors.newFixedThreadPool(3);
        CyclicBarrier barrier = new CyclicBarrier(3);
        try{
            Future<String> taskA_result = (Future<String>) appExecutor.submit(new PoolA(FILENAME, 1000, barrier));
            
            Future<String> taskB_result = (Future<String>) appExecutor.submit(new PoolB(FILENAME, cpds, barrier));
            
            Future<String> taskG_result = (Future<String>) appExecutor.submit(new PoolG(OUTPUTFILENAME, tasks ,cpds, barrier));
            /*results.add(taskG_result.get());  
            results.add(taskB_result.get());
            results.add(taskA_result.get());*/
        }finally{
            appExecutor.shutdown();
        }     
        
    }

   public static void configureConnectionPool(ComboPooledDataSource cpds){
        try {
            cpds.setDriverClass(JDBC_DRIVER); //loads the jdbc driver            
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ThreadsApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        cpds.setJdbcUrl( DB_URL );
        cpds.setUser(USER);                                  
        cpds.setPassword(PASS);                                  
        // the settings below are optional -- c3p0 can work with defaults
        cpds.setMinPoolSize(1);                                     
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(30);
    }
    public static void createFile(File file){
        try {
            //Созадть файл
            if (file.createNewFile())
                System.out.println("Файл" + file.getAbsolutePath() + " создан");
            else
                System.out.println("Файл" + file.getAbsolutePath() + " уже существует.");
        } catch (IOException ex) {
            Logger.getLogger(ThreadsApp.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public static void countStringsinFile(String FILENAME){
        long generatedObjectsCounter = 0;
        try {
            generatedObjectsCounter = Files.lines(Paths.get(FILENAME), StandardCharsets.UTF_8).count();
        } catch (IOException ex) {
            Logger.getLogger(ThreadsApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        //вывести количество количество объектов в файле
        System.out.println("Файл " + FILENAME + " содержит " + generatedObjectsCounter + " строк");
    }
}
