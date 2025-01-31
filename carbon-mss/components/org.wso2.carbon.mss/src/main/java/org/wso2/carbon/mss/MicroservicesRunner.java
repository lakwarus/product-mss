/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.internal.MSSNettyServerInitializer;
import org.wso2.carbon.mss.internal.MicroservicesRegistry;
import org.wso2.carbon.mss.internal.router.HandlerHook;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportDataHolder;
import org.wso2.carbon.transport.http.netty.internal.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.config.TransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.internal.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.listener.NettyListener;
import org.wso2.carbon.transports.TransportManager;

import java.util.Set;

/**
 * TODO: class level comment
 */
public class MicroservicesRunner {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    private TransportManager transportManager = new TransportManager();
    private MSSNettyServerInitializer serverInitializer;
    private long startTime = System.currentTimeMillis();

    public MicroservicesRunner() {

        TransportsConfiguration trpConfig = TransportConfigurationBuilder.build();
        Set<ListenerConfiguration> listenerConfigurations = trpConfig.getListenerConfigurations();
        for (ListenerConfiguration listenerConfiguration : listenerConfigurations) {
            NettyListener listener = new NettyListener(listenerConfiguration);
            transportManager.registerTransport(listener);
        }

        NettyTransportDataHolder nettyTransportDataHolder = NettyTransportDataHolder.getInstance();
        serverInitializer = new MSSNettyServerInitializer();
        nettyTransportDataHolder.
                addNettyChannelInitializer(ListenerConfiguration.DEFAULT_KEY, serverInitializer);
    }

    public MicroservicesRunner deploy(Object microservice) {
        MicroservicesRegistry.getInstance().addHttpService(microservice);
        return this;
    }

    public MicroservicesRunner addHook(HandlerHook hook) {
        serverInitializer.addHandlerHook(hook);
        return this;
    }

    public void start() {
        transportManager.startTransports();
        log.info("Microservices server started in " + (System.currentTimeMillis() - startTime) + "ms");
    }
}
