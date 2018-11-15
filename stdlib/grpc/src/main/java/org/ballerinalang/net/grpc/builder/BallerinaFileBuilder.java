/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.google.protobuf.DescriptorProtos;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.builder.components.ClientFile;
import org.ballerinalang.net.grpc.builder.components.Descriptor;
import org.ballerinalang.net.grpc.builder.components.EnumMessage;
import org.ballerinalang.net.grpc.builder.components.Message;
import org.ballerinalang.net.grpc.builder.components.Method;
import org.ballerinalang.net.grpc.builder.components.ServiceFile;
import org.ballerinalang.net.grpc.builder.components.ServiceStub;
import org.ballerinalang.net.grpc.builder.components.StubFile;
import org.ballerinalang.net.grpc.builder.utils.BalGenConstants;
import org.ballerinalang.net.grpc.exception.BalGenerationException;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.EmptyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.DEFAULT_SAMPLE_DIR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.DEFAULT_SKELETON_DIR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.EMPTY_DATA_TYPE;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.FILE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.GRPC_CLIENT;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.GRPC_SERVICE;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PACKAGE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SAMPLE_FILE_PREFIX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SAMPLE_SERVICE_FILE_PREFIX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SAMPLE_SERVICE_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SAMPLE_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SERVICE_INDEX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.SKELETON_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.STUB_FILE_PREFIX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.TEMPLATES_DIR_PATH_KEY;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.TEMPLATES_SUFFIX;
import static org.ballerinalang.net.grpc.proto.ServiceProtoConstants.PROTO_FILE_EXTENSION;

/**
 * Class is responsible of generating the ballerina stub which is mapping proto definition.
 */
public class BallerinaFileBuilder {
    public static final Logger LOG = LoggerFactory.getLogger(BallerinaFileBuilder.class);
    private byte[] rootDescriptor;
    private List<byte[]> dependentDescriptors;
    private String balOutPath;
    
    public BallerinaFileBuilder(byte[] rootDescriptor, List<byte[]> dependentDescriptors) {
        setRootDescriptor(rootDescriptor);
        this.dependentDescriptors = dependentDescriptors;
    }
    
    public BallerinaFileBuilder(byte[] rootDescriptor, List<byte[]> dependentDescriptors, String balOutPath) {
        setRootDescriptor(rootDescriptor);
        this.dependentDescriptors = dependentDescriptors;
        this.balOutPath = balOutPath;
    }
    
    public void build(String mode) {
        try (InputStream targetStream = new ByteArrayInputStream(rootDescriptor)) {
            DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                    .parseFrom(targetStream);
            List<DescriptorProtos.DescriptorProto> messageTypeList = fileDescriptorSet.getMessageTypeList();
            List<DescriptorProtos.EnumDescriptorProto> enumDescriptorProtos = fileDescriptorSet.getEnumTypeList();
            String filename = new File(fileDescriptorSet.getName()).getName().replace(PROTO_FILE_EXTENSION, "");
            String filePackage = fileDescriptorSet.getPackage();
            StubFile stubFileObject = new StubFile(filename);
            ClientFile clientFileObject = null;
            // Add root descriptor.
            Descriptor rootDesc = Descriptor.newBuilder(rootDescriptor).build();
            stubFileObject.setRootDescriptorKey(rootDesc.getKey());
            stubFileObject.addDescriptor(rootDesc);

            // Add dependent descriptors.
            for (byte[] descriptorData : dependentDescriptors) {
                Descriptor descriptor = Descriptor.newBuilder(descriptorData).build();
                stubFileObject.addDescriptor(descriptor);
            }
            if (fileDescriptorSet.getServiceCount() > 1) {
                throw new BalGenerationException("Protobuf tool doesn't support more than one service " +
                        "definition. but provided proto file contains " + fileDescriptorSet.getServiceCount() +
                        "service definitions");
            }
            ServiceFile.Builder sampleServiceBuilder = null;
            if (fileDescriptorSet.getServiceCount() == 1) {
                DescriptorProtos.ServiceDescriptorProto serviceDescriptor = fileDescriptorSet.getService(SERVICE_INDEX);
                ServiceStub.Builder serviceBuilder = ServiceStub.newBuilder(serviceDescriptor.getName());
                sampleServiceBuilder = ServiceFile.newBuilder(serviceDescriptor.getName());
                List<DescriptorProtos.MethodDescriptorProto> methodList = serviceDescriptor.getMethodList();
                boolean isUnaryContains = false;

                for (DescriptorProtos.MethodDescriptorProto methodDescriptorProto : methodList) {
                    String methodID;
                    if (filePackage != null && !filePackage.isEmpty()) {
                        methodID = filePackage + PACKAGE_SEPARATOR + fileDescriptorSet.getService(SERVICE_INDEX).getName
                                () + "/" + methodDescriptorProto.getName();
                    } else {
                        methodID = fileDescriptorSet.getService(SERVICE_INDEX).getName() + "/" + methodDescriptorProto
                                .getName();
                    }
                    Method method = Method.newBuilder(methodID).setMethodDescriptor(methodDescriptorProto).build();
                    serviceBuilder.addMethod(method);
                    sampleServiceBuilder.addMethod(method);
                    if (MethodDescriptor.MethodType.UNARY.equals(method.getMethodType())) {
                        isUnaryContains = true;
                    }
                    if (method.containsEmptyType() && !(stubFileObject.messageExists(EMPTY_DATA_TYPE))) {
                        Message message = Message.newBuilder(EmptyMessage.newBuilder().getDescriptor().toProto())
                                .build();
                        stubFileObject.addMessage(message);
                    }
                }
                if (isUnaryContains) {
                    serviceBuilder.setType(ServiceStub.StubType.BLOCKING);
                    stubFileObject.addServiceStub(serviceBuilder.build());
                }
                serviceBuilder.setType(ServiceStub.StubType.NONBLOCKING);
                stubFileObject.addServiceStub(serviceBuilder.build());
                if (mode.equals(GRPC_CLIENT)) {
                    clientFileObject = new ClientFile(serviceDescriptor.getName(), isUnaryContains);
                }
            }
            // read message types.
            for (DescriptorProtos.DescriptorProto descriptorProto : messageTypeList) {
                Message message = Message.newBuilder(descriptorProto).build();
                stubFileObject.addMessage(message);
            }
            // read enum types.
            for (DescriptorProtos.EnumDescriptorProto descriptorProto : enumDescriptorProtos) {
                EnumMessage enumMessage = EnumMessage.newBuilder(descriptorProto).build();
                stubFileObject.addEnumMessage(enumMessage);
            }
            // write definition objects to ballerina files.
            if (this.balOutPath == null) {
                this.balOutPath = StringUtils.isNotBlank(fileDescriptorSet.getPackage()) ?
                        fileDescriptorSet.getPackage().replace(PACKAGE_SEPARATOR, FILE_SEPARATOR) : BalGenConstants
                        .DEFAULT_PACKAGE;
            }
            String stubFilePath = generateOutputFile(this.balOutPath, filename + STUB_FILE_PREFIX);
            writeOutputFile(stubFileObject, DEFAULT_SKELETON_DIR, SKELETON_TEMPLATE_NAME, stubFilePath);
            if (clientFileObject != null) {
                String clientFilePath = generateOutputFile(this.balOutPath, filename + SAMPLE_FILE_PREFIX);
                writeOutputFile(clientFileObject, DEFAULT_SAMPLE_DIR, SAMPLE_TEMPLATE_NAME, clientFilePath);
            }
            if (mode.equals(GRPC_SERVICE) && fileDescriptorSet.getServiceCount() != 0) {
                String servicePath = generateOutputFile(this.balOutPath, filename + SAMPLE_SERVICE_FILE_PREFIX);
                writeOutputFile(sampleServiceBuilder.build(), DEFAULT_SAMPLE_DIR, SAMPLE_SERVICE_TEMPLATE_NAME,
                        servicePath);
            }
        } catch (IOException | GrpcServerException e) {
            throw new BalGenerationException("Error while generating .bal file.", e);
        }
    }
    
