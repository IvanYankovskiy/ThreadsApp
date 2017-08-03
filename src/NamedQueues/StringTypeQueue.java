/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NamedQueues;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Ivan
 */
public class StringTypeQueue {
    private LinkedBlockingQueue<String> concurrentQueue;
    private String taskName;
    public StringTypeQueue(LinkedBlockingQueue<String> concurrentQueue,String taskName){
        this.concurrentQueue = concurrentQueue;
        this.taskName = taskName;
    }
    public LinkedBlockingQueue<String> getQueue(){
        return concurrentQueue;
    }
    public String getTaskName(){
        return taskName;
    }
}
