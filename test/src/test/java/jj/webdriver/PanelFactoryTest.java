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
package jj.webdriver;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import jj.webdriver.pages.TestModel;
import jj.webdriver.pages.TestPage;
import jj.webdriver.pages.TestPage2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Tests that the page factory and by extension the PageBase and the
 * PageMethodGenerator set up, although individual classes there may
 * have their own tests depending upon complexity
 * 
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PanelFactoryTest {
	
	private static final String TEST_PAGE_NAME = "jj.webdriver.pages.TestPage";

	@Mock WebDriver webDriver;
	
	@Mock WebElementFinder finder;
	
	@Mock WebElement webElement;
	
	@Mock Logger logger;
	
	TestPage page;
	
	PanelFactory panelFactory;
	
	@Before
	public void before() throws Exception {
		
		Injector injector = Guice.createInjector(
			new AbstractModule() {
			
				@Override
				protected void configure() {
					bind(new TypeLiteral<Class<? extends PanelBase>>() {}).toInstance(PointlessPanelBase.class);
					bind(WebDriver.class).toInstance(webDriver);
					bind(WebElementFinder.class).toInstance(finder);
					bind(Logger.class).toInstance(logger);
					bind(String.class).annotatedWith(URLBase.BaseURL.class).toInstance("http://localhost:8080");
				}
			},
			new PanelMethodGeneratorsModule()
		);
		
		panelFactory = injector.getInstance(PanelFactory.class);
		
		given(webDriver.getCurrentUrl()).willReturn("url");
		
		page = panelFactory.create(TestPage.class);
		
		assertThat(page, is(instanceOf(Page.class)));
		assertThat(page, is(instanceOf(PanelBase.class)));
		assertThat(page, is(instanceOf(PointlessPanelBase.class)));
		
		verify(logger).info("{} created. url is {}", TEST_PAGE_NAME, "url");
		
		assertThat(page.getClass().getName(), is("jj.webdriver.GeneratedImplementationFor$$jj_webdriver_pages_TestPage$$"));
		
	}
	
	@Test
	public void testPageBase() {
		
		PanelBase pageBase = (PanelBase)page;
		
		assertThat(pageBase.webDriver(), is(webDriver));
		
		pageBase.currentUrl();
		
		verify(webDriver, times(2)).getCurrentUrl();
		
	}
	
	private By by(By by) {
		given(finder.find(webDriver, by)).willReturn(webElement);
		return by;
	}
	
	@Test
	public void testClick() {
		
		By by = by(By.id("hi"));
		
		assertThat(page.clickHi(), is(page));
		
		verify(logger).info("{} {} {}", TEST_PAGE_NAME, by, "click");
		
		verify(finder).find(webDriver, by);
		
		verify(webElement).click();
	}

	@Test
	public void testSet() {
		
		By by = by(By.className("blast"));
		
		assertThat(page.setBlast("blast"), is(page));
		
		verify(logger).info("{} {} {}", TEST_PAGE_NAME, by, "set blast");
		
		verify(finder).find(webDriver, by);
		
		verify(webElement).sendKeys("blast");
	}
	
	@Test
	public void testNavigation() {
		
		by(By.id("something"));
		
		given(webDriver.getCurrentUrl()).willReturn("http://localhost:8080/page2");
		TestPage2 page2 = page.clickSomething();
		given(webDriver.getCurrentUrl()).willReturn("http://localhost:8080/");
		TestPage page1 = page2.clickSomething();
		
		assertThat(page1, is(notNullValue()));
		
		assertThat(page1.testPanel(), is(notNullValue()));
	}
	
	@Test
	public void testModel() {
		
		TestModel t = new TestModel();
		t.name = "1";
		t.email = "2";
		
		given(finder.find(webDriver, By.id("name"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("email"))).willReturn(webElement);
		
		page.testPanel().setSomeForm(t);
		
		verify(webElement).sendKeys("1");
		verify(webElement).sendKeys("2");
	}
}
