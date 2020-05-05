package fr.max2.annotated.test.network;

import java.util.ArrayList;
import java.util.HashMap;

import fr.max2.annotated.api.processor.network.ClientPacket;
import fr.max2.annotated.api.processor.network.CustomData;
import fr.max2.annotated.api.processor.network.DataType;
import fr.max2.annotated.api.processor.network.PacketGenerator;
import fr.max2.annotated.api.processor.network.ServerPacket;
import fr.max2.annotated.test.util.EnumTest;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

@PacketGenerator(protocolVersion = "1")
public class TestData
{
	@ServerPacket
	public static void onServerReceive(ServerPlayerEntity sender)
	{
		System.out.println("Packet server");
	}

	@ClientPacket
	public static void onClientReceive()
	{
		System.out.println("Packet client");
	}
	
	@ClientPacket
	public static void integerPrimitives1(byte myByte, short myShort, int myInt, long myLong)
	{
		
	}
	
	@ClientPacket
	public static void integerPrimitives2(Byte myByte, Short myShort, Integer myInt, Long myLong)
	{
		
	}
	
	@ClientPacket
	public static void floatPrimitives1(float myFloat, double myDouble)
	{
		
	}
	
	@ClientPacket
	public static void floatPrimitives2(Float myFloat, Double myDouble)
	{
		
	}
	
	@ClientPacket
	public static void otherPrimitives1(boolean myBoolean, char myChar)
	{
		
	}
	
	@ClientPacket
	public static void otherPrimitives2(Boolean myBoolean, Character myChar)
	{
		
	}
	
	@ClientPacket
	public static void otherJavaObjets(String myString, EnumTest myEnum)
	{
		
	}
	
	@ClientPacket
	public static void otherMinecraftObjets(ItemStack myStack, CompoundNBT myTag)
	{
		
	}
	
	@ClientPacket
	public static void withCustomData(@CustomData(type = DataType.INT) Integer myCustomInt)
	{
		
	}
	
	@ClientPacket
	public static void primitiveArrays(int[] myIntArray, Integer[] myIntWrArray)
	{
		
	}
	
	@ClientPacket
	public static void objectArrays(String[] myStringArray, EnumTest[] myEnumArray, ItemStack[] myStackArray, CompoundNBT[] myTagArray)
	{
		
	}
	
	@ClientPacket
	public static void nestedArrays(int[][] myIntArrayArray)
	{
		
	}
	
	@ClientPacket
	public static void simpleList(ArrayList<Integer> myIntWrList)
	{
		
	}
	
	@ClientPacket
	public static void objectLists(ArrayList<String> myStringList, ArrayList<EnumTest> myEnumList, ArrayList<ItemStack> myStackList, ArrayList<CompoundNBT> myTagList)
	{
		
	}
	
	/*@ClientPacket
	public static void complexMap(HashMap<ItemStack, HashMap<@CustomData(type = DataType.NBT_BYTE_ARRAY) ByteArrayNBT, @CustomData(type = DataType.NBT_LIST) ListNBT[]>> stackToList)
	{
		
	}*/
	
}
