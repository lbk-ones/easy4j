/**
 * Copyright 2018-2019 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package easy4j.module.jaeger.spring.starter.customizers;

import easy4j.module.jaeger.spring.starter.TracerBuilderCustomizer;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.propagation.B3TextMapCodec;
import io.opentracing.propagation.Format;

/**
 * B3CodecTracerBuilderCustomizer
 *
 * @author bokun.li
 * @date 2025-05
 */
public class B3CodecTracerBuilderCustomizer implements TracerBuilderCustomizer {

  @Override
  public void customize(JaegerTracer.Builder builder) {
    B3TextMapCodec injector = new B3TextMapCodec.Builder().build();

    builder.registerInjector(Format.Builtin.HTTP_HEADERS, injector)
        .registerExtractor(Format.Builtin.HTTP_HEADERS, injector);

    builder.registerInjector(Format.Builtin.TEXT_MAP, injector)
        .registerExtractor(Format.Builtin.TEXT_MAP, injector);
  }
}