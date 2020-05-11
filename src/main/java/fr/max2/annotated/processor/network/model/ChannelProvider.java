package fr.max2.annotated.processor.network.model;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.api.processor.network.DelegateChannel;
import fr.max2.annotated.api.processor.network.GenerateChannel;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.exceptions.IOConsumer;

public enum ChannelProvider
{
	GENERATED(GenerateChannel.class)
	{
		@Override
		public boolean getNetworkClassContent(ProcessingTools tools, IImportClassBuilder<?> imports, IOConsumer<String> content, TypeElement enclosingClass, ClassName enclosingClassName, ClassName networkClassName, @Nullable String modId, Optional<? extends AnnotationMirror> annotation)
		{
			imports.addImport("java.util.function.Consumer");
			imports.addImport("java.util.Map");
			imports.addImport("java.util.TreeMap");
			imports.addImport("net.minecraft.util.ResourceLocation");
			imports.addImport("net.minecraftforge.eventbus.api.EventPriority");
			imports.addImport("net.minecraftforge.fml.network.NetworkRegistry");
			
			GenerateChannel generatorData = enclosingClass.getAnnotation(GenerateChannel.class);
			
			String channelName = generatorData.channelName();
			if (!channelName.contains(":"))
			{
				if (modId == null)
				{
					tools.log(Kind.ERROR, "Couldn't find @Mod annotation in the package '" + enclosingClassName.packageName() + "'", enclosingClass, annotation);
					return false;
				}
				if (channelName.isEmpty())
					channelName = enclosingClassName.shortName().toLowerCase();
				
				channelName = modId + ":" + channelName;
			}
			
			if (!channelName.toLowerCase().equals(channelName))
			{
				tools.log(Kind.WARNING, "The channel name '" + channelName + "' chould be in lower case, lower case is enforced", enclosingClass, annotation, "channelName");
				channelName = channelName.toLowerCase();	
			}
			
			Map<String, String> replacements = new HashMap<>();
			replacements.put("channelName", channelName);
			replacements.put("protocolVersion", generatorData.protocolVersion());
			
			return tools.templates.readWithLog("templates/TemplateGeneratedChannel.jvtp", replacements, content, enclosingClass, annotation);
		}
	},
	DELEGATED(DelegateChannel.class)
	{
		@Override
		public boolean getNetworkClassContent(ProcessingTools tools, IImportClassBuilder<?> imports, IOConsumer<String> content, TypeElement enclosingClass, ClassName enclosingClassName, ClassName networkClassName, @Nullable String modId, Optional<? extends AnnotationMirror> annotation)
		{
			DelegateChannel delegatorData = enclosingClass.getAnnotation(DelegateChannel.class);
			TypeElement delegatedClass = tools.elements.getTypeElement(delegatorData.value());
			
			if (delegatedClass == null)
			{
				tools.log(Kind.ERROR, "Unable to find the delegated class '" + delegatorData.value() + "'", enclosingClass, annotation, "value");
				return false;
			}
			
			ClassName delegatedName = tools.naming.buildClassName(delegatedClass);
			
			ClassName delegatedNetworkClassName = new ClassName(delegatedName.packageName(), delegatedName.shortName().replace('.', '_') + "Network");

			imports.addImport(delegatedNetworkClassName);
			
			Map<String, String> replacements = new HashMap<>();
			replacements.put("className", networkClassName.shortName());
			replacements.put("delegateClass", delegatedNetworkClassName.shortName());

			return tools.templates.readWithLog("templates/TemplateDelegatedChannel.jvtp", replacements, content, enclosingClass, annotation);
		}
	};
	
	private final Class<? extends Annotation> annotation;

	private ChannelProvider(Class<? extends Annotation> annotation)
	{
		this.annotation = annotation;
	}
	
	public Class<? extends Annotation> getAnnotationClass()
	{
		return annotation;
	}
	
	public abstract boolean getNetworkClassContent(ProcessingTools tools, IImportClassBuilder<?> imports, IOConsumer<String> content, TypeElement enclosingClass, ClassName enclosingClassName, ClassName networkClassName, @Nullable String modId, Optional<? extends AnnotationMirror> annotation);
}