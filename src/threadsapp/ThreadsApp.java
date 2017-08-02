/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp;
//import org.json.simple.*;
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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        File file = new File(FILENAME);
        File outputfile = new File(OUTPUTFILENAME);
        
        //Созадть входной файл
        if (file.createNewFile())
          System.out.println("Файл создан!");
        else
          System.out.println("Файл уже существует.");
        
        //Созадть входной файл
        if (outputfile.createNewFile())
          System.out.println("Файл объектов из БД создан!");
        else
          System.out.println("Файл объектов из БД уже существует.");
        
        //создать пул потоков задачи а) и запустить его
        //executeTaskAPool(FILENAME);
        //используя Stream API подсчитаем количество сгенерированных объектов
        long generatedObjectsCounter = Files.lines(Paths.get(FILENAME), StandardCharsets.UTF_8).count();
        //вывести количество количество объектов в файле
        System.out.println("Файл " + FILENAME + " содержит " + generatedObjectsCounter + " объектов");

        
        //создать пул потоков задач б)-в) и запустить его
        //executeTaskBPool(FILENAME);
        
        
        executeTaskGPool(OUTPUTFILENAME);
        
        generatedObjectsCounter = Files.lines(Paths.get(OUTPUTFILENAME), StandardCharsets.UTF_8).count();
        //вывести количество количество объектов в файле
        System.out.println("Файл " + OUTPUTFILENAME + " содержит " + generatedObjectsCounter + " объектов\n");
       /* */
    }
    public static void printSymbols(){
        //System.out.println((int)'A'); 
        //System.out.println(); 
        for(int i = 65; i<=90; i++){
            char o = (char)i;
            System.out.println((char)o);
            //0-9 в int 0-9
            //a-z в int 97-122
            //A-Z в int 65-90
        }
    }
    public static void writeJSON_objectToFile(JType object){
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
   public static void testGenerateAllTypesObjects(){
        for(int i = 0; i<10; i++){
            JTypeA objA = new JTypeA();
            writeJSON_objectToFile(objA);
            JTypeB objB = new JTypeB();
            writeJSON_objectToFile(objB);
            JTypeC objC = new JTypeC();
            writeJSON_objectToFile(objC);
            System.out.println("Сгенерированные объекты, итерация " + i + ":");
            System.out.println();
            System.out.println(objA.toString());
            System.out.println();
            System.out.println(objB.toString());
            System.out.println();
            System.out.println(objC.toString());
            System.out.println();
            System.out.println("Конвертированные в JSON-строку объекты, итерация " + i + ":");
            System.out.println();
            System.out.println(JSON.toJSONString(objA));
            System.out.println();
            System.out.println(JSON.toJSONString(objB));
            System.out.println();
            System.out.println(JSON.toJSONString(objC));
            System.out.println();
        }
   }
   public static void executeTaskAPool(String FILENAME) throws IOException, ExecutionException, InterruptedException{
        //Список для объектов CompleteFuture
        List<Future<String>> futures = new ArrayList<Future<String>>();
        //Списко для строк резульатов выполнения потоков
        List<String> results = new ArrayList<String>();
        //Пул потоков для обработки задачи а)
        ExecutorService task_A_Executor = Executors.newFixedThreadPool(3);
        try{
            futures.add((Future<String>) task_A_Executor.submit(new GenerateAndWriteTypeA(FILENAME)));
            futures.add((Future<String>) task_A_Executor.submit(new GenerateAndWriteTypeB(FILENAME)));
            futures.add((Future<String>) task_A_Executor.submit(new GenerateAndWriteTypeC(FILENAME)));
            for(Future<String> ftr : futures){
                results.add(ftr.get());
            }
        }finally{
            task_A_Executor.shutdown();
        }
        for(String result : results){
            System.out.println(result);
        }
   }
    public static void executeTaskBPool(String FILENAME) throws IOException, ExecutionException, InterruptedException{
        AtomicBoolean readFileIsDone = new AtomicBoolean(false);
        AtomicBoolean validationIsDone = new AtomicBoolean(false);
        LinkedBlockingQueue<String> queueFromFile = new LinkedBlockingQueue<String>(10000);
        LinkedBlockingQueue<JType> queueParsedObjects = new LinkedBlockingQueue<JType>(5000);
        //Список для объектов CompleteFuture
        List<CompletableFuture<String>> futures = new ArrayList<CompletableFuture<String>>();
        //Списко для строк резульатов выполнения потоков
        List<String> results = new ArrayList<String>();
        //Пул потоков для обработки задачи а)
        ExecutorService task_B_Executor = Executors.newFixedThreadPool(3);
        try{
            CompletableFuture.runAsync(new ReadFromFile(FILENAME, queueFromFile, readFileIsDone), task_B_Executor);
            CompletableFuture.runAsync(new RecognizeAndValidate(queueFromFile, queueParsedObjects, readFileIsDone, validationIsDone), task_B_Executor);
            CompletableFuture.runAsync(new WriteParsedToDataBase(queueParsedObjects, validationIsDone), task_B_Executor);
            
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
    public static void executeTaskGPool(String OUTPUTFILENAME) throws IOException, ExecutionException, InterruptedException{
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        configureConnectionPool(cpds);
        //список задач
        List<String> tasks = Arrays.asList("JTypeA", "JTypeB", "JTypeC"); 
        //Список для объектов CompleteFuture
        List<CompletableFuture<String>> futures = new ArrayList<CompletableFuture<String>>();
        //Списко для строк резульатов выполнения потоков
        List<String> results = new ArrayList<String>();
        //Пул потоков для обработки задачи а)
        ExecutorService task_G_Executor = Executors.newFixedThreadPool(tasks.size()*2);
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ThreadsApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ThreadsApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try{
            for(String task : tasks){
                AtomicBoolean isReadingFromDBisDone = new AtomicBoolean(false);
                LinkedBlockingQueue<String> jsonFromDBQueue = new LinkedBlockingQueue<String>(1000);
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

}
