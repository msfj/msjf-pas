package com.msjf.finance.pas.bpm.listener;


import com.msjf.finance.pas.bpm.service.EventHandler;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;

public class EntityCreatedListener implements EventHandler {

    @Override
    public void handle(ActivitiEvent event) {

        if(event instanceof ActivitiEntityEvent){

        }
    }
}