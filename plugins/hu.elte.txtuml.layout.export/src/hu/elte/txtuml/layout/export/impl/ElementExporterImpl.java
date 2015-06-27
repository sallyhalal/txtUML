package hu.elte.txtuml.layout.export.impl;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import hu.elte.txtuml.layout.export.DiagramType;
import hu.elte.txtuml.layout.export.diagramexporters.ClassDiagramExporter;
import hu.elte.txtuml.layout.export.elementinfo.ElementInfo;
import hu.elte.txtuml.layout.export.elementinfo.ElementType;
import hu.elte.txtuml.layout.export.elementinfo.LinkGroupInfo;
import hu.elte.txtuml.layout.export.elementinfo.LinkInfo;
import hu.elte.txtuml.layout.export.elementinfo.NodeGroupInfo;
import hu.elte.txtuml.layout.export.elementinfo.NodeInfo;
import hu.elte.txtuml.layout.export.interfaces.ElementExporter;
import hu.elte.txtuml.layout.export.interfaces.LinkGroupMap;
import hu.elte.txtuml.layout.export.interfaces.LinkList;
import hu.elte.txtuml.layout.export.interfaces.LinkMap;
import hu.elte.txtuml.layout.export.interfaces.NodeGroupMap;
import hu.elte.txtuml.layout.export.interfaces.NodeList;
import hu.elte.txtuml.layout.export.interfaces.NodeMap;
import hu.elte.txtuml.layout.lang.elements.LayoutAbstractNode;
import hu.elte.txtuml.layout.lang.elements.LayoutElement;
import hu.elte.txtuml.layout.lang.elements.LayoutGroup;
import hu.elte.txtuml.layout.lang.elements.LayoutLink;
import hu.elte.txtuml.layout.lang.elements.LayoutLinkGroup;
import hu.elte.txtuml.layout.lang.elements.LayoutNode;
import hu.elte.txtuml.layout.lang.elements.LayoutNodeGroup;
import hu.elte.txtuml.layout.lang.elements.LayoutNonGroupElement;
import hu.elte.txtuml.layout.lang.elements.LayoutPhantomNode;
import hu.elte.txtuml.layout.lang.statements.Alignment;
import hu.elte.txtuml.layout.lang.statements.Contains;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation;
import hu.elte.txtuml.layout.visualizer.model.RectangleObject;
import hu.elte.txtuml.utils.Pair;

/**
 * Default implementation for {@link ElementExporter}.
 * 
 * @author G�bor Ferenc Kov�cs
 *
 */
public class ElementExporterImpl implements ElementExporter {

	private DiagramType diagramType;
	// TODO check diagram type: when a new element is found, and it is of a
	// certain type (a node or a link), check if that type equals with this
	// field. If this field's value is Unknown, set it to the element's value.
	// If it is not Unknown, and also unequal to the element's type, an has to
	// be shown.
	private final NodeMap nodes;
	private final LinkMap links;
	private final NodeGroupMap nodeGroups;
	private final LinkGroupMap linkGroups;
	private final NodeList phantoms;
	private final LinkList generalizations;
	
	private AtomicLong phantomCounter = new AtomicLong(0);

	public ElementExporterImpl() {
	    this.nodes = NodeMap.create();
	    this.links = LinkMap.create();
	    this.nodeGroups = NodeGroupMap.create();
	    this.linkGroups = LinkGroupMap.create();
	    this.phantoms = NodeList.create();
	    this.generalizations = LinkList.create();
	    this.diagramType = DiagramType.Class;
	}
	
	// unused constructors
    // TODO check if additional constructors are needed
	
	/*public ElementExporterImpl(NodeMap nodes, LinkMap links, NodeGroupMap nodeGroups, LinkGroupMap linkGroups) {
		this(nodes, links, nodeGroups, linkGroups, DiagramType.Unknown);
	}

	public ElementExporterImpl(NodeMap nodes, LinkMap links, NodeGroupMap nodeGroups, LinkGroupMap linkGroups, DiagramType type) {
		this.nodes = nodes;
		this.links = links;
		this.nodeGroups = nodeGroups;
		this.linkGroups = linkGroups;
		this.diagramType = type;
	}*/
	
