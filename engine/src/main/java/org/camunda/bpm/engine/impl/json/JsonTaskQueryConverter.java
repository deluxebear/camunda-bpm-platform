/*
 * Copyright © 2012 - 2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.JsonMapper;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.TaskQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.impl.util.JsonMapper.addArrayField;
import static org.camunda.bpm.engine.impl.util.JsonMapper.addDateField;
import static org.camunda.bpm.engine.impl.util.JsonMapper.addDefaultField;
import static org.camunda.bpm.engine.impl.util.JsonMapper.addElement;
import static org.camunda.bpm.engine.impl.util.JsonMapper.addField;
import static org.camunda.bpm.engine.impl.util.JsonMapper.addListField;

/**
 * @author Sebastian Menski
 */
public class JsonTaskQueryConverter extends JsonObjectConverter<TaskQuery> {

  public static final String ID = "id";
  public static final String TASK_ID = "taskId";
  public static final String NAME = "name";
  public static final String NAME_NOT_EQUAL = "nameNotEqual";
  public static final String NAME_LIKE = "nameLike";
  public static final String NAME_NOT_LIKE = "nameNotLike";
  public static final String DESCRIPTION = "description";
  public static final String DESCRIPTION_LIKE = "descriptionLike";
  public static final String PRIORITY = "priority";
  public static final String MIN_PRIORITY = "minPriority";
  public static final String MAX_PRIORITY = "maxPriority";
  public static final String ASSIGNEE = "assignee";
  public static final String ASSIGNEE_LIKE = "assigneeLike";
  public static final String INVOLVED_USER = "involvedUser";
  public static final String OWNER = "owner";
  public static final String UNASSIGNED = "unassigned";
  public static final String ASSIGNED = "assigned";
  public static final String DELEGATION_STATE = "delegationState";
  public static final String CANDIDATE_USER = "candidateUser";
  public static final String CANDIDATE_GROUP = "candidateGroup";
  public static final String CANDIDATE_GROUPS = "candidateGroups";
  public static final String WITH_CANDIDATE_GROUPS = "withCandidateGroups";
  public static final String WITHOUT_CANDIDATE_GROUPS = "withoutCandidateGroups";
  public static final String WITH_CANDIDATE_USERS = "withCandidateUsers";
  public static final String WITHOUT_CANDIDATE_USERS = "withoutCandidateUsers";
  public static final String INCLUDE_ASSIGNED_TASKS = "includeAssignedTasks";
  public static final String INSTANCE_ID = "instanceId";
  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  public static final String EXECUTION_ID = "executionId";
  public static final String ACTIVITY_INSTANCE_ID_IN = "activityInstanceIdIn";
  public static final String CREATED = "created";
  public static final String CREATED_BEFORE = "createdBefore";
  public static final String CREATED_AFTER = "createdAfter";
  public static final String KEY = "key";
  public static final String KEYS = "keys";
  public static final String KEY_LIKE = "keyLike";
  public static final String PARENT_TASK_ID = "parentTaskId";
  public static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
  public static final String PROCESS_DEFINITION_KEYS = "processDefinitionKeys";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
  public static final String PROCESS_DEFINITION_NAME = "processDefinitionName";
  public static final String PROCESS_DEFINITION_NAME_LIKE = "processDefinitionNameLike";
  public static final String PROCESS_INSTANCE_BUSINESS_KEY = "processInstanceBusinessKey";
  public static final String PROCESS_INSTANCE_BUSINESS_KEYS ="processInstanceBusinessKeys";
  public static final String PROCESS_INSTANCE_BUSINESS_KEY_LIKE = "processInstanceBusinessKeyLike";
  public static final String DUE = "due";
  public static final String DUE_DATE = "dueDate";
  public static final String DUE_BEFORE = "dueBefore";
  public static final String DUE_AFTER = "dueAfter";
  public static final String FOLLOW_UP = "followUp";
  public static final String FOLLOW_UP_DATE = "followUpDate";
  public static final String FOLLOW_UP_BEFORE = "followUpBefore";
  public static final String FOLLOW_UP_NULL_ACCEPTED = "followUpNullAccepted";
  public static final String FOLLOW_UP_AFTER = "followUpAfter";
  public static final String EXCLUDE_SUBTASKS = "excludeSubtasks";
  public static final String CASE_DEFINITION_KEY = "caseDefinitionKey";
  public static final String CASE_DEFINITION_ID = "caseDefinitionId";
  public static final String CASE_DEFINITION_NAME = "caseDefinitionName";
  public static final String CASE_DEFINITION_NAME_LIKE = "caseDefinitionNameLike";
  public static final String CASE_INSTANCE_ID = "caseInstanceId";
  public static final String CASE_INSTANCE_BUSINESS_KEY = "caseInstanceBusinessKey";
  public static final String CASE_INSTANCE_BUSINESS_KEY_LIKE = "caseInstanceBusinessKeyLike";
  public static final String CASE_EXECUTION_ID = "caseExecutionId";
  public static final String ACTIVE = "active";
  public static final String SUSPENDED = "suspended";
  public static final String PROCESS_VARIABLES = "processVariables";
  public static final String TASK_VARIABLES = "taskVariables";
  public static final String CASE_INSTANCE_VARIABLES = "caseInstanceVariables";
  public static final String TENANT_IDS = "tenantIds";
  public static final String WITHOUT_TENANT_ID = "withoutTenantId";
  public static final String ORDERING_PROPERTIES = "orderingProperties";
  public static final String OR_QUERIES = "orQueries";

