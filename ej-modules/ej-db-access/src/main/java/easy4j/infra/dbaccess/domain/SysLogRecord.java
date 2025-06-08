/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.dbaccess.domain;

import easy4j.infra.dbaccess.annotations.JdbcColumn;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * SysLogRecord
 *
 * @author bokun.li
 * @date 2025-05
 */
@Data
public class SysLogRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    @JdbcColumn(isPrimaryKey = true)
    private String id;

    // 日志标签
    private String tag;

    // 标签描述
    private String tagDesc;

    // 链路ID
    private String traceId;

    // 处理状态
    private String status;

    // 处理时间
    private String processTime;

    // 操作时间(长文本)
    // 索引 IDX_SYS_LOG_RECORD_CREATE_DATE
    private Date createDate;

    // 参数(长文本)
    private String params;

    // 备注(长文本)
    private String remark;

    // 错误信息
    private String errorInfo;


    // 操作对象ID(当前操作的对象标识id)
    private String targetId;

    // 操作对象ID(当前操作对象第二个标识id)
    private String targetId2;

    // 操作人代码
    private String operateCode;

    // 操作人姓名
    private String operateName;

    public SysLogRecord toNewLogRecord() {
        SysLogRecord sysLogRecord = new SysLogRecord();
        sysLogRecord.setId(this.getId());
        sysLogRecord.setTag(this.getTag());
        sysLogRecord.setTagDesc(this.getTagDesc());
        sysLogRecord.setTraceId(this.getTraceId());
        sysLogRecord.setStatus(this.getStatus());
        sysLogRecord.setProcessTime(this.getProcessTime());
        sysLogRecord.setCreateDate(this.getCreateDate());
        sysLogRecord.setParams(this.getParams());
        sysLogRecord.setRemark(this.getRemark());
        sysLogRecord.setErrorInfo(this.getErrorInfo());
        sysLogRecord.setTargetId(this.getTargetId());
        sysLogRecord.setTargetId2(this.getTargetId2());
        sysLogRecord.setOperateCode(this.getOperateCode());
        sysLogRecord.setOperateName(this.getOperateName());
        return sysLogRecord;
    }
}
