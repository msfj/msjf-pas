
package organ;

import com.msjf.finance.mcs.facade.organ.OrganInfoFacade;
import com.msjf.finance.pas.bpm.service.*;
import com.msjf.finance.pas.common.response.Response;
import com.msjf.finance.pas.common.test.SpringTestCase;

import com.msjf.finance.pas.facade.organ.CustInfoFacade;

import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 11509 on 2018/12/18.
 */

public class OrganInfoFacadeTest   extends SpringTestCase{
/*    @Resource
    OrganInfoFacade organInfoFacade;*/

    /*@Resource
    CustInfoFacade custInfoFacade;*/

   /* @Resource
    ModelService modelService;*/
   /*@Resource
   ProStepAuditService proStepAuditService;*/

    /*@Resource
    OrganInfoFacade organInfoFacade;*/
    @Resource
    ProService proService;
    @Resource
    PublicTaskService publicTaskService;
    @Test
    /*public  void  queryOrganInfoList(){
       System.out.println("--------------------------"+organInfoFacade.queryOrganInfoList());
        while (true){

        }
    }*/
    public void queryCustInfoList() {
       /* System.out.println("结果--------" + modelService.modelList());*/
        Map<String, Object> mapParams = new HashMap();
       /* List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> mapParam1 = new HashMap();
        Map<String, Object> mapParam2 = new HashMap();
        mapParam1.put("proDefKey","kingdom_1523358114334:3:504856");
        mapParam1.put("proDefName","测试");
        mapParam1.put("stepId","sid_123456");
        mapParam1.put("stepName","测试");
        mapParam1.put("auditorId","123");
        mapParam1.put("auditorName","123");
        mapParam1.put("auditorType","1");
        mapParam1.put("areaNo","1");
        mapParam2.put("proDefKey","kingdom_1523358114334:3:504856");
        mapParam2.put("proDefName","测试");
        mapParam2.put("stepId","sid_123456");
        mapParam2.put("stepName","测试");
        mapParam2.put("auditorId","456");
        mapParam2.put("auditorName","456");
        mapParam2.put("auditorType","1");
        mapParam2.put("areaNo","1");
        list.add(mapParam1);
        list.add(mapParam2);*/
        mapParams.put("formParam",2);
        List<String> list = new ArrayList<>();
        list.add("111|张三");
        list.add("222|李四");
        mapParams.put("assigneeList",list);
        mapParams.put("userId","000");
        mapParams.put("processInstanceId","622501");
        mapParams.put("approve","1");
        mapParams.put("custName","1213");
        mapParams.put("custNo","132");
        mapParams.put("userName","123");
        mapParams.put("taskId","622525");
        mapParams.put("comment","530011");
        mapParams.put("taskDefinitionKey","sid_1525676826092");
        mapParams.put("processDefinitionId","kingdom_1526978404700:5:3479967");
        Response rs = new Response();
        try {
            publicTaskService.createFlow(mapParams,rs);
            /*publicTaskService.executeNextStep(mapParams,rs);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }


