/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.dataset;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.camel.BindToRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.junit4.TestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileDataSetProducerWithSplitTest extends CamelTestSupport {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BindToRegistry("foo")
    protected FileDataSet dataSet;

    final String testDataFileName = "src/test/data/file-dataset-test.txt";
    final int testDataFileRecordCount = 10;

    final String sourceUri = "direct://source";
    final String dataSetName = "foo";
    final String dataSetUri = "dataset://" + dataSetName;

    @Test
    public void testDefaultListDataSet() throws Exception {
        for (int i = 0; i < testDataFileRecordCount; ++i) {
            template.sendBodyAndHeader(sourceUri, "Line " + (1 + i), Exchange.DATASET_INDEX, i);
        }

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testDefaultListDataSetWithSizeGreaterThanListSize() throws Exception {
        int messageCount = 20;
        dataSet.setSize(messageCount);

        getMockEndpoint(dataSetUri).expectedMessageCount(messageCount);

        for (int i = 0; i < messageCount; ++i) {
            template.sendBodyAndHeader(sourceUri, "Line " + (1 + (i % testDataFileRecordCount)), Exchange.DATASET_INDEX, i);
        }

        assertMockEndpointsSatisfied();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        File fileDataset = createFileDatasetWithSystemEndOfLine();
        dataSet = new FileDataSet(fileDataset, TestSupport.LS);
        assertEquals("Unexpected DataSet size", testDataFileRecordCount, dataSet.getSize());
        super.setUp();
    }

    private File createFileDatasetWithSystemEndOfLine() throws IOException {
        tempFolder.create();
        File fileDataset = tempFolder.newFile("file-dataset-test.txt");
        ByteArrayInputStream content = new ByteArrayInputStream(String.format("Line 1%nLine 2%nLine 3%nLine 4%nLine 5%nLine 6%nLine 7%nLine 8%nLine 9%nLine 10%n").getBytes());
        Files.copy(content, fileDataset.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return fileDataset;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(sourceUri)
                        .to(dataSetUri);
            }
        };
    }
}
