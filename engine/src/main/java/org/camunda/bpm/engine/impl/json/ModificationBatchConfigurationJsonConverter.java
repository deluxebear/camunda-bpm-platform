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
import org.camunda.bpm.engine.impl.ModificationBatchConfiguration;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.util.JsonMapper;

import java.util.List;

public class ModificationBatchConfigurationJsonConverter extends JsonObjectConverter<ModificationBatchConfiguration>{

  public static final ModificationBatchConfigurationJsonConverter INSTANCE = new ModificationBatchConfigurationJsonConverter();
  public static final String INSTRUCTIONS = "instructions";
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String SKIP_LISTENERS = "skipListeners";
  public static final String SKIP_IO_MAPPINGS = "skipIoMappings";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

  @Override
  public JsonObject toJsonObject(ModificationBatchConfiguration configuration) {
    JsonObject jsonObjectBuilder = JsonMapper.createObjectNode();

    JsonMapper.addListField(jsonObjectBuilder, INSTRUCTIONS, ModificationCmdJsonConverter.INSTANCE, configuration.getInstructions());
    JsonMapper.addListField(jsonObjectBuilder, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonMapper.addField(jsonObjectBuilder, PROCESS_DEFINITION_ID, configuration.getProcessDefinitionId());
    JsonMapper.addField(jsonObjectBuilder, SKIP_LISTENERS, configuration.isSkipCustomListeners());
    JsonMapper.addField(jsonObjectBuilder, SKIP_IO_MAPPINGS, configuration.isSkipIoMappings());

    return jsonObjectBuilder;
  }

  @Override
  public ModificationBatchConfiguration toObject(JsonObject json) {

    List<String> processInstanceIds = readProcessInstanceIds(json);
    String processDefinitionId = json.get(PROCESS_DEFINITION_ID).getAsString();
    List<AbstractProcessInstanceModificationCommand> instructions = JsonMapper.asList((JsonArray) json.get(INSTRUCTIONS),
        ModificationCmdJsonConverter.INSTANCE);
    boolean skipCustomListeners = json.get(SKIP_LISTENERS).getAsBoolean();
    boolean skipIoMappings = json.get(SKIP_IO_MAPPINGS).getAsBoolean();

    return new ModificationBatchConfiguration(
        processInstanceIds,
        processDefinitionId,
        instructions,
        skipCustomListeners,
        skipIoMappings);
  }

  protected List<String> readProcessInstanceIds(JsonObject jsonObject) {
    return JsonMapper.asList(jsonObject.get(PROCESS_INSTANCE_IDS));
  }

}
