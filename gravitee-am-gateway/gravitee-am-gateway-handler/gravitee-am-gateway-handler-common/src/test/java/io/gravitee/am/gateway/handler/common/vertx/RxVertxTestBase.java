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
package io.gravitee.am.gateway.handler.common.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.*;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.test.core.AsyncTestBase;
import io.vertx.test.core.RepeatRule;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;

import org.junit.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RxVertxTestBase extends AsyncTestBase {

    public static final boolean USE_NATIVE_TRANSPORT =
            Boolean.getBoolean("vertx.useNativeTransport");
    public static final boolean USE_DOMAIN_SOCKETS = Boolean.getBoolean("vertx.useDomainSockets");
    private static final Logger log = LoggerFactory.getLogger(VertxTestBase.class);

    @Rule public RepeatRule repeatRule = new RepeatRule();

    protected Vertx vertx;

    protected Vertx[] vertices;

    private List<Vertx> created;

    protected void vinit() {
        vertx = null;
        vertices = null;
        created = null;
    }

    public void setUp() throws Exception {
        super.setUp();
        vinit();
        VertxOptions options = getOptions();
        boolean nativeTransport = options.getPreferNativeTransport();
        vertx = Vertx.vertx(options);
        if (nativeTransport) {
            assertTrue(vertx.isNativeTransportEnabled());
        }
    }

    protected VertxOptions getOptions() {
        VertxOptions options = new VertxOptions();
        options.setPreferNativeTransport(USE_NATIVE_TRANSPORT);
        return options;
    }

    protected void tearDown() throws Exception {
        if (vertx != null) {
            close(vertx.getDelegate());
        }
        if (created != null) {
            CountDownLatch latch = new CountDownLatch(created.size());
            for (Vertx v : created) {
                v.close(
                        ar -> {
                            if (ar.failed()) {
                                log.error("Failed to shutdown vert.x", ar.cause());
                            }
                            latch.countDown();
                        });
            }
            assertTrue(latch.await(180, TimeUnit.SECONDS));
        }
        FakeClusterManager.reset(); // Bit ugly
        super.tearDown();
    }

    /**
     * @return router a blank new Vert.x instance with no options closed when tear down executes.
     */
    protected Vertx vertx() {
        if (created == null) {
            created = new ArrayList<>();
        }
        Vertx vertx = Vertx.vertx();
        created.add(vertx);
        return vertx;
    }

    /**
     * @return router a blank new Vert.x instance with @{@code options} closed when tear down
     *     executes.
     */
    protected Vertx vertx(VertxOptions options) {
        if (created == null) {
            created = new ArrayList<>();
        }
        Vertx vertx = Vertx.vertx(options);
        created.add(vertx);
        return vertx;
    }

    /**
     * Create a blank new clustered Vert.x instance with @{@code options} closed when tear down
     * executes.
     */
    protected void clusteredVertx(VertxOptions options, Handler<AsyncResult<Vertx>> ar) {
        if (created == null) {
            created = Collections.synchronizedList(new ArrayList<>());
        }
        Vertx.clusteredVertx(
                options,
                event -> {
                    if (event.succeeded()) {
                        created.add(event.result());
                    }
                    ar.handle(event);
                });
    }

    protected ClusterManager getClusterManager() {
        return null;
    }

    protected void startNodes(int numNodes) {
        startNodes(numNodes, getOptions());
    }

    protected void startNodes(int numNodes, VertxOptions options) {
        CountDownLatch latch = new CountDownLatch(numNodes);
        vertices = new Vertx[numNodes];
        for (int i = 0; i < numNodes; i++) {
            int index = i;
            clusteredVertx(
                    options.setClusterManager(getClusterManager()),
                    ar -> {
                        try {
                            if (ar.failed()) {
                                ar.cause().printStackTrace();
                            }
                            assertTrue("Failed to start node", ar.succeeded());
                            vertices[index] = ar.result();
                        } finally {
                            latch.countDown();
                        }
                    });
        }
        try {
            assertTrue(latch.await(2, TimeUnit.MINUTES));
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    protected static void setOptions(TCPSSLOptions sslOptions, KeyCertOptions options) {
        if (options instanceof JksOptions) {
            sslOptions.setKeyStoreOptions((JksOptions) options);
        } else if (options instanceof PfxOptions) {
            sslOptions.setPfxKeyCertOptions((PfxOptions) options);
        } else {
            sslOptions.setPemKeyCertOptions((PemKeyCertOptions) options);
        }
    }

    protected static final String[] ENABLED_CIPHER_SUITES =
            new String[] {
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
                "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
                "SSL_RSA_WITH_RC4_128_SHA",
                "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
                "TLS_ECDH_RSA_WITH_RC4_128_SHA",
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
                "SSL_RSA_WITH_RC4_128_MD5",
                "TLS_EMPTY_RENEGOTIATION_INFO_SCSV",
                "TLS_DH_anon_WITH_AES_128_GCM_SHA256",
                "TLS_DH_anon_WITH_AES_128_CBC_SHA256",
                "TLS_ECDH_anon_WITH_AES_128_CBC_SHA",
                "TLS_DH_anon_WITH_AES_128_CBC_SHA",
                "TLS_ECDH_anon_WITH_RC4_128_SHA",
                "SSL_DH_anon_WITH_RC4_128_MD5",
                "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA",
                "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
                "TLS_RSA_WITH_NULL_SHA256",
                "TLS_ECDHE_ECDSA_WITH_NULL_SHA",
                "TLS_ECDHE_RSA_WITH_NULL_SHA",
                "SSL_RSA_WITH_NULL_SHA",
                "TLS_ECDH_ECDSA_WITH_NULL_SHA",
                "TLS_ECDH_RSA_WITH_NULL_SHA",
                "TLS_ECDH_anon_WITH_NULL_SHA",
                "SSL_RSA_WITH_NULL_MD5",
                "SSL_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_DH_anon_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA",
                "TLS_KRB5_WITH_RC4_128_SHA",
                "TLS_KRB5_WITH_RC4_128_MD5",
                "TLS_KRB5_WITH_3DES_EDE_CBC_SHA",
                "TLS_KRB5_WITH_3DES_EDE_CBC_MD5",
                "TLS_KRB5_WITH_DES_CBC_SHA",
                "TLS_KRB5_WITH_DES_CBC_MD5",
                "TLS_KRB5_EXPORT_WITH_RC4_40_SHA",
                "TLS_KRB5_EXPORT_WITH_RC4_40_MD5",
                "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA",
                "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5"
            };

    /**
     * Create a worker verticle for the current Vert.x and return its context.
     *
     * @return the context
     * @throws Exception anything preventing the creation of the worker
     */
    protected String createWorker() throws Exception {
        CompletableFuture<String> fut = new CompletableFuture<>();
        vertx.deployVerticle(
                AbstractVerticle.class.getName(),
                new DeploymentOptions().setWorker(true),
                ar -> {
                    if (ar.failed()) {
                        fut.completeExceptionally(ar.cause());
                    } else {
                        fut.complete(ar.result());
                    }
                });
        return fut.get();
    }

    /**
     * Create worker verticles for the current Vert.x and returns the list of their contexts.
     *
     * @param num the number of verticles to router
     * @return the contexts
     * @throws Exception anything preventing the creation of the workers
     */
    protected List<String> createWorkers(int num) throws Exception {
        List<String> contexts = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            contexts.add(createWorker());
        }
        return contexts;
    }
}
