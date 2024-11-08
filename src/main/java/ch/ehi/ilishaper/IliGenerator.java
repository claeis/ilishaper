package ch.ehi.ilishaper;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.AbstractClassDef;
import ch.interlis.ili2c.metamodel.AssociationDef;
import ch.interlis.ili2c.metamodel.AssociationPath;
import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.AttributeRef;
import ch.interlis.ili2c.metamodel.AxisAttributeRef;
import ch.interlis.ili2c.metamodel.Constraint;
import ch.interlis.ili2c.metamodel.Container;
import ch.interlis.ili2c.metamodel.Domain;
import ch.interlis.ili2c.metamodel.Element;
import ch.interlis.ili2c.metamodel.Evaluable;
import ch.interlis.ili2c.metamodel.ExistenceConstraint;
import ch.interlis.ili2c.metamodel.Expression;
import ch.interlis.ili2c.metamodel.Expression.Conjunction;
import ch.interlis.ili2c.metamodel.FunctionCall;
import ch.interlis.ili2c.metamodel.MandatoryConstraint;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.NumericalType;
import ch.interlis.ili2c.metamodel.ObjectPath;
import ch.interlis.ili2c.metamodel.ParameterValue;
import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.PathElAbstractClassRole;
import ch.interlis.ili2c.metamodel.PathElAssocRole;
import ch.interlis.ili2c.metamodel.PlausibilityConstraint;
import ch.interlis.ili2c.metamodel.RefSystemRef;
import ch.interlis.ili2c.metamodel.ReferenceType;
import ch.interlis.ili2c.metamodel.RoleDef;
import ch.interlis.ili2c.metamodel.SetConstraint;
import ch.interlis.ili2c.metamodel.StructAttributeRef;
import ch.interlis.ili2c.metamodel.Topic;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.UniqueEl;
import ch.interlis.ili2c.metamodel.UniquenessConstraint;
import ch.interlis.ili2c.metamodel.Unit;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iox_j.validator.ValidationConfig;

