package com.meltmedia.cadmium.core;

import java.lang.annotation.*;

/**
 * Annotation for servlet filters that need to be automatically bound with guice.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CadmiumFilter {
  /**
   * path pattern for filter mapping.
   * @return
   */
  String value();
}
