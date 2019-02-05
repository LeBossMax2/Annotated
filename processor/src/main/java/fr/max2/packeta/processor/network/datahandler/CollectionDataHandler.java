package fr.max2.packeta.processor.network.datahandler;

import java.util.Collection;
import java.util.function.Consumer;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.packeta.api.processor.network.ConstSize;
import fr.max2.packeta.processor.network.DataHandlerParameters;
import fr.max2.packeta.processor.utils.EmptyAnnotationConstruct;
import fr.max2.packeta.processor.utils.NamingUtils;
import fr.max2.packeta.processor.utils.TypeHelper;
import fr.max2.packeta.processor.utils.ValueInitStatus;

public enum CollectionDataHandler implements INamedDataHandler
{
	INSTANCE;
	//TODO [v1.1] put size in parameters
	@Override
	public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		DeclaredType collectionType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
		if (collectionType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		
		TypeMirror contentType = collectionType.getTypeArguments().get(0);
		String typeName = NamingUtils.simplifiedTypeName(contentType);
		
		boolean constSize = params.annotations.getAnnotation(ConstSize.class) != null || params.type.getAnnotation(ConstSize.class) != null;
		
		String elementVarName = params.simpleName + "Element";
		if (!constSize) saveInstructions.accept(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".size()"));
		saveInstructions.accept("for (" + typeName + " " + elementVarName + " : " + params.saveAccessExpr + ")");
		saveInstructions.accept("{");
		
		String lenghtVarName = params.simpleName + "Length";
		String indexVarName = params.simpleName + "Index";
		
		if (constSize)
		{
			loadInstructions.accept("int " + lenghtVarName + " = " + params.getLoadAccessExpr() + ".size();");
		}
		else
		{
			loadInstructions.accept("int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";");
		}
		
		if (params.initStatus.isInitialised())
		{
			loadInstructions.accept(params.getLoadAccessExpr() + ".clear();" );
		}
		else
		{
			params.setLoadedValue(loadInstructions, "new " + NamingUtils.simpleTypeName(params.type, true) + "()"); //TODO [v1.1] use parameters to use the right class
		}
		
		loadInstructions.accept("for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)");
		loadInstructions.accept("{");
		
		DataHandlerParameters contentHandler = params.finder.getDataType(elementVarName, elementVarName, params.getLoadAccessExpr() + ".get(" + indexVarName + ")", (loadInst, value) -> loadInst.accept(params.getLoadAccessExpr() + ".add(" + value + ");"), contentType, EmptyAnnotationConstruct.INSTANCE, ValueInitStatus.UNDEFINED);
		contentHandler.addInstructions(inst -> saveInstructions.accept("\t" + inst), inst -> loadInstructions.accept("\t" + inst), imports);
		
		saveInstructions.accept("}");
		loadInstructions.accept("}");
		
		if (!params.initStatus.isInitialised() && params.loadAccessExpr == null)
		{
			params.setExpr.accept(loadInstructions, params.simpleName); 
		}
	}

	@Override
	public String getTypeName()
	{
		return Collection.class.getCanonicalName();
	}
	
}
