package fr.max2.annotated.processor.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.processor.network.model.SimplePacketBuilder;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.EnumSide;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class PacketProcessingUnit
{
	private final ProcessingTools tools;
	private final NetworkProcessingUnit network;
	private final ExecutableElement method;
	private final EnumSide side;
	private Optional<? extends AnnotationMirror> annotation;
	public final ClassName messageClassName;

	public PacketProcessingUnit(ProcessingTools tools, NetworkProcessingUnit context, ExecutableElement packetMethod, EnumSide side)
	{
		this.tools = tools;
		this.network = context;
		this.method = packetMethod;
		this.side = side;
		this.annotation = tools.typeHelper.getAnnotationMirror(packetMethod, this.side.getAnnotationClass().getCanonicalName());
		
		String className = tools.typeHelper.getAnnotationValue(this.annotation, "className").map(anno -> anno.getValue().toString()).orElse("");
		
		int sep = className.lastIndexOf('.');
		
		String packageName = sep == -1 ? context.enclosingClassName.packageName() : className.substring(0, sep);
		
		if (sep != -1)
		{
			className = className.substring(sep + 1);
		}
		else if (className.isEmpty())
		{
			className = context.enclosingClassName.shortName().replace('.', '_') + "_" + this.method.getSimpleName().toString();
		}
		
		this.messageClassName = new ClassName(packageName, className);
	}
	
	public boolean processPacket()
	{
		try
        {
			return this.writePacket();
        }
        catch (IOException e)
        {
        	this.tools.log(Kind.ERROR, "An IOException occured during the generation of the '" + this.messageClassName.qualifiedName() + "' class: " + e.getMessage(), this.method, this.annotation);
        }
		catch (Exception e)
		{
			this.tools.log(Kind.ERROR, "An unexpected exception occured during the generation of the '" + this.messageClassName.qualifiedName() + "' class: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), this.method, this.annotation);
		}
		return false;
	}
	
	private boolean writePacket() throws IOException
	{
		if (!this.method.getModifiers().contains(Modifier.STATIC))
		{
			this.tools.log(Kind.ERROR, "Packet handler must be static", this.method, this.annotation);
			return false;
		}
		
		List<? extends VariableElement> parameters = this.method.getParameters();
		List<? extends VariableElement> messageParameters = parameters.stream().filter(p -> !this.specialValue(p.asType()).isPresent()).collect(Collectors.toList());
		List<DataHandlerParameters> dataHandlers = messageParameters.stream().map(p -> this.tools.handlers.getDataType(p)).collect(Collectors.toList());
		
		SimplePacketBuilder builder = new SimplePacketBuilder(this.tools.elements, messageClassName.packageName());
		
		messageParameters.forEach(f -> this.tools.typeHelper.provideTypeImports(f.asType(), builder::addImport));
		
		for (DataHandlerParameters handler : dataHandlers)
		{
			try
			{
				handler.addInstructions(builder);
				handler.properties.checkUnusedProperties();
			}
			catch (IncompatibleTypeException e)
			{
				this.tools.log(Kind.ERROR, "An IncompatibleTypeException occured on the '" + handler.uniqueName + "" + "' parameter: " + e.getMessage(), this.method, this.annotation);
				return false;
			}
			catch (Exception e)
			{
				this.tools.log(Kind.ERROR, "A '" + e.getClass().getCanonicalName() + "' exception occured during the processing of the '" + handler.uniqueName + "" + "' parameter: " + e.getMessage(), this.method, this.annotation);
				return false;
			}
		}

		String ls = System.lineSeparator();
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", messageClassName.packageName());
		replacements.put("className", this.messageClassName.shortName());
		replacements.put("generatorClass", network.networkClassName.shortName());
		replacements.put("allFields" , messageParameters.stream().map(p -> this.tools.naming.computeFullName(p.asType()) + " " + p.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("fieldsDeclaration", messageParameters.stream().map(p -> "\tprivate " + this.tools.naming.computeFullName(p.asType()) + " " + p.getSimpleName() + ";").collect(Collectors.joining(ls)));
		replacements.put("fieldsInit", messageParameters.stream().map(p -> "\t\tthis." + p.getSimpleName() + " = " + p.getSimpleName() + ";").collect(Collectors.joining(ls)));
		replacements.put("encode", builder.saveInstructions(2).collect(Collectors.joining(ls)));
		replacements.put("decode", builder.loadInstructions(2).collect(Collectors.joining(ls)));
		replacements.put("function", network.enclosingClassName.shortName() + "." + this.method.getSimpleName().toString());
		replacements.put("parameters", parameters.stream().map(p -> this.specialValue(p.asType()).orElse("msg." + p.getSimpleName())).collect(Collectors.joining(", ")));
		replacements.put("messageParameters", messageParameters.stream().map(VariableElement::getSimpleName).collect(Collectors.joining(", ")));
		replacements.put("imports", builder.imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));
		replacements.put("serverPacket", Boolean.toString(this.side.isServer()));
		replacements.put("clientPacket", Boolean.toString(this.side.isClient()));
		replacements.put("receiveSide", this.side.getSimpleName());
		replacements.put("sheduled", this.tools.typeHelper.getAnnotationValue(this.annotation, "runInMainThread").map(anno -> anno.getValue().toString()).orElse("true"));
		
		return this.tools.templates.writeFileWithLog(this.messageClassName.qualifiedName(), "templates/TemplateMessage.jvtp", replacements, this.method, this.annotation);
	}
	
	private Optional<String> specialValue(TypeMirror type)
	{
		TypeElement elem = this.tools.typeHelper.asTypeElement(this.tools.types.asElement(type));
		if (elem != null)
		{
			if (elem.getQualifiedName().contentEquals(ClassRef.FORGE_NETWORK_CONTEXT))
				return Optional.of("ctx");
			
			if (elem.getQualifiedName().contentEquals(ClassRef.SERVER_PLAYER))
				return Optional.of("ctx.getSender()");
		}
		
		return Optional.empty();
	}
}