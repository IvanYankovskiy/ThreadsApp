/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NamedQueues;

import java.util.concurrent.LinkedBlockingQueue;
import threadsapp.JType;

/**
 *
 * @author Ivan
 */
public class JTypeQueue {
    private LinkedBlockingQueue<JType> concurrentQueue;
    private String taskName;
    public JTypeQueue(LinkedBlockingQueue<JType> concurrentQueue,String taskName){
        this.concurrentQueue = concurrentQueue;
        this.taskName = taskName;
    }
    public LinkedBlockingQueue<JType> getQueue(){
        return concurrentQueue;
    }
    public String getTaskName(){
        return taskName;
    }
}