	@Override
	public DiagramType getDiagramTypeBasedOnElements() {
		return diagramType;
	}
	
	@Override
	public NodeMap getNodes() {
	    NodeMap result = NodeMap.create();
	    
	    nodes.forEach((k, v) -> {
	        if (!v.isPhantom()) {
	            result.put(k, v);
	        }
	    });
	    
	    return result;
	}
	
	@Override
	public NodeList getPhantoms() {
	    return phantoms;
	}
	
	@Override
	public LinkMap getLinks() {
	    return links;
	}
	
	@Override
	public Set<RectangleObject> getNodesAsObjects() {
	    return nodes.convert();
	}
	
    @Override
    public Set<LineAssociation> getLinksAsLines() {
        Set<LineAssociation> res = new HashSet<>();
        
        res.addAll(links.convert());
        for (LinkInfo gen : generalizations) {
            res.add(gen.convert());
        }
        
        return res;
    }	
	
	@Override
	public ElementInfo exportElement(Class<? extends LayoutElement> elementClass) {
        ElementInfo info;

        info = nodes.get(elementClass);
        if (info != null) {
            return info;
        }

        info = links.get(elementClass);
        if (info != null) {
            return info;
        }

        info = nodeGroups.get(elementClass);
        if (info != null) {
            if (info.asNodeGroupInfo().beingExported()) {
                // TODO show error
                return ElementInfo.createInvalid(elementClass);
            }
            
            return info;
        }

        info = linkGroups.get(elementClass);
        if (info != null) {
            if (info.asLinkGroupInfo().beingExported()) {
                // TODO show error
                return ElementInfo.createInvalid(elementClass);
            }
            
            return info;
        }    
        
        info = exportNewNode(elementClass);
        if (info != null) {
            return info;
        }

        info = exportNewPhantom(elementClass);
        if (info != null) {
            return info;
        }
        
        info = exportNewLink(elementClass);
        if (info != null) {
            return info;
        }
        
        info = exportNewNodeGroup(elementClass);
        if (info != null) {
            return info;
        }

        info = exportNewLinkGroup(elementClass);
        if (info != null) {
            return info;
        }    

        return ElementInfo.createInvalid(elementClass);
	}

	@Override
	public ElementInfo exportNonGroupElement(Class<? extends LayoutNonGroupElement> elementClass) {
		ElementInfo info;

		info = nodes.get(elementClass);
		if (info != null) {
			return info;
		}

		info = links.get(elementClass);
		if (info != null) {
			return info;
		}

		info = exportNewNode(elementClass);
		if (info != null) {
			return info;
		}

        info = exportNewPhantom(elementClass);
        if (info != null) {
            return info;
        }
		
		info = exportNewLink(elementClass);
		if (info != null) {
			return info;
		}

		return ElementInfo.createInvalid(elementClass);
	}
	
    @Override
    public ElementInfo exportGroupElement(Class<? extends LayoutGroup> elementClass) {
        ElementInfo info;

        info = nodeGroups.get(elementClass);
        if (info != null) {
            if (info.asNodeGroupInfo().beingExported()) {
                // TODO show error
                return ElementInfo.createInvalid(elementClass);
            }
            
            return info;
        }

        info = linkGroups.get(elementClass);
        if (info != null) {
            if (info.asLinkGroupInfo().beingExported()) {
                // TODO show error
                return ElementInfo.createInvalid(elementClass);
            }
            
            return info;
        }

        info = exportNewNodeGroup(elementClass);
        if (info != null) {
            return info;
        }

        info = exportNewLinkGroup(elementClass);
        if (info != null) {
            return info;
        }

        return ElementInfo.createInvalid(elementClass);
    }

	@Override
	public ElementInfo exportNode(Class<? extends LayoutNode> nodeClass) {
		ElementInfo info;

		info = nodes.get(nodeClass);
		if (info != null) {
			return info;
		}

		info = exportNewNode(nodeClass);
		if (info != null) {
			return info;
		}

		return ElementInfo.createInvalid(nodeClass);
	}

