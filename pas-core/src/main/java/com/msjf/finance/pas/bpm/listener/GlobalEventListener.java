package com.msjf.finance.pas.bpm.listener;


import com.msjf.finance.pas.bpm.service.EventHandler;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Activiti的全局事件监听器，即所有事件均需要在这里统一分发处理
 * @since 2018年3月15日17:58:06
 * 如果业务需要，可以用于其他事情，如邮件，短信提醒
 * 都会触发这个监听器
 */
public class GlobalEventListener implements ActivitiEventListener {
 /**
  * 日志处理器
  */
    public final static Log logger= LogFactory.getLog(GlobalEventListener.class);

    //事件及事件的处理器
    //private Map<String,EventHandler> handlers=new HashMap<String, EventHandler>();
    //更换为以下模式，可以防止Spring容器启动时，ProcessEngine尚未创建，而业务类中又使用了这个引用
    private Map<String,String> handlers = new HashMap<String, String>();

    @Override
    public void onEvent(ActivitiEvent event) {
      String eventType=event.getType().name();
      logger.debug("envent type is ========>" + eventType);
      //根据事件的类型ID,找到对应的事件处理器
      String eventHandlerBeanId = handlers.get(eventType);
      if(eventHandlerBeanId!=null){
          ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
         EventHandler handler=(EventHandler) ctx.getBean(eventHandlerBeanId);
         handler.handle(event);
      }
    }

    @Override
    public boolean isFailOnException() {
     return true;
    }

    public Map<String, String> getHandlers() {
     return handlers;
    }

    public void setHandlers(Map<String, String> handlers) {
     this.handlers = handlers;
    }
}