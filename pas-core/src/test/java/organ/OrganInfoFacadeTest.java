
package organ;

import com.msjf.finance.mcs.facade.organ.OrganInfoFacade;
import com.msjf.finance.pas.common.test.SpringTestCase;

import com.msjf.finance.pas.facade.organ.CustInfoFacade;

import org.junit.Test;

import javax.annotation.Resource;

/**
 * Created by 11509 on 2018/12/18.
 */

public class OrganInfoFacadeTest   extends SpringTestCase{
/*    @Resource
    OrganInfoFacade organInfoFacade;*/

    @Resource
    CustInfoFacade custInfoFacade;
//    @Resource
//    OrganInfoFacade organInfoFacadechen;
    @Test
//    public  void  queryOrganInfoList(){
//        System.out.println("--------------------------"+organInfoFacadechen.queryOrganInfoList());
///*        while (true){
//
//        }*/
//    }
    public void queryCustInfoList(){
        System.out.println("--------"+custInfoFacade.queryCustInfoList());
        while (true){

        }
    }
}

