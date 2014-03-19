/*
 *    Copyright 2012 Jason Miller
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
/**
 * <p>
 * Defines the {@link PropertiesResource}, a primitive resource that wraps a properties file
 * to expose the key-value pairs defined within in a manner analogous to {@link Properties},
 * but assuming UTF-8 (and soon this will be configurable)
 * 
 * <p>
 * Also defines a higher-level {@link MessagesResource}, which organizes a set of 
 * 
 * @author jason
 *
 */
package jj.messaging;

import java.util.Properties;