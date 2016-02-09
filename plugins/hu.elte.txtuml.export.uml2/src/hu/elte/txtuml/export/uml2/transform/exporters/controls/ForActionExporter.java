package hu.elte.txtuml.export.uml2.transform.exporters.controls;

import hu.elte.txtuml.export.uml2.transform.exporters.BlockExporter;

import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.uml2.uml.ActivityNode;

public class ForActionExporter extends AbstractLoopExporter {

	public ForActionExporter(BlockExporter<? extends ActivityNode> blockExporter) {
		super(blockExporter);
	}

	@SuppressWarnings("unchecked")
	public void exportForStatement(ForStatement statement) {

		exportLoop("for", statement.initializers(), statement.getExpression(),
				statement.updaters(), statement.getBody());

	}

}