    @Override
    public ElementInfo exportLink(Class<? extends LayoutLink> linkClass) {
        ElementInfo info;

        info = links.get(linkClass);
        if (info != null) {
            return info;
        }

        info = exportNewLink(linkClass);
        if (info != null) {
            return info;
        }

        return ElementInfo.createInvalid(linkClass);
    }
    
    @Override
    public ElementInfo exportNodeGroup(Class<? extends LayoutNodeGroup> nodeGroupClass) {
        ElementInfo info;

        info = nodeGroups.get(nodeGroupClass);
        if (info != null) {
            return info;
        }

        info = exportNewNodeGroup(nodeGroupClass);
        if (info != null) {
            return info;
        }

        return ElementInfo.createInvalid(nodeGroupClass);
    }

    @Override
    public ElementInfo exportLinkGroup(Class<? extends LayoutLinkGroup> linkGroupClass) {
        ElementInfo info;

        info = linkGroups.get(linkGroupClass);
        if (info != null) {
            return info;
        }

        info = exportNewLinkGroup(linkGroupClass);
        if (info != null) {
            return info;
        }

        return ElementInfo.createInvalid(linkGroupClass);
    }
	
    @Override
    public ElementInfo exportPhantom(Class<? extends LayoutPhantomNode> phantom) {
        ElementInfo info;

        info = nodes.get(phantom);
        if (info != null) {
            return info;
        }

        info = exportNewPhantom(phantom);
        if (info != null) {
            return info;
        }

        return ElementInfo.createInvalid(phantom);
    }
    
    @Override
    public ElementInfo exportAnonNodeGroup(Class<? extends LayoutAbstractNode>[] abstractNodes) {
        NodeGroupInfo info = NodeGroupInfo.create(null, null, null);
        
        for (Class<? extends LayoutAbstractNode> abstractNode : abstractNodes) {
            ElementInfo innerInfo = exportElement(abstractNode);
            ElementType innerType = innerInfo.getType();
            
            if (innerType.equals(ElementType.Node)) {
                info.addNode(innerInfo.asNodeInfo());
            
            } else if (innerType.equals(ElementType.NodeGroup)) {
                for (NodeInfo innerNode : innerInfo.asNodeGroupInfo().getAllNodes().values()) {
                    info.addNode(innerNode);
                }
           
            } else {
                // TODO show error
                return ElementInfo.createInvalid(null);
            }
        }

        return info;
    }
    
	@SuppressWarnings("unchecked")
	private NodeInfo exportNewNode(Class<? extends LayoutElement> cls) {
		if (ClassDiagramExporter.isNode(cls)) {
			NodeInfo info = NodeInfo.create(cls, DiagramType.Class,
					asString(cls));
			nodes.put((Class<? extends LayoutNode>) cls, info);
			return info;
		}
		// TODO add more diag types
		return null;
	}
	
    @SuppressWarnings("unchecked")
    private NodeInfo exportNewPhantom(Class<? extends LayoutElement> cls) {
        if (isPhantom(cls)) {
            NodeInfo info = NodeInfo.create(cls, DiagramType.Class, "#phantom_" + phantomCounter.addAndGet(1));
            nodes.put((Class<? extends LayoutNode>) cls, info);
            phantoms.add(info);
            
            return info;
        }
        
        return null;
    }	