  /**
   * Exists for backwards compatibility with 7.2; deprecated since 7.3
   */
  @Deprecated
  public static final String ORDER_BY = "orderBy";

  protected static JsonTaskQueryVariableValueConverter variableValueConverter = new JsonTaskQueryVariableValueConverter();

  @Override
  public JsonObject toJsonObject(TaskQuery taskQuery) {
    return toJsonObject(taskQuery, false);
  }

  public JsonObject toJsonObject(TaskQuery taskQuery, boolean isOrQueryActive) {
    JsonObject jsonObject = JsonMapper.createObjectNode();
    TaskQueryImpl query = (TaskQueryImpl) taskQuery;

    addField(jsonObject, TASK_ID, query.getTaskId());
    addField(jsonObject, NAME, query.getName());
    addField(jsonObject, NAME_NOT_EQUAL, query.getNameNotEqual());
    addField(jsonObject, NAME_LIKE, query.getNameLike());
    addField(jsonObject, NAME_NOT_LIKE, query.getNameNotLike());
    addField(jsonObject, DESCRIPTION, query.getDescription());
    addField(jsonObject, DESCRIPTION_LIKE, query.getDescriptionLike());
    addField(jsonObject, PRIORITY, query.getPriority());
    addField(jsonObject, MIN_PRIORITY, query.getMinPriority());
    addField(jsonObject, MAX_PRIORITY, query.getMaxPriority());
    addField(jsonObject, ASSIGNEE, query.getAssignee());
    addField(jsonObject, ASSIGNEE_LIKE, query.getAssigneeLike());
    addField(jsonObject, INVOLVED_USER, query.getInvolvedUser());
    addField(jsonObject, OWNER, query.getOwner());
    addDefaultField(jsonObject, UNASSIGNED, false, query.isUnassigned());
    addDefaultField(jsonObject, ASSIGNED, false, query.isAssigned());
    addField(jsonObject, DELEGATION_STATE, query.getDelegationStateString());
    addField(jsonObject, CANDIDATE_USER, query.getCandidateUser());
    addField(jsonObject, CANDIDATE_GROUP, query.getCandidateGroup());
    addListField(jsonObject, CANDIDATE_GROUPS, query.getCandidateGroupsInternal());
    addDefaultField(jsonObject, WITH_CANDIDATE_GROUPS, false, query.isWithCandidateGroups());
    addDefaultField(jsonObject, WITHOUT_CANDIDATE_GROUPS, false, query.isWithoutCandidateGroups());
    addDefaultField(jsonObject, WITH_CANDIDATE_USERS, false, query.isWithCandidateUsers());
    addDefaultField(jsonObject, WITHOUT_CANDIDATE_USERS, false, query.isWithoutCandidateUsers());
    addField(jsonObject, INCLUDE_ASSIGNED_TASKS, query.isIncludeAssignedTasksInternal());
    addField(jsonObject, PROCESS_INSTANCE_ID, query.getProcessInstanceId());
    addField(jsonObject, EXECUTION_ID, query.getExecutionId());
    addArrayField(jsonObject, ACTIVITY_INSTANCE_ID_IN, query.getActivityInstanceIdIn());
    addDateField(jsonObject, CREATED, query.getCreateTime());
    addDateField(jsonObject, CREATED_BEFORE, query.getCreateTimeBefore());
    addDateField(jsonObject, CREATED_AFTER, query.getCreateTimeAfter());
    addField(jsonObject, KEY, query.getKey());
    addArrayField(jsonObject, KEYS, query.getKeys());
    addField(jsonObject, KEY_LIKE, query.getKeyLike());
    addField(jsonObject, PARENT_TASK_ID, query.getParentTaskId());
    addField(jsonObject, PROCESS_DEFINITION_KEY, query.getProcessDefinitionKey());
    addArrayField(jsonObject, PROCESS_DEFINITION_KEYS, query.getProcessDefinitionKeys());
    addField(jsonObject, PROCESS_DEFINITION_ID, query.getProcessDefinitionId());
    addField(jsonObject, PROCESS_DEFINITION_NAME, query.getProcessDefinitionName());
    addField(jsonObject, PROCESS_DEFINITION_NAME_LIKE, query.getProcessDefinitionNameLike());
    addField(jsonObject, PROCESS_INSTANCE_BUSINESS_KEY, query.getProcessInstanceBusinessKey());
    addArrayField(jsonObject, PROCESS_INSTANCE_BUSINESS_KEYS, query.getProcessInstanceBusinessKeys());
    addField(jsonObject, PROCESS_INSTANCE_BUSINESS_KEY_LIKE, query.getProcessInstanceBusinessKeyLike());
    addVariablesFields(jsonObject, query.getVariables());
    addDateField(jsonObject, DUE, query.getDueDate());
    addDateField(jsonObject, DUE_BEFORE, query.getDueBefore());
    addDateField(jsonObject, DUE_AFTER, query.getDueAfter());
    addDateField(jsonObject, FOLLOW_UP, query.getFollowUpDate());
    addDateField(jsonObject, FOLLOW_UP_BEFORE, query.getFollowUpBefore());
    addDefaultField(jsonObject, FOLLOW_UP_NULL_ACCEPTED, false, query.isFollowUpNullAccepted());
    addDateField(jsonObject, FOLLOW_UP_AFTER, query.getFollowUpAfter());
    addDefaultField(jsonObject, EXCLUDE_SUBTASKS, false, query.isExcludeSubtasks());
    addSuspensionStateField(jsonObject, query.getSuspensionState());
    addField(jsonObject, CASE_DEFINITION_KEY, query.getCaseDefinitionKey());
    addField(jsonObject, CASE_DEFINITION_ID, query.getCaseDefinitionId());
    addField(jsonObject, CASE_DEFINITION_NAME, query.getCaseDefinitionName());
    addField(jsonObject, CASE_DEFINITION_NAME_LIKE, query.getCaseDefinitionNameLike());
    addField(jsonObject, CASE_INSTANCE_ID, query.getCaseInstanceId());
    addField(jsonObject, CASE_INSTANCE_BUSINESS_KEY, query.getCaseInstanceBusinessKey());
    addField(jsonObject, CASE_INSTANCE_BUSINESS_KEY_LIKE, query.getCaseInstanceBusinessKeyLike());
    addField(jsonObject, CASE_EXECUTION_ID, query.getCaseExecutionId());
    addTenantIdFields(jsonObject, query);

    if (query.getQueries().size() > 1 && !isOrQueryActive) {
      JsonArray orQueries = JsonMapper.createArrayNode();

      for (TaskQueryImpl orQuery: query.getQueries()) {
        if (orQuery != null && orQuery.isOrQueryActive()) {
          orQueries.add(toJsonObject(orQuery, true));
        }
      }

      addField(jsonObject, OR_QUERIES, orQueries);
    }

    if (query.getOrderingProperties() != null && !query.getOrderingProperties().isEmpty()) {
      addField(jsonObject, ORDERING_PROPERTIES,
          JsonQueryOrderingPropertyConverter.ARRAY_CONVERTER.toJsonArray(query.getOrderingProperties()));
    }


    // expressions
    for (Map.Entry<String, String> expressionEntry : query.getExpressions().entrySet()) {
      addField(jsonObject, expressionEntry.getKey() + "Expression", expressionEntry.getValue());
    }

    return jsonObject;
  }

