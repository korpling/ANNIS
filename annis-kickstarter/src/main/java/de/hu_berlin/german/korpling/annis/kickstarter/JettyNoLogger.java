/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hu_berlin.german.korpling.annis.kickstarter;

import org.eclipse.jetty.util.log.Logger;

/**
 * A "logger" for Jetty that does not log anything.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class JettyNoLogger implements Logger
{

  @Override
  public String getName()
  {
    return "NoLogger";
  }

  @Override
  public void warn(String msg, Object... args)
  {
  }

  @Override
  public void warn(Throwable thrown)
  {
  }

  @Override
  public void warn(String msg, Throwable thrown)
  {
  }

  @Override
  public void info(String msg, Object... args)
  {
  }

  @Override
  public void info(Throwable thrown)
  {
  }

  @Override
  public void info(String msg, Throwable thrown)
  {
  }

  @Override
  public boolean isDebugEnabled()
  {
    return false;
  }

  @Override
  public void setDebugEnabled(boolean enabled)
  {
  }

  @Override
  public void debug(String msg, Object... args)
  {
  }

  @Override
  public void debug(Throwable thrown)
  {
  }

  @Override
  public void debug(String msg, Throwable thrown)
  {
  }

  @Override
  public Logger getLogger(String arg0)
  {
    return this;
  }

  @Override
  public void ignore(Throwable ignored)
  {
  }
  
}
