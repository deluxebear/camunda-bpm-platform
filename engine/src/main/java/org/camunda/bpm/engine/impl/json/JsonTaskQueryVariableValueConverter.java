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

import com.google.gson.JsonObject;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.util.JsonMapper;

import static org.camunda.bpm.engine.impl.util.JsonMapper.addField;
import static org.camunda.bpm.engine.impl.util.JsonMapper.addFieldTyped;

/**
 * @author Sebastian Menski
 */
public class JsonTaskQueryVariableValueConverter extends JsonObjectConverter<TaskQueryVariableValue> {

  public JsonObject toJsonObject(TaskQueryVariableValue variable) {
    JsonObject jsonObject = JsonMapper.createObjectNode();
    addField(jsonObject, "name", variable.getName());
    addFieldTyped(jsonObject, "value", variable.getTypedValue());
    addField(jsonObject, "operator", variable.getOperator().name());

    return jsonObject;
  }

  public TaskQueryVariableValue toObject(JsonObject json) {
    String name = json.get("name").getAsString();
    Object value = json.get("value");
    QueryOperator operator = QueryOperator.valueOf(json.get("operator").getAsString());
    boolean isTaskVariable = json.get("taskVariable").getAsBoolean();
    boolean isProcessVariable = json.get("processVariable").getAsBoolean();
    return new TaskQueryVariableValue(name, value, operator, isTaskVariable, isProcessVariable);
  }
}
