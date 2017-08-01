/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadsapp.ReadFileAndWriteToSqlTask;
import threadsapp.*;
import com.alibaba.fastjson.JSON;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ivan
 */
public class RecognizeAndValidate implements Runnable {
    private LinkedBlockingQueue<String> inputQueue;
    private LinkedBlockingQueue<JType> outputQueue;
    private CyclicBarrier BARRIER;
    public RecognizeAndValidate(LinkedBlockingQueue<String> inputQueue, LinkedBlockingQueue<JType> outputQueue, CyclicBarrier BARRIER ){
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.BARRIER = BARRIER;
    }
    @Override
    public void run(){
        while(true){
            try{
                String obj = inputQueue.poll();
                if (!inputQueue.isEmpty()){
                    if(obj.contains("JTypeA"))
                        outputQueue.put(JSON.parseObject(obj, JTypeA.class));
                    else if (obj.contains("JTypeB"))
                        outputQueue.put(JSON.parseObject(obj, JTypeB.class));
                    else if (obj.contains("JTypeC"))
                        outputQueue.put(JSON.parseObject(obj, JTypeC.class));
                }        
            }catch(Exception ex){
                System.out.println(" Вы брос исключения в " + this.toString() + " " + ex.getMessage() + "\n");
                Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //System.out.println("Поток обработки " + this.toString() + " завершил свою работу ");    
        }
        
    }
        
}
