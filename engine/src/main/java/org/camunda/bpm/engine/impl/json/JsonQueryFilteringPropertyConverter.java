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
import org.camunda.bpm.engine.impl.QueryEntityRelationCondition;
import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.impl.util.JsonMapper;
import org.camunda.bpm.engine.query.QueryProperty;

import java.util.List;

/**
 * @author Thorben Lindhauer
 *
 */
public class JsonQueryFilteringPropertyConverter extends JsonObjectConverter<QueryEntityRelationCondition> {

  protected static JsonQueryFilteringPropertyConverter INSTANCE =
      new JsonQueryFilteringPropertyConverter();

  protected static JsonArrayConverter<List<QueryEntityRelationCondition>> ARRAY_CONVERTER = new JsonArrayOfObjectsConverter<>(INSTANCE);

  public static final String BASE_PROPERTY = "baseField";
  public static final String COMPARISON_PROPERTY = "comparisonField";
  public static final String SCALAR_VALUE = "value";

  public JsonObject toJsonObject(QueryEntityRelationCondition filteringProperty) {
    JsonObject jsonObjectBuilder = JsonMapper.createObjectNode();

    JsonMapper.addField(jsonObjectBuilder, BASE_PROPERTY, filteringProperty.getProperty().getName());

    QueryProperty comparisonProperty = filteringProperty.getComparisonProperty();
    if (comparisonProperty != null) {
      JsonMapper.addField(jsonObjectBuilder, COMPARISON_PROPERTY, comparisonProperty.getName());
    }

    String scalarValue = filteringProperty.getScalarValue();
    if (scalarValue != null) {
      JsonMapper.addField(jsonObjectBuilder, SCALAR_VALUE, scalarValue);
    }

    return jsonObjectBuilder;
  }

  public QueryEntityRelationCondition toObject(JsonObject jsonObject) {
    // this is limited in that it allows only String values;
    // that is sufficient for current use case with task filters
    // but could be extended by a data type in the future
    String scalarValue = null;
    if (jsonObject.has(SCALAR_VALUE)) {
      scalarValue = jsonObject.get(SCALAR_VALUE).getAsString();
    }

    QueryProperty baseProperty = null;
    if (jsonObject.has(BASE_PROPERTY)) {
      baseProperty = new QueryPropertyImpl(jsonObject.get(BASE_PROPERTY).getAsString());
    }

    QueryProperty comparisonProperty = null;
    if (jsonObject.has(COMPARISON_PROPERTY)) {
      comparisonProperty = new QueryPropertyImpl(jsonObject.get(COMPARISON_PROPERTY).getAsString());
    }

    return new QueryEntityRelationCondition(baseProperty, comparisonProperty, scalarValue);
  }

}