public class IliGenerator extends ch.interlis.ili2c.generator.Interlis2Generator {
    public static final String CONFIG_MODEL_NAME = "name";
    public static final String CONFIG_MODEL_VERISONEXPL = "versionExpl";
    public static final String CONFIG_MODEL_ISSUER = "issuer";
    public static final String CONFIG_MODEL_VERISON = "version";
    public static final String CONFIG_MODEL_DOC = "doc";
    public static final String CONFIG_IGNORE = "ignore";
    private List<Model> srcModels=new ArrayList<Model>();
    private java.util.Set<Element> skipElements=new java.util.HashSet<Element>();
    private ValidationConfig trafoConfig=null;
    public void generate(Writer out, TransferDescription td,ValidationConfig trafoConfig,Settings settings) throws Exception
    {
        this.trafoConfig=trafoConfig;
        for(String entry:trafoConfig.getIliQnames()) {
            if(!entry.contains(".")) {
                String modelName=entry;
                Model model=(Model)td.getElement(Model.class, modelName);
                if(model==null) {
                    throw new Exception("Model <"+modelName+"> nicht vorhanden");
                }else {
                    srcModels.add(model);
                }
            }
        }
        collectSkipElements(skipElements,td,srcModels,trafoConfig);
        generate(out, td, false);
    }
    public static void collectSkipElements(Set<Element> skipElements, TransferDescription td, List<Model> srcModels,ValidationConfig trafoConfig) {
        for(Iterator<Model> modelIt=td.iterator();modelIt.hasNext();){
            Model model=modelIt.next();
            collectSkipElements_Helper(skipElements, model, trafoConfig);
        }
    }
    private static void collectSkipElements_Helper(Set<Element> skipElements, ch.interlis.ili2c.metamodel.Element elt,ValidationConfig trafoConfig) {
        String scopedName=elt.getScopedName();
        if(elt instanceof AttributeDef && ValidationConfig.TRUE.equals(trafoConfig.getConfigValue(elt.getScopedName(), CONFIG_IGNORE))) {
            skipElements.add(elt);
        }else if(elt instanceof Topic) {
            if(ValidationConfig.TRUE.equals(trafoConfig.getConfigValue(scopedName, CONFIG_IGNORE))) {
                skipElements.add(elt);
                skipTopicElements(skipElements,(Topic)elt);
            }else {
                Topic topic=(Topic)elt;
                if(skipElements.contains(topic.getExtending())) {
                    skipElements.add(elt);
                    skipTopicElements(skipElements,(Topic)elt);
                }
            }
        }else if(elt instanceof Viewable) {
            if(ValidationConfig.TRUE.equals(trafoConfig.getConfigValue(scopedName, CONFIG_IGNORE))) {
                skipElements.add(elt);
            }else {
                Viewable viewable=(Viewable)elt;
                if(skipElements.contains(viewable.getExtending())) {
                    skipElements.add(elt);
                }
            }
            if(elt instanceof AssociationDef && containsRolesWithSkippedClassRefs(skipElements,(AssociationDef)elt)) {
                skipElements.add(elt);
            }
        }else if (elt instanceof Constraint && containsRefsWithSkippedElements(skipElements,(Constraint)elt)) {
            skipElements.add(elt);
        }else if(ValidationConfig.TRUE.equals(trafoConfig.getConfigValue(scopedName, CONFIG_IGNORE))) {
            skipElements.add(elt);
        }
        if(elt instanceof ch.interlis.ili2c.metamodel.Container) {
            for(Iterator<Element> it=((ch.interlis.ili2c.metamodel.Container) elt).iterator();it.hasNext();) {
                Element el=it.next();
                collectSkipElements_Helper(skipElements, el, trafoConfig);
            }
        }
    }
    @Override
    protected void printElements (Container container,String language)
    {
        if(container instanceof TransferDescription) {
            Class lastClass = null;
            for(Model model:srcModels) {
                lastClass = printElement(container, lastClass, model,language);
            }
        }else {
            super.printElements(container,language);
        }
    }
    @Override
    protected Class printElement(Container container, Class lastClass, ch.interlis.ili2c.metamodel.Element elt,
            String language) {
        String scopedName=elt.getScopedName();
        if(elt instanceof AttributeDef && ValidationConfig.TRUE.equals(trafoConfig.getConfigValue(elt.getScopedName(), CONFIG_IGNORE))) {
            skipElements.add(elt);
            return lastClass;
        }else if(elt instanceof Topic) {
            if(ValidationConfig.TRUE.equals(trafoConfig.getConfigValue(scopedName, CONFIG_IGNORE))) {
                skipElements.add(elt);
                skipTopicElements(skipElements,(Topic)elt);
                return lastClass;
            }
            Topic topic=(Topic)elt;
            if(skipElements.contains(topic.getExtending())) {
                skipElements.add(elt);
                skipTopicElements(skipElements,(Topic)elt);
                return lastClass;
            }
        }else if(elt instanceof Viewable) {
            if(ValidationConfig.TRUE.equals(trafoConfig.getConfigValue(scopedName, CONFIG_IGNORE))) {
                skipElements.add(elt);
                return lastClass;
            }
            Viewable viewable=(Viewable)elt;
            if(skipElements.contains(viewable.getExtending())) {
                skipElements.add(elt);
                return lastClass;
            }
            if(elt instanceof AssociationDef && containsRolesWithSkippedClassRefs(skipElements,(AssociationDef)elt)) {
                skipElements.add(elt);
                return lastClass;
            }
        }else if (elt instanceof Constraint && containsRefsWithSkippedElements(skipElements,(Constraint)elt)) {
            skipElements.add(elt);
            return lastClass;
        }else if(ValidationConfig.TRUE.equals(trafoConfig.getConfigValue(scopedName, CONFIG_IGNORE))) {
            skipElements.add(elt);
            return lastClass;
        }
        return super.printElement(container, lastClass, elt, language);
    }
    private static boolean containsRefsWithSkippedElements(Set<Element> skipElements,Constraint elt) {
        if(elt instanceof UniquenessConstraint) {
            UniquenessConstraint unique=(UniquenessConstraint) elt;
            UniqueEl unqEles = unique.getElements();
            for(ObjectPath path:unqEles.getAttributes()) {
                if(containsRefsWithSkippedElements(skipElements,path)) {
                    return true;
                }
            }
            Evaluable condition = unique.getPreCondition();
            if(containsRefsWithSkippedElements(skipElements,condition)) {
                return true;
            }
            ObjectPath prefix = unique.getPrefix();
            if(containsRefsWithSkippedElements(skipElements,prefix)) {
                return true;
            }
        }else if(elt instanceof MandatoryConstraint) {
            Evaluable condition = elt.getCondition();
            if(containsRefsWithSkippedElements(skipElements,condition)) {
                return true;
            }
        }else if(elt instanceof PlausibilityConstraint) {
            Evaluable condition = elt.getCondition();
            if(containsRefsWithSkippedElements(skipElements,condition)) {
                return true;
            }
        }else if(elt instanceof SetConstraint) {
            Evaluable condition = elt.getCondition();
            if(containsRefsWithSkippedElements(skipElements,condition)) {
                return true;
            }
            Evaluable preCondition = ((SetConstraint) elt).getPreCondition();
            if(containsRefsWithSkippedElements(skipElements,preCondition)) {
                return true;
            }
        }else if(elt instanceof ExistenceConstraint) {
            ObjectPath restrictedAttr = ((ExistenceConstraint) elt).getRestrictedAttribute();
            if(containsRefsWithSkippedElements(skipElements,restrictedAttr)) {
                return true;
            }
            Iterator<ObjectPath> reqIt=((ExistenceConstraint) elt).iteratorRequiredIn();
            while(reqIt.hasNext()) {
                if(containsRefsWithSkippedElements(skipElements,reqIt.next())) {
                    return true;
                }
            }
        }else {
            throw new IllegalArgumentException("unexpected Class "+elt.getClass().getName());
        }
        return false;
    }
    private static boolean containsRefsWithSkippedElements(Set<Element> skipElements,Evaluable ev) {
        if(ev==null) {
            return false;
        }
        if(ev instanceof Expression.Addition) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.Addition)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.Addition)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression.Conjunction) {
            for(Evaluable evN:((Expression.Conjunction) ev).getConjoined()) {
                if(containsRefsWithSkippedElements(skipElements,evN)) {
                    return true;
                }
            }
        }else if(ev instanceof Expression.DefinedCheck) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.DefinedCheck)ev).getArgument())) {
                return true;
            }
        }else if(ev instanceof Expression.Disjunction) {
            for(Evaluable evN:((Expression.Disjunction) ev).getDisjoined()) {
                if(containsRefsWithSkippedElements(skipElements,evN)) {
                    return true;
                }
            }
        }else if(ev instanceof Expression.Division) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.Division)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.Division)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression.Equality) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.Equality)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.Equality)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression.GreaterThan) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.GreaterThan)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.GreaterThan)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression.GreaterThanOrEqual) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.GreaterThanOrEqual)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.GreaterThanOrEqual)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression.Implication) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.Implication)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.Implication)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression.LessThan) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.LessThan)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.LessThan)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression.LessThanOrEqual) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.LessThanOrEqual)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.LessThanOrEqual)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression.Multiplication) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.Multiplication)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.Multiplication)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression.Negation) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.Negation)ev).getNegated())) {
                return true;
            }
        }else if(ev instanceof Expression.Subtraction) {
            if(containsRefsWithSkippedElements(skipElements,((Expression.Subtraction)ev).getLeft())) {
                return true;
            }
            if(containsRefsWithSkippedElements(skipElements,((Expression.Subtraction)ev).getRight())) {
                return true;
            }
        }else if(ev instanceof Expression) {
            throw new IllegalArgumentException("unexpected Class "+ev.getClass().getName());
        }else if(ev instanceof FunctionCall) {
            for(Evaluable evN:((FunctionCall) ev).getArguments()) {
                if(containsRefsWithSkippedElements(skipElements,evN)) {
                    return true;
                }
            }
        }else if(ev instanceof ParameterValue) {
            if(skipElements.contains(((ParameterValue) ev).getParameter())) {
                return true;
            }
        }else if(ev instanceof ObjectPath) {
            return containsObjectPathRefsWithSkippedElements(skipElements,(ObjectPath) ev);
        }
        return false;
    }
    protected static boolean containsObjectPathRefsWithSkippedElements(Set<Element> skipElements,ObjectPath path) {
        PathEl pathEls[]=path.getPathElements();
        for(PathEl pathEl:pathEls) {
            if(pathEl instanceof AttributeRef) {
                final AttributeDef attr = ((AttributeRef) pathEl).getAttr();
                if(skipElements.contains(attr.getContainer()) || skipElements.contains(attr)) {
                    return true;
                }
            }else if(pathEl instanceof AxisAttributeRef) {
                final AttributeDef attr = ((AxisAttributeRef) pathEl).getAttr();
                if(skipElements.contains(attr.getContainer()) || skipElements.contains(attr)) {
                    return true;
                }
            }else if(pathEl instanceof StructAttributeRef) {
                final AttributeDef attr = ((StructAttributeRef) pathEl).getAttr();
                if(skipElements.contains(attr.getContainer()) || skipElements.contains(attr)) {
                    return true;
                }
            }else if(pathEl instanceof AssociationPath) {
                Viewable v=pathEl.getViewable();
                if(skipElements.contains(v)) {
                    return true;
                }
            }else if(pathEl instanceof PathElAbstractClassRole) {
                Viewable v=pathEl.getViewable();
                if(skipElements.contains(v)) {
                    return true;
                }
            }else if(pathEl instanceof PathElAssocRole) {
                Viewable v=pathEl.getViewable();
                if(skipElements.contains(v)) {
                    return true;
                }
            }
        }
        return false;
    }
    private static boolean containsRolesWithSkippedClassRefs(Set<Element> skipElements,AssociationDef assoc) {
        Iterator<Element> rolei = assoc.getAttributesAndRoles();
        while (rolei.hasNext()) {
            Element obj = rolei.next();
            if (obj instanceof RoleDef) {
                RoleDef role = (RoleDef) obj;
                Iterator<ReferenceType> refIt=role.iteratorReference();
                while(refIt.hasNext()) {
                    ReferenceType ref=refIt.next();
                    if(skipElements.contains(ref.getReferred())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private static void skipTopicElements(Set<Element> skipElements,ch.interlis.ili2c.metamodel.Topic topic) {
        java.util.Iterator<Element> eleIt=topic.iterator();
        while(eleIt.hasNext()) {
           Element ele=eleIt.next();
           skipElements.add(ele); 
        }
    }
    @Override
    public void printDocumentation(Element def,String language) {
        if(def!=null && def instanceof Model && srcModels.contains(def)) {
            String doc=trafoConfig.getConfigValue(def.getScopedName(), CONFIG_MODEL_DOC);
            if(doc!=null) {
                printDocumentation(doc);
            }
        }else {
            super.printDocumentation(def, language);
        }
    }
    @Override
    protected void printMetaValues(Object def,ch.ehi.basics.settings.Settings values, String language, String scopedNamePrefix)
    {
        if(def!=null && def instanceof Model) {
            /* Der Modellnamen, die Modellversion und der ilidoc-Kommentar zu
             * diesem Element werden nicht aus der Quelle uebernommen, sondern 
             * aus der Abbildungs-Config
             */
        }else {
            super.printMetaValues(def, values, language, scopedNamePrefix);
        }
        
    }
    @Override
    protected String getModelVersionExpl(Model elt) {
        if(elt instanceof Model && srcModels.contains(elt)) {
            String name=trafoConfig.getConfigValue(elt.getScopedName(), CONFIG_MODEL_VERISONEXPL);
            return name;
        }
        return super.getModelVersionExpl(elt);
    }
    @Override
    protected String getModelIssuer(Model elt) {
        if(elt instanceof Model && srcModels.contains(elt)) {
            String name=trafoConfig.getConfigValue(elt.getScopedName(), CONFIG_MODEL_ISSUER);
            if(name==null) {
                name="mailto:"+System.getProperty("user.name")+"@localhost";
            }
            return name;
        }
        return super.getModelIssuer(elt);
    }
    @Override
    protected String getModelVersion(Model elt) {
        if(elt instanceof Model && srcModels.contains(elt)) {
            String name=trafoConfig.getConfigValue(elt.getScopedName(), CONFIG_MODEL_VERISON);
            if(name==null) {
                java.util.Calendar current=java.util.Calendar.getInstance();
                java.text.DecimalFormat digit4 = new java.text.DecimalFormat("0000");
                java.text.DecimalFormat digit2 = new java.text.DecimalFormat("00");
                name=digit4.format(current.get(java.util.Calendar.YEAR))
                    +"-"+digit2.format(current.get(java.util.Calendar.MONTH)+1)
                    +"-"+digit2.format(current.get(java.util.Calendar.DAY_OF_MONTH));
                
            }
            return name;
        }
        return super.getModelVersion(elt);
    }
    
    @Override
    protected String getElementName(Element elt,String language)
    {
        if(elt instanceof Model && srcModels.contains(elt)) {
            String name=trafoConfig.getConfigValue(elt.getScopedName(), CONFIG_MODEL_NAME);
            if(name==null) {
                throw new IllegalStateException("missing config of new name for model "+elt.getScopedName());
            }
            return name;
        }
        return super.getElementName(elt,language);
    }
    
    @Override
    protected void printRef (Container scope, Element elt,String language)
    {
        Element path[]=elt.getElementPath();
        StringBuffer scopedName=new StringBuffer();
        String sep="";
        for(Element el:path) {
            String name=getElementName(el, language);
            scopedName.append(sep);
            scopedName.append(name);
            sep=".";
        }
        ipw.print(scopedName.toString());
    }
    @Override
    protected Domain getTopicClassOid(Topic topic) {
        return null;
    }
    @Override
    protected Domain getTopicBasketOid(Topic topic) {
        return null;
    }
    @Override
    protected Domain getAbstarctClassDefOid(AbstractClassDef def) {
        return null;
    }
    @Override
    protected Iterator getTopicDependsOn(Topic topic) {
        List<Topic> deps=new ArrayList<Topic>();
        Iterator topicIt=topic.getDependentOn();
        while(topicIt.hasNext()) {
            Topic dep=(Topic)topicIt.next();
            if(skipElements.contains(dep)) {
                // skip it
            }else {
                // keep it
                deps.add(dep);
            }
        }
        return deps.iterator();
    }
    @Override
    protected Unit getTypeUnit(NumericalType type) {
        Unit unit=type.getUnit();
        if(skipElements.contains(unit)) {
            return null;
        }
        return unit;
    }
    
}
