/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.installer.factory.model.impl;

import org.apache.sling.feature.*;
import org.apache.sling.feature.builder.ArtifactProvider;
import org.apache.sling.feature.spi.context.ExtensionHandlerContext;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class APIRegionsExtensionHandlerTest {
    private static String FEATURE_EXT_1 =
            "[" +
            "   {" +
            "     \"name\": \"my-region\"," +
            "     \"exports\": [\"org.foo.bar\", \"la.di.da\"]" +
            "   }" +
            "]";

    @Test
    public void testHandle() throws Exception {
        APIRegionsExtensionHandler areh = new APIRegionsExtensionHandler();
        ArtifactId bundleId = ArtifactId.fromMvnId("my:my-bundle:1.0.0");

        ArtifactProvider artifactProvider = Mockito.mock(ArtifactProvider.class);
        Mockito.when(artifactProvider.provide(bundleId)).thenReturn(getClass().getResource("/test1/test1.jar"));
        ExtensionHandlerContext ctx = Mockito.mock(ExtensionHandlerContext.class);
        Mockito.when(ctx.getArtifactProvider()).thenReturn(artifactProvider);

        Extension ext = new Extension(ExtensionType.JSON, "api-regions", ExtensionState.REQUIRED);
        ext.setJSON(FEATURE_EXT_1);

        Feature feat = new Feature(ArtifactId.fromMvnId("x:y:8"));
        feat.getBundles().add(new Artifact(bundleId));

        assertTrue(areh.handle(ctx, ext, feat));

        Mockito.verify(ctx).addConfiguration(Mockito.isNull(),
            Mockito.eq("org.apache.sling.feature.apiregions.factory~y_8.jar"),
            Mockito.argThat(p -> {
                String[] pkgs = (String[]) p.get("mapping.region.packages");
                String[] bundleFeatures = (String[]) p.get("mapping.bundleid.features");
                return Arrays.deepEquals(new String [] {"my-region=org.foo.bar,la.di.da"}, pkgs)
                    && Arrays.deepEquals(new String[] {"my:my-bundle:1.0.0="}, bundleFeatures );
            }));
    }

    @Test
    public void testDoesNotHandle() throws Exception {
        APIRegionsExtensionHandler areh = new APIRegionsExtensionHandler();

        Extension ext = new Extension(ExtensionType.TEXT, "api-regions", ExtensionState.REQUIRED);
        assertFalse(areh.handle(null, ext, null));
    }
}
