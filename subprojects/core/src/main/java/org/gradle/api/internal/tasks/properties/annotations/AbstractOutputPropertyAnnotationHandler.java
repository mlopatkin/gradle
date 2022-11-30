/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.properties.annotations;

import org.gradle.api.tasks.Optional;
import org.gradle.internal.execution.model.annotations.ModifierAnnotationCategory;
import org.gradle.internal.properties.OutputFilePropertyType;
import org.gradle.internal.properties.PropertyValue;
import org.gradle.internal.properties.PropertyVisitor;
import org.gradle.internal.properties.annotations.AbstractPropertyAnnotationHandler;
import org.gradle.internal.properties.annotations.PropertyMetadata;

import java.lang.annotation.Annotation;

import static org.gradle.internal.execution.model.annotations.ModifierAnnotationCategory.OPTIONAL;

public abstract class AbstractOutputPropertyAnnotationHandler extends AbstractPropertyAnnotationHandler {

    public AbstractOutputPropertyAnnotationHandler(Class<? extends Annotation> annotationType) {
        super(annotationType, Kind.OUTPUT, ModifierAnnotationCategory.annotationsOf(OPTIONAL));
    }

    @Override
    public boolean isPropertyRelevant() {
        return true;
    }
}
