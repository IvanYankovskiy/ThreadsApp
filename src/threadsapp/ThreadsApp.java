/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp;
//import org.json.simple.*;
import NamedQueues.StringTypeQueue;
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
static final String FILENAME = "C:\\Users\\Ivan\\Desktop\\TomskLabs тестовое задание\\file.json";
static final String OUTPUTFILENAME = "C:\\Users\\Ivan\\Desktop\\TomskLabs тестовое задание\\outputfile.json";
//Задать имя JDBC драйвера и указать базу данных
static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
static final String DB_URL = "jdbc:mysql://localhost/Devices";
//Задать данные авторизации в БД 
static final String USER = "root";
static final String PASS = "12345";
static final List<String> tasks = Arrays.asList("JTypeA", "JTypeB", "JTypeC");
static final long N = 10000;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        File file = new File(FILENAME);
        createFile(file);
        
        File outputfile = new File(OUTPUTFILENAME);
        createFile(outputfile);

        
        ExecutorService task_A_Executor = Executors.newFixedThreadPool(3);
        for (Callable<Boolean> task : createTasksA(FILENAME) ){
           task_A_Executor.submit(task); 
        }
        
        //создать пул потоков задачи а) и запустить его
        //executeTaskAPool(FILENAME);
        //используя Stream API подсчитаем количество сгенерированных объектов
        long generatedObjectsCounter = Files.lines(Paths.get(FILENAME), StandardCharsets.UTF_8).count();
        //вывести количество количество объектов в файле
        System.out.println("Файл " + FILENAME + " содержит " + generatedObjectsCounter + " объектов");

        
        //создать пул потоков задач б)-в) и запустить его
        //executeTaskBPool(FILENAME);
        
        
        //executeTaskGPool(OUTPUTFILENAME, tasks);
        
        generatedObjectsCounter = Files.lines(Paths.get(OUTPUTFILENAME), StandardCharsets.UTF_8).count();
        //вывести количество количество объектов в файле
        System.out.println("Файл " + OUTPUTFILENAME + " содержит " + generatedObjectsCounter + " объектов\n");
       /* */
    }

   public static List<Callable<Boolean>> createTasksA(String FILENAME) throws IOException, ExecutionException, InterruptedException{
        //Список для объектов CompleteFuture
        List<Callable<Boolean>> taskListA = new ArrayList<Callable<Boolean>>();
        //List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
        //Списко для строк резульатов выполнения потоков
        //List<Boolean> results = new ArrayList<Boolean>();
        for(String task : tasks)
            taskListA.add((Callable<Boolean>) new GenerateAndWriteType(FILENAME, N , task));
        return taskListA;
   }
    public static void executeTaskBPool(String FILENAME) throws IOException, ExecutionException, InterruptedException{
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        configureConnectionPool(cpds);
        AtomicBoolean readFileIsDone = new AtomicBoolean(false);
        AtomicBoolean validationIsDone = new AtomicBoolean(false);
        LinkedBlockingQueue<String> queueFromFile = new LinkedBlockingQueue<String>(10000);
        LinkedBlockingQueue<JType> queueParsedObjects = new LinkedBlockingQueue<JType>(5000);
        //Список для объектов CompleteFuture
        List<CompletableFuture<String>> futures = new ArrayList<CompletableFuture<String>>();
        //Списко для строк резульатов выполнения потоков
        List<String> results = new ArrayList<String>();
        //Пул потоков для обработки задачи а)
        ExecutorService task_B_Executor = Executors.newFixedThreadPool(5);
        /*List<StringTypeQueue> sortedStringQueues = new ArrayList<StringTypeQueue>();
        List<StringTypeQueue> sortedJTypesQueues = new ArrayList<StringTypeQueue>();*/
        try{

            CompletableFuture.runAsync(new ReadFromFile(FILENAME, queueFromFile, readFileIsDone), task_B_Executor);
            CompletableFuture.runAsync(new RecognizeAndValidate(queueFromFile, queueParsedObjects, readFileIsDone, validationIsDone), task_B_Executor);
            CompletableFuture.runAsync(new WriteParsedToDataBase(queueParsedObjects, validationIsDone, cpds), task_B_Executor);
            CompletableFuture.runAsync(new WriteParsedToDataBase(queueParsedObjects, validationIsDone, cpds), task_B_Executor);
            CompletableFuture.runAsync(new WriteParsedToDataBase(queueParsedObjects, validationIsDone, cpds), task_B_Executor);
        }catch(Exception ex) {
            System.out.println(" Выброс исключения " + ex.getMessage()+"\n");
            Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            task_B_Executor.shutdown();
            /*for(Future<String> ftr : futures){
                results.add(ftr.get());
            }
            results.forEach((result) -> {System.out.println(result);});*/
            System.out.println("Пул потоков Б завершил работу");           
        }
   }
    public static void executeTaskGPool(String OUTPUTFILENAME, List<String> tasks) throws IOException, ExecutionException, InterruptedException{
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        configureConnectionPool(cpds);
        
        //Список для объектов CompleteFuture
        List<CompletableFuture<String>> futures = new ArrayList<CompletableFuture<String>>();
        //Списко для строк резульатов выполнения потоков
        List<String> results = new ArrayList<String>();
        //Пул потоков для обработки задачи а)
        ExecutorService task_G_Executor = Executors.newFixedThreadPool(tasks.size()*2);
        
        try{
            for(String task : tasks){
                AtomicBoolean isReadingFromDBisDone = new AtomicBoolean(false);
                LinkedBlockingQueue<String> jsonFromDBQueue = new LinkedBlockingQueue<String>(10000);
                CompletableFuture.runAsync(new ReadDataAndGenerateJSON(task, jsonFromDBQueue, isReadingFromDBisDone, cpds),task_G_Executor);
                CompletableFuture.runAsync(new pollAndWriteToFile(OUTPUTFILENAME, jsonFromDBQueue, task, isReadingFromDBisDone),task_G_Executor);
            }
            
            /*for(Future<String> ftr : futures){
                results.add(ftr.get());
            }*/
        }catch(Exception ex) {
            System.out.println(" Вы брос исключения " + ex.getMessage()+"\n");
            Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            task_G_Executor.shutdown();
            System.out.println("Пул потоков Г завершил работу");           
        }
        for(String result : results){
            System.out.println(result);
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
        cpds.setMaxPoolSize(20);
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
}
