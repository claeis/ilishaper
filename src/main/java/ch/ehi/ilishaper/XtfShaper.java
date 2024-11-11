package ch.ehi.ilishaper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.metamodel.AssociationDef;
import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.CompositionType;
import ch.interlis.ili2c.metamodel.Container;
import ch.interlis.ili2c.metamodel.Element;
import ch.interlis.ili2c.metamodel.EnumTreeValueType;
import ch.interlis.ili2c.metamodel.EnumerationType;
import ch.interlis.ili2c.metamodel.Evaluable;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.ObjectPath;
import ch.interlis.ili2c.metamodel.ObjectType;
import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.PredefinedModel;
import ch.interlis.ili2c.metamodel.RoleDef;
import ch.interlis.ili2c.metamodel.Topic;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Type;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.ili2c.metamodel.ViewableTransferElement;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.xtf.XtfModel;
import ch.interlis.iox.EndBasketEvent;
import ch.interlis.iox.EndTransferEvent;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxLogging;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox.StartBasketEvent;
import ch.interlis.iox.StartTransferEvent;
import ch.interlis.iox_j.PipelinePool;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.ValidationConfig;
import ch.interlis.iox_j.validator.Validator;
import ch.interlis.iox_j.validator.Value;

public class XtfShaper {

    private List<Model> srcModels=new ArrayList<Model>();
    private List<Model> destModels=new ArrayList<Model>();
    private java.util.Set<Element> skipElements=new java.util.HashSet<Element>();
    private java.util.List<IoxEvent> out=new java.util.ArrayList<IoxEvent>();
    private boolean ignoreBasket=false;
    private boolean letThroughBasket=false;
    private TransferDescription td=null;
    private ValidationConfig trafoConfig=null;
    private Validator validator=null;                
    public XtfShaper(TransferDescription td,ValidationConfig trafoConfig,Settings settings)
    {
        this.td=td;
        this.trafoConfig=trafoConfig;
        for(String entry:trafoConfig.getIliQnames()) {
            if(!entry.contains(".")) {
                String modelName=entry;
                Model model=(Model)td.getElement(Model.class, modelName);
                if(model==null) {
                    throw new IllegalStateException("Model <"+modelName+"> nicht vorhanden");
                }
                srcModels.add(model);
                String destModelName=getElementName(model);
                Model destModel=(Model)td.getElement(Model.class,destModelName);
                if(destModel==null) {
                    throw new IllegalStateException("Model <"+destModelName+"> nicht vorhanden");
                }
                destModels.add(destModel);
            }
        }
        {
            IoxValidationConfig validationConfig=new ValidationConfig();
            IoxLogging errs=new ch.interlis.iox_j.logging.Log2EhiLogger();
            LogEventFactory errFact=new LogEventFactory();; 
            PipelinePool pipelinePool=new PipelinePool();
            validator=new Validator(td,validationConfig,errs, errFact, pipelinePool, settings);
        }
        IliGenerator.collectSkipElements(skipElements,td,srcModels,trafoConfig);
    }
    public void close() {
    }
    private String getElementName(Element elt)
    {
        if(elt instanceof Model && srcModels.contains(elt)) {
            String name=trafoConfig.getConfigValue(elt.getScopedName(), IliGenerator.CONFIG_MODEL_NAME);
            if(name==null) {
                throw new IllegalStateException("missing config of new name for model "+elt.getScopedName());
            }
            return name;
        }
        return elt.getName();
    }
    private String getDestScopedName(Element elt)
    {
        Element path[]=elt.getElementPath();
        StringBuffer scopedName=new StringBuffer();
        String sep="";
        for(Element el:path) {
            String name=getElementName(el);
            scopedName.append(sep);
            scopedName.append(name);
            sep=".";
        }
        return scopedName.toString();
    }
    public XtfModel[] buildModelList(TransferDescription td){
        ArrayList modelv=new ArrayList();
        Iterator modeli=td.iterator();
        while(modeli.hasNext()){
            Object modelo=modeli.next();
            if(modelo instanceof PredefinedModel){
                continue;
            }
            if(modelo instanceof Model){
                if(srcModels.contains(modelo)) {
                    // skip it
                }else if(!skipElements.contains(modelo)) {
                    modelv.add(modelo);
                }
            }
        }
        XtfModel[] ret=new XtfModel[modelv.size()];
        for(int i=0;i<modelv.size();i++){
            Model model=(Model)modelv.get(i);
            ret[i]=new XtfModel();
            ret[i].setName(model.getName());
            String version=model.getModelVersion();
            ret[i].setVersion(version==null?"":version);
            String issuer=model.getIssuer();
            ret[i].setUri(issuer==null?"":issuer);
        }
        return ret;
    }

