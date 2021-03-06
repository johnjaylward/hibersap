/*
 * Copyright (c) 2008-2017 akquinet tech@spree GmbH
 *
 * This file is part of Hibersap.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibersap.mapping;

import java.util.Set;
import org.hibersap.annotations.Bapi;
import org.hibersap.annotations.Import;
import org.hibersap.annotations.Parameter;
import org.hibersap.annotations.Table;
import org.hibersap.mapping.model.BapiMapping;
import org.hibersap.mapping.model.ParameterMapping;
import org.hibersap.mapping.model.TableMapping;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

public class AnnotationBapiMapperTest {

    private final AnnotationBapiMapper mapper = new AnnotationBapiMapper();

    @Test
    public void mapsBapiStructureWithInheritedField() {
        final BapiMapping mapping = mapper.mapBapi(TestBapiClass.class);

        final TableMapping tableMapping = mapping.getTableParameters().iterator().next();
        final Set<ParameterMapping> parameters = tableMapping.getComponentParameter().getParameters();

        assertThat(parameters).hasSize(2)
                .onProperty("javaName").contains("structureParamSubClass", "structureParamSuperClass");
    }

    @Test
    public void mapsTable() {
        final BapiMapping mapping = mapper.mapBapi(TestBapiClass.class);

        final Set<TableMapping> tableParams = mapping.getTableParameters();
        assertThat(tableParams).hasSize(1);

        final TableMapping tableMapping = tableParams.iterator().next();
        assertThat(tableMapping.getAssociatedType().getName()).isEqualTo(TestBapiStructureSubClass.class.getName());
    }

    @Bapi("test")
    private class TestBapiClass {

        @Import
        @Parameter("ABAP_FIELD")
        @SuppressWarnings("unused")
        private int intParam;

        @Table
        @Parameter("ABAP_TABLE")
        @SuppressWarnings("unused")
        private Set<TestBapiStructureSubClass> tableParam;
    }

    private class TestBapiStructureSubClass extends TestBapiStructureSuperClass {

        @Parameter("ABAP_FIELD")
        @SuppressWarnings("unused")
        private long structureParamSubClass;
    }

    private class TestBapiStructureSuperClass {

        @Parameter("ABAP_FIELD")
        @SuppressWarnings("unused")
        private String structureParamSuperClass;
    }
}
