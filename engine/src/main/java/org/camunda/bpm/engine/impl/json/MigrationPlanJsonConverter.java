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

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.camunda.bpm.engine.impl.migration.MigrationPlanImpl;
import org.camunda.bpm.engine.impl.util.JsonMapper;
import org.camunda.bpm.engine.migration.MigrationPlan;

public class MigrationPlanJsonConverter extends JsonObjectConverter<MigrationPlan> {

  public static final MigrationPlanJsonConverter INSTANCE = new MigrationPlanJsonConverter();

  public static final String SOURCE_PROCESS_DEFINITION_ID = "sourceProcessDefinitionId";
  public static final String TARGET_PROCESS_DEFINITION_ID = "targetProcessDefinitionId";
  public static final String INSTRUCTIONS = "instructions";

  public JsonObject toJsonObject(MigrationPlan migrationPlan) {
    JsonObject jsonObject = JsonMapper.createObjectNode();

    JsonMapper.addField(jsonObject, SOURCE_PROCESS_DEFINITION_ID, migrationPlan.getSourceProcessDefinitionId());
    JsonMapper.addField(jsonObject, TARGET_PROCESS_DEFINITION_ID, migrationPlan.getTargetProcessDefinitionId());
    JsonMapper.addListField(jsonObject, INSTRUCTIONS, MigrationInstructionJsonConverter.INSTANCE, migrationPlan.getInstructions());

    return jsonObject;
  }

  public MigrationPlan toObject(JsonObject json) {
    MigrationPlanImpl migrationPlan = new MigrationPlanImpl(json.get(SOURCE_PROCESS_DEFINITION_ID).getAsString(), json.get(TARGET_PROCESS_DEFINITION_ID).getAsString());

    migrationPlan.setInstructions(JsonMapper.asList((JsonArray)json.get(INSTRUCTIONS), MigrationInstructionJsonConverter.INSTANCE));

    return migrationPlan;
  }

}
