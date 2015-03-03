package txtuml.importer;

import java.lang.reflect.*;
import java.util.WeakHashMap;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.VisibilityKind;

import txtuml.api.*;
import txtuml.importer.utils.ElementFinder;
import txtuml.importer.utils.ElementTypeTeller;
import txtuml.importer.utils.ModelTypeInformation;

abstract class AbstractImporter {
	
	protected static Object getObjectFieldVal(Object object,String fieldName)
	{	
		Field field = ElementFinder.findField(object.getClass(),fieldName);
		return accessObjectFieldVal(object, field);	
	}
	
	protected static Object accessObjectFieldVal(Object object, Field field)
	{
		Object val=null;
		
		if(field!=null)
		{
			field.setAccessible(true);
			try {
				val = field.get(object);
				return val;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			field.setAccessible(false);
		}
		
		
		return val;
	}
	
	
	
	protected static void importWarning(String msg) {
		System.out.println("Warning: " + msg);
	}
	
	protected static boolean isContainsStateMachine(Class<?> sourceClass){
		for(Class<?> c : sourceClass.getDeclaredClasses()){
			if(ElementTypeTeller.isState(c)){
				return true;
			}
	    }
		return false;
    }
	
	protected static boolean isContainsInitialState(Region region)
	{
		for(Object vert: region.getSubvertices().toArray())
		{
			if(vert instanceof Pseudostate)
			{
				if(((Pseudostate) vert).getKind()==PseudostateKind.INITIAL_LITERAL)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	protected static void setLocalInstanceToBeCreated(boolean bool) {
		localInstanceToBeCreated = bool;
	}
	
	
	private static void setVisibilityBasedOnModifiersGivenByReflection(NamedElement element,int modifiers)
	{
		if(Modifier.isPrivate(modifiers))
		{
			element.setVisibility(VisibilityKind.PRIVATE_LITERAL);
		}
		else if(Modifier.isProtected(modifiers))
		{
			element.setVisibility(VisibilityKind.PROTECTED_LITERAL);
		}
		else if(Modifier.isPublic(modifiers))
		{
			element.setVisibility(VisibilityKind.PUBLIC_LITERAL);
		}
		else
		{
			if(element instanceof Property)
			{
				element.setVisibility(VisibilityKind.PRIVATE_LITERAL);
			}
			else if(element instanceof Operation || element instanceof org.eclipse.uml2.uml.Classifier)
			{
				element.setVisibility(VisibilityKind.PUBLIC_LITERAL);
			}
			else
			{
				element.setVisibility(VisibilityKind.PACKAGE_LITERAL);
			}
		}
	}
	private static void setElementModifiersBasedOnModifiersGivenByReflection(NamedElement element,int modifiers)
	{
		setVisibilityBasedOnModifiersGivenByReflection(element,modifiers);
		
		if(element instanceof Classifier)
		{
			boolean isAbstract = Modifier.isAbstract(modifiers);
			Classifier classifierElem=(Classifier) element;
			classifierElem.setIsAbstract(isAbstract);
		}
		
	}
	protected static void setModifiers(NamedElement importedElement,Class<?> sourceClass)
	{
		setElementModifiersBasedOnModifiersGivenByReflection(importedElement,sourceClass.getModifiers());	
	}
	protected static void setModifiers(NamedElement importedElement, Method sourceMethod)
	{
		setElementModifiersBasedOnModifiersGivenByReflection(importedElement,sourceMethod.getModifiers());	
	}
	protected static void setModifiers(NamedElement importedElement,Field sourceField)
	{
		setElementModifiersBasedOnModifiersGivenByReflection(importedElement,sourceField.getModifiers());	
	}

	protected static boolean localInstanceToBeCreated = false;
	protected static PrimitiveType UML2Integer,UML2Bool,UML2String,UML2Real,UML2UnlimitedNatural;
	protected static Class<?> modelClass=null;
	protected static WeakHashMap<ModelType<?>, ModelTypeInformation> modelTypeInstancesInfo=null;


}