  protected void addSuspensionStateField(JsonObject jsonObject, SuspensionState suspensionState) {
    if (suspensionState != null) {
      if (suspensionState.equals(SuspensionState.ACTIVE)) {
        addField(jsonObject, ACTIVE, false);
      }
      else if (suspensionState.equals(SuspensionState.SUSPENDED)) {
        addField(jsonObject, SUSPENDED, true);
      }
    }
  }

  protected void addTenantIdFields(JsonObject jsonObject, TaskQueryImpl query) {
    if (query.isTenantIdSet()) {
      if (query.getTenantIds() != null) {
        addArrayField(jsonObject, TENANT_IDS, query.getTenantIds());
      } else {
        addField(jsonObject, WITHOUT_TENANT_ID, true);
      }
    }
  }

  protected void addVariablesFields(JsonObject jsonObject, List<TaskQueryVariableValue> variables) {
    for (TaskQueryVariableValue variable : variables) {
      if (variable.isProcessInstanceVariable()) {
        addVariable(jsonObject, PROCESS_VARIABLES, variable);
      }
      else if(variable.isLocal()) {
        addVariable(jsonObject, TASK_VARIABLES, variable);
      }
      else {
        addVariable(jsonObject, CASE_INSTANCE_VARIABLES, variable);
      }
    }
  }

