package jj.resource;

import jj.application.AppLocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author jason
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceIdentifierTest {

	@Mock
	AbstractFileResource<Void> target1;

	@Mock
	AbstractFileResource<Void> target2;

	@Test
	public void test() {
		ResourceIdentifier<Sha1Resource, Sha1ResourceTarget> ri1 =
			new ResourceIdentifier<>(Sha1Resource.class, AppLocation.AppBase, "something", new Sha1ResourceTarget(target1));

		ResourceIdentifier<Sha1Resource, Sha1ResourceTarget> ri2 =
			new ResourceIdentifier<>(Sha1Resource.class, AppLocation.AppBase, "something", new Sha1ResourceTarget(target1));

		ResourceIdentifier<Sha1Resource, Sha1ResourceTarget> ri3 =
			new ResourceIdentifier<>(Sha1Resource.class, AppLocation.AppBase, "something", new Sha1ResourceTarget(target2));

		assertThat(ri1, is(ri2));
		assertThat(ri2, is(ri1));
		assertThat(ri1.hashCode(), is(ri2.hashCode()));
		assertThat(ri1.toString(), is(ri1.toString()));

		assertThat(ri1, is(not(ri3)));
		assertThat(ri2, is(not(ri3)));
	}
}
