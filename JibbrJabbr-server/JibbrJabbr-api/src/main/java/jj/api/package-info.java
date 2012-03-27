/**
 * <p>
 * Defines the interface to running inside the JibbrJabbr container.  There
 * are several considerations in the design of this API
 * <p>
 * <ol>
 * <li>Simple to use, easy to understand.  Hopefully.</li>
 * <li>Nothing to extend or implement. The inheritance hierarchy of your
 * application is totally under your control.  JibbrJabbr interacts with 
 * your application through convention and the use of annotations.
 * </li>
 * <li>System internals are hidden.  There is no way to use this
 * API to divine what is happening behind the scenes. Hopefully this
 * will help to ease migration across versions and avoid any
 * leakage of abstractions. This implies that once functionality appears
 * here, it never goes away.  There should only be additions to the API.
 * </li>
 * </ol>
 */
package jj.api;