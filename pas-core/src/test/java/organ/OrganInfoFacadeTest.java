
package organ;

import com.msjf.finance.pas.common.test.SpringTestCase;

import com.msjf.finance.pas.facade.organ.OrganInfoFacade;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * Created by 11509 on 2018/12/18.
 */

public class OrganInfoFacadeTest   extends SpringTestCase{
    @Resource
    OrganInfoFacade organInfoFacade;

    @Test
    public  void  queryOrganInfoList(){
        System.out.println("--------------------------"+organInfoFacade.queryOrganInfoList());
        while (true){

        }
    }
}

