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
import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.impl.util.JsonMapper;
import org.camunda.bpm.engine.migration.MigrationInstruction;

public class MigrationInstructionJsonConverter extends JsonObjectConverter<MigrationInstruction> {

  public static final MigrationInstructionJsonConverter INSTANCE = new MigrationInstructionJsonConverter();

  public static final String SOURCE_ACTIVITY_IDS = "sourceActivityIds";
  public static final String TARGET_ACTIVITY_IDS = "targetActivityIds";
  public static final String UPDATE_EVENT_TRIGGER = "updateEventTrigger";

  public JsonObject toJsonObject(MigrationInstruction instruction) {
    JsonObject jsonObject = JsonMapper.createObjectNode();

    JsonMapper.addArrayField(jsonObject, SOURCE_ACTIVITY_IDS, new String[]{instruction.getSourceActivityId()});
    JsonMapper.addArrayField(jsonObject, TARGET_ACTIVITY_IDS, new String[]{instruction.getTargetActivityId()});
    JsonMapper.addField(jsonObject, UPDATE_EVENT_TRIGGER, instruction.isUpdateEventTrigger());

    return jsonObject;
  }

  public MigrationInstruction toObject(JsonObject json) {
    return new MigrationInstructionImpl(
      readSourceActivityId(json),
      readTargetActivityId(json),
      json.get(UPDATE_EVENT_TRIGGER).getAsBoolean()
    );
  }

  protected String readSourceActivityId(JsonObject json) {
    if (json.has(SOURCE_ACTIVITY_IDS)) {
      return json.get(SOURCE_ACTIVITY_IDS).getAsJsonArray().get(0).getAsString();
    }

    return null;
  }

  protected String readTargetActivityId(JsonObject json) {
    if (json.has(SOURCE_ACTIVITY_IDS)) {
      return json.get(TARGET_ACTIVITY_IDS).getAsJsonArray().get(0).getAsString();
    }

    return null;
  }


}
