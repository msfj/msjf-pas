package com.msjf.finance.pas.bpm.dao.mapper;


import com.msjf.finance.pas.bpm.entity.DeploymentEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Jsan on 2018/12/24.
 */
public interface DeploymentQueryMapper {

    @Select("SELECT P.*, D.DEPLOY_TIME_ FROM ${prefix}ACT_RE_PROCDEF P, ${prefix}ACT_RE_DEPLOYMENT D WHERE "
            + "P.DEPLOYMENT_ID_ = D.ID_ ORDER BY D.DEPLOY_TIME_ DESC ")
    @Results(value = {
            @Result(id = true, property = "id", column = "ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "key", column = "KEY_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "name", column = "NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "resourceName", column = "RESOURCE_NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "tenantId", column = "TENANT_ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "version", column = "VERSION_", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "category", column = "CATEGORY_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "deploymentId", column = "DEPLOYMENT_ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "description", column = "DESCRIPTION_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "diagramResourceName", column = "DGRM_RESOURCE_NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "graphicalNotationDefined", column = "HAS_GRAPHICAL_NOTATION_", javaType = Integer.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "state", column = "SUSPENSION_STATE_", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "deploymentTime", column = "DEPLOY_TIME_", javaType = Timestamp.class, jdbcType = JdbcType.TIMESTAMP), })
    List<DeploymentEntity> findDeployments(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT count(P.ID_) AS CO_ FROM ${prefix}ACT_RE_PROCDEF P, ${prefix}ACT_RE_DEPLOYMENT D WHERE "
            + "P.DEPLOYMENT_ID_ = D.ID_")
    @Result(column = "CO_", javaType = Long.class, jdbcType = JdbcType.INTEGER)
    Long countDeployments();

    @Select("SELECT P.*, D.DEPLOY_TIME_ FROM ${prefix}ACT_RE_PROCDEF P, ${prefix}ACT_RE_DEPLOYMENT D WHERE "
            + "P.DEPLOYMENT_ID_ = D.ID_ AND P.SUSPENSION_STATE_ = #{state,jdbcType=INTEGER} ORDER"
            + " BY D.DEPLOY_TIME_ DESC ")
    @Results(value = {
            @Result(id = true, property = "id", column = "ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "key", column = "KEY_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "name", column = "NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "resourceName", column = "RESOURCE_NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "tenantId", column = "TENANT_ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "version", column = "VERSION_", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "category", column = "CATEGORY_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "deploymentId", column = "DEPLOYMENT_ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "description", column = "DESCRIPTION_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "diagramResourceName", column = "DGRM_RESOURCE_NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "graphicalNotationDefined", column = "HAS_GRAPHICAL_NOTATION_", javaType = Integer.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "state", column = "SUSPENSION_STATE_", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "deploymentTime", column = "DEPLOY_TIME_", javaType = Timestamp.class, jdbcType = JdbcType.TIMESTAMP), })
    List<DeploymentEntity> findDeploymentsByStete(@Param("state") Integer state, @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);

    @Select("SELECT count(P.ID_) AS CO_ FROM ${prefix}ACT_RE_PROCDEF P, ${prefix}ACT_RE_DEPLOYMENT D WHERE "
            + "P.DEPLOYMENT_ID_ = D.ID_ AND P.SUSPENSION_STATE_ = #{state,jdbcType=INTEGER}")
    @Result(column = "CO_", javaType = Long.class, jdbcType = JdbcType.INTEGER)
    Long countDeploymentsByStete(Integer state);

    @Select("SELECT P.*, D.DEPLOY_TIME_ FROM ${prefix}ACT_RE_PROCDEF P, ${prefix}ACT_RE_DEPLOYMENT D WHERE "
            + "P.DEPLOYMENT_ID_ = D.ID_ AND P.VERSION_ = (SELECT MAX(P1.VERSION_)"
            + " FROM ${prefix}ACT_RE_PROCDEF P1 WHERE P1.KEY_ = P.KEY_ ) ORDER BY D.DEPLOY_TIME_ DESC")
    @Results(value = {
            @Result(id = true, property = "id", column = "ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "key", column = "KEY_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "name", column = "NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "resourceName", column = "RESOURCE_NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "tenantId", column = "TENANT_ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "version", column = "VERSION_", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "category", column = "CATEGORY_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "deploymentId", column = "DEPLOYMENT_ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "description", column = "DESCRIPTION_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "diagramResourceName", column = "DGRM_RESOURCE_NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "graphicalNotationDefined", column = "HAS_GRAPHICAL_NOTATION_", javaType = Integer.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "state", column = "SUSPENSION_STATE_", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "deploymentTime", column = "DEPLOY_TIME_", javaType = Timestamp.class, jdbcType = JdbcType.TIMESTAMP), })
    List<DeploymentEntity> findDeploymentsWithLastVersion(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT count(P.ID_) AS CO_ FROM ${prefix}ACT_RE_PROCDEF P, ${prefix}ACT_RE_DEPLOYMENT D WHERE "
            + "P.DEPLOYMENT_ID_ = D.ID_ AND P.VERSION_ = (SELECT MAX(P1.VERSION_)"
            + " FROM ${prefix}ACT_RE_PROCDEF P1 WHERE P1.KEY_ = P.KEY_ )")
    @Result(column = "CO_", javaType = Long.class, jdbcType = JdbcType.INTEGER)
    Long countDeploymentsWithLastVersion();

    @Select("SELECT P.*, D.DEPLOY_TIME_ FROM ${prefix}ACT_RE_PROCDEF P, ${prefix}ACT_RE_DEPLOYMENT D WHERE "
            + "P.DEPLOYMENT_ID_ = D.ID_ AND P.SUSPENSION_STATE_ = #{state,jdbcType=INTEGER} AND P.VERSION_ = (SELECT MAX(P1.VERSION_) "
            + "FROM ${prefix}ACT_RE_PROCDEF P1 WHERE P1.KEY_ = P.KEY_ ) ORDER BY D.DEPLOY_TIME_ DESC")
    @Results(value = {
            @Result(id = true, property = "id", column = "ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "key", column = "KEY_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "name", column = "NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "resourceName", column = "RESOURCE_NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "tenantId", column = "TENANT_ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "version", column = "VERSION_", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "category", column = "CATEGORY_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "deploymentId", column = "DEPLOYMENT_ID_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "description", column = "DESCRIPTION_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "diagramResourceName", column = "DGRM_RESOURCE_NAME_", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "graphicalNotationDefined", column = "HAS_GRAPHICAL_NOTATION_", javaType = Integer.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "state", column = "SUSPENSION_STATE_", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "deploymentTime", column = "DEPLOY_TIME_", javaType = Timestamp.class, jdbcType = JdbcType.TIMESTAMP), })
    List<DeploymentEntity> findDeploymentsBySteteWithLastVersion(@Param("state") Integer state,
                                                                 @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT count(P.ID_) AS CO_ FROM ${prefix}ACT_RE_PROCDEF P, ${prefix}ACT_RE_DEPLOYMENT D WHERE "
            + "P.DEPLOYMENT_ID_ = D.ID_ AND P.VERSION_ = (SELECT MAX(P1.VERSION_) "
            + "FROM ${prefix}ACT_RE_PROCDEF P1 WHERE P1.KEY_ = P.KEY_ )")
    @Result(column = "CO_", javaType = Long.class, jdbcType = JdbcType.INTEGER)
    Long countDeploymentsBySteteWithLastVersion(Integer state);
}
