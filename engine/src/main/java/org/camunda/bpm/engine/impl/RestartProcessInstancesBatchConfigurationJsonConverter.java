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
package org.camunda.bpm.engine.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.json.ModificationCmdJsonConverter;
import org.camunda.bpm.engine.impl.util.JsonMapper;

import java.util.List;

public class RestartProcessInstancesBatchConfigurationJsonConverter extends JsonObjectConverter<RestartProcessInstancesBatchConfiguration>{

  public static final RestartProcessInstancesBatchConfigurationJsonConverter INSTANCE = new RestartProcessInstancesBatchConfigurationJsonConverter();
  
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String INSTRUCTIONS = "instructions";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
  public static final String INITIAL_VARIABLES = "initialVariables";
  public static final String SKIP_CUSTOM_LISTENERS = "skipCustomListeners";
  public static final String SKIP_IO_MAPPINGS = "skipIoMappings";
  public static final String WITHOUT_BUSINESS_KEY = "withoutBusinessKey";

  @Override
  public JsonObject toJsonObject(RestartProcessInstancesBatchConfiguration configuration) {
    JsonObject jsonObjectBuilder = JsonMapper.createObjectNode();
    
    JsonMapper.addListField(jsonObjectBuilder, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonMapper.addField(jsonObjectBuilder, PROCESS_DEFINITION_ID, configuration.getProcessDefinitionId());
    JsonMapper.addListField(jsonObjectBuilder, INSTRUCTIONS, ModificationCmdJsonConverter.INSTANCE, configuration.getInstructions());
    JsonMapper.addField(jsonObjectBuilder, INITIAL_VARIABLES, configuration.isInitialVariables());
    JsonMapper.addField(jsonObjectBuilder, SKIP_CUSTOM_LISTENERS, configuration.isSkipCustomListeners());
    JsonMapper.addField(jsonObjectBuilder, SKIP_IO_MAPPINGS, configuration.isSkipIoMappings());
    JsonMapper.addField(jsonObjectBuilder, WITHOUT_BUSINESS_KEY, configuration.isWithoutBusinessKey());
    
    return jsonObjectBuilder;
  }

  @Override
  public RestartProcessInstancesBatchConfiguration toObject(JsonObject json) {
    List<String> processInstanceIds = readProcessInstanceIds(json);
    List<AbstractProcessInstanceModificationCommand> instructions = JsonMapper.asList((JsonArray) json.get(INSTRUCTIONS), ModificationCmdJsonConverter.INSTANCE);
    
    return new RestartProcessInstancesBatchConfiguration(processInstanceIds, instructions, json.get(PROCESS_DEFINITION_ID).getAsString(),
        json.get(INITIAL_VARIABLES).getAsBoolean(), json.get(SKIP_CUSTOM_LISTENERS).getAsBoolean(), json.get(SKIP_IO_MAPPINGS).getAsBoolean(), json.get(WITHOUT_BUSINESS_KEY).getAsBoolean());
  }

  protected List<String> readProcessInstanceIds(JsonObject jsonObject) {
    return JsonMapper.asList(jsonObject.get(PROCESS_INSTANCE_IDS));
  }
}
