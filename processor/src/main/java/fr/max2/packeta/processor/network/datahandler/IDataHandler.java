package fr.max2.packeta.processor.network.datahandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.packeta.processor.network.DataHandlerParameters;

public interface IDataHandler
{
	void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports);
	
	Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils);
	
	//TODO [v1.1] boolean isMultiline(DataHandlerParameters params);
}