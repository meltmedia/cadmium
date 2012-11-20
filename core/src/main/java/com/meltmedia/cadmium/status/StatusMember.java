/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.status;

import com.meltmedia.cadmium.core.lifecycle.UpdateState;

public class StatusMember {
  private String address;
  private String externalIp;
  private boolean coordinator = false;
  private boolean mine = false;
  private UpdateState state = UpdateState.IDLE;
  private UpdateState configState = UpdateState.IDLE;
  
  public StatusMember(){}
  
  public StatusMember(String address, boolean coordinator, boolean mine, UpdateState state, UpdateState configState) {
    this.address = address;
    this.coordinator = coordinator;
    this.mine = mine;
    this.state = state;
    this.configState = configState;
  }
  
  public StatusMember(String address, boolean coordinator, boolean mine) {
    this.address = address;
    this.coordinator = coordinator;
    this.mine = mine;
  }
  
  public StatusMember(String address) {
    this.address = address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setMine(boolean mine) {
    this.mine = mine;
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

  public UpdateState getConfigState() {
    return configState;
  }

  public void setConfigState(UpdateState configState) {
    this.configState = configState;
  }

  public String getExternalIp() {
    return externalIp;
  }

  public void setExternalIp(String externalIp) {
    this.externalIp = externalIp;
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

