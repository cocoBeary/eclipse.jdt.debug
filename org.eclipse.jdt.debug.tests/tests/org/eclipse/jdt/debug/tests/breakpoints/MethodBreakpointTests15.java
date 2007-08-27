/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.ui.IEditorPart;

/**
 * Tests method breakpoints for 1.5 source code.
 */
public class MethodBreakpointTests15 extends AbstractDebugTest {
	
	public MethodBreakpointTests15(String name) {
		super(name);
	}

	public void testStaticTypeParameter() throws Exception {
		IJavaMethodBreakpoint breakpoint  = createBreakpoint(25);
		assertEquals("Wrong method", "staticTypeParameter", breakpoint.getMethodName());
		runToBreakpoint(getTypeName(), breakpoint);		
	}
	
	public void testTypeParameter() throws Exception {
		IJavaMethodBreakpoint breakpoint  = createBreakpoint(29);
		assertEquals("Wrong method", "typeParameter", breakpoint.getMethodName());
		runToBreakpoint(getTypeName(), breakpoint);
	}
	
	public void testMethodTypeParameter() throws Exception {
		IJavaMethodBreakpoint breakpoint  = createBreakpoint(34);
		assertEquals("Wrong method", "methodTypeParameter", breakpoint.getMethodName());
		runToBreakpoint(getTypeName(), breakpoint);	
	}	
	
	private String getTypeName() {
		return "a.b.c.MethodBreakpoints";
	}
	
	private IJavaMethodBreakpoint createBreakpoint(int line) throws Exception {
		IType type = get15Project().findType(getTypeName());
		assertNotNull("Missing file", type);
		IResource resource = type.getResource();
		assertTrue("Missing file", resource instanceof IFile);
		IEditorPart editor = openEditor((IFile) resource);
		IBreakpoint breakpoint = toggleBreakpoint(editor, line);
		assertTrue("Wrong breakpoint", breakpoint instanceof IJavaMethodBreakpoint);
		return (IJavaMethodBreakpoint) breakpoint;
	}

	private void runToBreakpoint(String typeName, IBreakpoint mbp) throws Exception {
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(get15Project(), typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", mbp,hit);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}	
		
}