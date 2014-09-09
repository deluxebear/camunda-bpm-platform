/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.api.filter;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Sebastian Menski
 */
public class FilterAuthorizationsTest extends PluggableProcessEngineTestCase {

  protected User testUser;

  protected Authorization createAuthorization;
  protected Authorization updateAuthorization;
  protected Authorization readAuthorization;
  protected Authorization deleteAuthorization;

  public void setUp() {
    testUser = createTestUser("test");

    createAuthorization = createAuthorization(Permissions.CREATE, Authorization.ANY);
    updateAuthorization = createAuthorization(Permissions.UPDATE, null);
    readAuthorization = createAuthorization(Permissions.READ, null);
    deleteAuthorization = createAuthorization(Permissions.DELETE, null);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(testUser.getId());
  }

  public void tearDown() {
    processEngineConfiguration.setAuthorizationEnabled(false);
    for (Filter filter : filterService.createFilterQuery().list()) {
      filterService.deleteFilter(filter.getId());
    }
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

  public void testCreateFilterNotPermitted() {
    try {
      filterService.newFilter();
      fail("Exception expected");
    }
    catch (AuthorizationException e) {
      // expected
    }
  }

  public void testCreateFilterPermitted() {
    grantCreateFilter();
    Filter filter = filterService.newFilter();
    assertNotNull(filter);
  }

  public void testSaveFilterNotPermitted() {
    Filter filter = new FilterEntity();
    try {
      filterService.saveFilter(filter);
      fail("Exception expected");
    }
    catch (AuthorizationException e) {
      // expected
    }
  }

  public void testSaveFilterPermitted() {
    Filter filter = new FilterEntity()
      .setResourceType(EntityTypes.TASK)
      .setName("testFilter");

    grantCreateFilter();

    filterService.saveFilter(filter);

    assertNotNull(filter.getId());
  }

  public void testUpdateFilterNotPermitted() {
    Filter filter = createTestFilter();

    filter.setName("anotherName");

    try {
      filterService.saveFilter(filter);
      fail("Exception expected");
    }
    catch (AuthorizationException e) {
      // expected
    }
  }

  public void testUpdateFilterPermitted() {
    Filter filter = createTestFilter();

    filter.setName("anotherName");

    grantUpdateFilter(filter.getId());

    filter = filterService.saveFilter(filter);
    assertEquals("anotherName", filter.getName());
  }

  public void testDeleteFilterNotPermitted() {
    Filter filter = createTestFilter();

    try {
      filterService.deleteFilter(filter.getId());
      fail("Exception expected");
    }
    catch (AuthorizationException e) {
      // expected
    }
  }

  public void testDeleteFilterPermitted() {
    Filter filter = createTestFilter();

    grantDeleteFilter(filter.getId());

    filterService.deleteFilter(filter.getId());

    long count = filterService.createFilterQuery().count();
    assertEquals(0, count);
  }

  public void testReadFilterNotPermitted() {
    Filter filter = createTestFilter();

    long count = filterService.createFilterQuery().count();
    assertEquals(0, count);

    Filter returnedFilter = filterService.createFilterQuery().filterId(filter.getId()).singleResult();
    assertNull(returnedFilter);

    try {
      filterService.getFilter(filter.getId());
      fail("Exception expected");
    }
    catch (AuthorizationException e) {
      // expected
    }

    try {
      filterService.singleResult(filter.getId());
      fail("Exception expected");
    }
    catch (AuthorizationException e) {
      // expected
    }

    try {
      filterService.list(filter.getId());
      fail("Exception expected");
    }
    catch (AuthorizationException e) {
      // expected
    }

    try {
      filterService.listPage(filter.getId(), 1, 2);
      fail("Exception expected");
    }
    catch (AuthorizationException e) {
      // expected
    }

    try {
      filterService.count(filter.getId());
      fail("Exception expected");
    }
    catch (AuthorizationException e) {
      // expected
    }
  }

  public void testReadFilterPermitted() {
    Filter filter = createTestFilter();

    grantReadFilter(filter.getId());

    long count = filterService.createFilterQuery().count();
    assertEquals(1, count);

    Filter returnedFilter = filterService.createFilterQuery().filterId(filter.getId()).singleResult();
    assertNotNull(returnedFilter);

    returnedFilter = filterService.getFilter(filter.getId());
    assertNotNull(returnedFilter);

    // create test Task
    Task task = taskService.newTask("test");
    taskService.saveTask(task);

    Task result = filterService.singleResult(filter.getId());
    assertNotNull(result);
    assertEquals(task.getId(), result.getId());

    List<Task> resultList = filterService.list(filter.getId());
    assertNotNull(resultList);
    assertEquals(1, resultList.size());
    assertEquals(task.getId(), resultList.get(0).getId());

    resultList = filterService.listPage(filter.getId(), 0, 2);
    assertNotNull(resultList);
    assertEquals(1, resultList.size());
    assertEquals(task.getId(), resultList.get(0).getId());

    count = filterService.count(filter.getId());
    assertEquals(1, count);

    // remove Task
    taskService.deleteTask(task.getId(), true);
  }

  public void testDefaultFilterAuthorization() {
    // create two other users beside testUser
    User ownerUser = createTestUser("ownerUser");
    User anotherUser = createTestUser("anotherUser");

    // grant testUser create permission
    grantCreateFilter();

    // create a new filter with ownerUser as owner
    Filter filter = filterService.newTaskFilter("testFilter");
    filter.setOwner(ownerUser.getId());
    filterService.saveFilter(filter);

    assertFilterPermission(Permissions.CREATE, testUser, null, true);
    assertFilterPermission(Permissions.CREATE, ownerUser, null, false);
    assertFilterPermission(Permissions.CREATE, anotherUser, null, false);

    assertFilterPermission(Permissions.UPDATE, testUser, filter.getId(), false);
    assertFilterPermission(Permissions.UPDATE, ownerUser, filter.getId(), true);
    assertFilterPermission(Permissions.UPDATE, anotherUser, filter.getId(), false);

    assertFilterPermission(Permissions.READ, testUser, filter.getId(), false);
    assertFilterPermission(Permissions.READ, ownerUser, filter.getId(), true);
    assertFilterPermission(Permissions.READ, anotherUser, filter.getId(), false);

    assertFilterPermission(Permissions.DELETE, testUser, filter.getId(), false);
    assertFilterPermission(Permissions.DELETE, ownerUser, filter.getId(), true);
    assertFilterPermission(Permissions.DELETE, anotherUser, filter.getId(), false);
  }

  protected User createTestUser(String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);

    // give user all permission to manipulate authorisations
    Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setUserId(user.getId());
    authorization.setResource(Resources.AUTHORIZATION);
    authorization.setResourceId(Authorization.ANY);
    authorization.addPermission(Permissions.ALL);
    authorizationService.saveAuthorization(authorization);

    // give user all permission to manipulate users
    authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setUserId(user.getId());
    authorization.setResource(Resources.USER);
    authorization.setResourceId(Authorization.ANY);
    authorization.addPermission(Permissions.ALL);
    authorizationService.saveAuthorization(authorization);

    return user;
  }