  protected void addVariable(JsonObject jsonObject, String variableType, TaskQueryVariableValue variable) {
    JsonElement variables = jsonObject.get(variableType);
    if (variables == null) {
      variables = JsonMapper.createArrayNode();
    }

    addElement((JsonArray) variables, variableValueConverter, variable);

    addField(jsonObject, variableType, (JsonArray) variables);
  }

  @Override
  public TaskQuery toObject(JsonObject json) {
    TaskQueryImpl query = new TaskQueryImpl();

    if (json.has(OR_QUERIES)) {
      for (JsonElement jsonElement : json.get(OR_QUERIES).getAsJsonArray()) {
        query.addOrQuery((TaskQueryImpl) toObject(jsonElement.getAsJsonObject()));
      }
    }
    if (json.has(TASK_ID)) {
      query.taskId(json.get(TASK_ID).getAsString());
    }
    if (json.has(NAME)) {
      query.taskName(json.get(NAME).getAsString());
    }
    if (json.has(NAME_NOT_EQUAL)) {
      query.taskNameNotEqual(json.get(NAME_NOT_EQUAL).getAsString());
    }
    if (json.has(NAME_LIKE)) {
      query.taskNameLike(json.get(NAME_LIKE).getAsString());
    }
    if (json.has(NAME_NOT_LIKE)) {
      query.taskNameNotLike(json.get(NAME_NOT_LIKE).getAsString());
    }
    if (json.has(DESCRIPTION)) {
      query.taskDescription(json.get(DESCRIPTION).getAsString());
    }
    if (json.has(DESCRIPTION_LIKE)) {
      query.taskDescriptionLike(json.get(DESCRIPTION_LIKE).getAsString());
    }
    if (json.has(PRIORITY)) {
      query.taskPriority(json.get(PRIORITY).getAsInt());
    }
    if (json.has(MIN_PRIORITY)) {
      query.taskMinPriority(json.get(MIN_PRIORITY).getAsInt());
    }
    if (json.has(MAX_PRIORITY)) {
      query.taskMaxPriority(json.get(MAX_PRIORITY).getAsInt());
    }
    if (json.has(ASSIGNEE)) {
      query.taskAssignee(json.get(ASSIGNEE).getAsString());
    }
    if (json.has(ASSIGNEE_LIKE)) {
      query.taskAssigneeLike(json.get(ASSIGNEE_LIKE).getAsString());
    }
    if (json.has(INVOLVED_USER)) {
      query.taskInvolvedUser(json.get(INVOLVED_USER).getAsString());
    }
    if (json.has(OWNER)) {
      query.taskOwner(json.get(OWNER).getAsString());
    }
    if (json.has(ASSIGNED) && json.get(ASSIGNED).getAsBoolean()) {
      query.taskAssigned();
    }
    if (json.has(UNASSIGNED) && json.get(UNASSIGNED).getAsBoolean()) {
      query.taskUnassigned();
    }
    if (json.has(DELEGATION_STATE)) {
      query.taskDelegationState(DelegationState.valueOf(json.get(DELEGATION_STATE).getAsString()));
    }
    if (json.has(CANDIDATE_USER)) {
      query.taskCandidateUser(json.get(CANDIDATE_USER).getAsString());
    }
    if (json.has(CANDIDATE_GROUP)) {
      query.taskCandidateGroup(json.get(CANDIDATE_GROUP).getAsString());
    }
    if (json.has(CANDIDATE_GROUPS) && !json.has(CANDIDATE_USER) && !json.has(CANDIDATE_GROUP)) {
      query.taskCandidateGroupIn(getList((JsonArray)json.get(CANDIDATE_GROUPS)));
    }
    if (json.has(WITH_CANDIDATE_GROUPS) && json.get(WITH_CANDIDATE_GROUPS).getAsBoolean()) {
      query.withCandidateGroups();
    }
    if (json.has(WITHOUT_CANDIDATE_GROUPS) && json.get(WITHOUT_CANDIDATE_GROUPS).getAsBoolean()) {
      query.withoutCandidateGroups();
    }
    if (json.has(WITH_CANDIDATE_USERS) && json.get(WITH_CANDIDATE_USERS).getAsBoolean()) {
      query.withCandidateUsers();
    }
    if (json.has(WITHOUT_CANDIDATE_USERS) && json.get(WITHOUT_CANDIDATE_USERS).getAsBoolean()) {
      query.withoutCandidateUsers();
    }
    if (json.has(INCLUDE_ASSIGNED_TASKS) && json.get(INCLUDE_ASSIGNED_TASKS).getAsBoolean()) {
      query.includeAssignedTasksInternal();
    }
    if (json.has(PROCESS_INSTANCE_ID)) {
      query.processInstanceId(json.get(PROCESS_INSTANCE_ID).getAsString());
    }
    if (json.has(EXECUTION_ID)) {
      query.executionId(json.get(EXECUTION_ID).getAsString());
    }
    if (json.has(ACTIVITY_INSTANCE_ID_IN)) {
      query.activityInstanceIdIn(getArray((JsonArray)json.get(ACTIVITY_INSTANCE_ID_IN)));
    }
    if (json.has(CREATED)) {
      query.taskCreatedOn(new Date(json.get(CREATED).getAsLong()));
    }
    if (json.has(CREATED_BEFORE)) {
      query.taskCreatedBefore(new Date(json.get(CREATED_BEFORE).getAsLong()));
    }
    if (json.has(CREATED_AFTER)) {
      query.taskCreatedAfter(new Date(json.get(CREATED_AFTER).getAsLong()));
    }
    if (json.has(KEY)) {
      query.taskDefinitionKey(json.get(KEY).getAsString());
    }
    if (json.has(KEYS)) {
      query.taskDefinitionKeyIn(getArray((JsonArray)json.get(KEYS)));
    }
    if (json.has(KEY_LIKE)) {
      query.taskDefinitionKeyLike(json.get(KEY_LIKE).getAsString());
    }
    if (json.has(PARENT_TASK_ID)) {
      query.taskParentTaskId(json.get(PARENT_TASK_ID).getAsString());
    }
    if (json.has(PROCESS_DEFINITION_KEY)) {
      query.processDefinitionKey(json.get(PROCESS_DEFINITION_KEY).getAsString());
    }
    if (json.has(PROCESS_DEFINITION_KEYS)) {
      query.processDefinitionKeyIn(getArray((JsonArray)json.get(PROCESS_DEFINITION_KEYS)));
    }
    if (json.has(PROCESS_DEFINITION_ID)) {
      query.processDefinitionId(json.get(PROCESS_DEFINITION_ID).getAsString());
    }
    if (json.has(PROCESS_DEFINITION_NAME)) {
      query.processDefinitionName(json.get(PROCESS_DEFINITION_NAME).getAsString());
    }
    if (json.has(PROCESS_DEFINITION_NAME_LIKE)) {
      query.processDefinitionNameLike(json.get(PROCESS_DEFINITION_NAME_LIKE).getAsString());
    }
    if (json.has(PROCESS_INSTANCE_BUSINESS_KEY)) {
      query.processInstanceBusinessKey(json.get(PROCESS_INSTANCE_BUSINESS_KEY).getAsString());
    }
    if (json.has(PROCESS_INSTANCE_BUSINESS_KEYS)) {
      query.processInstanceBusinessKeyIn(getArray((JsonArray)json.get(PROCESS_INSTANCE_BUSINESS_KEYS)));
    }
    if (json.has(PROCESS_INSTANCE_BUSINESS_KEY_LIKE)) {
      query.processInstanceBusinessKeyLike(json.get(PROCESS_INSTANCE_BUSINESS_KEY_LIKE).getAsString());
    }
    if (json.has(TASK_VARIABLES)) {
      addVariables(query, json.get(TASK_VARIABLES).getAsJsonArray(), true, false);
    }
    if (json.has(PROCESS_VARIABLES)) {
      addVariables(query, json.get(PROCESS_VARIABLES).getAsJsonArray(), false, true);
    }
    if (json.has(CASE_INSTANCE_VARIABLES)) {
      addVariables(query, json.get(CASE_INSTANCE_VARIABLES).getAsJsonArray(), false, false);
    }
    if (json.has(DUE)) {
      query.dueDate(new Date(json.get(DUE).getAsLong()));
    }
    if (json.has(DUE_BEFORE)) {
      query.dueBefore(new Date(json.get(DUE_BEFORE).getAsLong()));
    }
    if (json.has(DUE_AFTER)) {
      query.dueAfter(new Date(json.get(DUE_AFTER).getAsLong()));
    }
    if (json.has(FOLLOW_UP)) {
      query.followUpDate(new Date(json.get(FOLLOW_UP).getAsLong()));
    }
    if (json.has(FOLLOW_UP_BEFORE)) {
      query.followUpBefore(new Date(json.get(FOLLOW_UP_BEFORE).getAsLong()));
    }
    if (json.has(FOLLOW_UP_AFTER)) {
      query.followUpAfter(new Date(json.get(FOLLOW_UP_AFTER).getAsLong()));
    }
    if (json.has(FOLLOW_UP_NULL_ACCEPTED)) {
      query.setFollowUpNullAccepted(json.get(FOLLOW_UP_NULL_ACCEPTED).getAsBoolean());
    }
    if (json.has(EXCLUDE_SUBTASKS) && json.get(EXCLUDE_SUBTASKS).getAsBoolean()) {
      query.excludeSubtasks();
    }
    if (json.has(SUSPENDED) && json.get(SUSPENDED).getAsBoolean()) {
      query.suspended();
    }
    if (json.has(ACTIVE) && json.get(ACTIVE).getAsBoolean()) {
      query.active();
    }
    if (json.has(CASE_DEFINITION_KEY)) {
      query.caseDefinitionKey(json.get(CASE_DEFINITION_KEY).getAsString());
    }
    if (json.has(CASE_DEFINITION_ID)) {
      query.caseDefinitionId(json.get(CASE_DEFINITION_ID).getAsString());
    }
    if (json.has(CASE_DEFINITION_NAME)) {
      query.caseDefinitionName(json.get(CASE_DEFINITION_NAME).getAsString());
    }
    if (json.has(CASE_DEFINITION_NAME_LIKE)) {
      query.caseDefinitionNameLike(json.get(CASE_DEFINITION_NAME_LIKE).getAsString());
    }
    if (json.has(CASE_INSTANCE_ID)) {
      query.caseInstanceId(json.get(CASE_INSTANCE_ID).getAsString());
    }
    if (json.has(CASE_INSTANCE_BUSINESS_KEY)) {
      query.caseInstanceBusinessKey(json.get(CASE_INSTANCE_BUSINESS_KEY).getAsString());
    }
    if (json.has(CASE_INSTANCE_BUSINESS_KEY_LIKE)) {
      query.caseInstanceBusinessKeyLike(json.get(CASE_INSTANCE_BUSINESS_KEY_LIKE).getAsString());
    }
    if (json.has(CASE_EXECUTION_ID)) {
      query.caseExecutionId(json.get(CASE_EXECUTION_ID).getAsString());
    }
    if (json.has(TENANT_IDS)) {
      query.tenantIdIn(getArray((JsonArray) json.get(TENANT_IDS)));
    }
    if (json.has(WITHOUT_TENANT_ID)) {
      query.withoutTenantId();
    }
    if (json.has(ORDER_BY)) {
      List<QueryOrderingProperty> orderingProperties =
          JsonLegacyQueryOrderingPropertyConverter.INSTANCE.fromOrderByString(json.get(ORDER_BY).getAsString());

      query.setOrderingProperties(orderingProperties);
    }
    if (json.has(ORDERING_PROPERTIES)) {
      JsonArray jsonArray = (JsonArray) json.get(ORDERING_PROPERTIES);
      query.setOrderingProperties(JsonQueryOrderingPropertyConverter.ARRAY_CONVERTER.toObject(jsonArray));
    }

    // expressions
    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
      String key = entry.getKey();
      if (key.endsWith("Expression")) {
        String expression = json.get(key).getAsString();
        query.addExpression(key.substring(0, key.length() - "Expression".length()), expression);
      }
    }

    return query;
  }

  protected String[] getArray(JsonArray array) {
    return getList(array).toArray(new String[array.size()]);
  }

  protected List<String> getList(JsonArray array) {
    List<String> list = new ArrayList<>();
    for (int i = 0; i < array.size(); i++) {
      list.add(array.get(i).getAsString());
    }
    return list;
  }

  protected void addVariables(TaskQueryImpl query, JsonArray variables, boolean isTaskVariable, boolean isProcessVariable) {
    for (JsonElement variable : variables) {
      JsonObject variableObj = variable.getAsJsonObject();
      String name = variableObj.get(NAME).getAsString();
      JsonPrimitive value = (JsonPrimitive) variableObj.get("value");
      QueryOperator operator = QueryOperator.valueOf(variableObj.get("operator").getAsString());
      query.addVariable(name, JsonMapper.asObject(value), operator, isTaskVariable, isProcessVariable);
    }
  }

}
