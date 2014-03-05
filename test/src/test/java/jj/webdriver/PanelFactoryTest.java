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

import jj.webdriver.URLBase.BaseURL;
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
 * <p>
 * Tests that the page factory and by extension the PageBase and the
 * PageMethodGenerator set up, although individual classes there may
 * have their own tests depending upon complexity
 * 
 * <p>
 * NEW PLAN! make a "TestablePageFactory" object that encapsulates the set-up
 * and start breaking this stuff into smaller tests.  this is really the only
 * way to validate everything but this is getting serious
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
					bind(String.class).annotatedWith(BaseURL.class).toInstance("http://localhost:8080");
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
	public void testSetModel() {
		
		TestModel t = new TestModel();
		t.name = "1";
		t.email = "2";
		
		given(finder.find(webDriver, By.id("test-panel-name"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-panel-email"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-submit"))).willReturn(webElement);
		
		page.testPanel().setSomeForm(t).clickFormSubmit();
		
		verify(webElement).sendKeys("1");
		verify(webElement).sendKeys("2");
		verify(webElement).click();

		given(finder.find(webDriver, By.id("test-panel-name"))).willReturn(null);
		given(finder.find(webDriver, By.id("test-panel-email"))).willReturn(null);
		given(finder.find(webDriver, By.id("test-submit"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("best-panel-name"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("best-panel-email"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("best-submit"))).willReturn(webElement);

		t.name = "3";
		t.email = "4";
		page.bestPanel().setSomeForm(t).clickFormSubmit();
		
		verify(webElement).sendKeys("3");
		verify(webElement).sendKeys("4");
		verify(webElement, times(2)).click();
	}
	
	@Test
	public void testSetModel2() {
		
		TestModel t = new TestModel();
		t.name = "1";
		t.email = "2";
		
		given(finder.find(webDriver, By.id("test-name"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-email"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-submit"))).willReturn(webElement);
		
		page.testPanel().setSameForm(t).clickFormSubmit();
		
		
		verify(webElement).sendKeys("1");
		verify(webElement).sendKeys("2");
		verify(webElement).click();
		
		given(finder.find(webDriver, By.id("test-name"))).willReturn(null);
		given(finder.find(webDriver, By.id("test-email"))).willReturn(null);
		given(finder.find(webDriver, By.id("test-submit"))).willReturn(null);
		given(finder.find(webDriver, By.id("best-name"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("best-email"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("best-submit"))).willReturn(webElement);

		t.name = "3";
		t.email = "4";
		page.bestPanel().setSameForm(t).clickFormSubmit();
		
		verify(webElement).sendKeys("3");
		verify(webElement).sendKeys("4");
		verify(webElement, times(2)).click();
	}
	
	@Test
	public void testSetModel3() {
		
		
		TestModel t = new TestModel();
		t.name = "1";
		t.email = "2";
		
		given(finder.find(webDriver, By.id("test-panel-group[1]-name"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-panel-group[1]-email"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-panel-section[2]-name"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-panel-section[2]-email"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-submit-group[1]"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-submit-section[2]"))).willReturn(webElement);
		
		page.testPanel().setAnotherForm(t, "group", 1).clickFormSubmit("group", 1);
		page.testPanel().setAnotherForm(t, "section", 2).clickFormSubmit("section", 2);
		
		verify(webElement, times(2)).sendKeys("1");
		verify(webElement, times(2)).sendKeys("2");
		verify(webElement, times(2)).click();
	}
	
	@Test
	public void testReadElement() {
		
		String value1 = "value1";
		String value2 = "value2";
		String value3 = "value3";

		given(finder.find(webDriver, By.id("test-user-0"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-user-3"))).willReturn(webElement);
		given(finder.find(webDriver, By.id("test-user-10"))).willReturn(webElement);
		given(webElement.getText()).willReturn(value1, value2, value3);
		
		assertThat(page.testPanel().readUsers(0), is(value1));
		assertThat(page.testPanel().readUsers(3), is(value2));
		assertThat(page.testPanel().readUsers(10), is(value3));
	}
}
