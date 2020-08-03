package com.mmall.pojo;

import java.util.Date;

public class Dict {
    private Integer id;

    private Integer pid;

    private String dataType;

    private String dataCode;

    private String dataValue;

    private Integer sortNo;

    private Byte status;

    private String dataDesc;

    private Date updateTime;

    public Dict(Integer id, Integer pid, String dataType, String dataCode, String dataValue, Integer sortNo, Byte status, String dataDesc, Date updateTime) {
        this.id = id;
        this.pid = pid;
        this.dataType = dataType;
        this.dataCode = dataCode;
        this.dataValue = dataValue;
        this.sortNo = sortNo;
        this.status = status;
        this.dataDesc = dataDesc;
        this.updateTime = updateTime;
    }

    public Dict() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType == null ? null : dataType.trim();
    }

    public String getDataCode() {
        return dataCode;
    }

    public void setDataCode(String dataCode) {
        this.dataCode = dataCode == null ? null : dataCode.trim();
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue == null ? null : dataValue.trim();
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public void setDataDesc(String dataDesc) {
        this.dataDesc = dataDesc == null ? null : dataDesc.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}