    private String generateOutputFile(String outputDir, String fileName) throws IOException {
        if (outputDir != null) {
            Files.createDirectories(Paths.get(outputDir));
        }
        File file = new File(outputDir + FILE_SEPARATOR + fileName);
        if (!file.isFile()) {
            Files.createFile(Paths.get(file.getAbsolutePath()));
        }
        return file.getAbsolutePath();
    }

    /**
     * Write ballerina definition of a <code>object</code> to a file as described by <code>template.</code>
     *
     * @param object       Context object to be used by the template parser
     * @param templateDir  Directory with all the templates required for generating the source file
     * @param templateName Name of the parent template to be used
     * @param outPath      Destination path for writing the resulting source file
     * @throws IOException when file operations fail
     */
    private static void writeOutputFile(Object object, String templateDir, String templateName, String outPath)
            throws IOException {
        PrintWriter writer = null;
        try {
            Template template = compileTemplate(templateDir, templateName);
            Context context = Context.newBuilder(object).resolver(
                    MapValueResolver.INSTANCE,
                    JavaBeanValueResolver.INSTANCE,
                    FieldValueResolver.INSTANCE).build();
            writer = new PrintWriter(outPath, StandardCharsets.UTF_8.name());
            writer.println(template.apply(context));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static Template compileTemplate(String defaultTemplateDir, String templateName) throws IOException {
        String templatesDirPath = System.getProperty(TEMPLATES_DIR_PATH_KEY, defaultTemplateDir);
        ClassPathTemplateLoader cpTemplateLoader = new ClassPathTemplateLoader((templatesDirPath));
        FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(templatesDirPath);
        cpTemplateLoader.setSuffix(TEMPLATES_SUFFIX);
        fileTemplateLoader.setSuffix(TEMPLATES_SUFFIX);
        // add handlebars with helpers.
        Handlebars handlebars = new Handlebars().with(cpTemplateLoader, fileTemplateLoader);
        handlebars.registerHelpers(StringHelpers.class);
        handlebars.registerHelper("equals", (object, options) -> {
            CharSequence result;
            Object param0 = options.param(0);

            if (param0 == null) {
                throw new IllegalArgumentException("found n'null', expected 'string'");
            }
            if (object != null && object.toString().equals(param0.toString())) {
                result = options.fn(options.context);
            } else {
                result = null;
            }

            return result;
        });
        handlebars.registerHelper("not_equal", (object, options) -> {
            CharSequence result;
            Object param0 = options.param(0);

            if (param0 == null) {
                throw new IllegalArgumentException("found n'null', expected 'string'");
            }
            if (object == null || !object.toString().equals(param0.toString())) {
                result = options.fn(options.context);
            } else {
                result = null;
            }

            return result;
        });
        return handlebars.compile(templateName);
    }
    
    private void setRootDescriptor(byte[] rootDescriptor) {
        this.rootDescriptor = new byte[rootDescriptor.length];
        this.rootDescriptor = Arrays.copyOf(rootDescriptor, rootDescriptor.length);
    }
}
