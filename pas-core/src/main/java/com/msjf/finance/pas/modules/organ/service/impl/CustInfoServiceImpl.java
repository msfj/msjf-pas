package com.msjf.finance.pas.modules.organ.service.impl;

import com.msjf.finance.pas.facade.organ.domain.CustInfoDomain;
import com.msjf.finance.pas.modules.organ.dao.CustInfoDao;
import com.msjf.finance.pas.modules.organ.entity.CustEntity;
import com.msjf.finance.pas.modules.organ.service.CustInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service("custInfoService")
public class CustInfoServiceImpl implements CustInfoService {

    @Resource
    CustInfoDao custInfoDao;

    @Override
    public List queryCustInfoList() {
        try {
        List<CustEntity> custEntityList = custInfoDao.queryCustInfoList();
        List<CustInfoDomain> custInfoDomains = new ArrayList<>();
        custEntityList.stream().forEach(custEntity ->
                {
                    CustInfoDomain custInfoDomain = new CustInfoDomain();
                    BeanUtils.copyProperties(custEntity, custInfoDomain);
                    custInfoDomains.add(custInfoDomain);
                }
        );
        return custInfoDomains;
        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
        }

        return null;
    }
}
