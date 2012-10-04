package com.meltmedia.cadmium.core.commands;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An abstract base class for command message bodies.  This class provides reflection based implementations for equals, hashCode, and toString,
 * based on the org.apache.commons.lang3.builder package.
 * 
 * @author Christian Trimble
 */
public abstract class AbstractMessageBody {

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
