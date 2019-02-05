package fr.max2.packeta.processor.utils.model.element;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

public class TestingPrimitiveType extends TestingType implements PrimitiveType
{
	
	public TestingPrimitiveType(TypeKind kind)
	{
		super(kind);
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		return v.visitPrimitive(this, p);
	}
	
}
