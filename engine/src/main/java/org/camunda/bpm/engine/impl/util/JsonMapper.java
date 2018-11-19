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

package org.camunda.bpm.engine.impl.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public final class JsonMapper {

  protected static final Type MAP_TYPE_TOKEN = new TypeToken<Map<String, Object>>() {}.getType();

  public static void addFieldTyped(JsonObject jsonObject, String name, TypedValue value) {
    if (value != null) {
      Object rawValue = value.getValue();
      JsonElement jsonNode = getObjectMapper().toJsonTree(rawValue);
      jsonObject.add(name, jsonNode);
    }
  }

  public static <T> void addField(JsonObject jsonObject, String name, JsonObjectConverter<T> converter, T value) {
    if (value != null) {
      jsonObject.add(name, converter.toJsonObject(value));
    }
  }

  public static void addListField(JsonObject jsonObject, String name, List<String> list) {
    if (list != null) {
      jsonObject.add(name, jsonNode(list));
    }
  }

  public static void addArrayField(JsonObject jsonObject, String name, String[] array) {
    if (array != null) {
      addListField(jsonObject, name, Arrays.asList(array));
    }
  }

  public static void addDateField(JsonObject jsonObject, String name, Date date) {
    if (date != null) {
      jsonObject.addProperty(name, date.getTime());
    }
  }

  public static <T> void addElement(JsonArray jsonNode, JsonObjectConverter<T> converter, T value) {
    jsonNode.add(converter.toJsonObject(value));
  }

  public static <T> void addListField(JsonObject jsonObject, String name, JsonObjectConverter<T> converter, List<T> list) {
    if (list != null) {
      JsonArray arrayNode = createArrayNode();

      for (T item : list) {
        arrayNode.add(converter.toJsonObject(item));
      }

      jsonObject.add(name, arrayNode);
    }
  }

  public static <T> T jsonObject(JsonObject jsonObject, JsonObjectConverter<T> converter) {
    return converter.toObject(jsonObject);
  }

  public static JsonObject addNullField(JsonObject jsonObject, String name) {
      jsonObject.add(name, JsonNull.INSTANCE);

    return jsonObject;
  }

  public static JsonObject addField(JsonObject jsonObject, String name, JsonArray value) {
    if (value != null) {
      jsonObject.add(name, value);
    }
    return jsonObject;
  }

  public static JsonObject addField(JsonObject jsonObject, String name, String value) {
    if (value != null) {
      jsonObject.addProperty(name, value);
    }
    return jsonObject;
  }


  public static JsonObject addField(JsonObject jsonObject, String name, Boolean value) {
    if (value != null) {
      jsonObject.addProperty(name, value);
    }
    return jsonObject;
  }


  public static JsonObject addField(JsonObject jsonObject, String name, Integer value) {
    if (value != null) {
      jsonObject.addProperty(name, value);
    }
    return jsonObject;
  }

  public static JsonObject addField(JsonObject jsonObject, String name, Short value) {
    if (value != null) {
      jsonObject.addProperty(name, value);
    }
    return jsonObject;
  }

  public static JsonObject addField(JsonObject jsonObject, String name, Long value) {
    if (value != null) {
      jsonObject.addProperty(name, value);
    }
    return jsonObject;
  }

  public static JsonObject addField(JsonObject jsonObject, String name, Double value) {
    if (value != null) {
      jsonObject.addProperty(name, value);
    }
    return jsonObject;
  }

  public static JsonObject addDefaultField(JsonObject jsonObject, String name, Boolean defaultValue, Boolean value) {
    if (value != null && !value.equals(defaultValue)) {
      addField(jsonObject, name, value);
    }

    return jsonObject;
  }

  public static JsonObject mapAsObjectNode(String jsonString) {
    try {
      return getObjectMapper().fromJson(jsonString, JsonObject.class);

    } catch (JsonSyntaxException ex) {
      throw new ProcessEngineException();

    }
  }

  public static byte[] writeValueAsBytes(JsonElement jsonObject) {
   return getObjectMapper().toJson(jsonObject).getBytes();
  }

  public static JsonObject readTree(byte[] byteArray) {
    try {
      return getObjectMapper().fromJson(new String(byteArray), JsonObject.class);

    } catch (JsonSyntaxException ex) {
      throw new ProcessEngineException();

    }
  }

  public static List<String> asList(JsonElement jsonObject) {
    JsonArray jsonArray = null;
    try {
      jsonArray = jsonObject.getAsJsonArray();
    } catch (IllegalStateException e) {
      throw new ProcessEngineException();

    }

    List<String> list = new ArrayList<>();
    if (jsonArray != null) {
      for (JsonElement entry : jsonArray) {
        String string = null;
        try {
          string = entry.getAsString();

        } catch (IllegalStateException ex) {
          throw new ProcessEngineException();

        } catch (ClassCastException ex) {
          throw new ProcessEngineException();

        }

        if (string != null) {
          list.add(string);
        }
      }
    }

    return list;
  }

  public static <T> List<T> asList(JsonArray jsonArray, JsonObjectConverter<T> converter) {
    List<T> list = new ArrayList<T>();

    for (JsonElement element : jsonArray) {
      JsonObject jsonObject = null;
      try {
        jsonObject = element.getAsJsonObject();

      } catch (IllegalStateException e) {
        throw new ProcessEngineException();

      }

      if (jsonObject != null) {
        list.add(converter.toObject(jsonObject));
      }
    }

    return list;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> asMap(String json) {
    try {
      return (Map<String, Object>) getObjectMapper().fromJson(json, MAP_TYPE_TOKEN);

    } catch (JsonSyntaxException e) {
      throw new ProcessEngineException();

    } catch (JsonParseException e) {
      throw new ProcessEngineException();

    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> asMap(JsonElement jsonNode) {
    return (Map<String, Object>) getObjectMapper().fromJson(jsonNode, MAP_TYPE_TOKEN);
  }

  public static String writeValueAsString(JsonObject jsonObject) {
    return jsonObject.toString();
  }

  public static JsonElement jsonNode(Map<String, Object> properties) {
    if (properties != null) {
      return getObjectMapper().toJsonTree(properties);
    } else {
      return createObjectNode();
    }
  }

  public static JsonArray jsonNode(List<String> list) {
    if (list != null) {
      JsonElement jsonElement = null;

      try {
        jsonElement = getObjectMapper().toJsonTree(list);

      } catch (JsonIOException e) {
        throw new ProcessEngineException();

      }

      if (jsonElement != null) {

        try {
          return jsonElement.getAsJsonArray();

        } catch (IllegalStateException e) {
          throw new ProcessEngineException();

        }

      } else {
        throw new ProcessEngineException();

      }
    } else {
      return createArrayNode();
    }
  }

  public static String asString(Map<String, Object> properties) {
    return jsonNode(properties).toString();
  }

  public static JsonObject createObjectNode() {
    return new JsonObject();
  }

  public static JsonArray createArrayNode() {
    return new JsonArray();
  }

  public static Object asObject(JsonPrimitive value) {
    try {
      return getObjectMapper().fromJson(value, Object.class);

    } catch (JsonSyntaxException e) {
      throw new ProcessEngineException();

    }
  }

  public static Gson getObjectMapper() {
    ProcessEngineConfigurationImpl engineConfiguration = Context.getProcessEngineConfiguration();

    if (engineConfiguration != null) {
      return engineConfiguration.getObjectMapper();
    } else {
      return createObjectMapper();
    }
  }

  public static Gson createObjectMapper() {
    return new GsonBuilder().serializeNulls().create();
  }

}
