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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import threadsapp.Validator.Validator;

/**
 *
 * @author Ivan
 */
public class RecognizeAndValidate implements Runnable {
    private LinkedBlockingQueue<String> inputQueue;
    private LinkedBlockingQueue<JType> outputQueue;
    AtomicBoolean readFileIsDone;
    AtomicBoolean validationIsDone;
    ReentrantLock lock = new ReentrantLock();
    public RecognizeAndValidate(LinkedBlockingQueue<String> inputQueue, LinkedBlockingQueue<JType> outputQueue, AtomicBoolean readFileIsDone, AtomicBoolean validationIsDone){
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.readFileIsDone = readFileIsDone;
        this.validationIsDone = validationIsDone;
    }
    @Override
    public void run(){
        while(!readFileIsDone.get() && !inputQueue.isEmpty()){
            try{
                String obj = inputQueue.poll();
                if (!inputQueue.isEmpty()){
                    boolean isValid = false;
                    lock.lock();
                    try {
                        isValid = Validator.validate(obj);
                    } finally {
                        lock.unlock();
                    }
                    if (isValid){
                        JType JTypeObj = parseJSON(obj);
                        outputQueue.put(JTypeObj);
                    }
                }        
            }catch(Exception ex){
                System.out.println(" Вы брос исключения в " + this.toString() + " " + ex.getMessage() + "\n");
                Logger.getLogger(ReadFromFile.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        validationIsDone.set(true);
        System.out.println("Поток обработки " + this.toString() + " завершил свою работу ");
    }
    private JType parseJSON(String obj) throws com.alibaba.fastjson.JSONException {
        JType jTypeObject = new JType();
        if(obj.contains("JTypeA"))
            jTypeObject = JSON.parseObject(obj, JTypeA.class);
        else if (obj.contains("JTypeB"))
            jTypeObject = JSON.parseObject(obj, JTypeB.class);
        else if (obj.contains("JTypeC"))
            jTypeObject = JSON.parseObject(obj, JTypeC.class);
        return jTypeObject;

    }
        
}
