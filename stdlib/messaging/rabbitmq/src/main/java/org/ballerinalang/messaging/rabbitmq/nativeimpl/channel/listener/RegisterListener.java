/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq.nativeimpl.channel.listener;

import com.rabbitmq.client.Channel;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.util.ChannelUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.ArrayList;

/**
 * Binds the ChannelListener to a service.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "registerListener",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.LISTENER_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ)
)
public class RegisterListener extends BlockingNativeCallableUnit {
    private static final ArrayList<ObjectValue> services = new ArrayList<>();

    @Override
    public void execute(Context context) {
    }

    public static void registerListener(Strand strand, ObjectValue listenerObjectValue, ObjectValue service) {
        ObjectValue channelObject = (ObjectValue) listenerObjectValue.get(RabbitMQConstants.CHANNEL_REFERENCE);
        Channel channel = (Channel) channelObject.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        MapValue serviceConfig = (MapValue) service.getType().getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ,
                RabbitMQConstants.SERVICE_CONFIG);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        MapValue<Strand, Object> queueConfig =
                (MapValue) serviceConfig.getMapValue(RabbitMQConstants.ALIAS_QUEUE_CONFIG);
        String queueName = queueConfig.getStringValue(RabbitMQConstants.ALIAS_QUEUE_NAME);
        boolean durable = queueConfig.getBooleanValue(RabbitMQConstants.ALIAS_QUEUE_DURABLE);
        boolean exclusive = queueConfig.getBooleanValue(RabbitMQConstants.ALIAS_QUEUE_EXCLUSIVE);
        boolean autoDelete = queueConfig.getBooleanValue(RabbitMQConstants.ALIAS_QUEUE_AUTODELETE);
        ChannelUtils.queueDeclare(channel, queueName, durable, exclusive, autoDelete);
        services.add(service);
        listenerObjectValue.addNativeData(RabbitMQConstants.CONSUMER_SERVICES, services);
    }
}
