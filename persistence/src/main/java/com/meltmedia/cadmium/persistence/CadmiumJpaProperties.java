package com.meltmedia.cadmium.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.inject.BindingAnnotation;

/**
 * Annotation to internally bind the jpa override properties object.
 * 
 * @author John McEntire
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
@interface CadmiumJpaProperties {}
