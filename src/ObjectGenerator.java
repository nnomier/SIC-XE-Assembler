import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ObjectGenerator {
	HeaderRecord header = new HeaderRecord();
	SicAssembler sic = new SicAssembler();
	boolean isPC = false;
	ArrayList<TextRecord> TextRecords = new ArrayList<TextRecord>();
	Load loader = new Load();
	String EndRecord=null;
	
	public String binToHex(String binaryStr) {
		int decimal = Integer.parseInt(binaryStr, 2);
		String hexStr = Integer.toString(decimal, 16);
		return hexStr;
	}

	public static boolean isNumeric(String strNum) {
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	ObjectGenerator(SicAssembler sic) {
		this.sic = sic;
	}

	public void headerLine() {
		if (sic.outputPhase1.get(0).opcode.equals("start")) {
			header.StartAddress = String.format("%6s", sic.outputPhase1.get(0).op1).replace(" ", "0");		
			header.NameOfProg = sic.outputPhase1.get(0).label;	
			header.ProgramLength = String.format("%6s", sic.ProgramLength).replace(" ", "0");	 
		}
		else {
			header.StartAddress = "000000";		
			header.NameOfProg = "      ";
			header.ProgramLength = String.format("%6s", sic.ProgramLength).replace(" ", "0");	
		}
	}

	public void GenerateObjectFile() {
		headerLine();
		 System.out.println("H"+"^"+header.NameOfProg+"^"+header.StartAddress+"^"+header.ProgramLength);
		 textRecord();
		 EndRecord();
		
		for(TextRecord t : TextRecords)
		{
			System.out.print("t" + "^" + t.startAddress+"^");
			for(String s : t.instructionCode)
			{
				System.out.print(s + "^");
			}
			System.out.println(t.Length);
		}
		
		System.out.println("E"+"^"+EndRecord);
	}
	
	public void textRecord() {
		int size = sic.outputPhase1.size();
		int counter=0;
		int format;
		String objCode = null;
		
		for(int i=0; i<size; i++)
		{
			counter = 0;
			TextRecord record = new TextRecord();
			//record.startAddress = startingTextAddress( stopIndex + 1);
			
			while( counter < 30 && i<size)
			{
				System.out.println("counter is " + counter);
				format =  sic.outputPhase1.get(i).format;
				//stopIndex++;
				Operation operation = sic.outputPhase1.get(i);
				if( counter == 0) {
					record.startAddress = sic.outputPhase1.get(i).address;
				    record.startAddress=  String.format("%5s", record.startAddress).replace(" ", "0");}
				counter += format;
				
				if( format == 0) {
					if(operation.opcode.equals("start") || operation.opcode.equals("end") || operation.opcode.equals("base") 
							|| operation.opcode.equals("org") || operation.opcode.equals("equ") || operation.opcode.equals("ltorg"))
					{
						i++; 
						continue;
					}
					else if(operation.opcode.equals("resw")||operation.opcode.equals("resb"))
					{
						i++;
					    break;	
					}
				}
				
				if( format + counter <= 30)
				{	//System.out.println(i);
					
						
					switch(format)
					{ 
						case 0:
							objCode = format0(sic.outputPhase1.get(i));
							break;
							
						case 2:
							objCode = format2(sic.outputPhase1.get(i));
							break;
						case 3:
							objCode = format3(sic.outputPhase1.get(i));
							break;
						case 4:
							objCode = format4(sic.outputPhase1.get(i));
							break;
					}
					record.instructionCode.add(objCode);
				}
				i++;
			}
		    record.Length =Integer.toHexString(counter);
			TextRecords.add(record);
		}
	}

	public String format0 (Operation operation)
	{
		String obj=null;
		switch(operation.opcode)
		{
			case "word" :
					obj = Integer.toHexString(Integer.parseInt(operation.op1.toString()));
				
			case "byte":
				if(operation.op1.charAt(0)=='x')
					obj = operation.op1.substring(2,operation.op1.length()-1);
				else if(operation.op1.charAt(0)=='c')
					obj=ASCIItoHEX(operation.op1.substring(2,operation.op1.length()-1));
				
					
		}
		obj=String.format("%6s",obj ).replace(" ", "0");
		
		return obj;	
	}
	
	public String ASCIItoHEX (String ascii)
	{
		char[] ch = ascii.toCharArray();

	      // Step-2 Iterate over char array and cast each element to Integer.
	      StringBuilder builder = new StringBuilder();

	      for (char c : ch) {
	         int i = (int) c;
	         // Step-3 Convert integer value to hex using toHexString() method.
	         builder.append(Integer.toHexString(i).toUpperCase());
	      }

	     
	      return builder.toString();
	}
	public String FirstExecutableAddress()
	{
		int n = sic.outputPhase1.size();
		int i=0;
		for( i=0; i< n; i++)
		{
			if( sic.outputPhase1.get(i).format != 0)
			{
				break;
			}
		}
		return sic.outputPhase1.get(i).address;
	}
	
	public String format2(Operation operation) {
		StringBuilder result = new StringBuilder();
		result.append(binToHex(loader.getInstructions().get(operation.opcode)));
		result.append(loader.getRegisters().get(operation.op1));
		if (operation.opcode.equals("tixr")) {
			result.append("0");
		} else {
			result.append(loader.getRegisters().get(operation.op2));
		}
		return new String(result);
	}

	public String format3(Operation operation) {
		String e = "0";
		String n = "0";
		String i = "0";
		String x = "0";
		String b = "0";
		String p = "0";
		String Displacement = null;
		String TargetAddress = null;
		String pc = sic.pcPlusFormat(operation.address, 3);
		StringBuilder result = new StringBuilder();
		String 	opcode = loader.getInstructions().get(operation.opcode).substring(0,6);
	

		if (operation.op1.contains(",x")) {
			x = "1";
			String[] operand = operation.op1.split(",");
			TargetAddress = sic.symbTab.get(operand[0]);
			System.out.println("TA"+TargetAddress + operation.opcode);
			Displacement = CalculateDisplacement(TargetAddress, pc);
			p =  isPC ? "1" : "0"; 
			b=   isPC  ? "0" : "1";
		}
		else if (operation.op1.charAt(0) == '#') {
			
			i = "1";
			String Substring = operation.op1.substring(1);
			if(isNumeric(Substring))
			{
				Displacement = Integer.toBinaryString(Integer.parseInt(Substring));
				Displacement = String.format("%12s", Displacement).replace(" ", "0");
			}
			else {
				TargetAddress = sic.symbTab.get(operation.op1.substring(1));
				Displacement = CalculateDisplacement(TargetAddress, pc);
				p =  isPC ? "1" : "0"; 
				b=   isPC  ? "0" : "1";
			}	
			
		} else if (operation.op1.charAt(0) == '@') {
			n = "1";
			TargetAddress = sic.symbTab.get(operation.op1.substring(1));
			Displacement = CalculateDisplacement(TargetAddress, pc);
			p =  isPC ? "1" : "0"; 
			b=   isPC  ? "0" : "1";
		} else {
			n = "1";
			i = "1";
			
			if( sic.literalTab.containsKey(operation.op1.trim()))
			{
				TargetAddress = sic.literalTab.get(operation.op1);
			} 
			else
				TargetAddress = sic.symbTab.get(operation.op1);
			Displacement = CalculateDisplacement(TargetAddress, pc);
			p =  isPC ? "1" : "0"; 
			b=   isPC  ? "0" : "1";
		}	
		result.append(opcode);
		result.append(n);
		result.append(i);
		result.append(x);
		result.append(b);
		result.append(p);
		result.append(e);
		result.append(Displacement);
		//System.out.println("OPCODE"+opcode+"n"+n+"i"+i+"x"+x+"b"+b+"p"+p+"e"+e+"disp"+Displacement);
	//System.out.println("result "+result);
	     String opcodeHex = Integer.toHexString(Integer.parseInt(result.toString(),2));
	   
		return opcodeHex;
	}

	
	public String CalculateDisplacement(String ta, String pc) {
		String Displacement = null;
		int disp;
		int Programcounter = Integer.parseInt(pc, 16);
		//System.out.println("HERE TA"+ta);
		int Targetaddress = Integer.parseInt(ta, 16);
		
		disp = Targetaddress - Programcounter;
		if (disp >= -2048 && disp <= 2047) {
			isPC=true;
			Displacement = Integer.toBinaryString(disp);
		} else {
			disp = Targetaddress - sic.base;
			if (disp >= 0 && disp <= 4095) {
				Displacement = Integer.toBinaryString(disp);
				Displacement = String.format("%12s", Displacement).replace(" ", "0");
			}
		}
		if(Displacement.length()>12)  Displacement= Displacement.substring(19);
		return Displacement;
	}
	
	public String format4(Operation operation)
	 { 
		 String e = "1";
	     String n = "0";
	     String i = "0";
	     String x = "0";
	     String b=  "0";
	     String p = "0";
	     String address;
	     String opcodeBinFinal =  loader.instructions.get(operation.opcode).substring(0,6);
	     
	 	StringBuilder result = new StringBuilder();

	 	if (operation.op1.contains(",x")) {
	 		x="1";
	 		address = sic.symbTab.get(operation.op1.substring(0, operation.op1.length() - 2)); //check last index in substring
	 	}
	 	
	 	if (operation.op1.charAt(0)=='#')
	 	{
	 		i="1";
	 		address = format4Immediate(operation.op1);
	 	}
	 	 
	    else if (operation.op1.charAt(0)=='@') 
	    {
	    	n="1";
	 	   address = sic.symbTab.get(operation.op1.substring(1));
	    }
	 	else {
	 		n="1";
	 		i="1";
	 		if( sic.literalTab.containsKey(operation.op1.trim()) )
	 		{
	 			address = sic.literalTab.get(operation.op1);
	 		}
	 		else
	 		address = sic.symbTab.get(operation.op1);
	 	}
	 	opcodeBinFinal += n;
	    opcodeBinFinal += i;
	    opcodeBinFinal += x;
	    opcodeBinFinal += b;
	    opcodeBinFinal += p;
	    opcodeBinFinal += e;
	    
	     String opcodeHex = Integer.toHexString(Integer.parseInt(opcodeBinFinal, 2));
	    //converting binary opcode to integer then to hex
	 	result.append(opcodeHex);
	 	
	 	
	 	address = String.format("%5s", address).replace(" ", "0");
	 	result.append(address);
	 	return result.toString();
	 	}
	 
	 public String format4Immediate(String operand){
		 
		String Substring = operand.substring(1);
		
		if (isNumeric(Substring)) 
		{
			Substring = Integer.toHexString(Integer.parseInt(Substring));
			return Substring;
		}
			 
		return sic.symbTab.get(Substring);
	 }
	 
	 public void writeToObjectFile()
				throws IOException {
		    BufferedWriter writer = new BufferedWriter(new FileWriter("Obj.txt"));
		    writer.write("H"+"^"+header.NameOfProg+"^"+header.StartAddress+"^"+header.ProgramLength);
		  	writer.write(System.getProperty( "line.separator" ));

		    for(TextRecord t : TextRecords)
			{
				writer.write("t" + "^" + t.startAddress+"^");
				for(String s : t.instructionCode)
				{
					writer.write(s + "^");
				}
				writer.write(t.Length);
			  	writer.write(System.getProperty( "line.separator" ));

			}
			writer.write("E"+"^"+EndRecord);
		    writer.close();
		}
	
	 public void EndRecord() {
		EndRecord= FirstExecutableAddress();
		EndRecord=  String.format("%6s", EndRecord).replace(" ", "0");
	 }
}

