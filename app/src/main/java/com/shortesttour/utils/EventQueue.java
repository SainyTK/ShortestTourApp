package com.shortesttour.utils;

import android.util.Log;

import com.shortesttour.models.Place;
import com.shortesttour.models.UserCommand;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static android.content.ContentValues.TAG;

public class EventQueue {
    public static final String ADD = "add";
    public static final String DELETE = "delete";
    public static final String CHANGE_ALGORITHM = "change_algorithm";

    private static Queue<UserCommand> commandList = new LinkedList<>();
    private static boolean isTaskRunning = false;
    private static boolean cancekTask = false;

    public static void addEvent(UserCommand cmd,FindPathUtils utils){
        commandList.add(cmd);

        if(commandList.peek().getCommand().contentEquals(DELETE)){
            while(!commandList.peek().getCommand().contentEquals(DELETE)){
                commandList.remove();
            }
        }

        if(!isTaskRunning()){
            startTask();
            dispatchTask(utils);
        }
    }

    private static void dispatchTask(FindPathUtils utils){
        UserCommand cmd = commandList.remove();
        switch (cmd.getCommand()){
            case ADD:
                utils.handleAdd(cmd);
                break;
            case DELETE:
                utils.handleDelete(cmd,true);
                break;
            case CHANGE_ALGORITHM:
                break;
        }
    }

    private static boolean isTaskRunning(){
        return isTaskRunning;
    }

    public static void taskFinished(FindPathUtils utils){
        isTaskRunning = false;
        if(commandList.size()>0)
            dispatchTask(utils);
    }

    private static void startTask(){
        isTaskRunning = true;
    }

}
