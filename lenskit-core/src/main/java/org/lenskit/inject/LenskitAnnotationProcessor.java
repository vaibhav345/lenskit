/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.inject;

import org.grouplens.grapht.annotation.AliasFor;
import org.lenskit.inject.Parameter;
import org.lenskit.inject.Shareable;

import javax.annotation.processing.*;
import javax.inject.Qualifier;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Annotation processor to provide basic linting of LensKit annotations.
 *
 * @see Shareable
 * @see Parameter
 */
@SupportedAnnotationTypes("org.grouplens.lenskit.core.*")
public class LenskitAnnotationProcessor extends AbstractProcessor {
    public LenskitAnnotationProcessor() {}

    private Types typeUtils;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        note("LensKit Shareable linting active");
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // support version 6 or 7
        // we can't compile against RELEASE_7 and maintain Java 6 compatibility, but the
        // processor is Java 7-compatible. We have not tested against Java 8, however.
        SourceVersion[] versions = SourceVersion.values();
        SourceVersion v6 = SourceVersion.RELEASE_6;
        assert v6.ordinal() < versions.length;
        // we support up through Java 8
        return versions[Math.min(v6.ordinal() + 2, versions.length - 1)];
    }

    private Messager getLog() {
        return processingEnv.getMessager();
    }

    private void note(String fmt, Object... args) {
        return; // do nothing to avoid being noisy
//        String msg = String.format(fmt, args);
//        getLog().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private void error(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        getLog().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void warning(Element e, String fmt, Object... args) {
        String msg = String.format(fmt, args);
        if (e == null) {
            getLog().printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg);
        } else {
            getLog().printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        checkShareableComponents(roundEnv);
        checkParameters(roundEnv);
        return false;
    }

    /**
     * Check shareable components for serializability.
     * @param round The round environment.
     */
    private void checkShareableComponents(RoundEnvironment round) {
        Set<? extends Element> elts = round.getElementsAnnotatedWith(Shareable.class);
        note("processing %d shareable elements", elts.size());
        TypeMirror serializable = elementUtils.getTypeElement("java.io.Serializable").asType();
        for (Element elt: elts) {
            note("examining %s", elt);
            if (elt instanceof TypeElement) {
                TypeMirror type = elt.asType();
                if (typeUtils.isAssignable(type, serializable)) {
                    note("shareable type %s is serializable", type);
                } else {
                    warning(elt, "shareable type %s is not serializable", type);
                }
            } else {
                note("non-type element %s cannot be verified serializable", elt);
            }
        }
    }

    /**
     * Check parameter annotations for being qualifiers.
     * @param round The round environment.
     */
    private void checkParameters(RoundEnvironment round) {
        Set<? extends Element> params = round.getElementsAnnotatedWith(Parameter.class);
        note("processing %d parameter annotations", params.size());
        for (Element param: params) {
            Qualifier q = param.getAnnotation(Qualifier.class);
            if (q == null) {
                warning(param, "parameter %s is not annotated as a qualifier", param);
            }
            AliasFor alias = param.getAnnotation(AliasFor.class);
            if (alias != null) {
                warning(param, "parameter %s is an alias (@Parameter should be on target)", param);
            }
        }
    }
}
