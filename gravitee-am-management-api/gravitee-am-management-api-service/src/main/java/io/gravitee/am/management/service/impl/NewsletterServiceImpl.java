/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.management.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.management.service.NewsletterService;
import io.reactivex.Single;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;


import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class NewsletterServiceImpl implements NewsletterService, InitializingBean, DisposableBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(NewsletterServiceImpl.class);
  private ExecutorService executorService;

  @Value("${newsletter.url:https://newsletter.gravitee.io}")
  private String newsletterURI;

  @Value("${newsletter.enabled:true}")
  private boolean newsletterEnabled = true;

  @Autowired private ObjectMapper mapper;

  @Autowired
  @Qualifier("newsletterWebClient")
  private WebClient client;

  @Override
  public void subscribe(Object user) {
    executorService.execute(
        () -> {
          client
              .post(newsletterURI)
              .sendJson(
                  user,
                  handler -> {
                    if (handler.failed()) {
                      LOGGER.error(
                          "An error has occurred while register newsletter for a user",
                          handler.cause());
                    }
                  });
        });
  }

  @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getTaglines_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
  public Single<List<String>> getTaglines() {
 return RxJava2Adapter.monoToSingle(getTaglines_migrated());
}
@Override
  public Mono<List<String>> getTaglines_migrated() {
    if (!newsletterEnabled) {
      return Mono.just(Collections.emptyList());
    }

    String taglinesPath = "taglines";
    if (newsletterURI.lastIndexOf('/') != newsletterURI.length() - 1) {
      taglinesPath = "/" + taglinesPath;
    }

    return RxJava2Adapter.singleToMono(client.getAbs(newsletterURI + taglinesPath).rxSend())
            .map(
                RxJavaReactorMigrationUtil.<HttpResponse<Buffer>, List<String>>toJdkFunction(
                    res -> {
                      if (res.statusCode() != 200) {
                        LOGGER.error(
                            "An error has occurred when reading the newsletter taglines response: "
                                + res.statusMessage());
                        return Collections.emptyList();
                      }
                      return mapper.readValue(res.bodyAsString(), List.class);
                    }));
  }

  @Override
  public void afterPropertiesSet() {
    executorService = Executors.newCachedThreadPool();
  }

  @Override
  public void destroy() {
    if (executorService != null) {
      executorService.shutdown();
    }
  }
}
