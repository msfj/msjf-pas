package com.msjf.finance.pas.bpm.entity;

import java.sql.Timestamp;

/**
 * Created by Jsan on 2018/12/27.
 */
public class DeploymentEntity {


    private String id;
    private String key;
    private String name;
    private String resourceName;
    private String tenantId;
    private Integer version;
    private String category;
    private String deploymentId;
    private String description;
    private String diagramResourceName;
    private Integer graphicalNotationDefined;
    private Integer state;
    private Timestamp deploymentTime;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getDeploymentId() {
        return deploymentId;
    }
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDiagramResourceName() {
        return diagramResourceName;
    }
    public void setDiagramResourceName(String diagramResourceName) {
        this.diagramResourceName = diagramResourceName;
    }
    public Boolean getGraphicalNotationDefined() {
        return graphicalNotationDefined.intValue() == 1;
    }
    public void setGraphicalNotationDefined(Integer graphicalNotationDefined) {
        this.graphicalNotationDefined = graphicalNotationDefined;
    }
    public Integer getState() {
        return state;
    }
    public void setState(Integer state) {
        this.state = state;
    }
    public Timestamp getDeploymentTime() {
        return deploymentTime;
    }
    public void setDeploymentTime(Timestamp deploymentTime) {
        this.deploymentTime = deploymentTime;
    }
}
