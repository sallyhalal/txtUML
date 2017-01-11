package hu.elte.txtuml.xtxtuml.validation;

import hu.elte.txtuml.xtxtuml.xtxtUML.TUAttribute
import hu.elte.txtuml.xtxtuml.xtxtUML.TUExternality
import org.eclipse.xtext.validation.Check

import static hu.elte.txtuml.xtxtuml.validation.XtxtUMLIssueCodes.*
import static hu.elte.txtuml.xtxtuml.xtxtUML.XtxtUMLPackage.Literals.*

class XtxtUMLAttributeValidator extends XtxtUMLConnectorValidator {
	
	@Check
	def checkAttributeIsNotStatic(TUAttribute attr) {
		if (attr.prefix.isStatic) {
			error("Attributes in txtUML cannot be static.", attr.prefix, TU_ATTRIBUTE_OR_OPERATION_DECLARATION_PREFIX__STATIC, ATTRIBUTE_IS_STATIC);
		}
	}
	
	@Check
	def checkAttributeDoesNotHaveExternalBody(TUAttribute attr) {
		if (attr.prefix.externality == TUExternality.EXTERNAL_BODY) {
			error("Attributes cannot be marked to have external body.", attr.prefix, TU_ATTRIBUTE_OR_OPERATION_DECLARATION_PREFIX__EXTERNALITY, ATTRIBUTE_HAS_EXTERNAL_BODY);
		}
	}
	
}