    public void addInput(IoxEvent srcEvent) {
        if(srcEvent instanceof StartTransferEvent){
            // ignore; caller should write start/end event, so that he can merge
        }else if(srcEvent instanceof StartBasketEvent){
            StartBasketEvent startBasket=(StartBasketEvent)srcEvent;
            String topicName=startBasket.getType();
            Topic topic=(Topic) td.getElement(topicName);
            if(topic==null) {
                letThroughBasket=true;
                out.add(srcEvent);
            }else if(skipElements.contains(topic)) {
                ignoreBasket=true;
            }else {
                letThroughBasket=false;
                ignoreBasket=false;
                String destName=getDestScopedName(topic);
                StartBasketEvent destEvent=new ch.interlis.iox_j.StartBasketEvent(destName,Long.toString(mapId(startBasket.getBid())));
                out.add(destEvent);
            }
        }else if(srcEvent instanceof ObjectEvent){
            if(ignoreBasket) {
                // ignore object
            }else if(letThroughBasket) {
                out.add(srcEvent);
            }else {
                // convert obect
                IomObject srcIomObj = ((ObjectEvent) srcEvent).getIomObject();
                mapObject(srcIomObj);
            }
        }else if(srcEvent instanceof EndBasketEvent){
            EndBasketEvent destEvent=new ch.interlis.iox_j.EndBasketEvent();
            if(ignoreBasket) {
                
            }else {
                out.add(destEvent);
            }
        }else if(srcEvent instanceof EndTransferEvent){
            // ignore; caller should write start/end event, so that he can merge
        }
        
    }

