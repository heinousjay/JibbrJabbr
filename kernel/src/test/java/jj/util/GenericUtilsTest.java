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
package jj.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class GenericUtilsTest {
	
	public static class Target<TargetTypeOne, TargetTypeTwo> {}
	
	public static class Impl extends Target<String, Integer> {}
	
	public static class Impl2 extends Impl {}
	
	public static class SomeImpl<PassthroughOne> extends Target<Double, PassthroughOne> {}
	
	public static class SomeImpl2<PassthroughTwo> extends SomeImpl<PassthroughTwo> {}
	
	public static class SomeImpl3 extends SomeImpl2<Long> {}

	@Test
	public void testNewSchool() {
		assertEquals(String.class, GenericUtils.extractTypeParameter(Impl2.class, Target.class, "TargetTypeOne"));
		assertEquals(Integer.class, GenericUtils.extractTypeParameter(Impl2.class, Target.class, "TargetTypeTwo"));
		assertEquals(String.class, GenericUtils.extractTypeParameter(Impl.class, Target.class, "TargetTypeOne"));
		assertEquals(Integer.class, GenericUtils.extractTypeParameter(Impl.class, Target.class, "TargetTypeTwo"));
		
		assertEquals(String.class, GenericUtils.extractTypeParameterAsClass(Impl2.class, Target.class, "TargetTypeOne"));
		assertEquals(Integer.class, GenericUtils.extractTypeParameterAsClass(Impl2.class, Target.class, "TargetTypeTwo"));
		assertEquals(String.class, GenericUtils.extractTypeParameterAsClass(Impl.class, Target.class, "TargetTypeOne"));
		assertEquals(Integer.class, GenericUtils.extractTypeParameterAsClass(Impl.class, Target.class, "TargetTypeTwo"));
	}
	
	@Test
	public void testNewNewSchool() {

		assertEquals(Double.class, GenericUtils.extractTypeParameter(SomeImpl3.class, Target.class, "TargetTypeOne"));
		assertEquals(Long.class, GenericUtils.extractTypeParameter(SomeImpl3.class, SomeImpl2.class, "PassthroughTwo"));
		assertEquals(Long.class, GenericUtils.extractTypeParameter(SomeImpl3.class, Target.class, "TargetTypeTwo"));
		
		assertEquals(Double.class, GenericUtils.extractTypeParameterAsClass(SomeImpl3.class, Target.class, "TargetTypeOne"));
		assertEquals(Long.class, GenericUtils.extractTypeParameterAsClass(SomeImpl3.class, SomeImpl2.class, "PassthroughTwo"));
		assertEquals(Long.class, GenericUtils.extractTypeParameterAsClass(SomeImpl3.class, Target.class, "TargetTypeTwo"));
	}
}
