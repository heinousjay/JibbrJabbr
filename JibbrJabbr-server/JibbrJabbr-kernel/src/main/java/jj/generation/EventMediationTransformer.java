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
package jj.generation;

import java.lang.reflect.Method;

import jj.BootstrapClassLoader.ClassEmitter;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author jason
 *
 */
public class EventMediationTransformer implements Opcodes, ClassEmitter {
	
	private static final String EVENT = "event";
	private static final String INSTANCE = "instance";

	private final Class<?> eventClass;
	private final Class<?> listenerClass;
	private final Method listener;
	
	public EventMediationTransformer(
		final Class<?> eventClass,
		final Class<?> listenerClass,
		final Method listener
	) {
		this.eventClass = eventClass;
		this.listenerClass = listenerClass;
		this.listener = listener;
	}

	public interface EventRunnable extends Runnable {
		public void set(final Object instance, final Object event);
	}
	
	public String name() {
		return "jj.EventMediationService$" + listenerClass.getSimpleName() + "EventRunnable";
	}
	

	public byte[] emit() {
		
		String name = "jj/EventMediationService$" + listenerClass.getSimpleName() + "EventRunnable";
		
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;

		cw.visit(
				V1_7,
				ACC_PUBLIC + ACC_SUPER,
				name,
				null,
				"java/lang/Object",
				new String[] { Type.getInternalName(EventRunnable.class) });

		
		{
			fv = cw.visitField(ACC_PRIVATE, INSTANCE,
					Type.getDescriptor(listenerClass),
					null, null);
			fv.visitEnd();
		}
		
		{
			fv = cw.visitField(ACC_PRIVATE, EVENT,
					Type.getDescriptor(eventClass), null,
					null);
			fv.visitEnd();
		}
		
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		
		{
			mv = cw.visitMethod(ACC_PUBLIC, "set",
					"(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, Type.getInternalName(listenerClass));
			mv.visitFieldInsn(
					PUTFIELD,
					name,
					INSTANCE,
					Type.getDescriptor(listenerClass));
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(CHECKCAST, Type.getInternalName(eventClass));
			mv.visitFieldInsn(
					PUTFIELD,
					name,
					EVENT, 
					Type.getDescriptor(eventClass));
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
		}
		
		{
			mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(
					GETFIELD,
					name,
					INSTANCE,
					Type.getDescriptor(listenerClass));
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(
					GETFIELD,
					name,
					EVENT, 
					Type.getDescriptor(eventClass));
			mv.visitMethodInsn(INVOKEVIRTUAL,
					Type.getInternalName(listenerClass),
					listener.getName(),
					"("+ Type.getDescriptor(eventClass) +")V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		
		cw.visitEnd();

		return cw.toByteArray();
	}
}