  protected Filter createTestFilter() {
    grantCreateFilter();
    Filter filter = filterService.newTaskFilter("testFilter");
    return filterService.saveFilter(filter);
  }

  protected Authorization createAuthorization(Permission permission, String resourceId) {
    Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setUserId(testUser.getId());
    authorization.setResource(Resources.FILTER);
    authorization.addPermission(permission);
    if (resourceId != null) {
      authorization.setResourceId(resourceId);
    }
    return authorization;
  }

  protected void grantCreateFilter() {
    grantFilterPermission(createAuthorization, null);
    assertFilterPermission(Permissions.CREATE, testUser, null, true);
  }

  protected void grantUpdateFilter(String filterId) {
    grantFilterPermission(updateAuthorization, filterId);
    assertFilterPermission(Permissions.UPDATE, testUser, filterId, true);
  }

  protected void grantReadFilter(String filterId) {
    grantFilterPermission(readAuthorization, filterId);
    assertFilterPermission(Permissions.READ, testUser, filterId, true);
  }

  protected void grantDeleteFilter(String filterId) {
    grantFilterPermission(deleteAuthorization, filterId);
    assertFilterPermission(Permissions.DELETE, testUser, filterId, true);
  }

  protected void grantFilterPermission(Authorization authorization, String filterId) {
    if (filterId != null) {
      authorization.setResourceId(filterId);
    }
    authorizationService.saveAuthorization(authorization);
  }

  protected void assertFilterPermission(Permission permission, User user, String filterId, boolean expected) {
    boolean result;
    if (filterId != null) {
      result = authorizationService.isUserAuthorized(user.getId(), null, permission, Resources.FILTER, filterId);
    }
    else {
      result = authorizationService.isUserAuthorized(user.getId(), null, permission, Resources.FILTER);
    }
    assertEquals(expected, result);
  }

}
