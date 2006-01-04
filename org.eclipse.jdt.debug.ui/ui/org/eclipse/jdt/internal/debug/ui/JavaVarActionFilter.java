/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaStructureErrorValue;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.ui.actions.EditVariableLogicalStructureAction;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.ui.IActionFilter;

/**
 * Provides the action filter for Java and Inspect actions
 * 
 * @since 3.2
 */
public class JavaVarActionFilter implements IActionFilter {

	/**
	 * The set or primitive types
	 */
	private static final Set fgPrimitiveTypes = initPrimitiveTypes();

	/**
	 * The predefined set of primitive types
	 * @return the set of predefined types
	 */
	private static Set initPrimitiveTypes() {
		HashSet set = new HashSet(8);
		set.add("short"); //$NON-NLS-1$
		set.add("int"); //$NON-NLS-1$
		set.add("long"); //$NON-NLS-1$
		set.add("float"); //$NON-NLS-1$
		set.add("double"); //$NON-NLS-1$
		set.add("boolean"); //$NON-NLS-1$
		set.add("byte"); //$NON-NLS-1$
		set.add("char"); //$NON-NLS-1$
		set.add("null"); //$NON-NLS-1$
		return set;
	}
	
	/**
	 * Determines if the declared value is the same as the concrete value
	 * @param var the variable to inspect
	 * @return true if the types are the same, false otherwise
	 */
	protected boolean isDeclaredSameAsConcrete(IJavaVariable var) {
		try {
			IValue value = var.getValue();
			if (value instanceof JDINullValue) {
				return false;
			}
			return !var.getReferenceTypeName().equals(value.getReferenceTypeName());
		}
		catch(DebugException e) {JDIDebugUIPlugin.log(e);}
		return false;
	}

	/**
	 * Determines if the passed object is a primitive type or not
	 * @param obj the obj to test 
	 * @return true if the object is primitive, false otherwise
	 */
	protected boolean isPrimitiveType(Object obj) {
		if(obj instanceof IJavaVariable) {
			try {
				return !fgPrimitiveTypes.contains(removeArray(((IJavaVariable)obj).getReferenceTypeName()));
			} 
			catch (DebugException e) {JDIDebugUIPlugin.log(e);}
		}
		else if(obj instanceof JavaInspectExpression) {
			try {
				JavaInspectExpression exp = (JavaInspectExpression)obj;
				IValue value = exp.getValue();
				if (value != null) {
					return fgPrimitiveTypes.contains(removeArray(value.getReferenceTypeName()));
				}
			} catch (DebugException e) {JDIDebugUIPlugin.log(e);}
		}
		return false;
	}
	
	/**
	 * Determines if the ref type of the value is primitive
	 * 
	 * @param var the variable to inspect
	 * @return true if the the values ref type is primitive, false otherwise
	 */
	protected boolean isValuePrimitiveType(IValue value) {
		try {
			return !fgPrimitiveTypes.contains(removeArray(value.getReferenceTypeName()));
		} 
		catch (DebugException e) {JDIDebugUIPlugin.log(e);}
		return false;
	}
	
	/**
	 * Method removes the array declaration characters to return just the type
	 * 
	 * @param typeName the type name we want to strip the array delimiters from
	 * @return the altered type
	 */
	protected String removeArray(String type) {
		if (type != null) {
			int index= type.indexOf('[');
			if (index > 0) {
				return type.substring(0, index);
			}
		}
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionFilter#testAttribute(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (target instanceof IJavaVariable) {
			IJavaVariable var = (IJavaVariable) target;
			IValue varValue;
			try {
				varValue = var.getValue();
				if (name.equals("PrimitiveVariableActionFilter")) { //$NON-NLS-1$ 
					if (value.equals("isPrimitive")) { //$NON-NLS-1$
						return isPrimitiveType(var);
					} 
					else if (value.equals("isValuePrimitive")) { //$NON-NLS-1$
						return isValuePrimitiveType(varValue);
					}
				} 
				else if (name.equals("ConcreteVariableActionFilter") && value.equals("isConcrete")) { //$NON-NLS-1$ //$NON-NLS-2$
					return isDeclaredSameAsConcrete(var);
				} 
				else if (name.equals("JavaVariableActionFilter") && value.equals("instanceFilter")) { //$NON-NLS-1$ //$NON-NLS-2$
					return !var.isStatic() && (varValue instanceof IJavaObject) && (((IJavaObject)varValue).getJavaType() instanceof IJavaClassType) && ((IJavaDebugTarget)var.getDebugTarget()).supportsInstanceBreakpoints();
				} 
				else if (name.equals("DetailFormatterFilter") && value.equals("isDefined")) { //$NON-NLS-1$ //$NON-NLS-2$
					return (varValue instanceof IJavaObject) && (JavaDetailFormattersManager.getDefault().hasAssociatedDetailFormatter(((IJavaObject)varValue).getJavaType()));
				} 
				else if (name.equals("JavaLogicalStructureFilter") && value.equals("canEditLogicalStructure")) {  //$NON-NLS-1$ //$NON-NLS-2$
                    return varValue instanceof JavaStructureErrorValue || EditVariableLogicalStructureAction.getLogicalStructure(varValue) != null;
                }
			} catch (DebugException e) {JDIDebugUIPlugin.log(e);}	
		}
		else if (target instanceof JavaInspectExpression) {
			JavaInspectExpression exp = (JavaInspectExpression) target;
			if (name.equals("PrimitiveVariableActionFilter") && value.equals("isNotPrimitive")) { //$NON-NLS-1$ //$NON-NLS-2$
				return !isPrimitiveType(exp);
			} 
			else if (name.equals("DetailFormatterFilter") && value.equals("isDefined")) { //$NON-NLS-1$ //$NON-NLS-2$
				try {
					IValue varValue= exp.getValue();
					return (varValue instanceof IJavaObject) && (JavaDetailFormattersManager.getDefault().hasAssociatedDetailFormatter(((IJavaObject)varValue).getJavaType()));
				} 
				catch (DebugException exception) {JDIDebugUIPlugin.log(exception);}
			}
		}
		return false;
	}	
}//end class