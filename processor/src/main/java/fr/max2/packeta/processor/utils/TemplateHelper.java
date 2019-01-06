package fr.max2.packeta.processor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaFileObject;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

import fr.max2.packeta.processor.network.PacketProcessor;

public class TemplateHelper
{

	
	public static void writeFileFromTemplate(ProcessingEnvironment env, String className, String templateFile, Map<String, String> replacements) throws IOException
	{
		try
		{
			env.getMessager().printMessage(Kind.NOTE, "Generation file '" + className + "' from tmplate '" + templateFile + "'");
			
			writeFileFromTemplateImpl(env.getFiler(), className, templateFile, replacements);
		}
		catch (IOException e)
		{
			env.getMessager().printMessage(Kind.ERROR, "An error occured during the generation of the file '" + className + "' from tmplate '" + templateFile + "'");
			throw e;
		}
		
	}
	
	private static void writeFileFromTemplateImpl(Filer filer, String className, String templateFile, Map<String, String> replacements) throws IOException
	{
		JavaFileObject file = filer.createSourceFile(className);
		Writer writer = file.openWriter();
		
		try (InputStream fileStream = PacketProcessor.class.getClassLoader().getResourceAsStream(templateFile))
		{
			new BufferedReader(new InputStreamReader(fileStream)).lines().map(line -> mapKeys(line, replacements) + System.lineSeparator()).forEach(ExceptionUtils.wrapIOExceptions(writer::write));
		}
		
		writer.close();
		
	}
	
	private static String mapKeys(String content, Map<String, String> replacements)
	{
		Pattern p = Pattern.compile("\\$\\{(.+?)\\}"); //	${key}
		Matcher m = p.matcher(content);
		
		StringBuffer sb = new StringBuffer();
		while (m.find())
		{
			String key = m.group(1);
			String rep = replacements.get(key);
			
			try
			{
				m.appendReplacement(sb, rep == null ? key : rep);
			}
			catch (RuntimeException e)
			{
				throw new RuntimeException("Unable tu replace the value of '" + key + "' in line '" + content + "' with '" + rep + "'", e);
			}
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
}
