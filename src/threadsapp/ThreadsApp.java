/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp;
//import org.json.simple.*;
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
import java.io.IOException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Ivan
 */
public class ThreadsApp {
static final String FILENAME = "C:\\Users\\Ivan\\Desktop\\TomskLabs тестовое задание\\file.json";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        File file = new File(FILENAME);
        //Create the file
        if (file.createNewFile())
          System.out.println("File is created!");
        else
          System.out.println("File already exists.");
        executeTaskAPool(FILENAME);
        
        
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
                ftr.get();
            }
        }finally{
            task_A_Executor.shutdown();
        }
        for(String result : results){
            System.out.println(result);
        }
   } 
}