	@SuppressWarnings("unchecked")
	private LinkInfo exportNewLink(Class<? extends LayoutElement> cls) {
		if (ClassDiagramExporter.isLink(cls)) {
			Pair<Class<? extends LayoutNode>, Class<? extends LayoutNode>> p = ClassDiagramExporter
					.startAndEndOfLink(cls);

			LinkInfo info = LinkInfo.create(cls, DiagramType.Class,
					asString(cls), exportNode(p.getKey()).asNodeInfo(),
					exportNode(p.getValue()).asNodeInfo());
			links.put((Class<? extends LayoutLink>) cls, info);
			return info;
		}
		// TODO add more diag types
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private NodeGroupInfo exportNewNodeGroup(Class<? extends LayoutElement> cls) {
	    if (isNodeGroup(cls)) {
	        NodeGroupInfo info = NodeGroupInfo.create(cls, DiagramType.Class, asString(cls));
	        nodeGroups.put((Class<? extends LayoutNodeGroup>) cls, info);
	        info.setBeingExported(true);
	        
            for (Annotation annot : cls.getAnnotations()) {
                if (isOfType(Contains.class, annot)) {
                    
                    for (Class<? extends LayoutElement> containedClass : cls.getAnnotation(Contains.class).value()) {
                        ElementInfo innerInfo = exportElement(containedClass);
                        ElementType innerType = innerInfo.getType();
                        
                        if (innerType.equals(ElementType.Node)) {
                            info.addNode(innerInfo.asNodeInfo());
                            
                        } else if (innerType.equals(ElementType.NodeGroup)) {                            
                            for (NodeInfo node : innerInfo.asNodeGroupInfo().getAllNodes().values()) {
                                info.addNode(node);
                            }
                            
                        } else {
                            // TODO show error
                        }
                    }
                    
                } else if (isOfType(Alignment.class, annot)) {
                    info.setAlignment(((Alignment) annot).value());
                
                } else {
                    // TODO show warning
                }
            }
	        
            info.setBeingExported(false);
	        return info;
	    }
	    
	    return null;
	}
	
	@SuppressWarnings("unchecked")
    private LinkGroupInfo exportNewLinkGroup(Class<? extends LayoutElement> cls) {
        if (isLinkGroup(cls)) {
            LinkGroupInfo info = LinkGroupInfo.create(cls, DiagramType.Class, asString(cls));
            linkGroups.put((Class<? extends LayoutLinkGroup>) cls, info);
            info.setBeingExported(true);
            
            for (Annotation annot : cls.getAnnotations()) {
                
                if (isOfType(Contains.class, annot)) {    
                    for (Class<? extends LayoutElement> containedClass : cls.getAnnotation(Contains.class).value()) {
                        ElementInfo innerInfo = exportElement(containedClass);
                        ElementType innerType = innerInfo.getType();
                        
                        if (innerType.equals(ElementType.Link)) {
                            info.addLink(innerInfo.asLinkInfo());
                            
                        } else if (innerType.equals(ElementType.LinkGroup)) {
                            for (LinkInfo link : innerInfo.asLinkGroupInfo().getAllLinks().values()) {
                                info.addLink(link);
                            }
                            
                        } else {
                            // TODO show error
                        }
                    }
                    
                } else {
                    // TODO show warning
                }
            }
            
            info.setBeingExported(false);
            return info;
        }
        
        return null;
    }
	
    @Override
    public NodeInfo createPhantom() {
        NodeInfo newPhantom = NodeInfo.create(null, null, "#phantom_" + phantomCounter.addAndGet(1));
        phantoms.add(newPhantom);
        
        return newPhantom;
    }
    
    @Override
    public void exportGeneralization(Class<? extends LayoutNode> base,
            Class<? extends LayoutNode> derived)
    {   
        NodeInfo baseInfo = exportNode(base).asNodeInfo();
        NodeInfo derivedInfo = exportNode(derived).asNodeInfo();
        
        LinkInfo info = LinkInfo.createGeneralization(null, baseInfo.toString() + "_" + derivedInfo.toString(),
                baseInfo, derivedInfo);
        
        generalizations.add(info);
    }
    
    @Override
    public void exportImpliedLinks() {
        ClassDiagramExporter classDiagramExporter = new ClassDiagramExporter(this);
        for (NodeInfo info : nodes.values()) {
            classDiagramExporter.exportAssociationsStartingFromThisNode(info.getElementClass());
        }
    }
	
	// helper functions
    
    private String asString(Class<?> cls) {
        return cls.getCanonicalName();
    }
	
	private boolean isPhantom(Class<?> cls) {
	    return LayoutPhantomNode.class.isAssignableFrom(cls);
	}
	
    private boolean isNodeGroup(Class<?> cls) {
        return LayoutNodeGroup.class.isAssignableFrom(cls);
    }
    
    private boolean isLinkGroup(Class<?> cls) {
        return LayoutLinkGroup.class.isAssignableFrom(cls);
    }

    private boolean isOfType(Class<? extends Annotation> annotationClass, Annotation annot) {
        return annot.annotationType() == annotationClass;
    }
    
}
