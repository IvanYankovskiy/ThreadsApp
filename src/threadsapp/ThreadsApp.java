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
static final List<String> tasks = Arrays.asList("JTypeA", "JTypeB", "JTypeC");
static final String FILENAME;
static File file = new File("generated.json");
static final String OUTPUTFILENAME;
static File outputfile = new File("output.json");
static final String USER; //= "root";
static final String PASS; // = "12345";
static final long N;

static{
        //File file = new File("generated.json");
        FILENAME = file.getAbsolutePath();
        createFile(file);
        //файл, куда будут записаны объеты, считанные из базы данных; Задача Г
        //File outputfile = new File("output.json");
        OUTPUTFILENAME = outputfile.getAbsolutePath();
        createFile(outputfile);
        
        USER = setUserName();
        PASS = setPassword();
        N = setOjectQuantity();
}
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        
        
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        configureConnectionPool(cpds);
        //файл, куда будут записаны сгенерированные объекты; Задача А
        
        
        List<Future<String>> futures = new ArrayList<Future<String>>();
        List<String> results = new ArrayList<String>();
        
        ExecutorService appExecutor = Executors.newFixedThreadPool(1);
        Phaser phaser = new Phaser(3);
        try{
            
            Future<String> taskA_result = (Future<String>) appExecutor.submit(new PoolA(FILENAME, N, phaser));
            futures.add(taskA_result);
            Future<String> taskB_result = (Future<String>) appExecutor.submit(new PoolB(FILENAME, cpds, phaser));
            futures.add(taskB_result);
            Future<String> taskG_result = (Future<String>) appExecutor.submit(new PoolG(OUTPUTFILENAME, tasks ,cpds, phaser));
            futures.add(taskG_result);
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
            else{
                System.out.println("Файл" + file.getAbsolutePath() + " уже существует.");
                char choice, ignore;
                do{
                    System.out.println("Вы хотите дописать сгенерированные объекты в существующий файл или пересоздать файл");
                    System.out.println("Введите 1 - если вы хотите создать файл заново.");
                    System.out.println("Введите 2 - если вы хотите добавить объекты к существующему.");
                    System.out.print("Ваш выбор: ");
                    choice = (char)System.in.read();
                    do{
                        ignore = (char)System.in.read();
                    }
                    while (ignore != '\n');
                }while (choice < 0 | choice < 3 );
                switch(choice){
                    case '1':
                        if(file.delete())
                            if(file.createNewFile())
                                System.out.println("Файл" + file.getAbsolutePath() + " создан заново");  
                        break;
                    case '2':
                        break;
                }
            }
                
                
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
        //вывести количество количество строк в файле
        System.out.println("Файл " + FILENAME + " содержит " + generatedObjectsCounter + " строк");
    }
    public static void writeStringToFile(String object, String FILENAME){
        try (FileWriter writer = new FileWriter(FILENAME, true)){
            //writer.append(JSON.toJSONString(object));
            if(!object.equals(null)){
                writer.write(JSON.toJSONString(object) + "\n");
                writer.flush();
                writer.close();
            }
                
        } catch (IOException ex) {
            Logger.getLogger(ThreadsApp.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public static String setUserName(){
        String USER;
        Scanner in = new Scanner(System.in);
        System.out.print("Введите имя пользователя: ");
        USER = in.nextLine();
        System.out.println();
        return USER;
    }
    public static String setPassword(){
        String PASSWORD;
        Scanner in = new Scanner(System.in);
        System.out.print("Введите пароль: ");
        PASSWORD = in.nextLine();
        System.out.println();
        return PASSWORD;
    }
    
    public static long setOjectQuantity(){
        long quantity;
        Scanner in = new Scanner(System.in);
        System.out.print("Введите введите количество генерируемых объектов каждого типа (целое число): ");
        quantity = in.nextLong();
        System.out.println();
        return quantity;
    }
}
