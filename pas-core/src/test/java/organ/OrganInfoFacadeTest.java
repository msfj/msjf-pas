
package organ;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.*;

import com.msjf.finance.pas.facade.organ.CustInfoFacade;

import common.SpringTestCase;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 11509 on 2018/12/18.
 */

public class OrganInfoFacadeTest   extends SpringTestCase {
/*    @Resource
    OrganInfoFacade organInfoFacade;*/

    /*@Resource
    CustInfoFacade custInfoFacade;*/

   @Resource
    ModelService modelService;
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
        Response rs = new Response();
        mapParams.put("userId","111");
        mapParams.put("userName","张三");
        mapParams.put("custName","1213");
        mapParams.put("custNo","132");
       mapParams.put("processDefinitionId","kingdom_1526978404700:5:3479967");
       /* mapParams.put("taskId","3697561");
        mapParams.put("comment","同意");
        mapParams.put("processInstanceId","3697521");
        mapParams.put("approve","1");
        mapParams.put("taskDefinitionKey","sid_1525662628491");*/


        try {
           publicTaskService.createFlow(mapParams);
            /*publicTaskService.executeNextStep(mapParams);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        mapParams.put("modelId","367561");
        modelService.editorJson(mapParams,rs);*/
    }
    }


