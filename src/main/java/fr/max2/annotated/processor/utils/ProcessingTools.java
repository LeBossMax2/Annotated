package fr.max2.annotated.processor.utils;

import java.util.Optional;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.utils.template.TemplateHelper;

public class ProcessingTools
{
	private final Messager messager;
	
	public final Elements elements;
	public final Types types;
	public final Filer filer;
	public final TypeHelper typeHelper;
	public final NamingUtils naming;
	public final DataHandlerParameters.Finder handlers;
	public final TemplateHelper templates;
	
	public ProcessingTools(ProcessingEnvironment env)
	{
		this.messager = env.getMessager();
		this.elements = env.getElementUtils();
		this.filer = env.getFiler();
		this.types = env.getTypeUtils();
		this.typeHelper = new TypeHelper(this);
		this.naming = new NamingUtils(this);
		this.handlers = new DataHandlerParameters.Finder(this);
		this.templates = new TemplateHelper(this);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg, Element e)
	{
		this.messager.printMessage(kind, msg, e);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg, Element e, Optional<? extends AnnotationMirror> a)
	{
		if (a.isPresent())
			this.messager.printMessage(kind, msg, e, a.get());
		else
			this.log(kind, msg, e);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg, Element e, Optional<? extends AnnotationMirror> a, String property)
	{
		if (a.isPresent())
		{
			Optional<? extends AnnotationValue> value = this.typeHelper.getAnnotationValue(a, property);
			if (value.isPresent())
			{
				this.messager.printMessage(kind, msg, e, a.get(), value.get());
			}
			else
			{
				this.messager.printMessage(kind, msg, e, a.get());
			}
		}
		else
		{
			this.log(kind, msg, e);
		}
	}
}