    private java.util.Map<String,Long> src2dest=new java.util.HashMap<String,Long>();
    private long mapId(String srcId) {
        Long destId=src2dest.get(srcId);
        if(destId==null) {
            destId=destOid++;
            src2dest.put(srcId,destId);
        }
        return destId;
    }
    private void mapObject(IomObject srcIomObj) {
        String className=srcIomObj.getobjecttag();
        Viewable viewable=(Viewable)td.getElement(className);
        if(!skipElements.contains(viewable)) {
            IomObject destIomObj=translateObject(srcIomObj);
            if(destIomObj!=null) {
                ObjectEvent destEvent=new ch.interlis.iox_j.ObjectEvent(destIomObj);
                out.add(destEvent);
            }
        }
    }
    private long destOid=1;
    private IomObject translateObject(IomObject srcIomObj) {
        Iom_jObject destIomObj=null;
        String srcClassName=srcIomObj.getobjecttag();
        Viewable srcClass=(Viewable)td.getElement(srcClassName);
        if(!skipElements.contains(srcClass)) {
            {
                String filterText=trafoConfig.getConfigValue(srcClass.getScopedName(), IliGenerator.CONFIG_VIEWABLE_FILTER);
                Evaluable filter = null;
                if(filterText!=null) {
                    try {
                        filter = ch.interlis.ili2c.Main.parseExpression(srcClass, PredefinedModel.getInstance().BOOLEAN.getType(),filterText,srcClass.getScopedName()+":"+IliGenerator.CONFIG_VIEWABLE_FILTER);
                    } catch (Ili2cException e) {
                        EhiLogger.logError("failed to parse filter <"+filterText+">",e);
                    }
                }
                if(filter!=null) {
                    Value result = validator.evaluateExpression(null, null, srcClassName, srcIomObj, filter, null);
                    if (result.skipEvaluation() || !result.isTrue()) {
                        return null; // skip it
                    }
                }
            }
            String destClassName=getDestScopedName(srcClass);
            Viewable destClass=(Viewable)td.getElement(destClassName);
            if(destClass instanceof AssociationDef && !(((AssociationDef) destClass).getOid()!=null || ((AssociationDef) destClass).isIdentifiable())) {
                destIomObj=new Iom_jObject(destClassName,null);
            }else {
                destIomObj=new Iom_jObject(destClassName,Long.toString(mapId(srcIomObj.getobjectoid())));
            }
            // handle attrs
            Iterator iter = destClass.getAttributesAndRoles2();
            while (iter.hasNext()) {
                ViewableTransferElement destProp = (ViewableTransferElement)iter.next();
                if (destProp.obj instanceof AttributeDef) {
                    AttributeDef destAttr = (AttributeDef) destProp.obj;
                    if(!destAttr.isTransient()){
                        Type proxyType=destAttr.getDomain();
                        if(proxyType!=null && (proxyType instanceof ObjectType)){
                            // skip implicit particles (base-viewables) of views
                        }else{
                            if(!skipElements.contains(destAttr)) {
                                translateAttrValue(destIomObj,srcIomObj,destAttr);
                            }
                        }
                    }
                }
                if (destProp.obj instanceof RoleDef) {
                    RoleDef destRole = (RoleDef) destProp.obj;
                    if(!skipElements.contains(destRole)) {
                        String destRoleName = destRole.getName();
                        if(srcIomObj.getattrvaluecount(destRoleName)>0){
                            IomObject structValue = srcIomObj.getattrobj(destRoleName, 0);
                            String ref=structValue.getobjectrefoid();
                            if(destModels.contains(destRole.getDestination().getContainer(Model.class))) {
                               ref=Long.toString(mapId(ref)); 
                               structValue.setobjectrefoid(ref);
                            }
                            if(!structValue.getobjecttag().equals(Iom_jObject.REF)) {
                                structValue=translateObject(structValue);
                            }
                            if(structValue!=null) {
                                destIomObj.addattrobj(destRoleName,structValue);
                            }
                        }
                    }
                }
            }
            
        }
        return destIomObj;
    }
    private void translateAttrValue(IomObject destIomObj,IomObject srcIomObj, AttributeDef attr) {
        String attrName=attr.getName();
        int attrc=srcIomObj.getattrvaluecount(attrName);
        if(attrc==0){
            return;
        }
        boolean isCompType=attr.getDomain() instanceof CompositionType ? true :false;
        AttributeDef destAttr=(AttributeDef)attr.getTranslationOfOrSame();
        String destAttrName=destAttr.getName();
        ArrayList<Object> attrValues=new ArrayList<Object>();
        for(int attri=0;attri<attrc;attri++){
            String attrValue=srcIomObj.getattrprim(attrName,attri);
            if(attrValue!=null){
                attrValues.add(attrValue);
            }else{
                IomObject structValue=srcIomObj.getattrobj(attrName,attri);
                attrValues.add(structValue);
            }
        }
        for(int attri=0;attri<attrc;attri++){
            Object attrValue=attrValues.get(attri);
            if(attrValue!=null){
                if(attrValue instanceof String){
                    destIomObj.setattrvalue(destAttrName, (String)attrValue);
                }else{
                    IomObject structValue=(IomObject)attrValue;
                    if(isCompType){
                        // STRUCTURE
                        structValue=translateObject(structValue);
                        if(structValue!=null) {
                            destIomObj.addattrobj(destAttrName,structValue);
                        }
                    }else {
                        // COORD,  ...
                        destIomObj.addattrobj(destAttrName,structValue);
                    }
                }
            }
        }
    }
    public IoxEvent getMappedObject() {
        if(out.size()==0) {
            return null;
        }
        IoxEvent ret=out.remove(0);
        return ret;
    }
}
