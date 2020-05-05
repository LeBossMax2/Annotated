package fr.max2.annotated.processor.network.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import fr.max2.annotated.processor.utils.TypeHelper;

public class SimplePacketBuilder implements IPacketBuilder
{
	public Set<String> imports = new TreeSet<>();
	public List<String> fields = new ArrayList<>();
	private SimpleFunctionBuilder saveFunction = new SimpleFunctionBuilder();
	private SimpleFunctionBuilder loadFunction = new SimpleFunctionBuilder();
	
	private Elements elemUtils;
	private PackageElement packetPackage;
	
	public SimplePacketBuilder(Elements elemUtils, PackageElement packetPackage)
	{
		this.elemUtils = elemUtils;
		this.packetPackage = packetPackage;
	}

	@Override
	public IPacketBuilder addImport(String className)
	{
		TypeElement type = elemUtils.getTypeElement(className);
		if (!packetPackage.equals(TypeHelper.getPackage(type)))
		{
			imports.add(className);
		}
		return this;
	}

	@Override
	public IFunctionBuilder save()
	{
		return this.saveFunction;
	}

	@Override
	public IFunctionBuilder load()
	{
		return this.loadFunction;
	}
	
	private static String indentString(String str, int indent)
	{
		if (indent == 0)
			return str;

	    final char[] array = new char[indent];
	    for (int i = 0; i < indent; i++)
	    {
	    	array[i] = '\t';
	    }
		
		return new String(array) + str;
	}
	
	public Stream<String> saveInstructions(int indent)
	{
		return this.saveFunction.instructions.stream().map(str -> indentString(str, indent));
	}
	
	public Stream<String> loadInstructions(int indent)
	{
		return this.loadFunction.instructions.stream().map(str -> indentString(str, indent));
	}
	
	private class SimpleFunctionBuilder implements IFunctionBuilder
	{
		private List<String> instructions = new ArrayList<>();
		private int indent;

		@Override
		public IPacketBuilder end()
		{
			return SimplePacketBuilder.this;
		}

		@Override
		public IFunctionBuilder add(String... instructions)
		{
			for (String instruction : instructions)
			{
				this.instructions.add(indentString(instruction, this.indent));
			}
			return this;
		}

		@Override
		public IFunctionBuilder indent(int indent)
		{
			this.indent += indent;
			
			if (this.indent < 0)
			{
				throw new IllegalStateException("Negative indent");
			}
			
			return this;
		}
		
	}
}
