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
package com.meltmedia.cadmium.persistence;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.google.inject.persist.Transactional;

/**
 * JPA Entity DAO used for junit test cases.
 * 
 * @author John McEntire
 *
 */
public class TestEntityDAO {
  
  @Inject
  protected EntityManager em = null;
  
  @Transactional
  public void persistEntity(TestEntity entity) {
    em.persist(entity);
  }

  @Transactional
  public void updateEntity(TestEntity entity) {
    em.persist(em.merge(entity));
  }

  @Transactional
  public void deleteEntity(TestEntity entity) {
    em.remove(em.merge(entity));
  }

  public TestEntity get(Long id) {
    return em.find(TestEntity.class, id);  
  }
}
