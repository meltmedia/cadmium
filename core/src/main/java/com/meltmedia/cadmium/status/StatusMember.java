package com.meltmedia.cadmium.status;

import org.jgroups.Address;

import com.meltmedia.cadmium.core.lifecycle.UpdateState;

public class StatusMember {
  private String address;
  private boolean coordinator = false;
  private boolean mine = false;
  private UpdateState state = UpdateState.IDLE;
  
  public StatusMember(String address, boolean coordinator, boolean mine, UpdateState state) {
    this.address = address;
    this.coordinator = coordinator;
    this.mine = mine;
    this.state = state;
  }
  
  public StatusMember(String address, boolean coordinator, boolean mine) {
    this.address = address;
    this.coordinator = coordinator;
    this.mine = mine;
  }
  
  public StatusMember(String address) {
    this.address = address;
  }

  public String getAddress() {
    return address;
  }

  public boolean isCoordinator() {
    return coordinator;
  }

  public void setCoordinator(boolean coordinator) {
    this.coordinator = coordinator;
  }

  public boolean isMine() {
    return mine;
  }

  public UpdateState getState() {
    return state;
  }

  public void setState(UpdateState state) {
    this.state = state;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.toString().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StatusMember other = (StatusMember) obj;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (!address.toString().equals(other.address.toString()))
      return false;
    return true;
  }
  
}

