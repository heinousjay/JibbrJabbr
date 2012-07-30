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
package jj.module;

/**
 * Module class loading is a complicated subject.
 * 
 * At the basis, there are two types of modules - modules that work only with the API and
 * modules that are meant to integrate into the kernel.  This distinction determines
 * what the parent classloader of this classloader is.
 * 
 * API based modules can expose REST service APIs
 * 
 * Kernel modules can expose an API package that will be exposed to other kernel modules.
 * 
 * maybe.  this is all too much right now.
 * 
 * @author jason
 *
 */
public class ModuleClassLoader extends ClassLoader {

}
