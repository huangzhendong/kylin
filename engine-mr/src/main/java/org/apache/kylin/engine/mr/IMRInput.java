/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.engine.mr;

import org.apache.hadoop.mapreduce.Job;
import org.apache.kylin.job.execution.DefaultChainedExecutable;
import org.apache.kylin.metadata.model.IJoinedFlatTableDesc;
import org.apache.kylin.metadata.model.ISegment;
import org.apache.kylin.metadata.model.TableDesc;

/**
 * Any ITableSource that wishes to serve as input of MapReduce build engine must adapt to this interface.
 * 任何想作为MapReduce输入table的数据源都必须实现这个接口
 */
public interface IMRInput {

    /** Return a helper to participate in batch cubing job flow. 获取数据块，以支持数据流*/
    public IMRBatchCubingInputSide getBatchCubingInputSide(IJoinedFlatTableDesc flatDesc);

    /** Return an InputFormat that reads from specified table. 读取特定表InputFormat*/
    public IMRTableInputFormat getTableInputFormat(TableDesc table);

    /** Return a helper to participate in batch cubing merge job flow. 用于批量cubing合并*/
    public IMRBatchMergeInputSide getBatchMergeInputSide(ISegment seg);

    /**
     * Utility that configures mapper to read from a table.
     * 工具：用于配置mapper从table读入文件
     */
    public interface IMRTableInputFormat {

        /** Configure the InputFormat of given job. 配置job的InputFormat*/
        public void configureJob(Job job);

        /** Parse a mapper input object into column values. 解析table中的字段*/
        public String[] parseMapperInput(Object mapperInput);
    }

    /**
     * Participate the batch cubing flow as the input side. Responsible for creating
     * intermediate flat table (Phase 1：阶段一) and clean up any leftover (Phase 4：阶段四).
     * 
     * - Phase 1: Create Flat Table
     * - Phase 2: Build Dictionary (with FlatTableInputFormat)
     * - Phase 3: Build Cube (with FlatTableInputFormat)
     * - Phase 4: Update Metadata & Cleanup
     */
    public interface IMRBatchCubingInputSide {

        /** Return an InputFormat that reads from the intermediate flat table 中间宽表对应的InputFormat*/
        public IMRTableInputFormat getFlatTableInputFormat();

        /** Add step that creates an intermediate flat table as defined by CubeJoinedFlatTableDesc 创建一张列按降序排列的FlatTable*/
        public void addStepPhase1_CreateFlatTable(DefaultChainedExecutable jobFlow);

        /** Add step that does necessary clean up, like delete the intermediate flat table */
        public void addStepPhase4_Cleanup(DefaultChainedExecutable jobFlow);
    }

    public interface IMRBatchMergeInputSide {

        /** Add step that executes before merge dictionary and before merge cube. 在merge dic 和 cube之前要做的步骤*/
        public void addStepPhase1_MergeDictionary(DefaultChainedExecutable jobFlow);

    }
}