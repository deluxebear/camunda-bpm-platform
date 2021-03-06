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
package org.camunda.bpm.engine.test.standalone.history;

import org.camunda.bpm.engine.impl.history.AbstractHistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class AbstractHistoryLevelTest {


  public static class MyHistoryLevel extends AbstractHistoryLevel {

    @Override
    public int getId() {
      return 4711;
    }

    @Override
    public String getName() {
      return "myName";
    }

    @Override
    public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
      return false;
    }
  }

  @Test
  public void ensureCorrectToString() {
    Assert.assertThat(new MyHistoryLevel().toString(), CoreMatchers.is("MyHistoryLevel(name=myName, id=4711)"));
  }
}