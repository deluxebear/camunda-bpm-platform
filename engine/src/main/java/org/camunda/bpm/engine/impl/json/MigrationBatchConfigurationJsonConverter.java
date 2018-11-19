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
import org.camunda.bpm.engine.impl.migration.batch.MigrationBatchConfiguration;
import org.camunda.bpm.engine.impl.util.JsonMapper;

import java.util.List;

public class MigrationBatchConfigurationJsonConverter extends JsonObjectConverter<MigrationBatchConfiguration> {

  public static final MigrationBatchConfigurationJsonConverter INSTANCE = new MigrationBatchConfigurationJsonConverter();

  public static final String MIGRATION_PLAN = "migrationPlan";
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String SKIP_LISTENERS = "skipListeners";
  public static final String SKIP_IO_MAPPINGS = "skipIoMappings";

  public JsonObject toJsonObject(MigrationBatchConfiguration configuration) {
    JsonObject jsonObject = JsonMapper.createObjectNode();

    JsonMapper.addField(jsonObject, MIGRATION_PLAN, MigrationPlanJsonConverter.INSTANCE, configuration.getMigrationPlan());
    JsonMapper.addListField(jsonObject, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonMapper.addField(jsonObject, SKIP_LISTENERS, configuration.isSkipCustomListeners());
    JsonMapper.addField(jsonObject, SKIP_IO_MAPPINGS, configuration.isSkipIoMappings());

    return jsonObject;
  }

  public MigrationBatchConfiguration toObject(JsonObject json) {
    MigrationBatchConfiguration configuration = new MigrationBatchConfiguration(readProcessInstanceIds(json));

    configuration.setMigrationPlan(JsonMapper.jsonObject(json.get(MIGRATION_PLAN).getAsJsonObject(), MigrationPlanJsonConverter.INSTANCE));
    configuration.setSkipCustomListeners(json.get(SKIP_LISTENERS).getAsBoolean());
    configuration.setSkipIoMappings(json.get(SKIP_IO_MAPPINGS).getAsBoolean());

    return configuration;
  }

  protected List<String> readProcessInstanceIds(JsonObject jsonObject) {
    return JsonMapper.asList(jsonObject.get(PROCESS_INSTANCE_IDS));
  }


}
