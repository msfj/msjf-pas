package com.msjf.finance.pas.bpm.service;

import org.activiti.engine.delegate.event.ActivitiEvent;

/**
 * Created by Jsan on 2019/1/3.
 */
public interface EventHandler {

    /**
     * 事件处理器
     * @param event
     */
    public void handle(ActivitiEvent event);
}
