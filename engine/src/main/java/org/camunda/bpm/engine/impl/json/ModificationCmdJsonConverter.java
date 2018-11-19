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
import com.google.gson.JsonObject;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.cmd.ActivityAfterInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityBeforeInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityInstanceCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstanceCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstantiationCmd;
import org.camunda.bpm.engine.impl.util.JsonMapper;

public class ModificationCmdJsonConverter extends JsonObjectConverter<AbstractProcessInstanceModificationCommand> {

  public static final ModificationCmdJsonConverter INSTANCE = new ModificationCmdJsonConverter();

  public static final String START_BEFORE = "startBeforeActivity";
  public static final String START_AFTER = "startAfterActivity";
  public static final String START_TRANSITION = "startTransition";
  public static final String CANCEL_ALL = "cancelAllForActivity";
  public static final String CANCEL_CURRENT = "cancelCurrentActiveActivityInstances";
  public static final String CANCEL_ACTIVITY_INSTANCES = "cancelActivityInstances";
  public static final String PROCESS_INSTANCE = "processInstances";
  public static final String CANCEL_TRANSITION_INSTANCES = "cancelTransitionInstances";

  @Override
  public JsonObject toJsonObject(AbstractProcessInstanceModificationCommand command) {
    JsonObject jsonObject = JsonMapper.createObjectNode();

    if (command instanceof ActivityAfterInstantiationCmd) {
      JsonMapper.addField(jsonObject, START_AFTER, ((ActivityAfterInstantiationCmd) command).getTargetElementId());
    }
    else if (command instanceof ActivityBeforeInstantiationCmd) {
      JsonMapper.addField(jsonObject, START_BEFORE, ((ActivityBeforeInstantiationCmd) command).getTargetElementId());
    }
    else if (command instanceof TransitionInstantiationCmd) {
      JsonMapper.addField(jsonObject, START_TRANSITION, ((TransitionInstantiationCmd) command).getTargetElementId());
    }
    else if (command instanceof ActivityCancellationCmd) {
      JsonMapper.addField(jsonObject, CANCEL_ALL, ((ActivityCancellationCmd) command).getActivityId());
      JsonMapper.addField(jsonObject, CANCEL_CURRENT, ((ActivityCancellationCmd) command).isCancelCurrentActiveActivityInstances());
    }
    else if (command instanceof ActivityInstanceCancellationCmd) {
      JsonMapper.addField(jsonObject, CANCEL_ACTIVITY_INSTANCES, ((ActivityInstanceCancellationCmd) command).getActivityInstanceId());
      JsonMapper.addField(jsonObject, PROCESS_INSTANCE, ((ActivityInstanceCancellationCmd) command).getProcessInstanceId());
    }
    else if (command instanceof TransitionInstanceCancellationCmd) {
      JsonMapper.addField(jsonObject, CANCEL_TRANSITION_INSTANCES, ((TransitionInstanceCancellationCmd) command).getTransitionInstanceId());
      JsonMapper.addField(jsonObject, PROCESS_INSTANCE, ((TransitionInstanceCancellationCmd) command).getProcessInstanceId());
    }

    return jsonObject;
  }

  @Override
  public AbstractProcessInstanceModificationCommand toObject(JsonObject json) {

    AbstractProcessInstanceModificationCommand cmd = null;

    if (json.has(START_BEFORE)) {
      cmd = new ActivityBeforeInstantiationCmd(json.get(START_BEFORE).getAsString());
    }
    else if (json.has(START_AFTER)) {
      cmd = new ActivityAfterInstantiationCmd(json.get(START_AFTER).getAsString());
    }
    else if (json.has(START_TRANSITION)) {
      cmd = new TransitionInstantiationCmd(json.get(START_TRANSITION).getAsString());
    }
    else if (json.has(CANCEL_ALL)) {
      cmd = new ActivityCancellationCmd(json.get(CANCEL_ALL).getAsString());
      boolean cancelCurrentActiveActivityInstances = json.get(CANCEL_CURRENT).getAsBoolean();
      ((ActivityCancellationCmd) cmd).setCancelCurrentActiveActivityInstances(cancelCurrentActiveActivityInstances);
    }
    else if (json.has(CANCEL_ACTIVITY_INSTANCES)) {
      cmd = new ActivityInstanceCancellationCmd(json.get(PROCESS_INSTANCE).getAsString(), json.get(CANCEL_ACTIVITY_INSTANCES).getAsString());
    }
    else if (json.has(CANCEL_TRANSITION_INSTANCES)) {
      cmd = new TransitionInstanceCancellationCmd(json.get(PROCESS_INSTANCE).getAsString(), json.get(CANCEL_TRANSITION_INSTANCES).getAsString());
    }

    return cmd;
  }

}
