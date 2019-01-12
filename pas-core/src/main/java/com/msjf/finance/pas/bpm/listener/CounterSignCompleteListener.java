package com.msjf.finance.pas.bpm.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Component
public class CounterSignCompleteListener implements TaskListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateTask delegateTask) {
		String approved = (String) delegateTask.getVariable("approved");
		if (approved.equals("1")) {
			Long agreeCounter = (Long) delegateTask.getVariable("approvedCounter");
			delegateTask.setVariable("approvedCounter", agreeCounter + 1);
		}
		else if (approved.equals("true")) {
			Long agreeCounter = (Long) delegateTask.getVariable("approvedCounter");
			delegateTask.setVariable("approvedCounter", agreeCounter + 1);
		}
	}

}