package jj.resource;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import jj.css.StylesheetResource;
import jj.http.server.RouteProcessor;
import jj.http.server.ServableResources;
import jj.http.server.resource.StaticResource;
import jj.http.server.uri.Route;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.Router;
import jj.http.server.uri.URIMatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServableLoaderTest {
	
	private @Mock ServableResources servables;
	private @Mock Router router;
	
	ServableLoader sl;
	
	private @Mock Route route;
	private @Mock RouteProcessor routeProcessor;
	
	private @Mock RouteMatch routeMatch1;
	private @Mock RouteMatch routeMatch2;
	private @Mock RouteMatch routeMatch3;
	
	private @Mock StylesheetResource cssResource;
	private URIMatch cssResourcePath = new URIMatch("/css/path.css");
	private @Mock StaticResource staticResource1;
	private URIMatch baseStaticPath = new URIMatch("/base/static.path");
	private @Mock StaticResource staticResource2;
	private URIMatch assetStaticPath = new URIMatch("/asset/static-1.2.path");

	@Test
	public void test() {
		sl = new ServableLoader(servables, router);
		
		// given
		given(router.routeRequest(GET, cssResourcePath)).willReturn(routeMatch1);
		given(routeMatch1.matched()).willReturn(true);
		given(routeMatch1.resourceName()).willReturn("stylesheet");
		given(routeMatch1.route()).willReturn(route);
		given(router.routeRequest(GET, baseStaticPath)).willReturn(routeMatch2);
		given(routeMatch2.matched()).willReturn(true);
		given(routeMatch2.resourceName()).willReturn("static");
		given(routeMatch2.route()).willReturn(route);
		given(router.routeRequest(GET, assetStaticPath)).willReturn(routeMatch3);
		given(routeMatch3.matched()).willReturn(true);
		given(routeMatch3.resourceName()).willReturn("static");
		given(routeMatch3.route()).willReturn(route);
		
		willReturn(StylesheetResource.class).given(servables).classFor("stylesheet");
		willReturn(StaticResource.class).given(servables).classFor("static");
		
		given(servables.routeProcessor("stylesheet")).willReturn(routeProcessor);
		given(servables.routeProcessor("static")).willReturn(routeProcessor);
		
		given(routeProcessor.loadResource(StylesheetResource.class, cssResourcePath, route)).willReturn(cssResource);
		given(routeProcessor.loadResource(StaticResource.class, baseStaticPath, route)).willReturn(staticResource1);
		given(routeProcessor.loadResource(StaticResource.class, assetStaticPath, route)).willReturn(staticResource2);
		
		assertThat(sl.loadResource(cssResourcePath), is(cssResource));
		assertThat(sl.loadResource(baseStaticPath), is(staticResource1));
		assertThat(sl.loadResource(assetStaticPath), is(staticResource2));
	}

}
