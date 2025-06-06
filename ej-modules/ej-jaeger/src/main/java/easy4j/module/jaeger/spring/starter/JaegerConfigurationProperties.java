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

package easy4j.module.jaeger.spring.starter;

import io.jaegertracing.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * JaegerConfigurationProperties
 *
 * @author bokun.li
 * @date 2025-05
 */
@ConfigurationProperties("opentracing.jaeger")
public class JaegerConfigurationProperties {

  private final RemoteReporter remoteReporter = new RemoteReporter();
  private final HttpSender httpSender = new HttpSender();
  private final UdpSender udpSender = new UdpSender();
  private final ConstSampler constSampler = new ConstSampler();
  private final ProbabilisticSampler probabilisticSampler = new ProbabilisticSampler();
  private final RateLimitingSampler rateLimitingSampler = new RateLimitingSampler();
  private final RemoteControlledSampler remoteControlledSampler = new RemoteControlledSampler();


  /**
   * Enable Jaeger Tracer
   */
  private boolean enabled = true;

  /**
   * Service name to be used
   * Expect certain properties in order, falling back to 'unknown-spring-boot'.
   */
  @Value("${jaeger.service-name:${spring.application.name:unknown-spring-boot}}")
  private String serviceName;

  /**
   * Whether spans should be logged to the console
   */
  private boolean logSpans = false;


  private boolean enableWebFilter = true;

  /**
   * Enable the handling of B3 headers like "X-B3-TraceId" This setting should be used when it is
   * desired for Jaeger to be able to join traces started by other Zipkin instrumented applications
   */
  private boolean enableB3Propagation = false;

  /**
   * Enable the generation of 128 bit trace ids. Default is 64 bit.
   */
  private boolean enable128BitTraces = false;

  private boolean expandExceptionLogs = false;

  private Map<String, String> tags = new HashMap<>();

  /**
   * If enabled, tags from the JAEGER_TAGS env variable will be included
   */
  private boolean includeJaegerEnvTags = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public boolean isLogSpans() {
    return logSpans;
  }

  public boolean isEnableWebFilter() {
    return enableWebFilter;
  }

  public void setEnableWebFilter(boolean enableWebFilter) {
    this.enableWebFilter = enableWebFilter;
  }


  public void setLogSpans(boolean logSpans) {
    this.logSpans = logSpans;
  }

  public boolean isEnableB3Propagation() {
    return enableB3Propagation;
  }

  public void setEnableB3Propagation(boolean enableB3Propagation) {
    this.enableB3Propagation = enableB3Propagation;
  }

  public boolean isEnable128BitTraces() {
    return enable128BitTraces;
  }

  public void setEnable128BitTraces(boolean enable128BitTraces) {
    this.enable128BitTraces = enable128BitTraces;
  }

  public boolean isExpandExceptionLogs() {
    return expandExceptionLogs;
  }

  public void setExpandExceptionLogs(boolean expandExceptionLogs) {
    this.expandExceptionLogs = expandExceptionLogs;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }

  public Map<String, String> determineTags() {
    if (!includeJaegerEnvTags) {
      return tags;
    }

    final Map<String, String> result = new HashMap<>(tags);
    result.putAll(tracerTagsFromEnv());
    return result;
  }

  // inspired by io.jaegertracing.Configuration#tracerTagsFromEnv
  private Map<String, String> tracerTagsFromEnv() {
    final Map<String, String> tracerTagMaps = new HashMap<>();
    final String tracerTags = getProperty(Configuration.JAEGER_TAGS);
    if (tracerTags != null) {
      final String[] tags = tracerTags.split("\\s*,\\s*");
      for (String tag : tags) {
        final String[] tagValue = tag.split("\\s*=\\s*");
        if (tagValue.length == 2) {
          tracerTagMaps.put(tagValue[0], tagValue[1]);
        }
      }
    }
    return tracerTagMaps;
  }

  /**
   * Retrieve a property name either from the Java system properties or Environment Variables
   */
  private String getProperty(String name) {
    return System.getProperty(name, System.getenv(name));
  }

  public boolean isIncludeJaegerEnvTags() {
    return includeJaegerEnvTags;
  }

  public void setIncludeJaegerEnvTags(boolean includeJaegerEnvTags) {
    this.includeJaegerEnvTags = includeJaegerEnvTags;
  }

  public HttpSender getHttpSender() {
    return httpSender;
  }

  public RemoteReporter getRemoteReporter() {
    return remoteReporter;
  }

  public UdpSender getUdpSender() {
    return udpSender;
  }

  public ConstSampler getConstSampler() {
    return constSampler;
  }

  public ProbabilisticSampler getProbabilisticSampler() {
    return probabilisticSampler;
  }

  public RateLimitingSampler getRateLimitingSampler() {
    return rateLimitingSampler;
  }

  public RemoteControlledSampler getRemoteControlledSampler() {
    return remoteControlledSampler;
  }


  public static class RemoteReporter {

    private Integer flushInterval;

    private Integer maxQueueSize;

    public Integer getFlushInterval() {
      return flushInterval;
    }

    public void setFlushInterval(Integer flushInterval) {
      this.flushInterval = flushInterval;
    }

    public Integer getMaxQueueSize() {
      return maxQueueSize;
    }

    public void setMaxQueueSize(Integer maxQueueSize) {
      this.maxQueueSize = maxQueueSize;
    }
  }

  /**
   * If the URL is set, then HttpSender is used regardless
   * of the configuration in UdpSender
   */
  public static class HttpSender {

    private String url;

    private Integer maxPayload = 0;

    private String username;

    private String password;

    private String authToken;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public Integer getMaxPayload() {
      return maxPayload;
    }

    public void setMaxPayload(Integer maxPayload) {
      this.maxPayload = maxPayload;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getAuthToken() {
      return authToken;
    }

    public void setAuthToken(String authToken) {
      this.authToken = authToken;
    }
  }

  public static class UdpSender {

    private String host = "localhost";

    private int port = 6831;

    private int maxPacketSize = 0;

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public int getMaxPacketSize() {
      return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
      this.maxPacketSize = maxPacketSize;
    }
  }

  public static class ConstSampler {

    private Boolean decision;

    public Boolean getDecision() {
      return decision;
    }

    public void setDecision(Boolean decision) {
      this.decision = decision;
    }
  }

  public static class ProbabilisticSampler {

    private Double samplingRate;

    public Double getSamplingRate() {
      return samplingRate;
    }

    public void setSamplingRate(Double samplingRate) {
      this.samplingRate = samplingRate;
    }
  }

  public static class RateLimitingSampler {

    private Double maxTracesPerSecond;

    public Double getMaxTracesPerSecond() {
      return maxTracesPerSecond;
    }

    public void setMaxTracesPerSecond(Double maxTracesPerSecond) {
      this.maxTracesPerSecond = maxTracesPerSecond;
    }
  }

  public static class RemoteControlledSampler {

    /**
     * i.e. localhost:5778
     */
    private String hostPort;

    private String host;

    private int port = 5778;

    private Double samplingRate =
        io.jaegertracing.internal.samplers.ProbabilisticSampler.DEFAULT_SAMPLING_PROBABILITY;


    @DeprecatedConfigurationProperty(replacement = "Use host + port properties instead")
    public String getHostPort() {
      return hostPort;
    }

    public void setHostPort(String hostPort) {
      this.hostPort = hostPort;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public Double getSamplingRate() {
      return samplingRate;
    }

    public void setSamplingRate(Double samplingRate) {
      this.samplingRate = samplingRate;
    }
  }
}
