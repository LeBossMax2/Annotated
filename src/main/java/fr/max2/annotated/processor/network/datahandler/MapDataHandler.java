package fr.max2.annotated.processor.network.datahandler;

import java.util.Map;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.EmptyAnnotationConstruct;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum MapDataHandler implements INamedDataHandler
{
	INSTANCE;
	
	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		DeclaredType mapType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
		if (mapType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		DataHandlerUtils.requireDefaultConstructor(params.finder.typeUtils, params.type);
		
		TypeMirror keyFullType = mapType.getTypeArguments().get(0);
		TypeMirror valueFullType = mapType.getTypeArguments().get(1);
		TypeMirror keyType = TypeHelper.shallowErasure(keyFullType, params.finder.elemUtils);
		TypeMirror valueType = TypeHelper.shallowErasure(valueFullType, params.finder.elemUtils);
		DeclaredType type = TypeHelper.replaceTypeArgument(TypeHelper.replaceTypeArgument((DeclaredType)params.type, keyFullType, keyType, params.finder.typeUtils), valueFullType, valueType, params.finder.typeUtils);
		
		String keyVarName = params.uniqueName + "Key";
		String valueVarName = params.uniqueName + "Element";
		String entryVarName = params.uniqueName + "Entry";
		
		String lenghtVarName = params.uniqueName + "Length";
		String indexVarName = params.uniqueName + "Index";
		
		builder.addImport(this.getTypeName());
		
		builder.encoder()
			.add(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".size()"))
			.add("for (Map.Entry<" + NamingUtils.computeFullName(keyFullType) + ", " + NamingUtils.computeFullName(valueFullType) + "> " + entryVarName + " : " + params.saveAccessExpr + ".entrySet())")
			.add("{");
		
		
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";",
			NamingUtils.computeFullName(type) + " " + params.uniqueName + " = new " + NamingUtils.computeSimplifiedName(type) + "();", //TODO [v2.1] use parameters to use the right class
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		

		DataHandlerParameters keyHandler = params.finder.getDataType(keyVarName, entryVarName + ".getKey()", (loadInst, key) -> loadInst.add(NamingUtils.computeFullName(keyType) + " " + keyVarName + " = " + key + ";"), keyType, EmptyAnnotationConstruct.INSTANCE);
		keyHandler.addInstructions(1, builder);
		
		DataHandlerParameters valueHandler = params.finder.getDataType(valueVarName, entryVarName + ".getValue()", (loadInst, value) -> loadInst.add(params.uniqueName + ".put(" + keyVarName + ", " + value + ");"), valueType, EmptyAnnotationConstruct.INSTANCE);
		valueHandler.addInstructions(1, builder);
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		
		params.setExpr.accept(builder.decoder(), params.uniqueName);
	}

	@Override
	public String getTypeName()
	{
		return Map.class.getCanonicalName();
	}
	
}
