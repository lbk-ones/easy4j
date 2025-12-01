/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easy4j.infra.rpc.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetrics {

    // CPU
    private double systemCpuUsagePercentage;
    private double jvmCpuUsagePercentage;

    // JVM-Memory
    private double jvmMemoryUsed;
    private double jvmMemoryMax;
    private double jvmMemoryUsedPercentage;

    // System-Memory
    private double systemMemoryUsed;
    private double systemMemoryMax;
    private double systemMemoryUsedPercentage;

    // Disk
    private double diskUsed;
    private double diskTotal;
    private double diskUsedPercentage;


    // Pod
    double podCpuUsagePercentage;
    double podMemoryMax;
    double podMemoryUsed;
    double podMemoryUsedPercentage;
    double podDiskTotal;
    double podDiskUsed;
    double podDiskUsedPercentage;

}
