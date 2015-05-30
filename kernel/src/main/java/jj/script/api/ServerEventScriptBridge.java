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
package jj.script.api;

import static jj.script.api.ServerEventScriptResult.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;

import com.google.inject.Injector;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.TaskRunner;
import jj.script.CurrentScriptEnvironment;
import jj.script.ScriptEnvironment;
import jj.script.ScriptEnvironmentDied;
import jj.util.CodeGenHelper;

/**
 * <p>
 * Component to allow scripts to adhoc register for server events.
 * 
 * <p>
 * Basic flow is codegen a new class if needed, and put an instance of it
 * into a map keyed ScriptEnvironment instance -> lots of stuff. see below!
 * 
 * <p>
 * When the environment dies, it gets unregistered automatically.  it is also
 * possible to unsubscribe directly
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
public class ServerEventScriptBridge {

	// ugh! okay map of ScriptEnvironment -> (map of event name -> (map of Callable -> generated invoker instance))
	// only the top level map needs to accomodate concurrency because a given script environment is guaranteed
	// to only execute from a single thread.
	private final ConcurrentMap<ScriptEnvironment, Map<String, Map<Callable, ServerEventCallableInvoker>>> invokers =
		new ConcurrentHashMap<>(16, 0.75F, 4);
	
	private final ConcurrentMap<String, Class<? extends ServerEventCallableInvoker>> invokerClasses = new ConcurrentHashMap<>(16, 0.75F, 2);
	
	private final CurrentScriptEnvironment env;
	
	private final Injector injector;
	
	private final ClassPool classPool = CodeGenHelper.classPool();
	
	private final CtClass superClass;
	
	private final CtConstructor superConstructor;
	
	@Inject
	ServerEventScriptBridge(final CurrentScriptEnvironment env, final Injector injector) throws Exception {
		this.env = env;
		this.injector = injector;
		
		superClass = classPool.get(ServerEventCallableInvoker.class.getName());
		
		CtClass taskRunner = classPool.get(TaskRunner.class.getName());
		
		superConstructor = superClass.getDeclaredConstructor(new CtClass[] {taskRunner});
	}
	
	private String generateInvokerClassName(String eventClassName) {
		return getClass().getPackage().getName() + ".GeneratedInvokerFor$$" + eventClassName.replace('.', '_');
	}
	
	private void generateConstructor(final CtClass invokerClass) throws Exception {
		CtConstructor ctor = 
			CtNewConstructor.make(
				superConstructor.getParameterTypes(),
				superConstructor.getExceptionTypes(),
				invokerClass
			);
		
		invokerClass.addConstructor(ctor);
		
		CodeGenHelper.addAnnotationToMethod(ctor, Inject.class);
	}
	
	private void generateInvocationMethod(final CtClass invokerClass, final String eventClassName) throws Exception {
		
		CtMethod method = CtNewMethod.make(
			"void invocationBridge(" + eventClassName + " event) { " +
				"super.invoke(event);" + 
			"}",
			invokerClass
		);
		
		CodeGenHelper.addAnnotationToMethod(method, Listener.class);
		invokerClass.addMethod(method);
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends ServerEventCallableInvoker> makeOrFindInvokerClass(String eventClassName) {
		final  String className = generateInvokerClassName(eventClassName);
		
		// this pattern is required to make this class testable and resilient in the face of
		// multiple simultaneous requests for the same event.  granted, it's not the most
		// likely scenario, but it would sure be a pain if it happened, and that also implies
		// that contention should be low anyway
		return invokerClasses.computeIfAbsent(className, name -> {
			try {
				return (Class<? extends ServerEventCallableInvoker>)Class.forName(className);
			} catch (ClassNotFoundException cnfe) {}
			
			try {
				CtClass invokerClass = classPool.makeClass(className, superClass);
				
				generateConstructor(invokerClass);
				
				CodeGenHelper.addAnnotationToClass(invokerClass, Subscriber.class);
				
				generateInvocationMethod(invokerClass, eventClassName);
				
				CodeGenHelper.storeGeneratedClass(invokerClass);
				
				return classPool.toClass(
					invokerClass, 
					getClass().getClassLoader(), 
					getClass().getProtectionDomain()
				);
				
				// don't bother detaching, it's going to get looked up in the event system.
				
			} catch (Exception cce) {
				throw new AssertionError(cce);
			}
		});
	}
	
	@Listener
	void scriptEnvironmentDied(ScriptEnvironmentDied sed) {
		Map<String, Map<Callable, ServerEventCallableInvoker>> map = invokers.remove(sed.scriptEnvironment());
		// make sure the various invokers don't get invoked before cleanup
		if (map != null) {
			map.values().forEach(callablesMap -> callablesMap.values().forEach(invoker -> invoker.kill()));
		}
	}
	
	private Map<String, Map<Callable, ServerEventCallableInvoker>> scriptEnvironmentMap() {
		return invokers.computeIfAbsent(env.current(), se -> new HashMap<>());
	}
	
	public ServerEventScriptResult subscribe(final String eventClassName, final Callable callable) {
		try {
			Class.forName(eventClassName);
		} catch (ClassNotFoundException cnfe) {
			return NotAnEventClass;
		}
		
		Map<String, Map<Callable, ServerEventCallableInvoker>> callablesMap = scriptEnvironmentMap();
		Map<Callable, ServerEventCallableInvoker> invokerMap = callablesMap.computeIfAbsent(eventClassName, name -> new HashMap<>());
		if (invokerMap.containsKey(callable)) { // no double adding!
			return AlreadyBound;
		}
		
		Class<? extends ServerEventCallableInvoker> invokerClass = makeOrFindInvokerClass(eventClassName);
		ServerEventCallableInvoker invoker = injector.getInstance(invokerClass);
		invoker.invocationInstances(env.current(), callable);
		invokerMap.put(callable, invoker);
		
		return Success;
	}
	
	public ServerEventScriptResult unsubscribe(final String eventName, final Callable callable) {
		Map<Callable, ServerEventCallableInvoker> callableMap = scriptEnvironmentMap().get(eventName);
		if (callableMap != null) {
			ServerEventCallableInvoker invoker = callableMap.remove(callable);
			if (invoker != null) {
				invoker.kill();
				return Success;
			}
		}
		
		return NotBound;
	}
}
